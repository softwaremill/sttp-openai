import sbt.*

object Dependencies {

  object V {
    val scalaTest = "3.2.18"
    val scalaTestCats = "1.5.0"

    val sttpClient = "4.0.0-M14"
    val pekkoStreams = "1.0.3"
    val akkaStreams = "2.6.20"
    val uPickle = "3.1.4"
  }

  object Libraries {

    val scalaTest = "org.scalatest" %% "scalatest" % V.scalaTest % Test

    val sttpClient = Seq(
      "com.softwaremill.sttp.client4" %% "core" % V.sttpClient,
      "com.softwaremill.sttp.client4" %% "upickle" % V.sttpClient
    )

    val sttpClientFs2 = Seq(
      "com.softwaremill.sttp.client4" %% "fs2" % V.sttpClient,
      "org.typelevel" %% "cats-effect-testing-scalatest" % V.scalaTestCats % Test
    )

    val sttpClientZio = "com.softwaremill.sttp.client4" %% "zio" % V.sttpClient

    val sttpClientPekko = Seq(
      "com.softwaremill.sttp.client4" %% "pekko-http-backend" % V.sttpClient,
      "org.apache.pekko" %% "pekko-stream" % V.pekkoStreams
    )

    val sttpClientAkka = Seq(
      "com.softwaremill.sttp.client4" %% "akka-http-backend" % V.sttpClient,
      "com.typesafe.akka" %% "akka-stream" % V.akkaStreams
    )

    val uPickle = "com.lihaoyi" %% "upickle" % V.uPickle

  }

}
