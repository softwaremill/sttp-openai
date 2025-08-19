//> using dep com.microsoft.playwright:playwright:1.54.0
//> using dep org.typelevel::cats-effect::3.6.3

import cats.effect.{IO, IOApp, Resource}
import cats.syntax.all._
import com.microsoft.playwright._
import com.microsoft.playwright.options.WaitUntilState
import scala.util.Try

case class EndpointInfo(
  name: String,
  apiPath: String,
  isActive: Boolean
)

case class ModelInfo(
  name: String,
  activeEndpoints: List[EndpointInfo],
  inactiveEndpoints: List[EndpointInfo],
  contextWindow: Option[String],
  trainingData: Option[String],
  maxOutputTokens: Option[String],
  url: String
)

object ModelEndpointScraper extends IOApp.Simple {
  
  def run: IO[Unit] = {
    firefoxResource.use { case (playwright, browser) =>
      for {
        _ <- IO.println("🦊 Starting Firefox-based OpenAI endpoint scraper...")
        models <- scrapeAllModels(browser)
        _ <- displayResults(models)
      } yield ()
    }
  }
  
  private def firefoxResource: Resource[IO, (Playwright, Browser)] = {
    Resource.make(
      IO {
        println("🦊 Initializing Firefox browser...")
        val playwright = Playwright.create()
        
        val browser = playwright.firefox().launch(
          new BrowserType.LaunchOptions()
            .setHeadless(true) // Change to false if you want to see the browser
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
  }
  
  private def scrapeAllModels(browser: Browser): IO[List[ModelInfo]] = {
    val modelPages = List(
//      ("GPT-4o", "https://platform.openai.com/docs/models/gpt-4o"),
//      ("GPT-4o mini", "https://platform.openai.com/docs/models/gpt-4o-mini"),
//      ("GPT-4 Turbo", "https://platform.openai.com/docs/models/gpt-4-turbo-and-gpt-4"),
//      ("GPT-3.5 Turbo", "https://platform.openai.com/docs/models/gpt-3-5-turbo"),
//      ("DALL·E 3", "https://platform.openai.com/docs/models/dall-e"),
      ("Whisper", "https://platform.openai.com/docs/models/whisper"),
//      ("TTS", "https://platform.openai.com/docs/models/tts"),
//      ("Embeddings", "https://platform.openai.com/docs/models/embeddings"),
//      ("Moderation", "https://platform.openai.com/docs/models/moderation")
    )
    
    modelPages.traverse { case (modelName, url) =>
      scrapeModelPage(browser, modelName, url)
    }.map(_.flatten)
  }
  
  private def scrapeModelPage(browser: Browser, modelName: String, url: String): IO[Option[ModelInfo]] = {
    IO {
      println(s"\n🔍 Scraping $modelName from $url...")
      val page = browser.newPage()
      
      try {
        // Firefox is fast and reliable with OpenAI pages
        page.navigate(url, new Page.NavigateOptions()
          .setTimeout(60000)
          .setWaitUntil(WaitUntilState.LOAD)
        )
        
        // Brief wait for content to render
        page.waitForTimeout(3000)
        
        val title = Option(page.title()).getOrElse("No title")
        println(s"  📄 Title: $title")
        
        if (title.contains("Just a moment")) {
          println(s"  ⚠️  Unexpected Cloudflare challenge - skipping $modelName")
          None
        } else {
          // Extract content
          val textContent = page.textContent("body")
          println(s"  📊 Content length: ${textContent.length} characters")
          
          val endpoints = extractEndpoints(page)
          val activeEndpoints = endpoints.filter(_.isActive)
          val inactiveEndpoints = endpoints.filter(!_.isActive)
          val contextWindow = extractContextWindow(textContent)
          val trainingData = extractTrainingData(textContent)
          val maxOutput = extractMaxOutput(textContent)
          
          val modelInfo = ModelInfo(
            name = modelName,
            activeEndpoints = activeEndpoints,
            inactiveEndpoints = inactiveEndpoints,
            contextWindow = contextWindow,
            trainingData = trainingData,
            maxOutputTokens = maxOutput,
            url = url
          )
          
          println(s"  ✅ Extracted ${activeEndpoints.length} active endpoints, ${inactiveEndpoints.length} inactive")
          Some(modelInfo)
        }
        
      } catch {
        case e: Exception =>
          println(s"  ❌ Failed to scrape $modelName: ${e.getMessage}")
          None
      } finally {
        page.close()
      }
    }
  }
  
  private def extractEndpoints(page: Page): List[EndpointInfo] = {
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
            
            val finalApiPath = if (apiPath.startsWith("v1/")) apiPath 
                               else foundV1.getOrElse("")
            
            val finalName = if (name.nonEmpty && !name.startsWith("v1/")) name
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
  }
  
  private def extractContextWindow(text: String): Option[String] = {
    val patterns = List(
      """(\d+[,\d]*)\s*tokens?\s*context""".r,
      """context\s*(?:window\s*)?(?:of\s*)?(\d+[,\d]*)\s*tokens?""".r,
      """(\d+[,\d]*)\s*token\s*context""".r,
      """up\s*to\s*(\d+[,\d]*)\s*tokens?""".r
    )
    
    patterns.view
      .flatMap(_.findFirstMatchIn(text.toLowerCase))
      .map(m => s"${m.group(1)} tokens")
      .headOption
  }
  
  private def extractTrainingData(text: String): Option[String] = {
    val patterns = List(
      """training\s*data\s*(?:through\s*)?([a-z]+\s*\d{4})""".r,
      """knowledge\s*cutoff\s*(?:of\s*)?([a-z]+\s*\d{4})""".r,
      """data\s*(?:through\s*)?([a-z]+\s*\d{4})""".r,
      """trained\s*(?:through\s*)?([a-z]+\s*\d{4})""".r
    )
    
    patterns.view
      .flatMap(_.findFirstMatchIn(text.toLowerCase))
      .map(m => m.group(1).split("\\s+").map(_.capitalize).mkString(" "))
      .headOption
  }
  
  private def extractMaxOutput(text: String): Option[String] = {
    val patterns = List(
      """up\s*to\s*(\d+[,\d]*)\s*output\s*tokens?""".r,
      """(\d+[,\d]*)\s*max(?:imum)?\s*output\s*tokens?""".r,
      """maximum\s*of\s*(\d+[,\d]*)\s*tokens?""".r,
      """(\d+[,\d]*)\s*tokens?\s*output""".r
    )
    
    patterns.view
      .flatMap(_.findFirstMatchIn(text.toLowerCase))
      .map(m => s"${m.group(1)} tokens")
      .headOption
  }
  
  private def displayResults(models: List[ModelInfo]): IO[Unit] = {
    IO {
      println("\n" + "=" * 80)
      println("🤖 OPENAI MODEL ENDPOINTS (scraped with Firefox)")
      println("=" * 80)
      
      models.foreach { model =>
        println(s"\n🔸 ${model.name}")
        println("─" * (model.name.length + 3))
        
        if (model.activeEndpoints.nonEmpty) {
          println("  ✅ Active Endpoints:")
          model.activeEndpoints.foreach(endpoint => 
            println(s"    🟢 ${endpoint.name} → ${endpoint.apiPath}")
          )
        }
        
        if (model.inactiveEndpoints.nonEmpty) {
          println("  ❌ Inactive Endpoints:")
          model.inactiveEndpoints.foreach(endpoint => 
            println(s"    🔴 ${endpoint.name} → ${endpoint.apiPath}")
          )
        }
        
        model.contextWindow.foreach(ctx => 
          println(s"  🧠 Context Window: $ctx")
        )
        
        model.trainingData.foreach(data => 
          println(s"  📚 Training Data: Through $data")
        )
        
        model.maxOutputTokens.foreach(tokens => 
          println(s"  📝 Max Output: $tokens")
        )
        
        println(s"  🔗 Source: ${model.url}")
      }
      
      println("\n" + "=" * 80)
      println(s"📊 Successfully scraped ${models.length} models")
      
      // Summary statistics
      val totalActiveEndpoints = models.flatMap(_.activeEndpoints).length
      val totalInactiveEndpoints = models.flatMap(_.inactiveEndpoints).length
      val uniqueActiveEndpoints = models.flatMap(_.activeEndpoints.map(_.apiPath)).distinct.length
      val modelsWithContext = models.count(_.contextWindow.isDefined)
      val modelsWithTraining = models.count(_.trainingData.isDefined)
      
      println(s"🟢 Found $totalActiveEndpoints active endpoints ($uniqueActiveEndpoints unique)")
      println(s"🔴 Found $totalInactiveEndpoints inactive endpoints")
      println(s"🧠 $modelsWithContext models have context window info")
      println(s"📚 $modelsWithTraining models have training data info")
      println("=" * 80)
    }
  }
}
