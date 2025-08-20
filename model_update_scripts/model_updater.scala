//> using scala 3.3.4
//> using dep org.typelevel::cats-effect::3.6.3
//> using dep org.typelevel::log4cats-slf4j::2.7.1
//> using dep ch.qos.logback:logback-classic:1.5.18
//> using dep com.github.scopt::scopt::4.1.0
//> using dep io.circe::circe-core::0.14.14
//> using dep io.circe::circe-generic::0.14.14
//> using dep io.circe::circe-parser::0.14.14
//> using dep io.circe::circe-yaml::1.15.0

import cats.effect.{IO, IOApp, ExitCode}
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ch.qos.logback.classic.{Level, LoggerContext}
import org.slf4j.LoggerFactory
import scopt.OParser
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.yaml.parser
import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.{Try, Using}

case class UpdaterConfig(
  input: Option[String] = None,
  config: String = "model_update_config.yaml",
  dryRun: Boolean = false,
  debug: Boolean = false,
  scrapeAndUpdate: Boolean = false
)

case class EndpointConfig(
  file: String,
  className: String,
  insertBeforeMarker: String,
  valuesSetName: Option[String]
)

case class NameConversionConfig(
  replacements: List[Replacement],
  capitalizeAfter: List[String],
  specialCases: Map[String, String]
)

case class Replacement(from: String, to: String)

case class ModelWithSnapshots(name: String, snapshots: List[String])

case class ModelUpdateConfig(
  endpoints: Map[String, EndpointConfig],
  nameConversion: NameConversionConfig
)

object ModelUpdater extends IOApp {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private def configureLogging(level: Level = Level.INFO): IO[Unit] = IO {
    val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    rootLogger.setLevel(Level.INFO)
    val scraperLogger = loggerContext.getLogger("ModelUpdater")
    scraperLogger.setLevel(level)
  }

  private def parseArgs(args: List[String]): IO[Either[String, UpdaterConfig]] = IO {
    val builder = OParser.builder[UpdaterConfig]
    val parser = {
      import builder._
      OParser.sequence(
        programName("model-updater"),
        
        opt[String]("input")
          .action((x, c) => c.copy(input = Some(x)))
          .text("Input JSON file with endpoint-to-models mapping")
          .valueName("<models.json>"),
          
        opt[String]("config")
          .action((x, c) => c.copy(config = x))
          .text("Configuration file path (default: model_update_config.yaml)")
          .valueName("<config.yaml>"),
          
        opt[Unit]("dry-run")
          .action((_, c) => c.copy(dryRun = true))
          .text("Preview changes without applying them"),
          
        opt[Unit]("debug")
          .action((_, c) => c.copy(debug = true))
          .text("Enable debug logging"),
          
        opt[Unit]("scrape-and-update")
          .action((_, c) => c.copy(scrapeAndUpdate = true))
          .text("Run scraper first, then update model classes")
      )
    }
    
    OParser.parse(parser, args, UpdaterConfig()) match {
      case Some(config) => Right(config)
      case None => 
        if (args.contains("--help") || args.contains("-h")) {
          Left("help")
        } else {
          Left("error")
        }
    }
  }

  def run(args: List[String]): IO[ExitCode] =
    if (args.contains("--help") || args.contains("-h")) {
      IO {
        println("OpenAI Model Case Class Updater 1.0")
        println("Usage: model-updater [options]")
        println()
        println("  --input <models.json>    Input JSON file with endpoint-to-models mapping")
        println("  --config <config.yaml>   Configuration file path (default: model_update_config.yaml)")
        println("  --dry-run               Preview changes without applying them")
        println("  --debug                 Enable debug logging")
        println("  --scrape-and-update     Run scraper first, then update model classes")
        println("  --help                  Show this help message")
      }.as(ExitCode.Success)
    } else {
      parseArgs(args).flatMap {
        case Right(config) =>
          runUpdater(config).as(ExitCode.Success)
        case Left(_) =>
          IO.pure(ExitCode.Error)
      }
    }

