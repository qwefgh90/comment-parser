package io.github.qwefgh90.commentparser

import org.scalatest._
import org.scalactic._
import Matchers._
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._

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

class CommentParserSpec extends FlatSpec with Matchers {
  val logger = Logger(classOf[CommentParserSpec])

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
    val javaList = CommentParser.extractComments(javaUri, "java.java").get.asScala
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
}
