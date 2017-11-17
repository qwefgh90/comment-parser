package io.github.qwefgh90.commentparser

import org.scalatest._
import org.scalactic._
import Matchers._
import scala.collection.mutable.ArrayBuffer

import java.net._
import java.io.File

import Parser._
import Extractor._
import Boilerplate._

import org.mozilla.universalchardet.UniversalDetector
import io.github.qwefgh90.jsearch._

import com.typesafe.scalalogging._
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class ExtractorSpec extends FlatSpec with Matchers {
  val logger = Logger(classOf[ExtractorSpec])

  val ignoreSpecialChars =
    new Uniformity[String] {
      def normalized(s: String) = s.replaceAll("[\t\n\r ]", "").toLowerCase
      def normalizedCanHandle(o: Any) = o.isInstanceOf[String]
      def normalizedOrSame(o: Any): Any =
        o match {
          case str: String => normalized(str)
          case _ => o
        }
    }
  
  "Results of extracting java.java" should "be equal to 5th, 6th comment" in {
    val javaUri = getClass.getResource("/java.java").toURI
    val javaList = extractComments(javaUri, "java.java").get
    (javaList(5).comment shouldEqual """*
	 * factory class of <b>TikaMimeXmlObject</b>
	 * 
	 * @author choechangwon
	 *
	 """) (after being ignoreSpecialChars)
    (javaList(6).comment should equal (""" load from property file""")) (after being ignoreSpecialChars)
    
    javaList.foreach{e => 
      readUri(javaUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )
    }
  }

  "Results of extracting py.py" should "be equal to 5th, 6th comment" in {
    val pyUri = getClass.getResource("/py.py").toURI
    val pyList = extractComments(pyUri, "py.py").get
    (pyList(5).comment should startWith ("""
Requests HTTP library
~~~~~~~~~~~~~~~~~~~~~

Requests is an HTTP library, written in Python, for human beings. Basic GET
usage:"""))

    logger.info(pyList(6).toString)

    (pyList(6).comment should equal (""" Attempt to enable urllib3's SNI support, if possible""")) (after being ignoreSpecialChars)

    pyList.foreach{e => 
      readUri(pyUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )
    }
  }

  "Results of extracting c.c" should "be equal to 14th, 15th comment" in {
    val cUri = getClass.getResource("/c.c").toURI
    val cList = extractComments(cUri, "c.c").get
    (cList(13).comment shouldEqual """ Rotate the list removing the tail node and inserting it to the head. """) (after being ignoreSpecialChars)
    (cList(16).comment should equal (""" Test a EOF Comment""")) (after being ignoreSpecialChars)
    
    cList.foreach{e => 
      readUri(cUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )
    }
  }
  
  "Results of extracting h.h" should "be equal to 2th, 3th comment" in {
    val hUri = getClass.getResource("/h.h").toURI
    val hList = extractComments(hUri, "h.h").get
    (hList(1).comment shouldEqual """This comment is for test """) (after being ignoreSpecialChars)
    (hList(2).comment should equal (""" Node, List, and Iterator are the only data structures used currently.""")) (after being ignoreSpecialChars)

    hList.foreach{e => 
      readUri(hUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )
    }
  }

  "Results of extracting scala.scala" should "be equal to 1th, 3th comment" in {
    val scalaUri = getClass.getResource("/scala.scala").toURI
    val scalaList = extractComments(scalaUri, "scala.scala").get
    (scalaList(0).comment shouldEqual """* * Copyright (C) 2009-2017 Lightbend Inc. <http://www.lightbend.com> """) (after being ignoreSpecialChars)
    (scalaList(12).comment should equal ("""look at you""")) (after being ignoreSpecialChars)

    scalaList.foreach{e => 
      readUri(scalaUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )
    }
  }


  "Results of extracting rb.rb" should "be equal to 1th, 2th comment" in {
    val rbUri = getClass.getResource("/rb.rb").toURI
    val rbList = extractComments(rbUri, "rb.rb").get
    (rbList(0).comment shouldEqual """ Returns the version of the currently loaded Rails as a <tt>Gem::Version</tt> """) (after being ignoreSpecialChars)
    (rbList(1).comment should equal ("""can you see it? mutiline comment""")) (after being ignoreSpecialChars)

    rbList.foreach{e => 
      readUri(rbUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )

    }
  }

  "Results of extracting go.go" should "be equal to 5th, 6th comment" in {
    val goUri = getClass.getResource("/go.go").toURI
    val goList = extractComments(goUri, "go.go").get
    (goList(4).comment shouldEqual """ e.g. it matches what we generate with Sign() """) (after being ignoreSpecialChars)
    (goList(5).comment should equal (""" It's a traditional comment.It's a traditional comment.""")) (after being ignoreSpecialChars)

    goList.foreach{e => 
      readUri(goUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )

    }
  }

  "Results of extracting js.js" should "be equal to 1th, 16th comment" in {
    val jsUri = getClass.getResource("/js.js").toURI
    val jsList = extractComments(jsUri, "js.js").get
    (jsList(0).comment shouldEqual """eslint-disable no-unused-vars""") (after being ignoreSpecialChars)
    (jsList(15).comment should equal ("""  build.js inserts compiled jQuery here""")) (after being ignoreSpecialChars)

    jsList.foreach{e => 
      readUri(jsUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )
    
    }
  }

  "Results of extracting html.html" should "be equal to 1th, 2th comment" in {
    val htmlUri = getClass.getResource("/html.html").toURI
    val htmlList = extractComments(htmlUri, "html.html").get
    (htmlList(0).comment shouldEqual """<script type='text/javascript' src='http://getfirebug.com/releases/lite/1.2/firebug-lite-compressed.js'></script>""") (after being ignoreSpecialChars)
    (htmlList(1).comment should equal (""" app title """)) (after being ignoreSpecialChars)


    htmlList.foreach{e => 
      readUri(htmlUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )
    }
  }

  "Results of extracting bat.bat" should "be equal to 1th, 18th comment" in {
    val batUri = getClass.getResource("/bat.bat").toURI
    val batList = extractComments(batUri, "bat.bat").get
    (batList(0).comment shouldEqual """Licensed to the Apache Software Foundation (ASF) under one or more""") (after being ignoreSpecialChars)
    (batList(17).comment should equal ("""Get remaining unshifted command line arguments and save them in the""")) (after being ignoreSpecialChars)

    batList.foreach{e => 
      readUri(batUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))

        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )
    }
  }

  "Results of extracting sh.sh" should "be equal to 1th, 22th comment" in {
    val shUri = getClass.getResource("/sh.sh").toURI
    val shList = extractComments(shUri, "sh.sh").get
    (shList(0).comment shouldEqual """!/bin/sh""") (after being ignoreSpecialChars)
    (shList(21).comment should equal (""" -x will Only work on the os400 if the files are:""")) (after being ignoreSpecialChars)


    shList.foreach{e => 
      readUri(shUri)(inputStream => {
        val body = new String(streamToArray(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        logger.info("1)"+e.startOffset + ", 2)"+e.comment.size)

        assert(body.substring(e.startOffset, e.startOffset
          + e.comment.size) == e.comment)
      }
      )
    }
    }



  "Parser" should "return a count of comments we excect" in {
    val javaStream = getClass.getResourceAsStream("/java.java")
    val javaList = parseJavaType(new InputStreamReader(javaStream, StandardCharsets.UTF_8))
    javaList should not be None
    javaList.get.size shouldEqual 11
    javaStream.close()

    val pyStream = getClass.getResourceAsStream("/py.py")
    val pyList = parsePyType(new InputStreamReader(pyStream, StandardCharsets.UTF_8))
    pyList should not be None
    pyList.get.size shouldEqual 11
    pyStream.close()

    val cStream = getClass.getResourceAsStream("/c.c")
    val cList = parseCType(new InputStreamReader(cStream, StandardCharsets.UTF_8))
    cList should not be None
    cList.get.size shouldEqual 17
    cStream.close()
    
    val hStream = getClass.getResourceAsStream("/h.h")
    val hList = parseCType(new InputStreamReader(hStream, StandardCharsets.UTF_8))
    hList should not be None
    hList.get.size shouldEqual 7
    hStream.close()

    val scalaStream = getClass.getResourceAsStream("/scala.scala")
    val scalaList = parseScalaType(new InputStreamReader(scalaStream, StandardCharsets.UTF_8))
    scalaList should not be None
    scalaList.get.size shouldEqual 13
    scalaStream.close()

    val rubyStream = getClass.getResourceAsStream("/rb.rb")
    val rubyList = parseRubyType(new InputStreamReader(rubyStream, StandardCharsets.UTF_8))
    rubyList should not be None
    rubyList.get.size shouldEqual 2
    rubyStream.close()

    val goStream = getClass.getResourceAsStream("/go.go")
    val goList = parseGoType(new InputStreamReader(goStream, StandardCharsets.UTF_8))
    goList should not be None
    goList.get.size shouldEqual 6
    goStream.close()

    val jsStream = getClass.getResourceAsStream("/js.js")
    val jsList = parseJsType(new InputStreamReader(jsStream, StandardCharsets.UTF_8))
    jsList should not be None
    jsList.get.size shouldEqual 16
    jsStream.close()

    val htmlStream = getClass.getResourceAsStream("/html.html")
    val htmlList = parseHtmlType(new InputStreamReader(htmlStream, StandardCharsets.UTF_8))
    htmlList should not be None
    htmlList.get.size shouldEqual 9
    htmlStream.close()

    val batStream = getClass.getResourceAsStream("/bat.bat")
    val batList = parseBatType(new InputStreamReader(batStream, StandardCharsets.UTF_8))
    batList should not be None
    batList.get.size shouldEqual 18
    batStream.close()

    val shStream = getClass.getResourceAsStream("/sh.sh")
    val shList = parseShType(new InputStreamReader(shStream, StandardCharsets.UTF_8))
    shList should not be None
    shList.get.size shouldEqual 25
    shStream.close()

  }

  "URI Scheme" should "http or file" in {
    var uri = new URI("file://c:/한글")
    assert(uri.getScheme equalsIgnoreCase "file")
    (uri.getSchemeSpecificPart shouldEqual "//c:/한글") (after being ignoreSpecialChars)
    uri = new URI("http://google.com")
    assert(uri.getScheme equalsIgnoreCase "http")
    (uri.getSchemeSpecificPart shouldEqual "//google.com") (after being ignoreSpecialChars)
  }

  "UniversalDetector" should "detect currect encoding" in {
    val uri = new URI("https://raw.githubusercontent.com/qwefgh90/test/master/euc_kr.txt")
    val is = uri.toURL.openStream()
    val buf = new ArrayBuffer[Byte]()

    var byte = is.read()
    while(byte != -1){
      buf += byte
      byte = is.read()
    }
    val immuBuf = buf.toArray
    val detector = new UniversalDetector(null);
	detector.handleData(immuBuf, 0, immuBuf.length);
	detector.dataEnd();
	val detectedCharset = detector.getDetectedCharset();
    assert(detectedCharset equals "EUC-KR")
    is.close()
  }

  "getContentType() " should "print types" in {
    val tempFile = File.createTempFile("will be deleted", "will be deleted");
    tempFile.deleteOnExit()
    //Languages work on System
    logger.info(JSearch.getContentType(tempFile, "hello.java").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.py").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.c").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.cpp").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.h").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.hpp").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.scala").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.rb").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.go").toString);

    //Languages work on Browser
    logger.info(JSearch.getContentType(tempFile, "hello.js").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.html").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.css").toString);

    //Languages work on Shell
    logger.info(JSearch.getContentType(tempFile, "hello.bat").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.sh").toString);

    //Text format
    logger.info(JSearch.getContentType(tempFile, "hello.xml").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.properties").toString);
    logger.info(JSearch.getContentType(tempFile, "hello.txt").toString);

    //ETC format
    logger.info(JSearch.getContentType(tempFile, "hello.md").toString);
    logger.info(JSearch.getContentType(tempFile, "hello").toString);

  }
}
