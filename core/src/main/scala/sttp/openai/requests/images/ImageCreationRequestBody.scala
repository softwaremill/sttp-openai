package sttp.openai.requests.images

import sttp.client4.DeserializationException
import sttp.openai.json.SnakePickle
import ujson.Str

object ImageCreationRequestBody {

  case class ImageCreationBody(
      prompt: String,
      n: Option[Int] = None,
      size: Option[String] = None,
      responseFormat: Option[String] = None,
      user: Option[String] = None
  )

  object ImageCreationBody {
    def createWithSize(prompt: String, size: Size): ImageCreationBody = ImageCreationBody(
      prompt = prompt,
      size = Some(size.value)
    )

    implicit val imageCreationBodyRW: SnakePickle.ReadWriter[ImageCreationBody] = SnakePickle.macroRW[ImageCreationBody]
  }

  sealed abstract class Size(val value: String)

  object Size {
    case object Small extends Size("256x256")

    case object Medium extends Size("512x512")

    case object Large extends Size("1024x1024")

    private case object NotSupportedSize extends Size("-1")

    val values: Set[Size] = Set(Small, Medium, Large)

    private val byValue =
      values.map(s => (s.value, s)).toMap

    private def withValue(size: String): Option[Size] = byValue.get(size)

    implicit val sizeRW: SnakePickle.ReadWriter[Size] = SnakePickle
      .readwriter[ujson.Value]
      .bimap[Size](
        _.value,
        jsonValue =>
          SnakePickle.read[ujson.Value](jsonValue) match {
            case Str(value) =>
              withValue(value) match {
                case Some(size) => size
                case None       => NotSupportedSize
                // TODO
                // There are 3 options in my opinion what can we return in this scenario
                // 1. We can return default size for image generation, which is Large
                // 2. We can throw an exception (I'm not a fan of that)
                // 3. Or go with what I've done
              }
            case e => throw DeserializationException(e.str, new Exception(s"Could not deserialize: $e"))
          }
      )
  }

}
