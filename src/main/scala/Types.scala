package io.github.qwefgh90.commentparser

import java.io._
import org.apache.tika.mime._
import io.github.qwefgh90.jsearch._

object Types {
  val dummy = File.createTempFile("will be deleted", "will be deleted");
  dummy.deleteOnExit()
  val JAVA_TYPE = JSearch.getContentType(dummy, "hello.java")
  val PY_TYPE = JSearch.getContentType(dummy, "hello.py")
  val C_TYPE = JSearch.getContentType(dummy, "hello.c")
  val CPP_TYPE = JSearch.getContentType(dummy, "hello.cpp")
  val C_HEADER_TYPE = JSearch.getContentType(dummy, "hello.h")
  val CPP_HEADER_TYPE = JSearch.getContentType(dummy, "hello.hpp")
  val SCALA_TYPE = JSearch.getContentType(dummy, "hello.scala")
  val RUBY_TYPE = JSearch.getContentType(dummy, "hello.rb")
  val GO_TYPE = JSearch.getContentType(dummy, "hello.go")

  //Languages work on Browser
  val JS_TYPE = JSearch.getContentType(dummy, "hello.js")
  val HTML_TYPE = JSearch.getContentType(dummy, "hello.html")
  //val CSS_TYPE = JSearch.getContentType(dummy, "hello.css")

  //Languages work on Shell
  val BAT_TYPE = JSearch.getContentType(dummy, "hello.bat")
  val SH_TYPE = JSearch.getContentType(dummy, "hello.sh")

  //Text format
  val XML_TYPE = JSearch.getContentType(dummy, "hello.xml")
  //val PROP_TPYE = JSearch.getContentType(dummy, "hello.properties")
  val TEXT_TYPE = JSearch.getContentType(dummy, "hello.txt")

  //ETC format
  val MD_TYPE = JSearch.getContentType(dummy, "hello.md")
  val ETC_TYPE = JSearch.getContentType(dummy, "hello")
}
