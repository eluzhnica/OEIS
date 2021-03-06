package library

import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import java.io.File
import java.net.URL
import java.util.Calendar

import parser._
import processor.TextParserIns
import relations.{GeneratingFunctionSearch}

import scala.io.Source
import scala.util.Random
import scala.xml.Elem
import parser.Expression.format

object Library {
  //store everything, check before crawling

  private def getURL(entryID : String) : URL = new URL("""http://oeis.org/search?q=id:"""+entryID+"""&fmt=text""")

  //will just give id of number-th OEIS entry
  def createID( number : String) : String = "A"+"000000".substring(0,6-number.length) + number

  def crawlDocuments(from : Int, to :Int) = {
    if(from < 1){
      throw new Error("There is no entry "+from+" in OEIS!")
    }

    from to to foreach(i =>{
      val theory = createID(i.toString)
      val file = Source.fromURL(getURL(theory))

      printToFile(new File("resources/"+theory)){
        p => file.getLines().foreach(p.println)
      }

      if(i % 10 == 0){
        println("Fetching entry "+ theory)
      }

      file.close()
    })
  }

  def crawlXML(from : Int, to : Int)= {
    if(from < 1){
      throw new Error("There is no entry "+from+" in OEIS!")
    }

    from to to foreach(i =>{
      val theory = createID(i.toString)
      val file = Source.fromURL(getURL(theory))

      val xml = DocumentParser.fromReaderToXML(file)

      if(i % 10 == 0){
        println("Fetching entry "+ theory)
      }

      file.close()
      writeXML(xml, theory)
    })
  }

  def crawlXMLLocal(from : Int, to : Int)= {
    if(from < 1){
      throw new Error("There is no entry "+from+" in OEIS!")
    }

    from to to foreach(i =>{
      val theory = createID(i.toString)
      val fileLoc = s"all/$theory.txt"
      val ioFile = new java.io.File(fileLoc)
      if(ioFile.exists) {
        val file = Source.fromFile(ioFile)
        val xml = DocumentParser.fromReaderToXML(file)

        if (i % 1000 == 0) {
          println("Fetching entry " + theory)
        }

        file.close()
        writeXML(xml, theory)
      }else{
        println("File doesn't exists: " + theory)
      }
    })
  }

  def parseLocalTheory(from : Int, to : Int)= {
    if(from < 1){
      throw new Error("There is no entry "+from+" in OEIS!")
    }
    var count = 0
    from to to foreach(i =>{
      val theoryId = createID(i.toString)
      val fileLoc = s"all/$theoryId.txt"
      val ioFile = new java.io.File(fileLoc)
      if(ioFile.exists) {
        val file = Source.fromFile(ioFile)
        val theory = DocumentParser.parseLinesTheory(file.getLines().toList)

        if (i % 1000 == 0) {
          println("Fetching entry " + theory)
        }

        file.close()

        if (theory.formulas.nonEmpty) {
          val formulas = theory.formulas
          val generatingFunctions = formulas.flatMap(DocumentParser.getGeneratingFunction)
//          theory.generatingFunctions = generatingFunctions.map(GeneratingFunctionSearch.getGeneratingFunction)
          theory.generatingFunctions = generatingFunctions.map(_.body)

          if (theory.generatingFunctions.nonEmpty) {
            count += theory.generatingFunctions.length
          }
          if (theory.generatingFunctions.nonEmpty) {
            try {
              TheoryRepDao.save(theory)
            } catch {
              case e => println(" Error when saving to DAO" + theory, e)
            }
          }
        }
      } else {
        println("File doesn't exists: " + theoryId)
      }
    })

    println("TOTAL " + count)
  }

  def getXML(entry : Int) : Elem = {
    val id = createID(entry.toString)
//    if(storage.get(id).isEmpty){
      DocumentParser.fromReaderToXML(Source.fromURL(getURL(id)))
//    }else{
//      storage.get(id).get.toNode
//    }
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val print = new java.io.PrintWriter(f)
    try {
      op(print)
    } finally {
      print.close()
    }
  }

  def writeXML(xml : Elem, theory : String) = {
//    XML.save("xml_out/" + theory +".omdoc", xml, "UTF-8", true, null)
  }

  def writeFormula(formulas : List[String], theory : String) : Unit = {
    printToFile(new File("xml_out/"+theory)) { p =>
      formulas.foreach(p.println)
    }
  }

  def main(args : Array[String]) = {
//    crawlXMLLocal(1, 3000)
    val start = Calendar.getInstance().getTime()
    val max = 270000
    val scriptPath = "logs/copyScript"
    val rndm = new Random()

    parseLocalTheory(1, max)

//    crawlXMLLocal(1, max)
    println("Succeded " + TextParserIns.succeded)
    println("Calls " +TextParserIns.calls)
    println("Exceptions " + TextParserIns.exceptions)
    println(start, Calendar.getInstance().getTime())
  }
}
