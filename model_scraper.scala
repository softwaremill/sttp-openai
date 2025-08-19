//> using dep com.microsoft.playwright:playwright:1.54.0
//> using dep org.typelevel::cats-effect::3.6.3

import cats.effect.{IO, IOApp, Resource}
import cats.syntax.all._
import com.microsoft.playwright._
import com.microsoft.playwright.options.WaitUntilState
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
)

object ModelEndpointScraper extends IOApp.Simple {

  def run: IO[Unit] =
    for {
      _ <- IO.println("🦊 Starting Firefox-based OpenAI endpoint scraper...")
      modelList <-
        firefoxResource.use { case (_, browser) =>
          fetchModelList(browser)
        }
      _ <- IO.println(s"📋 Found ${modelList.length} models to scrape")
      models <- scrapeModels(modelList)
      _ <- displayResults(models)
    } yield ()

  private def firefoxResource: Resource[IO, (Playwright, Browser)] =
    Resource.make(
      IO {
        println("🦊 Initializing Firefox browser...")
        val playwright = Playwright.create()

        val browser = playwright
          .firefox()
          .launch(
            new BrowserType.LaunchOptions()
              .setHeadless(true) 
              .setTimeout(60000)
          )

        (playwright, browser)
      }
    ) { case (playwright, browser) =>
      IO {
        println("🦊 Closing Firefox...")
        browser.close()
        playwright.close()
      }.handleError(_ => ())
    }

