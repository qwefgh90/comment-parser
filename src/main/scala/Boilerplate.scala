package io.github.qwefgh90.commentparser

import java.io._
import java.net._
import java.nio.charset.Charset
import org.mozilla.universalchardet.UniversalDetector
import scala.collection.mutable._
import java.security.InvalidParameterException
import io.github.qwefgh90.jsearch.JSearch
import java.nio.file.Files

object Boilerplate {
  
  val fileScheme = "file"
  val httpsScheme = "https"
  val httpScheme = "http"
  
  /**
   * @param input Integer value to be converted to Byte
   */
  implicit def intToByte(input: Int): Byte = {
    input.toByte
  }
  
  /**
   * @param byteArray Byte array to be properly decoded and converted to String  
   */
  implicit def byteArrayToString(byteArray: Array[Byte]): String = {
    val detector = new UniversalDetector(null);
  	detector.handleData(byteArray, 0, byteArray.length)
  	detector.dataEnd()
  	val detectedCharset = detector.getDetectedCharset();
  	new String(byteArray, Charset.forName(if(detectedCharset == null) "UTF-8" else detectedCharset));
  }

  /**
   * @param stream stream which bytes connect with
   */
  implicit def streamToArray(stream: InputStreamReader): Array[Char] = {
    val buf = new ArrayBuffer[Char](50000) //50KB
    readStream(stream)(buf+=_)
    buf.toArray
  }

  implicit def streamToArray(stream: InputStream): Array[Byte] = {
    val buf = new ArrayBuffer[Byte](50000) //50KB
    readStream(stream)(buf+=_)
    buf.toArray
  }

  /** Automatically close the stream after run op().
    * 
    * @param inputStream a inputstream to close
    * @param op a operation to execute
    * @return A return value of op()
    */
  def autoClose[A](inputStream: InputStream)(op: InputStream => A): A = {
    try{
      op(inputStream)
    }finally{
      if(inputStream!=null)
        inputStream.close()
    }
  }

  def timer(block: => Unit)(handler: (Long, Long) => Unit){
    val before = System.currentTimeMillis
    block
    val after = System.currentTimeMillis
    handler(before, after)
  }

  def bytesToReadableString(bytes: Array[Byte], fileName: String): String = {
    val tempPath = Files.createTempFile("for_extract", "" + fileName)
    Files.write(tempPath, bytes)
    val content = JSearch.extractContentsFromFile(tempPath.toFile)
    Files.delete(tempPath)
    content
  }

  /**
   * Read all bytes from stream and don't close it.
   * 
   * @param stream A stream to read
   */
  def readStream(stream: InputStreamReader)(op: Char => Unit) = {
    var currentByte = stream.read()
    while(currentByte != -1){
      op(currentByte.toChar)
      currentByte = stream.read()
    }
  }

  def readStream(stream: InputStream)(op: Byte => Unit) = {
    var currentByte = stream.read()
    while(currentByte != -1){
      op(currentByte)
      currentByte = stream.read()
    }
  }
  
  /**
   * Process uri as inputStream and close it.
   * 
   * @param uri a uri to be converted to stream
   * @return A return value of process
   */
  def readUri[A](uri: URI)(process: InputStream => A): A = {
    val tempFile = File.createTempFile("will be deleted", "will be deleted");
    
    uri.getScheme() match {
      case `fileScheme` => {
        val file = new File(uri) //take a file part. A file object avoids error of leading slash
        val is = new FileInputStream(file)
        autoClose(is){is =>
          process(is)
        }
      }
      case `httpsScheme` => {
        val url = uri.toURL
        val is = url.openStream()
        autoClose(is){is =>
          process(is)
        }
      }
      case `httpScheme` => {
        val url = uri.toURL
        val is = url.openStream()
        autoClose(is){is =>
          process(is)
        }
      }
      case _ => {
        //can't handle it
        throw new InvalidParameterException(s"There isn't a handler for ${uri.toString()}")
      }
    }
  }
}
