//> using scala 3.3.4
//> using dep org.typelevel::cats-effect::3.6.3
//> using dep org.typelevel::log4cats-slf4j::2.7.1
//> using dep ch.qos.logback:logback-classic:1.5.18
//> using dep com.github.scopt::scopt::4.1.0
//> using dep com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-core::2.37.6
//> using dep com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-macros::2.37.6
//> using dep org.virtuslab::scala-yaml::0.3.0

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ch.qos.logback.classic.{Level, LoggerContext}
import org.slf4j.LoggerFactory
import scopt.OParser
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import org.virtuslab.yaml._
import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.{Try, Using}

opaque type Endpoint = String

object Endpoint {
  def apply(value: String): Endpoint = value

  extension (endpoint: Endpoint) {
    def value: String = endpoint
  }
}

given JsonValueCodec[NameConversionConfig] = JsonCodecMaker.make
given JsonValueCodec[ModelUpdateConfig] = JsonCodecMaker.make
given JsonValueCodec[ModelWithSnapshots] = JsonCodecMaker.make
given JsonValueCodec[Map[String, List[ModelWithSnapshots]]] = JsonCodecMaker.make

case class UpdaterConfig(
    input: Option[String] = None,
    config: String = "model_update_config.yaml",
    dryRun: Boolean = false,
    debug: Boolean = false
)

case class EndpointConfig(
    file: String,
    className: String,
    insertBeforeMarker: String,
    valuesSetName: Option[String]
) derives YamlCodec

case class NameConversionConfig(
    preserveUppercase: List[String], // Words that should remain uppercase (GPT, DALL, etc.)
    capitalizeWords: List[String],   // Words that should be capitalized (mini, nano, etc.)
    specialCases: Map[String, String] // Direct mappings for special cases
) derives YamlCodec

case class ModelWithSnapshots(name: String, snapshots: List[String])

case class ModelUpdateConfig(
    endpoints: Map[String, EndpointConfig],
    nameConversion: NameConversionConfig
) derives YamlCodec

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
          .text("Enable debug logging")
      )
    }

    OParser.parse(parser, args, UpdaterConfig()) match {
      case Some(config) => Right(config)
      case None =>
        Left("error")
    }
  }

  def run(args: List[String]): IO[ExitCode] =
    parseArgs(args).flatMap {
      case Right(config) =>
        runUpdater(config).as(ExitCode.Success)
      case Left("help") =>
        IO.pure(ExitCode.Success)
      case Left(_) =>
        IO.pure(ExitCode.Error)
    }

  private def runUpdater(config: UpdaterConfig): IO[Unit] =
    for {
      _ <- configureLogging(if (config.debug) Level.DEBUG else Level.INFO)
      _ <- logger.info("🔧 Starting Model Case Class Updater...")

      inputFile <- config.input match {
        case Some(file) => IO.pure(file)
        case None       => IO.raiseError(new Exception("--input parameter must be specified"))
      }

      modelConfig <- loadModelConfig(config.config)
      endpointMapping <- loadEndpointMapping(inputFile)

      _ <- updateModelClasses(modelConfig, endpointMapping, config.dryRun)

      _ <- logger.info("✅ Model update process completed!")
    } yield ()

  private def loadModelConfig(configPath: String): IO[ModelUpdateConfig] =
    for {
      resolvedConfigPath <- IO.pure(resolveFilePath(configPath))
      _ <- logger.debug(s"📖 Loading config from $resolvedConfigPath...")
      content <- IO {
        Using(Source.fromFile(resolvedConfigPath))(_.mkString).get
      }
      config <- IO {
        try
          content.as[ModelUpdateConfig] match {
            case Right(config) => config
            case Left(error)   => throw new Exception(s"Failed to parse YAML config: $error")
          }
        catch {
          case e: Exception => throw e
        }
      }
      _ <- logger.debug(s"✅ Loaded config with ${config.endpoints.size} endpoints")
    } yield config

  private def loadEndpointMapping(inputPath: String): IO[Map[String, List[ModelWithSnapshots]]] =
    for {
      resolvedInputPath <- IO.pure(resolveFilePath(inputPath))
      _ <- logger.debug(s"📖 Loading endpoint mapping from $resolvedInputPath...")
      content <- IO {
        Using(Source.fromFile(resolvedInputPath))(_.mkString).get
      }
      mapping <- IO {
        try
          readFromString[Map[String, List[ModelWithSnapshots]]](content)
        catch {
          case e: JsonReaderException => throw new Exception(s"Failed to parse endpoint mapping: ${e.getMessage}")
        }
      }
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
            val allModelNames = modelsWithSnapshots.flatMap { modelWithSnapshots =>
              modelWithSnapshots.snapshots
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
      newModels <- models.traverse(modelName => convertModelNameToScalaId(modelName, nameConversion).map(modelName -> _))

      // Filter out existing models
      modelsToAdd = newModels.filterNot { case (_, scalaId) =>
        existingModels.contains(scalaId)
      }

      _ <-
        if (modelsToAdd.nonEmpty) {
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
            _ <-
              if (dryRun) {
                logger.info("🔍 DRY RUN - Changes would be applied to file")
              } else {
                for {
                  // Write updated content
                  _ <- IO {
                    val writer = new PrintWriter(resolvedFilePath)
                    try
                      writer.write(newContent)
                    finally
                      writer.close()
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

        objectLines.collect { case caseObjectPattern(name) =>
          name
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
          val words = modelName.split("[\\-\\._\\s]+").filter(_.nonEmpty)
          val processedWords = words.map { word =>
            val lowerWord = word.toLowerCase
            val upperWord = word.toUpperCase

            // Check if word should be preserved as uppercase
            if (nameConversion.preserveUppercase.contains(upperWord)) {
              upperWord
            }
            // Check if word should be capitalized
            else if (nameConversion.capitalizeWords.contains(lowerWord)) {
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
}
