name := """multiplayer-framework"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.12"

libraryDependencies ++= Seq(
  jdbc,
  guice,
  ws
)

libraryDependencies += scalaVersion("org.scala-lang" % "scala-compiler" % _ ).value
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"
)