  private def runUpdater(config: UpdaterConfig): IO[Unit] =
    for {
      _ <- configureLogging(if (config.debug) Level.DEBUG else Level.INFO)
      _ <- logger.info("🔧 Starting Model Case Class Updater...")
      
      // Step 1: Run scraper if requested
      inputFile <- if (config.scrapeAndUpdate) {
        for {
          _ <- logger.info("🦊 Running scraper first...")
          tempFile = "temp_models.json"
          _ <- runScraper(tempFile)
        } yield tempFile
      } else {
        config.input match {
          case Some(file) => IO.pure(file)
          case None => IO.raiseError(new Exception("Either --input or --scrape-and-update must be specified"))
        }
      }
      
      // Step 2: Load configurations
      modelConfig <- loadModelConfig(config.config)
      endpointMapping <- loadEndpointMapping(inputFile)
      
      // Step 3: Update model classes
      _ <- updateModelClasses(modelConfig, endpointMapping, config.dryRun)
      
      // Step 4: Cleanup temp file if created
      _ <- if (config.scrapeAndUpdate) {
        IO(new File(inputFile).delete()).void
      } else IO.unit
      
      _ <- logger.info("✅ Model update process completed!")
    } yield ()

  private def runScraper(outputFile: String): IO[Unit] =
    for {
      _ <- logger.info(s"🦊 Running model scraper to generate $outputFile...")
      // We'll shell out to run the scraper
      result <- IO {
        import sys.process._
        // Check if we're in model_update_scripts directory and adjust path accordingly
        val scraperPath = if (new File("model_scraper.scala").exists()) {
          "model_scraper.scala"
        } else {
          "model_update_scripts/model_scraper.scala"
        }
        val command = s"scala-cli run $scraperPath --jvm 17 -- --output $outputFile"
        command.!
      }
      _ <- if (result == 0) {
        logger.info("✅ Scraper completed successfully")
      } else {
        IO.raiseError(new Exception(s"Scraper failed with exit code $result"))
      }
    } yield ()

  private def loadModelConfig(configPath: String): IO[ModelUpdateConfig] =
    for {
      resolvedConfigPath <- IO.pure(resolveFilePath(configPath))
      _ <- logger.debug(s"📖 Loading config from $resolvedConfigPath...")
      content <- IO {
        Using(Source.fromFile(resolvedConfigPath))(_.mkString).get
      }
      config <- IO.fromEither(
        parser.parse(content).flatMap(_.as[ModelUpdateConfig])
          .left.map(e => new Exception(s"Failed to parse config: $e"))
      )
      _ <- logger.debug(s"✅ Loaded config with ${config.endpoints.size} endpoints")
    } yield config

  private def loadEndpointMapping(inputPath: String): IO[Map[String, List[ModelWithSnapshots]]] =
    for {
      resolvedInputPath <- IO.pure(resolveFilePath(inputPath))
      _ <- logger.debug(s"📖 Loading endpoint mapping from $resolvedInputPath...")
      content <- IO {
        Using(Source.fromFile(resolvedInputPath))(_.mkString).get
      }
      mapping <- IO.fromEither(
        decode[Map[String, List[ModelWithSnapshots]]](content)
          .left.map(e => new Exception(s"Failed to parse endpoint mapping: $e"))
      )
      _ <- logger.info(s"✅ Loaded mappings for ${mapping.size} endpoints")
      _ <- mapping.toList.traverse_ { case (endpoint, models) =>
        logger.debug(s"  $endpoint: ${models.size} models with ${models.map(_.snapshots.size).sum} total snapshots")
      }
    } yield mapping

  private def updateModelClasses(
    config: ModelUpdateConfig,
    endpointMapping: Map[String, List[ModelWithSnapshots]],
    dryRun: Boolean
  ): IO[Unit] =
    for {
      _ <- logger.info(s"🔄 Updating model classes (dry-run: $dryRun)...")
      
      updates <- endpointMapping.toList.traverse { case (endpoint, modelsWithSnapshots) =>
        config.endpoints.get(endpoint) match {
          case Some(endpointConfig) =>
            // Extract all model names and their snapshots
            val allModelNames = modelsWithSnapshots.flatMap { modelWithSnapshots =>
              // Include the base model name and all its snapshots
              modelWithSnapshots.name :: modelWithSnapshots.snapshots
            }.distinct
            
            for {
              _ <- logger.debug(s"📸 Processing $endpoint with ${modelsWithSnapshots.size} models:")
              _ <- modelsWithSnapshots.traverse_ { model =>
                logger.debug(s"  ${model.name} (${model.snapshots.size} snapshots: ${model.snapshots.mkString(", ")})")
              }
              result <- updateSingleModelClass(endpointConfig, allModelNames, config.nameConversion, dryRun)
            } yield Some(result)
          case None =>
            logger.warn(s"⚠️ No config found for endpoint: $endpoint") *>
            IO.pure(None)
        }
      }
      
      successCount = updates.flatten.size
      _ <- logger.info(s"📊 Successfully processed $successCount model class files")
    } yield ()

