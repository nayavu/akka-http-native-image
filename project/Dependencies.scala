import sbt.*

object Dependencies {
  val scalaVersion = "3.2.2"

  val akkaHttpCirceVersion = "1.39.2"
  val akkaHttpVersion = "10.2.10"
  val akkaVersion = "2.6.20"
  val circeVersion = "0.14.5"

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion excludeAll ExclusionRule(organization = "io.circe"),
    "com.typesafe" %% "ssl-config-core" % "0.6.1",
  ).map(_.cross(CrossVersion.for3Use2_13))

  val circe = Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "org.scala-lang" %% "scala3-library" % scalaVersion
  )

  val logger = Seq(
    "org.slf4j" % "slf4j-api" % "2.0.5",
    "ch.qos.logback" % "logback-classic" % "1.4.6"
  )

  val tests = Seq(
    "org.scalatest" %% "scalatest" % "3.2.15" % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
  )

  val all = akka ++ circe ++ logger ++ tests
}
