package sttp.openai.utils

import sttp.openai.json.SnakePickle.{read, write}

object JsonUtils {
  def compactJson(json: String): String = write(read[ujson.Value](json))
}
