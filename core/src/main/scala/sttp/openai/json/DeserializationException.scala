package sttp.openai.json

class DeserializationException(cause: Exception) extends Exception(cause.getMessage, cause)
