package sttp.openai.requests.completions

import sttp.openai.json.SnakePickle

/** @param promptTokens
  *   Number of tokens in the prompt.
  * @param completionTokens
  *   Number of tokens in the generated completion.
  * @param totalTokens
  *   Total number of tokens used in the request (prompt + completion).
  * @param completionTokensDetails
  *   Breakdown of tokens used in a completion.
  * @param promptTokensDetails
  *   Breakdown of tokens used in the prompt.
  */
case class Usage(
    promptTokens: Int,
    completionTokens: Int,
    totalTokens: Int,
    completionTokensDetails: Option[CompletionTokensDetails] = None,
    promptTokensDetails: Option[PromptTokensDetails] = None
)

object Usage {
  implicit val choicesR: SnakePickle.Reader[Usage] = SnakePickle.macroR[Usage]
}

/** @param acceptedPredictionTokens
  *   When using Predicted Outputs, the number of tokens in the prediction that appeared in the completion.
  * @param audioTokens
  *   Audio input tokens generated by the model.
  * @param reasoningTokens
  *   Tokens generated by the model for reasoning.
  * @param rejectedPredictionTokens
  *   When using Predicted Outputs, the number of tokens in the prediction that did not appear in the completion. However, like reasoning
  *   tokens, these tokens are still counted in the total completion tokens for purposes of billing, output, and context window limits.
  */
case class CompletionTokensDetails(
    acceptedPredictionTokens: Int,
    audioTokens: Int,
    reasoningTokens: Int,
    rejectedPredictionTokens: Int
)

object CompletionTokensDetails {
  implicit val completionTokensDetailsR: SnakePickle.Reader[CompletionTokensDetails] = SnakePickle.macroR[CompletionTokensDetails]
}

/** @param audioTokens
  *   Audio input tokens present in the prompt.
  * @param cachedTokens
  *   Cached tokens present in the prompt.
  */
case class PromptTokensDetails(
    audioTokens: Int,
    cachedTokens: Int
)

object PromptTokensDetails {
  implicit val promptTokensDetailsR: SnakePickle.Reader[PromptTokensDetails] = SnakePickle.macroR[PromptTokensDetails]
}
