//> using scala 3.3.4
//> using dep com.microsoft.playwright:playwright:1.54.0
//> using dep org.typelevel::cats-effect::3.6.3
//> using dep org.typelevel::log4cats-slf4j::2.7.1
//> using dep ch.qos.logback:logback-classic:1.5.18
//> using dep com.github.scopt::scopt::4.1.0
//> using dep io.circe::circe-core::0.14.14
//> using dep io.circe::circe-generic::0.14.14
//> using dep io.circe::circe-parser::0.14.14

import cats.effect.{IO, IOApp, Resource}
import cats.syntax.all._
import com.microsoft.playwright._
import com.microsoft.playwright.options.WaitUntilState
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ch.qos.logback.classic.{Level, LoggerContext}
import org.slf4j.LoggerFactory
import scopt.OParser
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import java.io.PrintWriter
import scala.util.Try

opaque type ModelName = String
object ModelName {
  def apply(name: String): ModelName = name
  extension (modelName: ModelName) {
    def value: String = modelName
  }
  
  implicit val modelNameEncoder: Encoder[ModelName] = Encoder.encodeString.contramap(_.value)
}

opaque type URL = String
object URL {
  def apply(url: String): URL = url
  extension (url: URL) {
    def value: String = url
  }
  
  implicit val urlEncoder: Encoder[URL] = Encoder.encodeString.contramap(_.value)
}

case class EndpointInfo(
  name: String,
  apiPath: String,
  isActive: Boolean
)

case class Config(
  debug: Boolean = false,
  models: Option[List[ModelName]] = None,
  output: Option[String] = None
)

case class ModelInfo(
    name: ModelName,
    activeEndpoints: List[EndpointInfo],
    inactiveEndpoints: List[EndpointInfo],
    snapshots: List[String],
    url: URL
)

object ModelEndpointScraper extends IOApp {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private def configureLogging(level: Level = Level.INFO): IO[Unit] = IO {
    val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    
    val rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    rootLogger.setLevel(Level.INFO)

    val scraperLogger = loggerContext.getLogger("<empty>.ModelEndpointScraper")
    scraperLogger.setLevel(level)
  }

  private def parseArgs(args: List[String]): IO[Either[String, Config]] = IO {
    val builder = OParser.builder[Config]
    val parser = {
      import builder._
      OParser.sequence(
        programName("model-scraper"),
        
        opt[Unit]("debug")
          .action((_, c) => c.copy(debug = true))
          .text("Enable debug logging for detailed output"),
          
        opt[String]("models")
          .action((x, c) => c.copy(models = Some(x.split(",").map(_.trim).map(ModelName.apply).toList)))
          .text("Comma-separated list of model names to scrape (e.g., \"GPT-4o,GPT-3.5\")")
          .valueName("<model1,model2,...>"),
          
        opt[String]("output")
          .action((x, c) => c.copy(output = Some(x)))
          .text("Output file path for JSON endpoint-to-models mapping")
          .valueName("<file.json>")
      )
    }
    
    OParser.parse(parser, args, Config()) match {
      case Some(config) => Right(config)
      case None => 
        // Check if help was requested (which is a normal exit)
        if (args.contains("--help") || args.contains("-h")) {
          Left("help")
        } else {
          Left("error")
        }
    }
  }

  def run(args: List[String]): IO[cats.effect.ExitCode] =
    // Quick check for help before parsing
    if (args.contains("--help") || args.contains("-h")) {
      IO {
        println("OpenAI Model Endpoint Scraper 1.0")
        println("Usage: model-scraper [options]")
        println()
        println("  --debug                  Enable debug logging for detailed output")
        println("  --models <model1,model2,...>")
        println("                           Comma-separated list of model names to scrape (e.g., \"GPT-4o,GPT-3.5\")")
        println("  --output <file.json>     Output file path for JSON endpoint-to-models mapping")
        println("  --help                   Show this help message")
      }.as(cats.effect.ExitCode.Success)
    } else {
      parseArgs(args).flatMap {
        case Right(config) =>
          runScraper(config).as(cats.effect.ExitCode.Success)
        case Left(_) =>
          IO.pure(cats.effect.ExitCode.Error) // Parse error
      }
    }

