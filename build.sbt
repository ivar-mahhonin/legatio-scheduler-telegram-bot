import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"
val AkkaVersion = "2.6.19"

lazy val root = (project in file("."))
  .settings(
    name := "legatio-scheduler-telegram-bot",
    libraryDependencies += scalaTest % Test
  )

libraryDependencies += "com.bot4s" %% "telegram-core" % "5.6.0"

libraryDependencies ++= Seq(
  "biz.enef" %% "slogging-slf4j" % "0.6.2",
  "org.slf4j" % "slf4j-simple" % "1.7.36"  // or another slf4j implementation
)
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.3"
libraryDependencies += "org.postgresql" % "postgresql" % "42.3.6"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"



// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
