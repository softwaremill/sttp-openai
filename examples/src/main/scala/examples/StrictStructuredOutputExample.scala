package examples

/** Manual playground for the *Structured Outputs* feature (`strict = true`).
  *
  * It sends the instruction "Say hello" but constrains the model with a JSON-Schema that requires the reply to be a number. When
  * `strict = true` is set, the schema wins and the model must produce something that validates – hence we expect a number, not the word
  * "hello".
  *
  * Run from the project root folder with: OPENAI_API_KEY=… sbt "examples3/runMain examples.StrictStructuredOutputExample"
  */
object StrictStructuredOutputExample extends App {
  import sttp.apispec.{Schema, SchemaType}
  import sttp.client4.{DefaultSyncBackend, SyncBackend}
  import sttp.openai.OpenAI
  import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel, ResponseFormat}
  import sttp.openai.requests.completions.chat.message.{Content, Message}

  // Reads the API key from the environment – fail fast if it's not there
  val apiKey = sys.env.getOrElse("OPENAI_API_KEY", sys.error("OPENAI_API_KEY env variable not set"))

  // Create a JSON-Schema object with a single numeric field `value`.
  import scala.collection.immutable.ListMap
  val numberSchema: Schema = Schema(SchemaType.Object)
    .copy(
      properties = ListMap("value" -> Schema(SchemaType.Number)),
      required = List("value")
    )

  val chatBody = ChatBody(
    model = ChatCompletionModel.GPT4oMini,
    messages = Seq(Message.UserMessage(Content.TextContent("Say hello"))),
    temperature = Some(0),
    responseFormat = Some(ResponseFormat.JsonSchema("numeric_object", strict = true, numberSchema))
  )

  val backend: SyncBackend = DefaultSyncBackend()
  val openAI = new OpenAI(apiKey)

  println("Sending request …")
  val responseEither = openAI.createChatCompletion(chatBody).send(backend).body

  responseEither match {
    case Left(err) =>
      System.err.println(s"OpenAI returned an error: $err")
    case Right(resp) =>
      val answerRaw = resp.choices.head.message.content
      println(s"Raw content returned by the model: $answerRaw")

      // Parse the JSON object and extract the numeric field
      val parsed = ujson.read(answerRaw)
      val maybeNum = parsed.obj.get("value").flatMap(_.numOpt)

      maybeNum match {
        case Some(n) => println(s"Success, the reply contains a numeric value: $n")
        case None    => println("Failure, the reply didn't conform to the expected schema (no `value` number field).")
      }
  }

  backend.close()
}
