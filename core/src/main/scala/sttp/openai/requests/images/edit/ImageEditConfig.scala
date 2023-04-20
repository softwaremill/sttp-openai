package sttp.openai.requests.images.edit

import sttp.openai.requests.images.{ResponseFormat, Size}

import java.io.File

//class ImageEditConfig
//
//object ImageEditConfig {
//  case class DefaultConfig(
//      mask: Option[File],
//      n: Option[Int],
//      size: Option[String],
//      responseFormat: Option[String]
//  ) extends ImageEditConfig
//
//  case class ImageConfig(
//      mask: Option[File],
//      n: Option[Int],
//      size: Option[Size],
//      responseFormat: Option[ResponseFormat]
//  ) extends ImageEditConfig
//}

case class ImageEditConfig(image: File, prompt: String, size: Option[Size] = None)