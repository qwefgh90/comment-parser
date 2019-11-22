package io.github.qwefgh90.commentparser

import java.net._
import java.io._
import java.nio.file._
import Types._
import Parser._

import scala.collection.mutable._

import Boilerplate._

import org.apache.tika.mime._
import io.github.qwefgh90.jsearch._
import com.typesafe.scalalogging._
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/** Object to extract comments. */
private[commentparser] object Extractor {
  private val logger = Logger(Extractor.getClass)
  private val dummy = File.createTempFile("will be deleted", "will be deleted");
  dummy.deleteOnExit()
  
  case class ExtractResult(startOffset: Int, comment: String, uri: URI)
  
  /** Extract a list of comments from uri.
    * 
    * @param uri a uri to create stream from
    * @param fileName a file name to detect a media type
    * @param charset a character encoding of this file
    * @return a list of comments
    */
  def extractComments(uri: URI, fileName: String, charset: Charset = StandardCharsets.UTF_8): Option[List[ExtractResult]] = {
	  val tempFile = File.createTempFile("will be deleted", "will be deleted");
    try {
  	  readUri(uri){is =>
    	  val result = extractComments(is, fileName, JSearch.getContentType(tempFile, fileName), charset)
    	  result.map{list => {
    		  for(comment <- list; str = new String(comment.charArray))
    			  yield ExtractResult(comment.startOffset, str, uri)
    	  }
  	      }
	  }.asInstanceOf[Option[List[ExtractResult]]]
    }finally {
      tempFile.delete();
    }
  }

  def extractCommentsByStream(is: InputStream, fileName: String, charset: Charset = StandardCharsets.UTF_8): Option[List[ExtractResult]] = {
    val tempFile = File.createTempFile("will be deleted", "will be deleted");
    try {
      val result = extractComments(is, fileName, JSearch.getContentType(tempFile, fileName), charset)
      result.map { list => {
        for (comment <- list; str = new String(comment.charArray))
          yield ExtractResult(comment.startOffset, str, tempFile.toURI)
      }
      }.asInstanceOf[Option[List[ExtractResult]]]
    }finally {
      tempFile.delete();

    }
  }

  /** Extract a list of byte arrays from a stream.
    * 
    * @param stream a byte stream to extract a text
    * @param mediaType a media type that decide a parser
    * @return a list of byte arrays
    */
  private def extractComments(stream: InputStream, fileName: String, mediaType: MediaType, charset: Charset): Option[List[CommentResult]] = {
    val streamReader = new InputStreamReader(stream, charset)
    mediaType match {
      case JAVA_TYPE => { 
        parseJavaType(streamReader)
      }
      case PY_TYPE => { 
        parsePyType(streamReader)
      }
      case C_TYPE => {
        parseCType(streamReader)
      }
      case C_HEADER_TYPE => {
        parseCType(streamReader)
      }
      case CPP_TYPE => {
        parseCType(streamReader)
      }
      case CPP_HEADER_TYPE => {
        parseCType(streamReader)
      }
      case SCALA_TYPE => {
        parseCType(streamReader)
      }
      case RUBY_TYPE => {
        parseRubyType(streamReader)
      }
      case GO_TYPE => {
        parseGoType(streamReader)
      }
      case JS_TYPE => {
        parseJsType(streamReader)
      }
      case HTML_TYPE => {
        parseHtmlType(streamReader)
      }
      case XML_TYPE => {
        parseHtmlType(streamReader)
      }
      case BAT_TYPE => {
        parseBatType(streamReader)
      }
      case SH_TYPE => {
        parseShType(streamReader)
      }
      case _ => {
        None
      }
    }
  }
}
