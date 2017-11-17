package io.github.qwefgh90.commentparser

import java.io._
import scala.collection.mutable._
import scala.util.matching.Regex

import Boilerplate._
import com.typesafe.scalalogging._

/** Object contains various comment parsers. */
private[commentparser] object Parser{
  val logger = Logger(Parser.getClass)

  implicit def bufferToWrapper(array: ArrayBuffer[Char]) = {
    new WrapperArrayByffer(array)
  }

  /**
    * @constructor create new wrapper for finding sequence
    * @param array an array to be wrapped
    */
  class WrapperArrayByffer(array: ArrayBuffer[Char]){
    def findFromLast(ch: Char, allowFrontBackslash: Boolean):Boolean = {
      findFromLast(ch.toString, allowFrontBackslash)
    }
    /** Find string at end of array
      * 
      * @param str string to find
      * @return if string exists in array return true, if not false.
      */
    def findFromLast(str: String, allowFrontBackslash: Boolean): Boolean = {
      val bytesToFind = str.getBytes()
      val rightPart = array.takeRight(bytesToFind.length)
      val existBackslash = if(array.length > bytesToFind.length) array(array.length - bytesToFind.length - 1) == '\\' else false
      if(rightPart.lengthCompare(bytesToFind.length) != 0)
        false
      else if(!allowFrontBackslash && existBackslash)
        false
      else
        rightPart.sameElements(bytesToFind)
    }
    def findFromFirst(str: String): Boolean = {
      val bytesToFind = str.getBytes()
      val leftPart = array.take(bytesToFind.length)
      if(leftPart.lengthCompare(bytesToFind.length) != 0)
        false
      else
        leftPart.sameElements(bytesToFind)
    }
    def findFromFirstIgnoreCase(str: String): Boolean = {
      val bytesToFind = str.toLowerCase.getBytes()
      val leftPart = array.take(bytesToFind.length).map(b => b.toChar.toLower.toByte)
      if(leftPart.lengthCompare(bytesToFind.length) != 0)
        false
      else
        leftPart.sameElements(bytesToFind)
    }
  }
  
  case class CommentResult(startOffset: Int, charArray: Array[Char])
  
  /** Parse comments from a byte stream of .java
    * 
    * @param stream a stream to parse
    * @return a list of byte arrays
    */
  def parseJavaType(stream: InputStreamReader): Option[List[CommentResult]] = {
    //https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.7
    val listBuffer = new ListBuffer[CommentResult]()
    val lexicalBuf = new ArrayBuffer[Char]()
  
    sealed trait JavaState
    case object Nop extends JavaState
    case object StartTranditionalComment extends JavaState
    case object StartEolComment extends JavaState
    case object StartString extends JavaState

    var currentOffset: Int = 0
    var state: JavaState = Nop
    var currentByte = stream.read() //read a first byte
    while(currentByte != -1){
      currentOffset += 1
      lexicalBuf += currentByte.toChar
      state match {
        case Nop => {
          if(lexicalBuf.findFromLast('"', true)){
            state = StartString
          }
          else if(lexicalBuf.findFromLast("/*", true)){
            lexicalBuf.clear()
            state = StartTranditionalComment
          }
          else if(lexicalBuf.findFromLast("//", true)){
            lexicalBuf.clear()
            state = StartEolComment
          }
        }
        case StartTranditionalComment =>{
          if(lexicalBuf.findFromLast("*/", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartEolComment => {
          if(lexicalBuf.findFromLast("\r\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }else if(lexicalBuf.findFromLast("\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(1)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartString => {
          if(lexicalBuf.findFromLast('"', false)){
            state = Nop
          }
        }
      }
      currentByte = stream.read() //read a next byte
    }
    Option(listBuffer.toList)
  }
  
  def parsePyType(stream: InputStreamReader): Option[List[CommentResult]] = {
    val listBuffer = new ListBuffer[CommentResult]()
    val lexicalBuf = new ArrayBuffer[Char]()
    
    sealed trait PyState
    case object Nop extends PyState
    case object StartSharp extends PyState
    case object StartTriple extends PyState
    case object StartStringSingle extends PyState
    case object StartStringDouble extends PyState
    case object StartStringTriple extends PyState

    var currentOffset: Int = 0
    var state: PyState = Nop
    readStream(stream){ currentByte: Char => {
      //https://docs.python.org/3/reference/lexical_analysis.html#literals
      //https://docs.python.org/2/reference/lexical_analysis.html#string-literals

      currentOffset += 1
      lexicalBuf += currentByte.toChar
      state match {
        case Nop => {
          if(lexicalBuf.findFromLast("'", true)){
            state = StartStringSingle
          }else if(lexicalBuf.findFromLast('"', true)){
            state = StartStringDouble
          }else if(lexicalBuf.findFromLast('#', true)){
            lexicalBuf.clear()
            state = StartSharp
          }
        }
        case StartStringSingle => {
          if(lexicalBuf.findFromLast("'''", false)){
            lexicalBuf.clear()
            state = StartStringTriple
          }
          else if(lexicalBuf.dropRight(1).findFromLast("''", false)){
            state = Nop
          }
          else if(lexicalBuf.findFromLast("''", false)){
            //pass
          }
          else if(lexicalBuf.findFromLast("'", false)){
            state = Nop
          }
        }
        case StartStringDouble => {
          if(lexicalBuf.findFromLast("\"\"\"", false)){
            lexicalBuf.clear()
            state = StartTriple
          }
          else if(lexicalBuf.dropRight(1).findFromLast("\"\"", false)){
            state = Nop
          }
          else if(lexicalBuf.findFromLast("\"\"", false)){
            //pass
          }
          else if(lexicalBuf.findFromLast("\"", false)){
            state = Nop
          }
        }
        case StartStringTriple => {
          if(lexicalBuf.findFromLast("'''", false)){
            state = Nop
          }
        }
        case StartTriple => {
          if(lexicalBuf.findFromLast("\"\"\"", false)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(3)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartSharp => {
          if(lexicalBuf.findFromLast("\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(1)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }else if(lexicalBuf.findFromLast("\r\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
      }
    }
    }
    Option(listBuffer.toList)
  }
  def parseCType(stream: InputStreamReader): Option[List[CommentResult]] = 
  {
    //http://www.open-std.org/jtc1/sc22/wg14/www/docs/n1124.pdf
    //http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2014/n4296.pdf
    val listBuffer = new ListBuffer[CommentResult]()
    val lexicalBuf = new ArrayBuffer[Char]()
    
    sealed trait CState
    case object Nop extends CState
    case object StartTranditionalComment extends CState
    case object StartEolComment extends CState
    case object StartString extends CState

    var currentOffset: Int = 0
    var state: CState = Nop
    var currentByte = stream.read() //read a first byte
    while(currentByte != -1){
      currentOffset += 1
      lexicalBuf += currentByte.toChar
      state match {
        case Nop => {
          if(lexicalBuf.findFromLast('"', true)){
            state = StartString
          }
          else if(lexicalBuf.findFromLast("/*", true)){
            lexicalBuf.clear()
            state = StartTranditionalComment
          }
          else if(lexicalBuf.findFromLast("//", true)){
            lexicalBuf.clear()
            state = StartEolComment
          }
        }
        case StartTranditionalComment =>{
          if(lexicalBuf.findFromLast("*/", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartEolComment => {
          if(lexicalBuf.findFromLast("\r\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }else if(lexicalBuf.findFromLast("\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(1)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartString => {
          if(lexicalBuf.findFromLast('"', false)){
            state = Nop
          }
        }
      }
      currentByte = stream.read() //read a next byte
    }
    Option(listBuffer.toList)
  }

def parseScalaType(stream: InputStreamReader): Option[List[CommentResult]] = {
    val listBuffer = new ListBuffer[CommentResult]()
    val lexicalBuf = new ArrayBuffer[Char]()
    /*val NOP = -1
    val START_TRANDITIONAL_COMMENT = 1
    val START_EOL_COMMENT = 2
    val START_STRING = 3
    val START_MULTI_STRING = 4
    */
    sealed trait ScalaState
    case object Nop extends ScalaState
    case object StartTranditionalComment extends ScalaState
    case object StartEolComment extends ScalaState
    case object StartString extends ScalaState
    case object StartMultiString extends ScalaState
    
    var currentOffset: Int = 0
    var state: ScalaState = Nop
    readStream(stream){ currentByte: Char => {
      //https://www.scala-lang.org/files/archive/spec/2.11/01-lexical-syntax.html#string-literals
      currentOffset += 1
      lexicalBuf += currentByte.toChar
      state match {
        case Nop => {
          if(lexicalBuf.findFromLast("\"", false)){
            state = StartString
          }else if(lexicalBuf.findFromLast("/*", false)){
            lexicalBuf.clear()
            state = StartTranditionalComment
          }else if(lexicalBuf.findFromLast("//", false)){
            lexicalBuf.clear()
            state = StartEolComment
          }
        }
        case StartString => {
          if(lexicalBuf.findFromLast("\"\"\"", false)){
            lexicalBuf.clear()
            state = StartMultiString
          }
          else if(lexicalBuf.dropRight(1).findFromLast("\"\"", false)){
            state = Nop
          }
          else if(lexicalBuf.findFromLast("\"\"", false)){
            //pass
          }
          else if(lexicalBuf.findFromLast("\"", false)){
            state = Nop
          }
        }
        case StartMultiString => {
          if(lexicalBuf.findFromLast("\"\"\"", false)){
            state = Nop
          }
        }
        case StartTranditionalComment => {
          if(lexicalBuf.findFromLast("*/", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartEolComment => {
          if(lexicalBuf.findFromLast("\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(1)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }else if(lexicalBuf.findFromLast("\r\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
      }
    }
    }
    Option(listBuffer.toList)
  }
def parseRubyType(stream: InputStreamReader): Option[List[CommentResult]] = {
    val listBuffer = new ListBuffer[CommentResult]()
    val lexicalBuf = new ArrayBuffer[Char]()
    
    sealed trait RubyState
    case object Nop extends RubyState
    case object StartCommentSingle extends RubyState
    case object StartCommentMulti extends RubyState
    case object StartStringSingle extends RubyState
    case object StartStringDouble extends RubyState
    case object StartStringNonExpanded extends RubyState
    case object StartStringExpanded extends RubyState
    
   def IS_IDENTIFIER_CHARS(byte: Byte) = {
     if(('a' <= byte && byte <= 'z') || ('A' <= byte && byte <= 'Z') || byte == '_' || ('0' <= byte && byte <= '9')) true else false
   }
  val BEGINING_DELIMITER_LIST = List('{','(','[','<')
  val ENDING_DELIMITER_LIST = List('}',')',']','>')
  var beginingDelimiter = 0
    var signifierDelimiter = ""
    var currentOffset: Int = 0
    var state: RubyState = Nop
    readStream(stream){ currentByte: Char => {
      //http://www.ipa.go.jp/files/000011432.pdf
      currentOffset += 1
      lexicalBuf += currentByte.toChar
      state match {
        case Nop => {
          if(lexicalBuf.findFromLast("'", false)){
            lexicalBuf.clear()
            state = StartStringSingle
          }else if(lexicalBuf.findFromLast("\"", false)){
            lexicalBuf.clear()
            state = StartStringDouble
          }else if(lexicalBuf.findFromLast("#", false)){
            lexicalBuf.clear()
            state = StartCommentSingle
          }else if(lexicalBuf.findFromLast("=begin", false)){
            lexicalBuf.clear()
            state = StartCommentMulti
          }else if(lexicalBuf.dropRight(1).findFromLast("%q", false)){
            if(BEGINING_DELIMITER_LIST.exists(lexicalBuf.findFromLast(_,false))){
              beginingDelimiter = lexicalBuf.last
              lexicalBuf.clear()
              state = StartStringNonExpanded
            }
          }else if(lexicalBuf.dropRight(1).findFromLast("%Q", false)){
            if(BEGINING_DELIMITER_LIST.exists(lexicalBuf.findFromLast(_,false))){
              beginingDelimiter = lexicalBuf.last
              lexicalBuf.clear()
              state = StartStringExpanded
            }
          }
        }
        case StartStringSingle => {
          if(lexicalBuf.findFromLast("'", false)){
            lexicalBuf.clear()
            state = Nop
          }
        }
        case StartStringDouble => {
          if(lexicalBuf.findFromLast("\"", false)){
            lexicalBuf.clear()
            state = Nop
          }
        }
        case StartCommentSingle => {
          if(lexicalBuf.findFromLast("\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(1)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }else if(lexicalBuf.findFromLast("\r\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartCommentMulti => {
          if(lexicalBuf.findFromLast("=end", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(4)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartStringNonExpanded => {
          if(lexicalBuf.findFromLast(ENDING_DELIMITER_LIST(BEGINING_DELIMITER_LIST.indexOf(beginingDelimiter)), false)){
            lexicalBuf.clear()
            state = Nop
          }
        }
        case StartStringExpanded => {
          if(lexicalBuf.findFromLast(ENDING_DELIMITER_LIST(BEGINING_DELIMITER_LIST.indexOf(beginingDelimiter)), false)){
            lexicalBuf.clear()
            state = Nop
          }
        }
      }
    }
    }
    Option(listBuffer.toList)
  }

def parseGoType(stream: InputStreamReader): Option[List[CommentResult]] = 
  {
    //https://golang.org/ref/spec#Comments
    val listBuffer = new ListBuffer[CommentResult]()
    val lexicalBuf = new ArrayBuffer[Char]()
    
    sealed trait GoState
    case object Nop extends GoState
    case object StartTranditionalComment extends GoState
    case object StartEolComment extends GoState
    case object StartString extends GoState
    case object StartRawString extends GoState

    var currentOffset: Int = 0
    var state: GoState = Nop
    var currentByte = stream.read() //read a first byte
    while(currentByte != -1){
      currentOffset += 1
      lexicalBuf += currentByte.toChar
      state match {
        case Nop => {
          if(lexicalBuf.findFromLast('"', true)){
            state = StartString
          }
          else if(lexicalBuf.findFromLast("`", true)){
            lexicalBuf.clear()
            state = StartRawString
          }
          else if(lexicalBuf.findFromLast("/*", true)){
            lexicalBuf.clear()
            state = StartTranditionalComment
          }
          else if(lexicalBuf.findFromLast("//", true)){
            lexicalBuf.clear()
            state = StartEolComment
          }
        }
        case StartTranditionalComment =>{
          if(lexicalBuf.findFromLast("*/", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartEolComment => {
          if(lexicalBuf.findFromLast("\r\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }else if(lexicalBuf.findFromLast("\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(1)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartString => {
          if(lexicalBuf.findFromLast('"', false)){
            state = Nop
          }
        }
        case StartRawString => {
          if(lexicalBuf.findFromLast('`', false)){
            state = Nop
          }
        }
      }
      currentByte = stream.read() //read a next byte
    }
    Option(listBuffer.toList)
  }


  /** Parse comments from a byte stream of .js
    * 
    * @param stream a stream to parse
    * @return a list of byte arrays
    */
  def parseJsType(stream: InputStreamReader): Option[List[CommentResult]] = {
    //https://www.ecma-international.org/publications/files/ECMA-ST/Ecma-262.pdf
    val listBuffer = new ListBuffer[CommentResult]()
    val lexicalBuf = new ArrayBuffer[Char]()
    
    sealed trait JsState
    case object Nop extends JsState
    case object StartTranditionalComment extends JsState
    case object StartEolComment extends JsState
    case object StartDoubleString extends JsState
    case object StartSingleString extends JsState

    var currentOffset: Int = 0
    var state: JsState = Nop
    var currentByte = stream.read() //read a first byte
    while(currentByte != -1){
      currentOffset += 1
      lexicalBuf += currentByte.toChar
      state match {
        case Nop => {
          if(lexicalBuf.findFromLast('"', true)){
            state = StartDoubleString
          }
          else if(lexicalBuf.findFromLast('\'', true)){
            state = StartSingleString
          }
          else if(lexicalBuf.findFromLast("/*", true)){
            lexicalBuf.clear()
            state = StartTranditionalComment
          }
          else if(lexicalBuf.findFromLast("//", true)){
            lexicalBuf.clear()
            state = StartEolComment
          }
        }
        case StartTranditionalComment =>{
          if(lexicalBuf.findFromLast("*/", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartEolComment => {
          if(lexicalBuf.findFromLast("\r\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }else if(lexicalBuf.findFromLast("\n", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(1)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }
        }
        case StartDoubleString => {
          if(lexicalBuf.findFromLast('"', false)){
            state = Nop
          }
        }
        case StartSingleString => {
          if(lexicalBuf.findFromLast('\'', false)){
            state = Nop
          }
        }
      }
      currentByte = stream.read() //read a next byte
    }
    Option(listBuffer.toList)
  }
  /** Parse comments from a byte stream of .html
    * 
    * @param stream a stream to parse
    * @return a list of byte arrays
    */
  def parseHtmlType(stream: InputStreamReader): Option[List[CommentResult]] = {
    //https://www.w3.org/TR/html4/intro/sgmltut.html#h-3.2.4
    val listBuffer = new ListBuffer[CommentResult]()
    val lexicalBuf = new ArrayBuffer[Char]()
    
    sealed trait HtmlState
    case object Nop extends HtmlState
    case object StartComment extends HtmlState
    case object StartCommentDoubleDash extends HtmlState

    var currentOffset: Int = 0
    var state: HtmlState = Nop
    var currentByte = stream.read() //read a first byte
    while(currentByte != -1){
      currentOffset += 1
      lexicalBuf += currentByte.toChar
      state match {
        case Nop => {
          if(lexicalBuf.findFromLast("<!--", true)){
            lexicalBuf.clear()
            state = StartComment
          }
        }
        case StartComment =>{
          if(lexicalBuf.findFromLast("--", true)){
            state = StartCommentDoubleDash
          }
        }
        case StartCommentDoubleDash => {
          if(lexicalBuf.findFromLast(">", true)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(3)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            state = Nop
          }else if(lexicalBuf.findFromLast(" ", true)){
            lexicalBuf.trimEnd(1)
          }else{
            state = StartComment
          }
        }
      }
      currentByte = stream.read() //read a next byte
    }
    Option(listBuffer.toList)
  }

  /** Parse comments from a byte stream of .bat
    * 
    * @param stream a stream to parse
    * @return a list of byte arrays
    */
  def parseBatType(stream: InputStreamReader): Option[List[CommentResult]] = {
    val listBuffer = new ListBuffer[CommentResult]()
    val lexicalBuf = new ArrayBuffer[Char]()
    
    sealed trait BatState
    case object Nop extends BatState
    case object StartComment extends BatState

    var currentOffset: Int = 0
    var state: BatState = Nop
    var currentByte = stream.read() //read a first byte
    while(currentByte != -1){
      currentOffset += 1
      lexicalBuf += currentByte.toChar
      state match {
        case Nop => {
          if(currentByte == '\n')
            lexicalBuf.clear()
          else if(lexicalBuf.findFromFirstIgnoreCase("rem ")){
            lexicalBuf.clear()
            state = StartComment
          }
          else if(currentByte == ' ')
            lexicalBuf.trimEnd(1)
        }
        case StartComment =>{
          if(lexicalBuf.findFromLast("\r\n", false)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            lexicalBuf.clear()
            state = Nop
          }
          else if(lexicalBuf.findFromLast("\n", false)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(1)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            lexicalBuf.clear()
            state = Nop
          }
        }
      }
      currentByte = stream.read() //read a next byte
    }
    Option(listBuffer.toList)
  }



  /** Parse comments from a byte stream of .sh
    * 
    * @param stream a stream to parse
    * @return a list of byte arrays
    */
  def parseShType(stream: InputStreamReader): Option[List[CommentResult]] = {
    //http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html
    val listBuffer = new ListBuffer[CommentResult]()
    val lexicalBuf = new ArrayBuffer[Char]()
    
    sealed trait ShState
    case object Nop extends ShState
    case object StartComment extends ShState
    case object StartStringSingle extends ShState
    case object StartStringDouble extends ShState

    var currentOffset: Int = 0
    var state: ShState = Nop
    var currentByte = stream.read() //read a first byte
    while(currentByte != -1){
      currentOffset += 1
      lexicalBuf += currentByte.toChar
      state match {
        case Nop => {
          if(currentByte == '\''){
            lexicalBuf.clear()
            state = StartStringSingle
          }
          else if(currentByte == '\"'){
            lexicalBuf.clear()
            state = StartStringDouble
          }
          else if(currentByte == '#'){
            lexicalBuf.clear()
            state = StartComment
          }
        }
        case StartStringSingle => {
          if(lexicalBuf.findFromLast('\'', false)){
            lexicalBuf.clear()
            state = Nop
          }
        }
        case StartStringDouble =>{
          if(lexicalBuf.findFromLast('"', false)){
            lexicalBuf.clear()
            state = Nop
          }
        }
        case StartComment => {
          if(lexicalBuf.findFromLast("\r\n", false)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(2)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            lexicalBuf.clear()
            state = Nop
          }
          else if(lexicalBuf.findFromLast('\n', false)){
            val startOffset = currentOffset - lexicalBuf.length
            lexicalBuf.trimEnd(1)
            listBuffer += CommentResult(startOffset, lexicalBuf.toArray)
            lexicalBuf.clear()
            state = Nop
          }
        }
      }
      currentByte = stream.read() //read a next byte
    }
    Option(listBuffer.toList)
  }

//https://www.w3.org/XML/Core/#Publications
}
