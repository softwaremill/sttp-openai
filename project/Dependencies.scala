import sbt.*

object Dependencies {

  object V {
    val sttpClient = "4.0.0-M1"
    val uPickle = "3.1.0"
    val scalaTest = "3.2.15"
  }

  object Libraries {

    val sttpClient = Seq(
      "com.softwaremill.sttp.client4" %% "core" % V.sttpClient,
      "com.softwaremill.sttp.client4" %% "upickle" % V.sttpClient
    )
    val scalaTest = "org.scalatest" %% "scalatest" % V.scalaTest % Test

    val uPickle = "com.lihaoyi" %% "upickle" % V.uPickle

  }

}
