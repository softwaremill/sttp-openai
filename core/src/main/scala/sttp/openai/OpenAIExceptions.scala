package sttp.openai

import sttp.client4.{DeserializationException, HttpError, ResponseException}

object OpenAIExceptions {
  sealed abstract class OpenAIException(
      message: Option[String],
      `type`: Option[String],
      param: Option[String],
      code: Option[String],
      cause: ResponseException[String, Exception]
  ) extends Exception(cause.getMessage, cause)

  object OpenAIException {
    case class DeserializationOpenAIException(
        message: String,
        cause: DeserializationException[Exception]
    ) extends OpenAIException(Some(message), None, None, None, cause)

    object DeserializationOpenAIException {
      def apply(cause: DeserializationException[Exception]): DeserializationOpenAIException = apply(cause.getMessage, cause)

      def apply(cause: Exception): DeserializationOpenAIException = apply(DeserializationException(cause.getMessage, cause))
    }
    case class RateLimitException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    case class InvalidRequestException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    case class AuthenticationException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    case class PermissionException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    case class TryAgain(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    case class ServiceUnavailableException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    case class APIException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)
  }
}
