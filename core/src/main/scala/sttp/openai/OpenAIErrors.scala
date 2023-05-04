package sttp.openai

import sttp.client4.HttpError

object OpenAIErrors {
  sealed abstract class OpenAIError(
      message: Option[String],
      `type`: Option[String],
      param: Option[String],
      code: Option[String],
      cause: HttpError[String]
  )

  object OpenAIError {
    case class RateLimitError(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIError(message, `type`, param, code, cause)

    case class InvalidRequestError(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIError(message, `type`, param, code, cause)

    case class AuthenticationError(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIError(message, `type`, param, code, cause)

    case class PermissionError(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIError(message, `type`, param, code, cause)

    case class TryAgain(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIError(message, `type`, param, code, cause)

    case class ServiceUnavailableError(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIError(message, `type`, param, code, cause)

    case class APIError(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIError(message, `type`, param, code, cause)
  }
}
