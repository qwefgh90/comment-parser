import Dependencies._
import sys.process._
import java.nio.file.{Files, Paths}

enablePlugins(JShellPlugin)

lazy val javadoc = taskKey[Unit]("generate javadoc in target in Compile")
lazy val root = (project in file(".")).
  settings(
    javacOptions in (Compile,doc) ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "utf-8"),
    javacOptions in (Compile,compile) ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "utf-8"),
    scalacOptions ++= Seq("-feature", "-encoding", "UTF-8"),
    inThisBuild(List(
      organization := "io.github.qwefgh90",
      scalaVersion := "2.12.4",
      version      := "0.1.0",
      crossScalaVersions := Seq("2.11.11")
    )),
    name := "comment-parser",
    libraryDependencies += "io.github.qwefgh90" % "jsearch" % "0.3.0" exclude("org.slf4j", "slf4j-log4j12") exclude("log4j", "log4j"),
    libraryDependencies += "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3",
    libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.25" % Test,
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    libraryDependencies +=  "org.scalatest" %% "scalatest" % "3.0.3" % Test,
    publishTo := Some(
      if (isSnapshot.value)
        Opts.resolver.sonatypeSnapshots
      else
        Opts.resolver.sonatypeStaging
    ),
    javadoc := { 
      val classPathDeps: Seq[File] = (dependencyClasspath in Compile).value.files
      val classPathStr = classPathDeps.mkString(";") + ";" + (classDirectory in Compile).value
      val apiPathStr = (classDirectory in Compile).value + """\..\api"""
      deleteRecursively(Paths.get(apiPathStr).toFile)
      Process("javadoc" :: "-classpath" :: classPathStr :: "-d" :: (classDirectory in Compile).value + "\\..\\api" :: """src\main\java\CommentParser.java""" :: Nil) !
    },
    doc in Compile := {
      val result = (doc in Compile).value
      javadoc.value
      result
    }
  )

def deleteRecursively(file: File): Unit = {
  if (file.isDirectory)
    file.listFiles.foreach(deleteRecursively)
  if (file.exists)
    file.delete()
}

//References to publish to maven central
//https://github.com/xerial/sbt-sonatype
//https://leonard.io/blog/2017/01/an-in-depth-guide-to-deploying-to-maven-central/
