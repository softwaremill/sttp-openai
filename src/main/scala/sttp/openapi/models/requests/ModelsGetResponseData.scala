package sttp.openapi.models.requests
import upickle.default._

object ModelsGetResponseData {

  case class Data(
      id: String,
      `object`: String,
      created: Int,
      ownedBy: String,
      permission: Seq[Permission],
      root: String,
      parent: Option[String]
  )

  object Data {
    implicit def dataReadWriter: SnakePickle.ReadWriter[Data] = SnakePickle.macroRW[Data]
  }

  case class Permission(
      id: String,
      `object`: String,
      created: Int,
      allowCreateEngine: Boolean,
      allowSampling: Boolean,
      allowLogprobs: Boolean,
      allowSearchIndices: Boolean,
      allowView: Boolean,
      allowFineTuning: Boolean,
      organization: String,
      group: Option[String],
      isBlocking: Boolean
  )

  object Permission {
    implicit def permissionReadWriter: SnakePickle.ReadWriter[Permission] = SnakePickle.macroRW[Permission]
  }

  case class ModelsResponse(`object`: String, data: Seq[Data])
  object ModelsResponse {
    implicit def modelsResponseReadWriter: ReadWriter[ModelsResponse] =
      SnakePickle.macroRW[ModelsResponse].asInstanceOf[ReadWriter[ModelsResponse]]
  }

  object SnakePickle extends upickle.AttributeTagged {
    private def camelToSnake(s: String): String =
      s.replaceAll("([A-Z])", "#$1").split('#').map(_.toLowerCase).mkString("_")

    private def snakeToCamel(s: String): String = {
      val res = s.split("_", -1).map(x => s"${x(0).toUpper}${x.drop(1)}").mkString
      s"${s(0).toLower}${res.drop(1)}"
    }

    override def objectAttributeKeyReadMap(s: CharSequence): String =
      snakeToCamel(s.toString)

    override def objectAttributeKeyWriteMap(s: CharSequence): String =
      camelToSnake(s.toString)

    override def objectTypeKeyReadMap(s: CharSequence): String =
      snakeToCamel(s.toString)

    override def objectTypeKeyWriteMap(s: CharSequence): String =
      camelToSnake(s.toString)
  }
}
