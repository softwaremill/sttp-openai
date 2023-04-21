package sttp.openai.requests.moderations

import sttp.openai.json.SnakePickle
object ModerationsRequestBody {

  /** @param input
    *   The input text to classify.
    * @param model
    *   Specifies content moderation models.
    */
  case class ModerationsBody(input: String, model: Option[ModerationModel] = None)

  object ModerationsBody {
    implicit val moderationsBodyWriter: SnakePickle.Writer[ModerationsBody] = SnakePickle.macroW[ModerationsBody]
  }
  sealed trait ModerationModel
  object ModerationModel {
    case object TextModerationStable extends ModerationModel

    case object TextModerationLatest extends ModerationModel

    case class Custom(value: String) extends ModerationModel

    implicit val moderationsBodyWriter: SnakePickle.Writer[ModerationModel] =
      SnakePickle.writer[String].comap[ModerationModel] {
        case TextModerationStable => "text-moderation-stable"
        case TextModerationLatest => "text-moderation-latest"
        case Custom(value)        => value
      }

  }
}
