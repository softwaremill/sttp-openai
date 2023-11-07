package sttp.openai.fixtures

import sttp.client4._
import sttp.model.StatusCode
import sttp.model.StatusCode._
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.OpenAIExceptions.OpenAIException._

object ErrorFixture {
  private val (message, errorType, param, code) = ("Some error message.", "error_type", "null", "invalid_api_key")

  val errorResponse =
    s"""
      |{
      |  "error": {
      |    "message": "$message",
      |    "type": "$errorType",
      |    "param": $param,
      |    "code": "$code"
      |  }
      |}""".stripMargin

  val testData: Seq[(StatusCode, OpenAIException)] = List(
    (
      TooManyRequests,
      new RateLimitException(Some(message), Some(errorType), None, Some(code), HttpError(errorResponse, TooManyRequests))
    ),
    (
      BadRequest,
      new InvalidRequestException(Some(message), Some(errorType), None, Some(code), HttpError(errorResponse, BadRequest))
    ),
    (
      NotFound,
      new InvalidRequestException(Some(message), Some(errorType), None, Some(code), HttpError(errorResponse, NotFound))
    ),
    (
      UnsupportedMediaType,
      new InvalidRequestException(Some(message), Some(errorType), None, Some(code), HttpError(errorResponse, UnsupportedMediaType))
    ),
    (
      Unauthorized,
      new AuthenticationException(Some(message), Some(errorType), None, Some(code), HttpError(errorResponse, Unauthorized))
    ),
    (
      Forbidden,
      new PermissionException(Some(message), Some(errorType), None, Some(code), HttpError(errorResponse, Forbidden))
    ),
    (
      Conflict,
      new TryAgain(Some(message), Some(errorType), None, Some(code), HttpError(errorResponse, Conflict))
    ),
    (
      ServiceUnavailable,
      new ServiceUnavailableException(Some(message), Some(errorType), None, Some(code), HttpError(errorResponse, ServiceUnavailable))
    ),
    (
      Gone,
      new APIException(Some(message), Some(errorType), None, Some(code), HttpError(errorResponse, Gone))
    )
  )
}
