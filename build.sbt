import Dependencies._

lazy val root = (project in file(".")).
  settings(
    javacOptions in (Compile,doc) ++= Seq("-notimestamp", "-linksource"),
    inThisBuild(List(
      organization := "io.github.qwefgh90",
      scalaVersion := "2.12.4",
      version      := "0.1.0"
    )),
    name := "comment-parser",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "io.github.qwefgh90" % "jsearch" % "0.3.0" exclude("org.slf4j", "slf4j-log4j12"),
    libraryDependencies += "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3",
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

  )
