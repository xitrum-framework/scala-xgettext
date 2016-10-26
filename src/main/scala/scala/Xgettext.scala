package scala

import java.io.{BufferedWriter, File, FileWriter}
import scala.collection.mutable.{HashMap => MHashMap, MultiMap, Set => MSet}

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

// http://www.scala-lang.org/node/140
class Xgettext(val global: Global) extends Plugin with ScalaVersionAdapter {
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

  // -P:xgettext:<i18n trait or class>[,t=xxx,tn=xxx,tc=xxx,tcn=xxx]
  var i18n_class = ""
  var i18n_t     = Seq.empty[String]
  var i18n_tn    = Seq.empty[String]
  var i18n_tc    = Seq.empty[String]
  var i18n_tcn   = Seq.empty[String]
  var rawPluralForm: Option[String] = None
  var sourceLang: Option[String] = None

  val outputFile            = new File(OUTPUT_FILE)
  val emptyOutputFileExists = outputFile.exists && outputFile.isFile && outputFile.length == 0

  val msgToLines = new MHashMap[i18nKey, i18nValues] with MultiMap[i18nKey, i18nValue]

  // Avoid running ReduceComponent multiple times
  var reduced = false

  override def processOptions(options: List[String], error: String => Unit) {
    for (option <- options) {
      if (option.startsWith("t:"))
        i18n_t   +:= option.stripPrefix("t:")

      else if (option.startsWith("tn:"))
        i18n_tn  +:= option.stripPrefix("tn:")

      else if (option.startsWith("tc:"))
        i18n_tc  +:= option.stripPrefix("tc:")

      else if (option.startsWith("tcn:"))
        i18n_tcn +:= option.stripPrefix("tcn:")

      else if (option.startsWith("sourceLang:"))
        sourceLang = Option(option.stripPrefix("sourceLang:"))

      else if (option.startsWith("rawPluralForm:"))
        rawPluralForm = Option(option.stripPrefix("rawPluralForm:"))

      else
        i18n_class = option
    }

    if (i18n_t.isEmpty)   i18n_t   = Seq("t")
    if (i18n_tn.isEmpty)  i18n_tn  = Seq("tn")
    if (i18n_tc.isEmpty)  i18n_tc  = Seq("tc")
    if (i18n_tcn.isEmpty) i18n_tcn = Seq("tcn")
    if (rawPluralForm.isEmpty) rawPluralForm =
      sourceLang.map(lang => PluralForms.LangToForm.getOrElse(lang,
        {
          val errorMessage = s"sourceLang '$lang' unknown to $name. Supply $name:rawPluralForm property instead."
          println(errorMessage)
          throw new NoSuchElementException(errorMessage)
        }
      ))

  }

  private object MapComponent extends PluginComponent {
    val global: Xgettext.this.global.type = Xgettext.this.global

    val runsAfter = List("refchecks")

    val phaseName = "xgettext-map"

    def newPhase(_prev: Phase) = new MapPhase(_prev)

    class MapPhase(prev: Phase) extends StdPhase(prev) {
      override def name = phaseName

      def apply(unit: CompilationUnit) {
        val shouldExtract = !i18n_class.isEmpty && emptyOutputFileExists
        if (shouldExtract) {
          val i18nType = getTypeFor(i18n_class)

          for (tree @ Apply(Select(x1, x2), list) <- unit.body) {
            if (x1.tpe <:< i18nType) {
              val methodName = x2.toString
              val pos        = tree.pos  // scala.tools.nsc.util.OffsetPosition
              val line       = (relPath(pos.source.path), pos.line)

              if (i18n_t.contains(methodName)) {
                val msgid = fixBackslashSingleQuote(list(0).toString)
                msgToLines.addBinding((None, msgid, None), line)
              } else if (i18n_tn.contains(methodName)) {
                val msgid       = fixBackslashSingleQuote(list(0).toString)
                val msgidPlural = fixBackslashSingleQuote(list(1).toString)
                msgToLines.addBinding((None, msgid, Some(msgidPlural)), line)
              } else if (i18n_tc.contains(methodName)) {
                val msgctxt = fixBackslashSingleQuote(list(0).toString)
                val msgid   = fixBackslashSingleQuote(list(1).toString)
                msgToLines.addBinding((Some(msgctxt), msgid, None), line)
              } else if (i18n_tcn.contains(methodName)) {
                val msgctxt     = fixBackslashSingleQuote(list(0).toString)
                val msgid       = fixBackslashSingleQuote(list(1).toString)
                val msgidPlural = fixBackslashSingleQuote(list(2).toString)
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

      /**
       * t("Don't go") will be extracted as "Don\'t go"
       * (including the surrounding double quotes).
       *
       * Poedit will report "invalid control sequence" for key "Don\'t go", so
       * we should change it to just "Don't go".
       */
      private def fixBackslashSingleQuote(s: String): String = {
        s.replaceAllLiterally("""\'""", "'")
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

      def preparePluralForms(pluralForm: String) = s""""Plural-Forms: $pluralForm\\n""""

      def apply(unit: CompilationUnit) {
        val shouldExtract = !i18n_class.isEmpty && emptyOutputFileExists
        if (shouldExtract && !reduced) {
          val builder = new StringBuilder(HEADER)

          rawPluralForm.map(pluralForm => builder.append(preparePluralForms(pluralForm)))

          builder.append("\n\n")

          // Sort by key (msgctxto, msgid, msgidPluralo)
          // so that it's easier too see diffs between versions of the .pot/.po file
          val sortedMsgToLines = msgToLines.toSeq.sortBy { case (k, v) => k }

          for (((msgctxto, msgid, msgidPluralo), lines) <- sortedMsgToLines) {
            val sortedLines = lines.toSeq.sorted
            for ((srcPath, lineNo) <- sortedLines) {
              builder.append("#: " + srcPath + ":" + lineNo + "\n")
            }

            if (msgctxto.isDefined) builder.append("msgctxt " + msgctxto.get + "\n")
            builder.append("msgid " + msgid + "\n")
            msgidPluralo.map { msgidPlural =>
              builder.append(s"msgid_plural ${msgidPlural}\n")
              builder.append("msgstr[0] \"\"" + "\n\n")
            }.getOrElse(builder.append("msgstr \"\"" + "\n\n"))
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
