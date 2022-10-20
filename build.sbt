import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"
val AkkaVersion = "2.6.20"

lazy val root = (project in file("."))
  .settings(
    name := "legatio-scheduler-telegram-bot",
    libraryDependencies += scalaTest % Test
  )

libraryDependencies += "com.bot4s" %% "telegram-core" % "5.6.1"
libraryDependencies += "biz.enef" %% "slogging-slf4j" % "0.6.2"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.3"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.4.1"
libraryDependencies += "org.postgresql" % "postgresql" % "42.5.0"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1"



// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
