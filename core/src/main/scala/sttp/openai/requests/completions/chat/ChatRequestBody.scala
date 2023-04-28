package sttp.openai.requests.completions.chat

import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.Stop

object ChatRequestBody {

  /** @param model
    *   ID of the model to use.
    * @param messages
    *   A list of messages describing the conversation so far.
    * @param temperature
    *   What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like
    *   0.2 will make it more focused and deterministic.
    * @param topP
    *   An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p
    *   probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.
    * @param n
    *   How many chat completion choices to generate for each input message.
    * @param stop
    *   Up to 4 sequences where the API will stop generating further tokens.
    * @param maxTokens
    *   The maximum number of tokens to generate in the chat completion.
    * @param presencePenalty
    *   Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far, increasing the
    *   model's likelihood to talk about new topics.
    * @param frequencyPenalty
    *   Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far, decreasing
    *   the model's likelihood to repeat the same line verbatim.
    * @param logitBias
    *   Modify the likelihood of specified tokens appearing in the completion.
    * @param user
    *   A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
    */
  case class ChatBody(
      model: String,
      messages: Seq[Message],
      temperature: Option[Double] = None,
      topP: Option[Double] = None,
      n: Option[Int] = None,
      stop: Option[Stop] = None,
      maxTokens: Option[Int] = None,
      presencePenalty: Option[Double] = None,
      frequencyPenalty: Option[Double] = None,
      logitBias: Option[Map[String, Float]] = None,
      user: Option[String] = None
  )

  object ChatBody {
    implicit val chatRequestW: SnakePickle.Writer[ChatBody] = SnakePickle.macroW[ChatBody]
  }

}
