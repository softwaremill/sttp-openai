package sttp.openai.requests.images

import sttp.client4.DeserializationException
import sttp.openai.json.SnakePickle
import ujson.Str

object ImageCreationRequestBody {

  case class ImageCreationBody(
      prompt: String,
      n: Option[Int] = None,
      size: String = "", //
      responseFormat: Option[String] = None,
      user: Option[String] = None
  )

  object ImageCreationBody {
    def apply(
        prompt: String,
        n: Option[Int] = None,
        size: String = "1024x1024",
        responseFormat: Option[String] = None,
        user: Option[String] = None
    ): ImageCreationBody =
      new ImageCreationBody(prompt, n, size, responseFormat, user)

    def apply(
        prompt: String,
        n: Option[Int],
        size: Size,
        responseFormat: Option[String],
        user: Option[String]
    ): ImageCreationBody =
      new ImageCreationBody(prompt, n, size.value, responseFormat, user)

    implicit val imageCreationBodyRW: SnakePickle.ReadWriter[ImageCreationBody] = SnakePickle.macroRW[ImageCreationBody]
  }

  sealed abstract class Size(val value: String)

  object Size {
    case object Small extends Size("256x256")

    case object Medium extends Size("512x512")

    case object Large extends Size("1024x1024")

    val values: Set[Size] = Set(Small, Medium, Large)

    private val byName =
      values.map(s => (s.getClass.getSimpleName.stripSuffix("$"), s)).toMap

    private val byValue =
      values.map(s => (s.value, s)).toMap

    def withName(name: String): Option[Size] = byName.get(name)

    def withValue(size: String): Option[Size] = byValue.get(size)

    implicit val sizeRW: SnakePickle.ReadWriter[Size] = SnakePickle
      .readwriter[ujson.Value]
      .bimap[Size](
        {
          case Small  => SnakePickle.writeJs(Small.value)
          case Medium => SnakePickle.writeJs(Medium.value)
          case Large  => SnakePickle.writeJs(Large.value)
        },
        json =>
          SnakePickle.read[ujson.Value](json) match {
            case Str(value) =>
              withValue(value) match {
                case Some(size) => size // TODO
                case None       => Large // TODO
              }
            case e => throw DeserializationException(e.str, new Exception(s"Could not deserialize: $e"))
          }
      )
  }

}
