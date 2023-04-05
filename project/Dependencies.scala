import sbt.*

object Dependencies {

  object V {
    val sttpClient = "4.0.0-M1"
    val jsoniter   = "2.21.3"
    val scalaTest  = "3.2.15"
  }

  object Libraries {

    val sttpClient = Seq(
      "com.softwaremill.sttp.client4" %% "core"     % V.sttpClient,
      "com.softwaremill.sttp.client4" %% "jsoniter" % V.sttpClient
    )
    val scalaTest = "org.scalatest" %% "scalatest" % V.scalaTest % Test
//    val catsCore = "org.typelevel" %% "cats-core" % V.cats
//    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
//    val cats = Seq(catsCore, catsEffect)
//
//    val pureConfig = "com.github.pureconfig" %% "pureconfig" % V.pureConfig

    val jsoniter = Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % V.jsoniter,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % V.jsoniter
    )

  }

}