  private def resolveFilePath(configPath: String): String = {
    val file = new File(configPath)
    if (file.exists()) {
      configPath
    } else {
      // Try relative to parent directory (in case we're in model_update_scripts/)
      val parentPath = s"../$configPath"
      val parentFile = new File(parentPath)
      if (parentFile.exists()) {
        parentPath
      } else {
        configPath // Return original path, let it fail with clear error
      }
    }
  }

  private def updateSingleModelClass(
    endpointConfig: EndpointConfig,
    models: List[String],
    nameConversion: NameConversionConfig,
    dryRun: Boolean
  ): IO[String] =
    for {
      resolvedFilePath <- IO.pure(resolveFilePath(endpointConfig.file))
      _ <- logger.info(s"🔧 Updating ${endpointConfig.className} in $resolvedFilePath")
      
      // Read current file
      currentContent <- IO {
        Using(Source.fromFile(resolvedFilePath))(_.mkString).get
      }
      
      // Extract existing case objects
      existingModels <- extractExistingModels(currentContent, endpointConfig.className)
      
      // Convert model names to Scala identifiers
      newModels <- models.traverse(modelName => 
        convertModelNameToScalaId(modelName, nameConversion).map(modelName -> _)
      )
      
      // Filter out existing models
      modelsToAdd = newModels.filterNot { case (_, scalaId) =>
        existingModels.contains(scalaId)
      }
      
      _ <- if (modelsToAdd.nonEmpty) {
        for {
          _ <- logger.info(s"➕ Adding ${modelsToAdd.size} new models:")
          _ <- modelsToAdd.traverse { case (original, scalaId) =>
            logger.info(s"   $original → case object $scalaId")
          }
          
          // Generate new content
          newContent <- generateUpdatedContent(
            currentContent, 
            endpointConfig, 
            modelsToAdd, 
            existingModels
          )
          
          // Write or preview
          _ <- if (dryRun) {
            logger.info("🔍 DRY RUN - Changes would be applied to file")
          } else {
            for {
              // Create backup
              _ <- createBackup(resolvedFilePath)
              
              // Write updated content
              _ <- IO {
                val writer = new PrintWriter(resolvedFilePath)
                try {
                  writer.write(newContent)
                } finally {
                  writer.close()
                }
              }
              
              _ <- logger.info(s"💾 Updated $resolvedFilePath")
            } yield ()
          }
        } yield ()
      } else {
        logger.info(s"✅ No new models to add for ${endpointConfig.className}")
      }
    } yield resolvedFilePath

  private def extractExistingModels(content: String, className: String): IO[List[String]] =
    IO {
      val lines = content.split("\n").toList
      val startPattern = s"object $className"
      val caseObjectPattern = """^\s*case object\s+(\w+).*""".r
      
      val startIndex = lines.indexWhere(_.contains(startPattern))
      
      if (startIndex == -1) {
        List.empty
      } else {
        val relevantLines = lines.drop(startIndex)
        // Find the end of the object by counting braces
        var braceCount = 0
        var endIndex = -1
        for (i <- relevantLines.indices if endIndex == -1) {
          val line = relevantLines(i)
          braceCount += line.count(_ == '{') - line.count(_ == '}')
          if (braceCount == 0 && i > 0) { // Don't end on the first line which contains the opening brace
            endIndex = i + 1
          }
        }
        
        val objectLines = if (endIndex == -1) relevantLines else relevantLines.take(endIndex)
        
        objectLines.collect {
          case caseObjectPattern(name) => name
        }
      }
    }

