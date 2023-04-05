package requests.models

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}

object ModelsGetResponseData {

  case class Data(
      id: String,
      `object`: String,
      created: Int,
      ownedBy: String,
      permission: Seq[Permission],
      root: String,
      parent: Option[String])

  case class Permission(
      id: String,
      `object`: String,
      created: Int,
      allowCreateEngine: Boolean,
      allowSampling: Boolean,
      allowLogprobs: Boolean,
      allowSearch_indices: Boolean,
      allowView: Boolean,
      allowFine_tuning: Boolean,
      organization: String,
      group: Option[String],
      isBlocking: Boolean)

  case class ModelsResponse(`object`: String, data: Seq[Data])

  implicit val jsonEitherDecoder: JsonValueCodec[ModelsResponse] = JsonCodecMaker.make {
    CodecMakerConfig.withFieldNameMapper(
      JsonCodecMaker.enforce_snake_case
    )
  }
}
