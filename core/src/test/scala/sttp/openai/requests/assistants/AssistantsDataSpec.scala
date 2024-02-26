package sttp.openai.requests.assistants

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client4.IsOption._
import sttp.openai.fixtures
import sttp.openai.json.SnakePickle
import sttp.openai.json.SttpUpickleApiExtension
import sttp.openai.requests.completions.chat.message.Tool.CodeInterpreterTool
import sttp.openai.requests.completions.chat.message.Tool.RetrievalTool

class AssistantsDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given create assistant request" should "be properly serialized to Json" in {
    // given
    val givenRequest = AssistantsRequestBody.CreateAssistantBody(
      instructions = Some("You are a personal math tutor. When asked a question, write and run Python code to answer the question."),
      name = Some("Math Tutor"),
      tools = Seq(CodeInterpreterTool),
      model = "gpt-4"
    )

    val jsonRequest: ujson.Value = ujson.read(fixtures.AssistantsFixture.jsonCreateAssistantRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

  "Given create assistant response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.assistants.AssistantsResponseData.AssistantData._
    import sttp.openai.requests.assistants.AssistantsResponseData._

    // given
    val jsonResponse = fixtures.AssistantsFixture.jsonCreateAssistantResponse
    val expectedResponse: AssistantData = AssistantData(
      id = "asst_abc123",
      `object` = "assistant",
      createdAt = 1698984975,
      name = Some("Math Tutor"),
      description = None,
      model = "gpt-4",
      instructions = Some("You are a personal math tutor. When asked a question, write and run Python code to answer the question."),
      tools = Seq(
        CodeInterpreterTool
      ),
      fileIds = Seq(),
      metadata = Map.empty
    )

    // when
    val givenResponse: Either[Exception, AssistantData] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given create assistant file request" should "be properly serialized to Json" in {
    // given
    val givenRequest = AssistantsRequestBody.CreateAssistantFileBody(
      fileId = "file-abc123"
    )

    val jsonRequest: ujson.Value = ujson.read(fixtures.AssistantsFixture.jsonCreateAssistantFileRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

  "Given create assistant file response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.assistants.AssistantsResponseData.AssistantFileData._
    import sttp.openai.requests.assistants.AssistantsResponseData._

    // given
    val jsonResponse = fixtures.AssistantsFixture.jsonCreateAssistantFileResponse
    val expectedResponse: AssistantFileData = AssistantFileData(
      id = "file-abc123",
      `object` = "assistant.file",
      createdAt = 1699055364,
      assistantId = "asst_abc123"
    )

    // when
    val givenResponse: Either[Exception, AssistantFileData] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given list assistants response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.assistants.AssistantsResponseData.ListAssistantsResponse._
    import sttp.openai.requests.assistants.AssistantsResponseData._

    // given
    val jsonResponse = fixtures.AssistantsFixture.jsonListAssistantsResponse
    val expectedResponse: ListAssistantsResponse = ListAssistantsResponse(
      `object` = "list",
      data = Seq(
        AssistantData(
          id = "asst_abc123",
          `object` = "assistant",
          createdAt = 1698982736,
          name = Some("Coding Tutor"),
          description = None,
          model = "gpt-4",
          instructions = Some("You are a helpful assistant designed to make me better at coding!"),
          tools = Seq(),
          fileIds = Seq(),
          metadata = Map.empty
        ),
        AssistantData(
          id = "asst_abc456",
          `object` = "assistant",
          createdAt = 1698982718,
          name = Some("My Assistant"),
          description = None,
          model = "gpt-4",
          instructions = Some("You are a helpful assistant designed to make me better at coding!"),
          tools = Seq(),
          fileIds = Seq(),
          metadata = Map.empty
        ),
        AssistantData(
          id = "asst_abc789",
          `object` = "assistant",
          createdAt = 1698982643,
          name = None,
          description = None,
          model = "gpt-4",
          instructions = None,
          tools = Seq(),
          fileIds = Seq(),
          metadata = Map.empty
        )
      ),
      firstId = "asst_abc123",
      lastId = "asst_abc789",
      hasMore = false
    )

    // when
    val givenResponse: Either[Exception, ListAssistantsResponse] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given list assistant files response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.assistants.AssistantsResponseData.ListAssistantsResponse._
    import sttp.openai.requests.assistants.AssistantsResponseData._

    // given
    val jsonResponse = fixtures.AssistantsFixture.jsonListAssistantsResponse
    val expectedResponse: ListAssistantsResponse = ListAssistantsResponse(
      `object` = "list",
      data = Seq(
        AssistantData(
          id = "asst_abc123",
          `object` = "assistant",
          createdAt = 1698982736,
          name = Some("Coding Tutor"),
          description = None,
          model = "gpt-4",
          instructions = Some("You are a helpful assistant designed to make me better at coding!"),
          tools = Seq(),
          fileIds = Seq(),
          metadata = Map.empty
        ),
        AssistantData(
          id = "asst_abc456",
          `object` = "assistant",
          createdAt = 1698982718,
          name = Some("My Assistant"),
          description = None,
          model = "gpt-4",
          instructions = Some("You are a helpful assistant designed to make me better at coding!"),
          tools = Seq(),
          fileIds = Seq(),
          metadata = Map.empty
        ),
        AssistantData(
          id = "asst_abc789",
          `object` = "assistant",
          createdAt = 1698982643,
          name = None,
          description = None,
          model = "gpt-4",
          instructions = None,
          tools = Seq(),
          fileIds = Seq(),
          metadata = Map.empty
        )
      ),
      firstId = "asst_abc123",
      lastId = "asst_abc789",
      hasMore = false
    )

    // when
    val givenResponse: Either[Exception, ListAssistantsResponse] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given retrieve assistant response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.assistants.AssistantsResponseData.AssistantData._
    import sttp.openai.requests.assistants.AssistantsResponseData._

    // given
    val jsonResponse = fixtures.AssistantsFixture.jsonRetrieveAssistantResponse
    val expectedResponse: AssistantData = AssistantData(
      id = "asst_abc123",
      `object` = "assistant",
      createdAt = 1699009709,
      name = Some("HR Helper"),
      description = None,
      model = "gpt-4",
      instructions = Some("You are an HR bot, and you have access to files to answer employee questions about company policies."),
      tools = Seq(
        RetrievalTool
      ),
      fileIds = Seq("file-abc123"),
      metadata = Map.empty
    )

    // when
    val givenResponse: Either[Exception, AssistantData] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given retrieve assistant file response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.assistants.AssistantsResponseData.AssistantFileData._
    import sttp.openai.requests.assistants.AssistantsResponseData._

    // given
    val jsonResponse = fixtures.AssistantsFixture.jsonRetrieveAssistantFileResponse
    val expectedResponse: AssistantFileData = AssistantFileData(
      id = "file-abc123",
      `object` = "assistant.file",
      createdAt = 1699055364,
      assistantId = "asst_abc123"
    )

    // when
    val givenResponse: Either[Exception, AssistantFileData] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given modify assistant request" should "be properly serialized to Json" in {
    // given
    val givenRequest = AssistantsRequestBody.ModifyAssistantBody(
      instructions = Some(
        "You are an HR bot, and you have access to files to answer employee questions about company policies. Always response with info from either of the files."
      ),
      tools = Seq(RetrievalTool),
      model = Some("gpt-4"),
      fileIds = Seq("file-abc123", "file-abc456")
    )

    val jsonRequest: ujson.Value = ujson.read(fixtures.AssistantsFixture.jsonModifyAssistantRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

  "Given modify assistant response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.assistants.AssistantsResponseData.AssistantData._
    import sttp.openai.requests.assistants.AssistantsResponseData._

    // given
    val jsonResponse = fixtures.AssistantsFixture.jsonModifyAssistantResponse
    val expectedResponse: AssistantData = AssistantData(
      id = "asst_abc123",
      `object` = "assistant",
      createdAt = 1699009709,
      name = Some("HR Helper"),
      description = None,
      model = "gpt-4",
      instructions = Some(
        "You are an HR bot, and you have access to files to answer employee questions about company policies. Always response with info from either of the files."
      ),
      tools = Seq(
        RetrievalTool
      ),
      fileIds = Seq("file-abc123", "file-abc456"),
      metadata = Map.empty
    )

    // when
    val givenResponse: Either[Exception, AssistantData] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given delete assistant response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.assistants.AssistantsResponseData.DeleteAssistantResponse._
    import sttp.openai.requests.assistants.AssistantsResponseData._

    // given
    val jsonResponse = fixtures.AssistantsFixture.jsonDeleteAssistantResponse
    val expectedResponse: DeleteAssistantResponse = DeleteAssistantResponse(
      id = "asst_abc123",
      `object` = "assistant.deleted",
      deleted = true
    )

    // when
    val givenResponse: Either[Exception, DeleteAssistantResponse] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given delete assistant file response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.assistants.AssistantsResponseData.DeleteAssistantFileResponse._
    import sttp.openai.requests.assistants.AssistantsResponseData._

    // given
    val jsonResponse = fixtures.AssistantsFixture.jsonDeleteAssistantFileResponse
    val expectedResponse: DeleteAssistantFileResponse = DeleteAssistantFileResponse(
      id = "file-abc123",
      `object` = "assistant.file.deleted",
      deleted = true
    )

    // when
    val givenResponse: Either[Exception, DeleteAssistantFileResponse] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }
}
