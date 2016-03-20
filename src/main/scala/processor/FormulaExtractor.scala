package processor

/**
 * Created by enxhi on 4/2/15.
 */

import java.io.Serializable

import parser._

import scala.util.parsing.combinator.{JavaTokenParsers, PackratParsers}
import scala.xml.{Node, Elem}

trait Line extends Expression

case class Sentence(parts : List[Expression]) extends Line{
  override def present: String = parts.mkString("")

  //  override def toString = present
  override def toNode(implicit theory: String): Elem = {
    <CMP>
      {parts.map {
      case a: Line => a.present
      case a: Expression => <OMOBJ>
        {a.toNode}
      </OMOBJ>
    }}
    </CMP>
  }

  def toSubNode(implicit theory: String): List[Serializable] = {
    parts.map {
      case a: Sentence => a.toSubNode
      case a: Line => a.present
      case a: Expression => <OMOBJ>{ a.toNode }</OMOBJ>
    }
  }
}

case class Delim(delim : String) extends Line{
  override def present: String = delim

  //  override def toString = present
  //shouldn't be called
  override def toNode(implicit theory: String): Elem = <text>{delim}</text>
}

case class Word(word : String) extends Line{
  override def present: String = word+" "

  //  override def toString = present

  //shouldn't be called
  override def toNode(implicit theory: String): Elem = <text>{word}</text>
}

case class Name(name : String) extends Line{
  override def present: String = name+" "

  //  override def toString = present
  //shouldn't be called
  override def toNode(implicit theory: String): Elem = <text>{name}</text>
}

case class Date(date : String) extends Line{
  override def present: String = date+" "

  //  override def toString = present
  //shouldn't be called
  override def toNode(implicit theory: String): Elem = <text>{date}</text>
}

case class Email(email : String) extends Line{
  override def present: String = email+" "

  override def toNode(implicit theory: String): Elem = <text>{email}</text>
}

class TextParser extends FormulaParser {

  def parseLine(line : String, theory : String = "") : Option[Line] = {
    if(line.isEmpty){
      return None
    }

    initSet()
    calls += 1
    try {
      val parsed = parseAll(sentence, line)
      parsed.successful match {
        case false =>
          failFile.println("----------------")
          failFile.write(theory + "\t" + parsed +"\n")
          failFile.println("----------------")
          None
        case true =>
          succeded +=1
          successFile.write(theory + "\t" + line+"\n")
           val processed = parsed.get.parts.map({
            case x : Line => x
            case x : Expression => postProcess(x)
          })
          Some(parsed.get)
      }
    }catch{
      case ex : Throwable =>
        println("ex:" + ex)
        println("line: " + line)
        println("theory: "+ theory)
        exceptions+=1
        exceptionFile.write(theory + "\t" + line + "\n")
        None
    }
  }
}

object TextParserIns extends TextParser{

  def main(args : Array[String]): Unit = {
    val test = "1   - 1    2 "
    println("input : "+ test)
    println(parseAll(expression, test))
  }

}