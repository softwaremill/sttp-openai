//> using scala 3.7.2
//> using dep org.typelevel::cats-effect::3.6.3
//> using dep org.typelevel::log4cats-slf4j::2.7.1
//> using dep ch.qos.logback:logback-classic:1.5.18
//> using dep com.github.scopt::scopt::4.1.0
//> using dep com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-core::2.37.6
//> using dep com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-macros::2.37.6
//> using dep org.virtuslab::scala-yaml::0.3.0

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all.*
import ch.qos.logback.classic.{Level, LoggerContext}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import org.slf4j.LoggerFactory
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.virtuslab.yaml.*
import scopt.OParser

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
    dryRun: Boolean = true,
    debug: Boolean = false
)

case class EndpointConfig(
    file: String,
    className: String,
    insertBeforeMarker: String,
    valuesSetName: Option[String]
) derives YamlCodec

case class NameConversionConfig(
    preserveCase: List[String],
    specialCases: Map[String, String]
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
      import builder.*
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
          .text("Preview changes without applying them (default behavior)"),
        opt[Unit]("apply")
          .action((_, c) => c.copy(dryRun = false))
          .text("Apply changes to files (overrides default dry-run mode)"),
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
      _ <-
        if (config.dryRun) {
          logger.info("🔧 Starting Model Case Class Updater (DRY-RUN MODE - use --apply to make changes)...")
        } else {
          logger.info("🔧 Starting Model Case Class Updater (APPLY MODE)...")
        }

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

      currentContent <- IO {
        Using(Source.fromFile(resolvedFilePath))(_.mkString).get
      }

      existingModels <- extractExistingModels(currentContent, endpointConfig.className)

      newModels <- models.traverse(modelName => convertModelNameToScalaId(modelName, nameConversion).map(modelName -> _))

      modelsToAdd = newModels.filterNot { case (_, scalaId) =>
        existingModels.contains(scalaId)
      }
      _ <-
        if (modelsToAdd.nonEmpty) {
          for {
            _ <- logger.info(s"➕ Adding ${modelsToAdd.size} new models:")
            _ <- modelsToAdd.traverse { case (modelName, scalaId) =>
              logger.info(s"   $modelName → case object $scalaId")
            }
            newContent <- IO(
              generateUpdatedContent(
                currentContent,
                endpointConfig,
                modelsToAdd,
                existingModels
              )
            )
            _ <-
              if (dryRun) {
                for {
                  _ <- logger.info(s"📄 File: $resolvedFilePath")
                  _ <- logger.info("📝 New case objects that would be added:")
                  _ <- modelsToAdd.traverse { case (modelName, scalaId) =>
                    logger.info(s"    case object $scalaId extends ${endpointConfig.className}(\"$modelName\")")
                  }
                } yield ()
              } else {
                for {
                  _ <- writeToFile(resolvedFilePath, newContent)
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
            nameConversion.preserveCase.find(_.equalsIgnoreCase(word)) match {
              case Some(preservedWord) => preservedWord
              case None                =>
                // For dates (YYYY-MM-DD format becomes YYYYMMDD), keep as is
                if (word.matches("\\d{4}\\d{2}\\d{2}") || word.matches("\\d+")) {
                  word
                } else {
                  word.toLowerCase.capitalize
                }
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
  ): String =
    val lines = currentContent.split("\n").toList
    val insertIndex = lines.indexWhere(_.contains(endpointConfig.insertBeforeMarker))

    if (insertIndex == -1) {
      throw new Exception(s"Could not find insertion marker: ${endpointConfig.insertBeforeMarker}")
    }

    val actualInsertIndex = findInsertionPoint(lines, insertIndex)

    val newCaseObjects = modelsToAdd.map { case (modelName, scalaId) =>
      s"  case object $scalaId extends ${endpointConfig.className}(\"$modelName\")"
    }

    val beforeInsert = lines.take(actualInsertIndex)
    val afterInsert = lines.drop(actualInsertIndex)

    val updatedLines = beforeInsert ++ newCaseObjects ++ List("") ++ afterInsert

    endpointConfig.valuesSetName match {
      case Some(valuesSetName) =>
        updateValuesSet(updatedLines.mkString("\n"), valuesSetName, existingModels ++ modelsToAdd.map(_._2), endpointConfig.className)
      case None =>
        updatedLines.mkString("\n")
    }

  private def updateValuesSet(content: String, valuesSetName: String, allModels: List[String], className: String): String = {
    val lines = content.split("\n").toList
    val valuesPattern = s"val $valuesSetName: Set\\[.*?\\] ="
    val startIndex = lines.indexWhere(_.matches(s".*$valuesPattern.*"))

    if (startIndex == -1) {
      content
    } else {
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
            (lines.take(startIndex) ++
              generateValuesSetLines(valuesSetName, allModels, className) ++
              lines.drop(endIndex + 1)).mkString("\n")
          }
        }
      }

      content
    }
  }

  private def findInsertionPoint(lines: List[String], markerIndex: Int): Int = {
    // Look backwards from the marker to find comment lines
    var currentIndex = markerIndex - 1
    var commentStart = markerIndex

    // Skip empty lines immediately before the marker
    while (currentIndex >= 0 && lines(currentIndex).trim.isEmpty)
      currentIndex -= 1

    // Check if we have comment lines
    while (currentIndex >= 0 && isCommentLine(lines(currentIndex))) {
      commentStart = currentIndex
      currentIndex -= 1
    }

    // If we found comments, insert before them (but after any preceding empty line)
    if (commentStart < markerIndex) {
      // Look for an empty line before the comments to maintain spacing
      if (currentIndex >= 0 && lines(currentIndex).trim.isEmpty) {
        currentIndex + 1 // Insert after the empty line
      } else {
        commentStart // Insert directly before comments
      }
    } else {
      markerIndex // No comments found, use original marker position
    }
  }

  private def isCommentLine(line: String): Boolean = {
    val trimmed = line.trim
    trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")
  }

  private def writeToFile(filePath: String, content: String): IO[Unit] = IO {
    val writer = new PrintWriter(filePath)
    try
      writer.write(content)
    finally
      writer.close()
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