  private def fetchModelList(browser: Browser): IO[List[(ModelName, URL)]] =
    IO {
      println("\n🔍 Fetching model list from https://platform.openai.com/docs/models...")
      val page = browser.newPage()

      try {
        page.navigate(
          "https://platform.openai.com/docs/models",
          new Page.NavigateOptions()
            .setTimeout(90000)
            .setWaitUntil(WaitUntilState.LOAD)
        )

        // Wait for content to load
        page.waitForTimeout(5000)

        val title = Option(page.title()).getOrElse("No title")
        println(s"  📄 Title: $title")

        if (title.contains("Just a moment")) {
          println(s"  ⚠️  Cloudflare challenge detected - this may take longer...")
          page.waitForTimeout(15000)
        }

        import scala.jdk.CollectionConverters._
        val modelLinks = page.querySelectorAll("a[href^='/docs/models/']").asScala.toSet

        println(s"  📦 Found ${modelLinks.length} model links")

        // Extract all models with their URLs
        val models = modelLinks.flatMap { link =>
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

        // Log the final list
        println(s"  🧹 After removing duplicates: ${models.length} unique models")
        models.foreach { case (name, url) =>
          println(s"  ✅ Model: $name → $url")
        }

        models

      } catch {
        case e: Exception =>
          println(s"  ❌ Failed to fetch model list: ${e.getMessage}")
          throw new Exception(s"Failed to fetch model list: ${e.getMessage}")
      } finally page.close()
    }

  private def scrapeModels(modelList: List[(ModelName, URL)]): IO[List[ModelInfo]] =
    IO.parTraverseN(10)(modelList) { case (modelName, url) =>
      firefoxResource.use { case (playwright, browser) =>
        scrapeModelPage(browser, modelName, url).map(_.getOrElse(ModelInfo(modelName, Nil, Nil, url)))
      }
    }

  private def scrapeModelPage(browser: Browser, modelName: ModelName, url: URL): IO[Option[ModelInfo]] =
    IO {
      println(s"\n🔍 Scraping ${modelName.value} from $url...")
      val browserContext = browser.newContext()
      val page = browserContext.newPage()

      try {
        // Firefox is fast and reliable with OpenAI pages
        page.navigate(
          url.value,
          new Page.NavigateOptions()
            .setTimeout(60000)
            .setWaitUntil(WaitUntilState.LOAD)
        )

        // Brief wait for content to render
        page.waitForTimeout(3000)

        val title = Option(page.title()).getOrElse("No title")
        println(s"  📄 Title: $title")

        if (title.contains("Just a moment")) {
          println(s"  ⚠️  Unexpected Cloudflare challenge - skipping ${modelName.value}")
          None
        } else {
          // Extract content
          val textContent = page.textContent("body")
          println(s"  📊 Content length: ${textContent.length} characters")

          val endpoints = extractEndpoints(page)
          val activeEndpoints = endpoints.filter(_.isActive)
          val inactiveEndpoints = endpoints.filter(!_.isActive)

          val modelInfo = ModelInfo(
            name = modelName,
            activeEndpoints = activeEndpoints,
            inactiveEndpoints = inactiveEndpoints,
            url = url
          )

          println(s"  ✅ Extracted ${activeEndpoints.length} active endpoints, ${inactiveEndpoints.length} inactive")
          Some(modelInfo)
        }

      } catch {
        case e: Exception =>
          println(s"  ❌ Failed to scrape $modelName: ${e.getMessage}")
          None
      } finally page.close()
    }

  private def extractEndpoints(page: Page): List[EndpointInfo] =
    Try {
      import scala.jdk.CollectionConverters._

      println(s"    🔍 Looking for endpoint cards in DOM structure...")

      // Wait a bit longer for dynamic content to load
      page.waitForTimeout(3000)

      // First, find cards that actually contain v1/ API paths
      val allCards = page.querySelectorAll("div.flex.flex-row.gap-2").asScala.toList
      val endpointCards = allCards.filter { card =>
        Try(card.textContent()).getOrElse("").contains("v1/")
      }

      println(s"    📦 Found ${allCards.length} total cards, ${endpointCards.length} contain 'v1/' endpoints")

      if (endpointCards.nonEmpty) {
        // Process each card
        val endpoints = endpointCards.flatMap { card =>
          Try {
            // Get the icon container to check active/inactive status
            val iconContainer = card.querySelector("div.rounded-lg")

            val isActive = if (iconContainer != null) {
              val classes = iconContainer.getAttribute("class")
              println(s"      🎨 Icon classes: $classes")

              // Active: has bg-primary-soft and NOT text-gray-400
              // Inactive: has text-gray-400 and possibly mask elements
              classes != null &&
              classes.contains("bg-primary-soft") &&
              !classes.contains("text-gray-400")
            } else {
              println(s"      ❌ No icon container found in card")
              false
            }

            // Try different selectors for the text content
            val nameElement = card.querySelector(".font-semibold")
            val pathElement = card.querySelector(".text-xs")

            val name = if (nameElement != null) nameElement.textContent().trim else ""
            val apiPath = if (pathElement != null) pathElement.textContent().trim else ""

            // Get all text from the card and parse it
            val allCardText = card.textContent().trim
            val cardLines = allCardText.split("\n").map(_.trim).filter(_.nonEmpty)

            println(s"      📝 Card text: ${cardLines.mkString(" | ")}")

            // Look for v1/ pattern in the card text
            val v1Pattern = """(v1/[a-z/]+)""".r
            val foundV1 = v1Pattern.findFirstIn(allCardText)

            val finalApiPath =
              if (apiPath.startsWith("v1/")) apiPath
              else foundV1.getOrElse("")

            val finalName =
              if (name.nonEmpty && !name.startsWith("v1/")) name
              else cardLines.find(line => !line.startsWith("v1/") && line.length < 30 && !line.startsWith("v1/")).getOrElse("")

            if (finalName.nonEmpty && finalApiPath.nonEmpty) {
              println(s"      ✅ Endpoint: '$finalName' → '$finalApiPath' (Active: $isActive)")
              Some(EndpointInfo(finalName, finalApiPath, isActive))
            } else {
              println(s"      ⚠️  Incomplete: name='$finalName', path='$finalApiPath'")
              None
            }
          }.toOption.flatten
        }

        if (endpoints.isEmpty) {
          println(s"    ⚠️  No valid endpoints found in cards, checking page HTML structure...")

          // Debug: Let's see what the actual HTML structure looks like
          val pageHtml = page.innerHTML("body")
          val shortHtml = pageHtml.take(2000) // First 2K chars
          println(s"    🔍 Page HTML preview: ${shortHtml.take(500)}...")

          // Look for any div containing "v1/" text
          val v1Elements = page.querySelectorAll("div:has-text('v1/')").asScala.toList
          println(s"    📦 Found ${v1Elements.length} divs containing 'v1/' text")

          List.empty
        } else {
          endpoints
        }
      } else {
        println(s"    ⚠️  No endpoint cards found with expected structure")
        List.empty
      }

    }.getOrElse {
      println(s"    ⚠️  Could not extract endpoints")
      List.empty
    }

  private def displayResults(models: List[ModelInfo]): IO[Unit] =
    IO {
      println("\n" + "=" * 80)
      println("🤖 OPENAI MODEL ENDPOINTS (scraped with Firefox)")
      println("=" * 80)

      models.foreach { model =>
        println(s"\n🔸 ${model.name}")

        if (model.activeEndpoints.nonEmpty) {
          println("  ✅ Active Endpoints:")
          model.activeEndpoints.foreach(endpoint => println(s"    🟢 ${endpoint.name} → ${endpoint.apiPath}"))
        }

        if (model.inactiveEndpoints.nonEmpty) {
          println("  ❌ Inactive Endpoints:")
          model.inactiveEndpoints.foreach(endpoint => println(s"    🔴 ${endpoint.name} → ${endpoint.apiPath}"))
        }

        println(s"  🔗 Source: ${model.url}")
      }

      println("\n" + "=" * 80)
      println(s"📊 Successfully scraped ${models.length} models")

      // Summary statistics
      val totalActiveEndpoints = models.flatMap(_.activeEndpoints).length
      val totalInactiveEndpoints = models.flatMap(_.inactiveEndpoints).length
      val uniqueActiveEndpoints = models.flatMap(_.activeEndpoints.map(_.apiPath)).distinct.length

      println(s"🟢 Found $totalActiveEndpoints active endpoints ($uniqueActiveEndpoints unique)")
      println(s"🔴 Found $totalInactiveEndpoints inactive endpoints")
      println("=" * 80)
    }
}
