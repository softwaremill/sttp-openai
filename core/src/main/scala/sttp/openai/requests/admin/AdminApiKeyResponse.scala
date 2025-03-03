package sttp.openai.requests.admin

import sttp.openai.json.SnakePickle

case class AdminApiKeyResponse(
    `object`: String = "organization.admin_api_key",
    id: String,
    name: String,
    redactedValue: String,
    createdAt: Int,
    owner: Owner,
    value: Option[String]
)

object AdminApiKeyResponse {
  implicit val adminApiKeyResponseR: SnakePickle.Reader[AdminApiKeyResponse] = SnakePickle.macroR[AdminApiKeyResponse]
}

case class Owner(
    `type`: String,
    `object`: String,
    id: String,
    name: String,
    createdAt: Int,
    role: String
)

object Owner {
  implicit val ownerR: SnakePickle.Reader[Owner] = SnakePickle.macroR[Owner]
}