  private def runScraper(config: Config): IO[Unit] =
    for {
      _ <- configureLogging(if (config.debug) Level.DEBUG else Level.INFO)
      _ <- logger.info("🦊 Starting Firefox-based OpenAI endpoint scraper...")
      _ <- config.models.fold(IO.unit)(models => 
        logger.info(s"🎯 Filtering for models: ${models.mkString(", ")}")
      )
      
      modelList <-
        firefoxResource.use { case (_, browser) =>
          fetchModelSet(browser, config.models)
        }
      _ <- logger.info(s"📋 Found ${modelList.size} models to scrape")
      models <- scrapeModels(modelList.toList)
      _ <- displayResults(models)
      _ <- config.output.fold(IO.unit)(outputPath => 
        generateAndSaveEndpointMapping(models, outputPath)
      )
    } yield ()

  case class ModelWithSnapshots(name: String, snapshots: List[String])

  private def generateAndSaveEndpointMapping(models: List[ModelInfo], outputPath: String): IO[Unit] =
    for {
      _ <- logger.info(s"📝 Generating endpoint-to-models mapping...")
      
      // Create mapping: endpoint -> list of models with their snapshots
      endpointMapping = models.flatMap { model =>
        model.activeEndpoints.map { endpoint =>
          endpoint.apiPath -> ModelWithSnapshots(model.name.value, model.snapshots)
        }
      }.groupBy(_._1).map { case (endpoint, pairs) =>
        endpoint -> pairs.map(_._2).distinctBy(_.name).sortBy(_.name)
      }
      
      _ <- logger.info(s"🔍 Found mappings for ${endpointMapping.size} endpoints")
      _ <- endpointMapping.toList.traverse_ { case (endpoint, modelInfos) =>
        val modelNames = modelInfos.map(_.name).mkString(", ")
        logger.debug(s"  $endpoint: $modelNames")
      }
      
      // Convert to JSON and save
      json = endpointMapping.asJson.spaces2
      _ <- IO {
        val writer = new PrintWriter(outputPath)
        try {
          writer.write(json)
        } finally {
          writer.close()
        }
      }
      
      _ <- logger.info(s"💾 Saved endpoint mapping to: $outputPath")
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

  private def fetchModelSet(browser: Browser, modelFilter: Option[List[ModelName]] = None): IO[Set[(ModelName, URL)]] =
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

        // Apply model filter if specified
        filteredModels <- IO {
          modelFilter match {
            case Some(filterList) =>
              val filterSet = filterList.map(_.value.toLowerCase).toSet
              models.filter { case (modelName, _) =>
                val nameStr = modelName.value.toLowerCase
                filterSet.exists(filter => 
                  nameStr.contains(filter) || filter.contains(nameStr)
                )
              }
            case None => models
          }
        }

        _ <- if (modelFilter.isDefined) {
          logger.info(s"🎯 Filtered to ${filteredModels.size} models matching criteria")
        } else IO.unit

      } yield filteredModels).handleErrorWith { e =>
        logger.error(s"❌ Failed to fetch model list: ${e.getMessage}") *>
        IO.raiseError(new Exception(s"Failed to fetch model list: ${e.getMessage}"))
      }.guarantee(IO(page.close()))
    } yield result

  private def scrapeModels(modelList: List[(ModelName, URL)]): IO[List[ModelInfo]] =
    IO.parTraverseN(10)(modelList) { case (modelName, url) =>
      firefoxResource.use { case (playwright, browser) =>
        scrapeModelPage(browser, modelName, url).map(_.getOrElse(ModelInfo(modelName, Nil, Nil, Nil, url)))
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
            
            snapshots <- extractSnapshots(page, modelName)

            modelInfo = ModelInfo(
              name = modelName,
              activeEndpoints = activeEndpoints,
              inactiveEndpoints = inactiveEndpoints,
              snapshots = snapshots,
              url = url
            )

            _ <- logger.info(s"✅ ${modelName.value}: ${activeEndpoints.length} active, ${inactiveEndpoints.length} inactive endpoints, ${snapshots.length} snapshots")
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

  private def extractSnapshots(page: Page, modelName: ModelName): IO[List[String]] =
    for {
      _ <- logger.debug(s"🔍 Looking for model snapshots for ${modelName.value}...")
      
      snapshots <- IO {
        import scala.jdk.CollectionConverters._
        
        // Method 1: Look for the snapshots section by finding div containing "Snapshots" text
        val snapshotSections = page.querySelectorAll("div").asScala.toList.filter { div =>
          val text = div.textContent()
          text.contains("Snapshots") && text.contains("lock in") && text.contains("version")
        }
        
        val snapshotsFromSection = snapshotSections.flatMap { section =>
          // Look for the snapshot list container (.flex.flex-col.gap-8.font-mono)
          val snapshotContainers = section.querySelectorAll(".flex.flex-col.gap-8.font-mono").asScala.toList
          
          snapshotContainers.flatMap { container =>
            // New structure: Look for individual model blocks (.flex.flex-col.gap-4)
            val modelBlocks = container.querySelectorAll(".flex.flex-col.gap-4").asScala.toList
            
            val snapshotsFromBlocks = modelBlocks.flatMap { block =>
              // Method 1a: Get snapshots from model icon images within this block
              val iconSnapshots = block.querySelectorAll("img[alt]").asScala.toList.flatMap { img =>
                Try {
                  val alt = img.getAttribute("alt")
                  if (alt != null && alt.nonEmpty && !alt.toLowerCase.contains("icon")) {
                    Some(alt.trim)
                  } else None
                }.toOption.flatten
              }
              
              // Method 1b: Get snapshots from the main model name (.text-sm.font-semibold) within this block
              val mainModelSnapshots = block.querySelectorAll(".text-sm.font-semibold").asScala.toList.flatMap { elem =>
                Try {
                  val text = elem.textContent().trim
                  if (text.nonEmpty && !text.contains(" ") && text.length < 50) {
                    Some(text)
                  } else None
                }.toOption.flatten
              }
              
              // Method 1c: Get snapshots from the arrow indicator section within this block
              val arrowSnapshots = block.querySelectorAll("svg + div").asScala.toList.flatMap { elem =>
                Try {
                  val text = elem.textContent().trim
                  if (text.nonEmpty && !text.contains(" ") && text.length < 50) {
                    Some(text)
                  } else None
                }.toOption.flatten
              }
              
              // Method 1c2: Also look for arrow indicators in different structures
              val arrowSnapshots2 = block.querySelectorAll(".flex.flex-row.items-center.gap-2.text-xs.text-tertiary").asScala.toList.flatMap { container =>
                container.querySelectorAll("div").asScala.toList.flatMap { div =>
                  Try {
                    val text = div.textContent().trim
                    if (text.nonEmpty && !text.contains(" ") && text.length < 50 && 
                        !text.toLowerCase.contains("svg") && !text.toLowerCase.contains("path")) {
                      Some(text)
                    } else None
                  }.toOption.flatten
                }
              }
              
              // Method 1d: Get snapshots from the snapshot list within this block
              val listSnapshots = block.querySelectorAll(".flex.flex-1.flex-col.gap-2 .flex.flex-row.items-center.gap-2.text-sm").asScala.toList.flatMap { row =>
                Try {
                  val text = row.textContent().trim
                  // Remove any leading dot indicators or spacing
                  val cleanText = text.replaceAll("^[•\\s]+", "").trim
                  if (cleanText.nonEmpty && !cleanText.contains(" ") && cleanText.length < 50) {
                    Some(cleanText)
                  } else None
                }.toOption.flatten
              }
              
              iconSnapshots ++ mainModelSnapshots ++ arrowSnapshots ++ arrowSnapshots2 ++ listSnapshots
            }
            
            // Fallback: If no model blocks found, use the old method on the entire container
            if (snapshotsFromBlocks.isEmpty) {
              // Method 1a: Get snapshots from model icon images
              val iconSnapshots = container.querySelectorAll("img[alt]").asScala.toList.flatMap { img =>
                Try {
                  val alt = img.getAttribute("alt")
                  if (alt != null && alt.nonEmpty && !alt.toLowerCase.contains("icon")) {
                    Some(alt.trim)
                  } else None
                }.toOption.flatten
              }
              
              // Method 1b: Get snapshots from the main model name (.text-sm.font-semibold)
              val mainModelSnapshots = container.querySelectorAll(".text-sm.font-semibold").asScala.toList.flatMap { elem =>
                Try {
                  val text = elem.textContent().trim
                  if (text.nonEmpty && !text.contains(" ") && text.length < 50) {
                    Some(text)
                  } else None
                }.toOption.flatten
              }
              
              // Method 1c: Get snapshots from the arrow indicator section (points to current version)
              val arrowSnapshots = container.querySelectorAll("svg + div").asScala.toList.flatMap { elem =>
                Try {
                  val text = elem.textContent().trim
                  if (text.nonEmpty && !text.contains(" ") && text.length < 50) {
                    Some(text)
                  } else None
                }.toOption.flatten
              }
              
              // Method 1d: Get snapshots from the snapshot list (dots with model names)
              val listSnapshots = container.querySelectorAll(".flex.flex-1.flex-col.gap-2 .flex.flex-row.items-center.gap-2.text-sm").asScala.toList.flatMap { row =>
                Try {
                  val text = row.textContent().trim
                  // Remove any leading dot indicators or spacing
                  val cleanText = text.replaceAll("^[•\\s]+", "").trim
                  if (cleanText.nonEmpty && !cleanText.contains(" ") && cleanText.length < 50) {
                    Some(cleanText)
                  } else None
                }.toOption.flatten
              }
              
              iconSnapshots ++ mainModelSnapshots ++ arrowSnapshots ++ listSnapshots
            } else {
              snapshotsFromBlocks
            }
          }
        }
        
        // Method 2: Fallback - look for model icons anywhere on the page with model-specific alt text
        val fallbackIconSnapshots = if (snapshotsFromSection.isEmpty) {
          page.querySelectorAll("img[src*='model-icons/']").asScala.toList.flatMap { img =>
            Try {
              val alt = Option(img.getAttribute("alt")).getOrElse("")
              val src = Option(img.getAttribute("src")).getOrElse("")
              
              if (alt.nonEmpty && alt.contains("-") && !alt.toLowerCase.contains("icon")) {
                Some(alt.trim)
              } else if (src.contains("model-icons/")) {
                // Extract from src path like "gpt-3.5-turbo.png"
                val filename = src.split("/").last.replaceAll("\\.(png|jpg|svg)$", "")
                if (filename.nonEmpty && filename.contains("-")) Some(filename) else None
              } else None
            }.toOption.flatten
          }
        } else List.empty[String]
        
        // Combine and clean up results
        val allSnapshots = (snapshotsFromSection ++ fallbackIconSnapshots).distinct
        
        // Filter to only valid model names
        val validSnapshots = allSnapshots.filter { name =>
          name.nonEmpty &&
          name.length > 1 && 
          name.length < 50 &&
          name.matches("^[a-z0-9\\-\\.]+$") && // Only letters, numbers, dashes, dots
          (name.startsWith("gpt-") || 
           name.startsWith("whisper-") || 
           name.startsWith("dall-e-") || 
           name.startsWith("chatgpt-") || 
           name.startsWith("o1") || 
           name.startsWith("o3") || 
           name.startsWith("o4") ||
           name.startsWith("davinci-") ||
           name.startsWith("babbage-") ||
           name.startsWith("curie-") ||
           name.startsWith("ada-") ||
           name.startsWith("text-") ||
           name.startsWith("tts-") ||
           name.startsWith("computer-")) &&
          !name.toLowerCase.contains("snapshot") &&
          !name.toLowerCase.contains("icon") &&
          !name.toLowerCase.contains("deprecated")
        }.sorted
        
        validSnapshots
      }
      
      _ <- logger.debug(s"📸 Found ${snapshots.length} snapshots: ${snapshots.mkString(", ")}")
    } yield snapshots

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

          _ <- if (model.snapshots.nonEmpty) {
            logger.info("  📸 Model Snapshots:") *>
            model.snapshots.traverse_(snapshot => 
              logger.info(s"    📷 $snapshot")
            )
          } else IO.unit

          _ <- logger.debug(s"  🔗 Source: ${model.url.value}")
        } yield ()
      }

      totalActiveEndpoints = models.flatMap(_.activeEndpoints).length
      totalInactiveEndpoints = models.flatMap(_.inactiveEndpoints).length
      uniqueActiveEndpoints = models.flatMap(_.activeEndpoints.map(_.apiPath)).distinct.length
      totalSnapshots = models.flatMap(_.snapshots).length

      _ <- logger.info("=" * 80)
      _ <- logger.info(s"📊 Successfully scraped ${models.length} models")
      _ <- logger.info(s"🟢 Found $totalActiveEndpoints active endpoints ($uniqueActiveEndpoints unique)")
      _ <- logger.info(s"🔴 Found $totalInactiveEndpoints inactive endpoints")
      _ <- logger.info(s"📸 Found $totalSnapshots model snapshots")
      _ <- logger.info("=" * 80)
    } yield ()
}
