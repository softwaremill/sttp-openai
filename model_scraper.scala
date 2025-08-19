//> using dep com.microsoft.playwright:playwright:1.54.0
//> using dep org.typelevel::cats-effect::3.6.3
//> using dep org.typelevel::log4cats-slf4j::2.7.1
//> using dep ch.qos.logback:logback-classic:1.5.18

import cats.effect.{IO, IOApp, Resource}
import cats.syntax.all._
import com.microsoft.playwright._
import com.microsoft.playwright.options.WaitUntilState
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ch.qos.logback.classic.{Level, LoggerContext}
import org.slf4j.LoggerFactory
import scala.util.Try

opaque type ModelName = String
object ModelName {
  def apply(name: String): ModelName = name
  extension (modelName: ModelName) {
    def value: String = modelName
  }
}

opaque type URL = String
object URL {
  def apply(url: String): URL = url
  extension (url: URL) {
    def value: String = url
  }
}

case class EndpointInfo(
    name: String,
    apiPath: String,
    isActive: Boolean
)

case class ModelInfo(
    name: ModelName,
    activeEndpoints: List[EndpointInfo],
    inactiveEndpoints: List[EndpointInfo],
    url: URL
    // Snapshot names
)

object ModelEndpointScraper extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  // Configure logging levels programmatically
  private def configureLogging(level: Level = Level.INFO): IO[Unit] = IO {
    val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    
    // Set root logger level
    val rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    rootLogger.setLevel(Level.INFO)
    
    // Set our scraper to the specified level (DEBUG for detailed logs, INFO for summary)
    val scraperLogger = loggerContext.getLogger("ModelEndpointScraper")
    scraperLogger.setLevel(level)
  }

  def run: IO[Unit] =
    for {
      _ <- configureLogging(Level.INFO) // Change to Level.DEBUG for detailed logs
      _ <- logger.info("🦊 Starting Firefox-based OpenAI endpoint scraper...")
      modelList <-
        firefoxResource.use { case (_, browser) =>
          fetchModelSet(browser)
        }
      _ <- logger.info(s"📋 Found ${modelList.size} models to scrape")
      models <- scrapeModels(modelList.toList)
      _ <- displayResults(models)
    } yield ()

  private def firefoxResource: Resource[IO, (Playwright, Browser)] =
    Resource.make(
      for {
        _ <- logger.debug("🦊 Initializing Firefox browser...")
        playwright <- IO(Playwright.create())
        browser <- IO(playwright
          .firefox()
          .launch(
            new BrowserType.LaunchOptions()
              .setHeadless(true)
              .setTimeout(60000)
          ))
      } yield (playwright, browser)
    ) { case (playwright, browser) =>
      for {
        _ <- logger.debug("🦊 Closing Firefox...")
        _ <- IO(browser.close()).handleError(_ => ())
        _ <- IO(playwright.close()).handleError(_ => ())
      } yield ()
    }

  private def fetchModelSet(browser: Browser): IO[Set[(ModelName, URL)]] =
    for {
      _ <- logger.info("🔍 Fetching model list from OpenAI models page...")
      page <- IO(browser.newPage())
      result <- (for {
        _ <- IO(page.navigate(
          "https://platform.openai.com/docs/models",
          new Page.NavigateOptions()
            .setTimeout(90000)
            .setWaitUntil(WaitUntilState.LOAD)
        ))
        _ <- IO(page.waitForTimeout(5000))
        
        title <- IO(Option(page.title()).getOrElse("No title"))
        _ <- logger.debug(s"📄 Page title: $title")

        _ <- if (title.contains("Just a moment")) {
          logger.warn("⚠️ Cloudflare challenge detected - waiting longer...") *>
          IO(page.waitForTimeout(15000))
        } else IO.unit

        modelLinks <- IO {
          import scala.jdk.CollectionConverters._
          page.querySelectorAll("a[href^='/docs/models/']").asScala.toSet
        }
        _ <- logger.debug(s"📦 Found ${modelLinks.size} model links")

        models <- IO {
          modelLinks.flatMap { link =>
            Try {
              val href = link.getAttribute("href")
              val nameElement = link.querySelector(".font-semibold")

              if (nameElement != null && href != null) {
                val modelName = nameElement.textContent().trim
                val fullUrl = s"https://platform.openai.com$href"

                // Skip the main models page itself
                if (href != "/docs/models" && modelName.nonEmpty) {
                  Some((ModelName(modelName), URL(fullUrl)))
                } else None
              } else None
            }.toOption.flatten
          }
        }

        _ <- models.toList.traverse { case (name, url) =>
          logger.debug(s"✅ Found model: $name → $url")
        }

      } yield models).handleErrorWith { e =>
        logger.error(s"❌ Failed to fetch model list: ${e.getMessage}") *>
        IO.raiseError(new Exception(s"Failed to fetch model list: ${e.getMessage}"))
      }.guarantee(IO(page.close()))
    } yield result

  private def scrapeModels(modelList: List[(ModelName, URL)]): IO[List[ModelInfo]] =
    IO.parTraverseN(10)(modelList) { case (modelName, url) =>
      firefoxResource.use { case (playwright, browser) =>
        scrapeModelPage(browser, modelName, url).map(_.getOrElse(ModelInfo(modelName, Nil, Nil, url)))
      }
    }

  private def scrapeModelPage(browser: Browser, modelName: ModelName, url: URL): IO[Option[ModelInfo]] =
    for {
      _ <- logger.info(s"🔍 Scraping ${modelName.value}...")
      _ <- logger.debug(s"URL: ${url.value}")
      browserContext <- IO(browser.newContext())
      page <- IO(browserContext.newPage())
      
      result <- (for {
        _ <- IO(page.navigate(
          url.value,
          new Page.NavigateOptions()
            .setTimeout(60000)
            .setWaitUntil(WaitUntilState.LOAD)
        ))
        _ <- IO(page.waitForTimeout(3000))

        title <- IO(Option(page.title()).getOrElse("No title"))
        _ <- logger.debug(s"📄 Page title: $title")

        result <- if (title.contains("Just a moment")) {
          logger.warn(s"⚠️ Cloudflare challenge detected for ${modelName.value} - skipping") *>
          IO.pure(None)
        } else {
          for {
            textContent <- IO(page.textContent("body"))
            _ <- logger.debug(s"📊 Content length: ${textContent.length} characters")

            endpoints <- extractEndpoints(page)
            activeEndpoints = endpoints.filter(_.isActive)
            inactiveEndpoints = endpoints.filter(!_.isActive)

            modelInfo = ModelInfo(
              name = modelName,
              activeEndpoints = activeEndpoints,
              inactiveEndpoints = inactiveEndpoints,
              url = url
            )

            _ <- logger.info(s"✅ ${modelName.value}: ${activeEndpoints.length} active, ${inactiveEndpoints.length} inactive endpoints")
          } yield Some(modelInfo)
        }
      } yield result).handleErrorWith { e =>
        logger.error(s"❌ Failed to scrape ${modelName.value}: ${e.getMessage}") *>
        IO.pure(None)
      }.guarantee(IO(page.close()))
    } yield result

  private def extractEndpoints(page: Page): IO[List[EndpointInfo]] =
    for {
      _ <- logger.debug("🔍 Looking for endpoint cards in DOM structure...")
      
      endpointCards <- IO {
        import scala.jdk.CollectionConverters._
        page.waitForTimeout(3000)
        
        val allCards = page.querySelectorAll("div.flex.flex-row.gap-2").asScala.toList
        allCards.filter { card =>
          Try(card.textContent()).getOrElse("").contains("v1/")
        }
      }
      
      _ <- logger.debug(s"📦 Found ${endpointCards.length} cards containing 'v1/' endpoints")
      
      endpoints <- if (endpointCards.nonEmpty) {
        IO {
          endpointCards.flatMap { card =>
            Try {
              val iconContainer = card.querySelector("div.rounded-lg")
              
              val isActive = if (iconContainer != null) {
                val classes = iconContainer.getAttribute("class")
                classes != null &&
                classes.contains("bg-primary-soft") &&
                !classes.contains("text-gray-400")
              } else false

              val nameElement = card.querySelector(".font-semibold")
              val pathElement = card.querySelector(".text-xs")
              val name = if (nameElement != null) nameElement.textContent().trim else ""
              val apiPath = if (pathElement != null) pathElement.textContent().trim else ""

              val allCardText = card.textContent().trim
              val cardLines = allCardText.split("\n").map(_.trim).filter(_.nonEmpty)

              val v1Pattern = """(v1/[a-z/]+)""".r
              val foundV1 = v1Pattern.findFirstIn(allCardText)

              val finalApiPath = if (apiPath.startsWith("v1/")) apiPath else foundV1.getOrElse("")
              val finalName = if (name.nonEmpty && !name.startsWith("v1/")) name
                             else cardLines.find(line => !line.startsWith("v1/") && line.length < 30 && !line.startsWith("v1/")).getOrElse("")

              if (finalName.nonEmpty && finalApiPath.nonEmpty) {
                Some(EndpointInfo(finalName, finalApiPath, isActive))
              } else None
            }.toOption.flatten
          }
        }.flatTap { endpoints =>
          logger.debug(s"✅ Successfully extracted ${endpoints.length} endpoints")
        }
      } else {
        logger.warn("⚠️ No endpoint cards found with expected structure") *>
        IO.pure(List.empty[EndpointInfo])
      }
    } yield endpoints

  private def displayResults(models: List[ModelInfo]): IO[Unit] =
    for {
      _ <- logger.info("=" * 80)
      _ <- logger.info("🤖 OPENAI MODEL ENDPOINTS")
      _ <- logger.info("=" * 80)

      _ <- models.traverse_ { model =>
        for {
          _ <- logger.info(s"🔸 ${model.name.value}")
          
          _ <- if (model.activeEndpoints.nonEmpty) {
            logger.info("  ✅ Active Endpoints:") *>
            model.activeEndpoints.traverse_(endpoint => 
              logger.info(s"    🟢 ${endpoint.name} → ${endpoint.apiPath}")
            )
          } else IO.unit

          _ <- if (model.inactiveEndpoints.nonEmpty) {
            logger.info("  ❌ Inactive Endpoints:") *>
            model.inactiveEndpoints.traverse_(endpoint => 
              logger.info(s"    🔴 ${endpoint.name} → ${endpoint.apiPath}")
            )
          } else IO.unit

          _ <- logger.debug(s"  🔗 Source: ${model.url.value}")
        } yield ()
      }

      totalActiveEndpoints = models.flatMap(_.activeEndpoints).length
      totalInactiveEndpoints = models.flatMap(_.inactiveEndpoints).length
      uniqueActiveEndpoints = models.flatMap(_.activeEndpoints.map(_.apiPath)).distinct.length

      _ <- logger.info("=" * 80)
      _ <- logger.info(s"📊 Successfully scraped ${models.length} models")
      _ <- logger.info(s"🟢 Found $totalActiveEndpoints active endpoints ($uniqueActiveEndpoints unique)")
      _ <- logger.info(s"🔴 Found $totalInactiveEndpoints inactive endpoints")
      _ <- logger.info("=" * 80)
    } yield ()
}