  private def convertModelNameToScalaId(
    modelName: String, 
    nameConversion: NameConversionConfig
  ): IO[String] =
    IO {
      // Check special cases first
      nameConversion.specialCases.get(modelName) match {
        case Some(specialCase) => specialCase
        case None =>
          // Split into words by separators (-, ., _, spaces)
          val words = modelName.split("[\\-\\._\\s]+").filter(_.nonEmpty)
          val processedWords = words.map { word =>
            val lowerWord = word.toLowerCase
            val upperWord = word.toUpperCase
            
            // Preserve original casing for certain base model names
            if (upperWord == "GPT" || upperWord == "DALL" || upperWord == "WHISPER" || upperWord == "CHATGPT") {
              upperWord
            } 
            // Capitalize first letter for other known model parts
            else if (Set("mini", "nano", "chat", "audio", "realtime", "transcribe", "search", "tts", "preview", "latest", "o").contains(lowerWord)) {
              lowerWord.capitalize
            }
            // For dates (YYYY-MM-DD format becomes YYYYMMDD), keep as is
            else if (word.matches("\\d{4}\\d{2}\\d{2}") || word.matches("\\d+")) {
              word
            }
            // Default: capitalize first letter
            else {
              lowerWord.capitalize
            }
          }
          
          processedWords.mkString("")
      }
    }

  private def generateUpdatedContent(
    currentContent: String,
    endpointConfig: EndpointConfig,
    modelsToAdd: List[(String, String)],
    existingModels: List[String]
  ): IO[String] =
    IO {
      val lines = currentContent.split("\n").toList
      val insertIndex = lines.indexWhere(_.contains(endpointConfig.insertBeforeMarker))
      
      if (insertIndex == -1) {
        throw new Exception(s"Could not find insertion marker: ${endpointConfig.insertBeforeMarker}")
      }
      
      // Generate new case object lines
      val newCaseObjects = modelsToAdd.map { case (original, scalaId) =>
        s"    case object $scalaId extends ${endpointConfig.className}(\"$original\")"
      }
      
      // Insert new case objects before the marker
      val beforeInsert = lines.take(insertIndex)
      val afterInsert = lines.drop(insertIndex)
      
      val updatedLines = beforeInsert ++ newCaseObjects ++ List("") ++ afterInsert
      
              // Update values set if it exists
        endpointConfig.valuesSetName match {
          case Some(valuesSetName) =>
            updateValuesSet(updatedLines.mkString("\n"), valuesSetName, existingModels ++ modelsToAdd.map(_._2), endpointConfig.className)
          case None =>
            updatedLines.mkString("\n")
        }
    }

  private def updateValuesSet(content: String, valuesSetName: String, allModels: List[String], className: String): String = {
    val lines = content.split("\n").toList
    val valuesPattern = s"val $valuesSetName: Set\\[.*?\\] ="
    val startIndex = lines.indexWhere(_.matches(s".*$valuesPattern.*"))
    
    if (startIndex == -1) {
      content // No values set found, return as-is
    } else {
      // Find the end of the Set definition
      var endIndex = startIndex
      var braceCount = 0
      var foundStart = false
      
      for (i <- startIndex until lines.length) {
        val line = lines(i)
        if (line.contains("Set(")) {
          foundStart = true
        }
        if (foundStart) {
          braceCount += line.count(_ == '(') - line.count(_ == ')')
          if (braceCount == 0 && line.contains(")")) {
            endIndex = i
            return (lines.take(startIndex) ++ 
              generateValuesSetLines(valuesSetName, allModels, className) ++ 
              lines.drop(endIndex + 1)).mkString("\n")
          }
        }
      }
      
      content // If we can't find the end, return as-is
    }
  }

  private def generateValuesSetLines(valuesSetName: String, models: List[String], className: String): List[String] = {
    val sortedModels = models.sorted
    
    List(
      s"    val $valuesSetName: Set[$className] =",
      "      Set(",
      sortedModels.map(model => s"        $model").mkString(",\n"),
      "      )"
    )
  }

  private def createBackup(filePath: String): IO[Unit] =
    IO {
      val backupPath = s"$filePath.backup"
      val content = Using(Source.fromFile(filePath))(_.mkString).get
      val writer = new PrintWriter(backupPath)
      try {
        writer.write(content)
      } finally {
        writer.close()
      }
    }.flatTap(_ => logger.debug(s"📋 Created backup: $filePath.backup"))
}
