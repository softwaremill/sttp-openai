package sttp.openai.requests.images.edit

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client4.{BasicBodyPart, BodyPart, FileBody, MultipartBody, StringBody}
import sttp.model.{MediaType, Part, Uri}
import sttp.openai.fixtures.ImageEditsFixture
import sttp.openai.OpenAI

class ImageEditsDataSpec extends AnyFlatSpec with Matchers with ImageEditsFixture {
  val openAI = new OpenAI("test-key", Uri.unsafeParse("https://api.openai.com/v1"))

  private def checkFilePart(part: Part[BodyPart[BasicBodyPart]], name: String, file: java.io.File) = {
    part.name shouldBe name
    part.body shouldBe a[FileBody]
    part.headers should contain(sttp.model.Header("Content-Type", "application/octet-stream"))
    part.otherDispositionParams should contain("filename" -> file.getName)
  }

  private def checkStringPart(part: Part[BodyPart[BasicBodyPart]], name: String, value: String) = {
    part.name shouldBe name
    part.body shouldBe a[StringBody]
    part.body.asInstanceOf[StringBody].s shouldBe value
    part.headers should contain(sttp.model.Header("Content-Type", "text/plain; charset=utf-8"))
  }

  "imageEdits" should "create correct request with minimal config (single image)" in {
    val request = openAI.imageEdits(minimalImageEditsConfig)
    val body = request.body.asInstanceOf[MultipartBody[BasicBodyPart]]
    val parts = body.parts

    parts should have size 2
    
    val imagePart = parts.find(_.name == "image").get
    checkFilePart(imagePart, "image", minimalImageEditsConfig.image.head)
    
    parts.find(_.name == "image[]") shouldBe None
    
    val promptPart = parts.find(_.name == "prompt").get
    checkStringPart(promptPart, "prompt", minimalImageEditsConfig.prompt)
  }

  it should "create correct request with all parameters for single image" in {
    val request = openAI.imageEdits(sampleImageEditsConfig)
    val body = request.body.asInstanceOf[MultipartBody[BasicBodyPart]]
    val parts = body.parts

    parts should have size 15

    val imagePart = parts.find(_.name == "image").get
    checkFilePart(imagePart, "image", sampleImageEditsConfig.image.head)
    
    parts.find(_.name == "image[]") shouldBe None

    val maskPart = parts.find(_.name == "mask").get
    checkFilePart(maskPart, "mask", sampleImageEditsConfig.mask.get)

    checkStringPart(parts.find(_.name == "prompt").get, "prompt", sampleImageEditsConfig.prompt)
    checkStringPart(parts.find(_.name == "background").get, "background", "transparent")
    checkStringPart(parts.find(_.name == "input_fidelity").get, "input_fidelity", "high")
    checkStringPart(parts.find(_.name == "model").get, "model", "gpt-image-1")
    checkStringPart(parts.find(_.name == "n").get, "n", "2")
    checkStringPart(parts.find(_.name == "output_compression").get, "output_compression", "80")
    checkStringPart(parts.find(_.name == "output_format").get, "output_format", "png")
    checkStringPart(parts.find(_.name == "partial_images").get, "partial_images", "2")
    checkStringPart(parts.find(_.name == "quality").get, "quality", "high")
    checkStringPart(parts.find(_.name == "size").get, "size", "1024x1024")
    checkStringPart(parts.find(_.name == "response_format").get, "response_format", "url")
    checkStringPart(parts.find(_.name == "stream").get, "stream", "false")
    checkStringPart(parts.find(_.name == "user").get, "user", "test-user")
  }

  it should "create correct request with multiple images" in {
    val request = openAI.imageEdits(multiImageEditsConfig)
    val body = request.body.asInstanceOf[MultipartBody[BasicBodyPart]]
    val parts = body.parts

    parts should have size 16 // one extra for the second image
    
    parts.find(_.name == "image") shouldBe None
    
    val imageParts = parts.filter(_.name == "image[]")
    imageParts should have size 2
    imageParts.zipWithIndex.foreach { case (part, idx) =>
      checkFilePart(part, "image[]", multiImageEditsConfig.image(idx))
    }
    
    checkStringPart(parts.find(_.name == "prompt").get, "prompt", multiImageEditsConfig.prompt)
  }

  it should "handle empty optional parameters" in {
    val request = openAI.imageEdits(minimalImageEditsConfig)
    val body = request.body.asInstanceOf[MultipartBody[BasicBodyPart]]
    val parts = body.parts

    parts.map(_.name) should not contain("background")
    parts.map(_.name) should not contain("input_fidelity")
    parts.map(_.name) should not contain("mask")
    parts.map(_.name) should not contain("model")
    parts.map(_.name) should not contain("n")
    parts.map(_.name) should not contain("output_compression")
    parts.map(_.name) should not contain("output_format")
    parts.map(_.name) should not contain("partial_images")
    parts.map(_.name) should not contain("quality")
    parts.map(_.name) should not contain("size")
    parts.map(_.name) should not contain("response_format")
    parts.map(_.name) should not contain("stream")
    parts.map(_.name) should not contain("user")
  }
} 