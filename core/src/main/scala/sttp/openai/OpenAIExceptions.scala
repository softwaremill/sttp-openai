package sttp.openai

import sttp.client4.{DeserializationException, HttpError, ResponseException}

object OpenAIExceptions {
  sealed abstract class OpenAIException(
      val message: Option[String],
      val `type`: Option[String],
      val param: Option[String],
      val code: Option[String],
      val cause: ResponseException[String, Exception]
  ) extends Exception(cause.getMessage, cause)

  object OpenAIException {
    class DeserializationOpenAIException(
        message: String,
        cause: DeserializationException[Exception]
    ) extends OpenAIException(Some(message), None, None, None, cause)

    object DeserializationOpenAIException {
      def apply(cause: DeserializationException[Exception]): DeserializationOpenAIException =
        new DeserializationOpenAIException(cause.getMessage, cause)

      def apply(cause: Exception): DeserializationOpenAIException = apply(DeserializationException(cause.getMessage, cause))
    }
    class RateLimitException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    class InvalidRequestException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    class AuthenticationException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    class PermissionException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    class TryAgain(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    class ServiceUnavailableException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)

    class APIException(
        message: Option[String],
        `type`: Option[String],
        param: Option[String],
        code: Option[String],
        cause: HttpError[String]
    ) extends OpenAIException(message, `type`, param, code, cause)
  }
}
