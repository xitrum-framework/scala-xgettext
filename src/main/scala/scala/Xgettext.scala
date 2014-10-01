package scala

import java.io.{BufferedWriter, File, FileWriter}
import scala.collection.mutable.{HashMap => MHashMap, MultiMap, Set => MSet}

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

// http://www.scala-lang.org/node/140
class Xgettext(val global: Global) extends Plugin {
  import global._

  type i18nKey = (
    Option[String],  // msgctxt
    String,          // msgid
    Option[String]   // msgid_plural
  )

  type i18nValue = (
    String,          // source
    Int              // line
  )

  type i18nValues = MSet[i18nValue]

  val name        = "xgettext"
  val description = "This Scala compiler plugin extracts and creates gettext.pot file"
  val components  = List[PluginComponent](MapComponent, ReduceComponent)

  val OUTPUT_FILE     = "i18n.pot"
  val HEADER          = """msgid ""
msgstr ""
"Project-Id-Version: \n"
"POT-Creation-Date: \n"
"PO-Revision-Date: \n"
"Last-Translator: Your Name <email@example.com>\n"
"Language-Team: \n"
"MIME-Version: 1.0\n"
"Content-Type: text/plain; charset=UTF-8\n"
"Content-Transfer-Encoding: 8bit\n"

"""

  // -P:xgettext:<i18n trait or class>
  var i18nClassName: Option[String] = None

  val outputFile            = new File(OUTPUT_FILE)
  val emptyOutputFileExists = outputFile.exists && outputFile.isFile && outputFile.length == 0

  val msgToLines = new MHashMap[i18nKey, i18nValues] with MultiMap[i18nKey, i18nValue]

  // Avoid running ReduceComponent multiple times
  var reduced = false

  override def processOptions(options: List[String], error: String => Unit) {
    i18nClassName = Some(options.head)
  }

  private object MapComponent extends PluginComponent {
    val global: Xgettext.this.global.type = Xgettext.this.global

    val runsAfter = List("refchecks")

    val phaseName = "xgettext-map"

    def newPhase(_prev: Phase) = new MapPhase(_prev)

    class MapPhase(prev: Phase) extends StdPhase(prev) {
      override def name = phaseName

      def apply(unit: CompilationUnit) {
        val shouldExtract = i18nClassName.isDefined && emptyOutputFileExists
        if (shouldExtract) {
          // Scala 2.10:
          //val i18nType = rootMirror.getClassByName(stringToTypeName(i18nClassName.get)).tpe

          // Scala 2.11:
          val i18nType = rootMirror.getClassByName(TypeName(i18nClassName.get)).tpe

          for (tree @ Apply(Select(x1, x2), list) <- unit.body) {
            if (x1.tpe <:< i18nType) {
              val methodName = x2.toString
              val pos        = tree.pos  // scala.tools.nsc.util.OffsetPosition
              val line       = (relPath(pos.source.path), pos.line)

              if (methodName == "t") {
                val msgid = list(0).toString
                msgToLines.addBinding((None, msgid, None), line)
              } else if (methodName == "tc") {
                val msgctxt = list(0).toString
                val msgid   = list(1).toString
                msgToLines.addBinding((Some(msgctxt), msgid, None), line)
              } else if (methodName == "tn") {
                val msgid       = list(0).toString
                val msgidPlural = list(1).toString
                msgToLines.addBinding((None, msgid, Some(msgidPlural)), line)
              } else if (methodName == "tcn") {
                val msgctxt     = list(0).toString
                val msgid       = list(1).toString
                val msgidPlural = list(2).toString
                msgToLines.addBinding((Some(msgctxt), msgid, Some(msgidPlural)), line)
              }
            }
          }
        }
      }

      private def relPath(absPath: String) = {
        val curDir   = System.getProperty("user.dir")
        val relPath  = absPath.substring(curDir.length)
        val unixPath = relPath.replace("\\", "/")  // Windows uses '\' to separate
        "../../../.." + unixPath  // po files should be put in src/main/resources/i18n directory
      }
    }
  }

  private object ReduceComponent extends PluginComponent {
    val global: Xgettext.this.global.type = Xgettext.this.global

    val runsAfter = List("jvm")

    val phaseName = "xgettext-reduce"

    def newPhase(_prev: Phase) = new ReducePhase(_prev)

    class ReducePhase(prev: Phase) extends StdPhase(prev) {
      override def name = phaseName

      def apply(unit: CompilationUnit) {
        val shouldExtract = i18nClassName.isDefined && emptyOutputFileExists
        if (shouldExtract && !reduced) {
          val builder = new StringBuilder(HEADER)

          // Sort by key (msgctxto, msgid, msgidPluralo)
          // so that it's easier too see diffs between versions of the .pot/.po file
          val sorted = msgToLines.toSeq.sortBy { case (k, v) => k }

          for (((msgctxto, msgid, msgidPluralo), lines) <- sorted) {
            for ((srcPath, lineNo) <- lines) {
              builder.append("#: " + srcPath + ":" + lineNo + "\n")
            }

            if (msgctxto.isDefined) builder.append("msgctxt " + msgctxto.get + "\n")
            builder.append("msgid " + msgid + "\n")
            if (msgidPluralo.isDefined) builder.append("msgid_plural " + msgidPluralo.get + "\n")
            builder.append("msgstr \"\"" + "\n\n")
          }

          val out = new BufferedWriter(new FileWriter(outputFile))
          out.write(builder.toString)
          out.close()
          println(OUTPUT_FILE + " created")

          reduced = true
        }
      }
    }
  }
}
