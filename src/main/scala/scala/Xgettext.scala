package scala

import java.io.{BufferedWriter, File, FileWriter, IOException, StringWriter}

import scala.collection.mutable
import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

// http://www.scala-lang.org/node/140
class Xgettext(val global: Global) extends Plugin {
  import global._

  private type i18nKey = (
    Option[String],  // msgctxt
    String,          // msgid
    Option[String]   // msgid_plural
  )

  private type i18nValue = (
    String,          // source
    Int              // line
  )

  private type i18nValues = mutable.Set[i18nValue]

  override val name        = "xgettext"
  override val description = "This Scala compiler plugin extracts and creates gettext.pot file"
  override val components  = List[PluginComponent](MapComponent, ReduceComponent)

  private val OUTPUT_FILE = "i18n.pot"
  private val HEADER      = """msgid ""
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
  private var i18n_class = ""
  private var i18n_t     = Seq.empty[String]
  private var i18n_tn    = Seq.empty[String]
  private var i18n_tc    = Seq.empty[String]
  private var i18n_tcn   = Seq.empty[String]

  // If this option is set to true, non-literal strings are ignored instead of throwing an exception
  private var ignoreNonLiteralStrings = false

  private var rawPluralForm: Option[String] = None
  private var sourceLang:    Option[String] = None

  // Only enable this plugin if the output file is empty; to avoid increasing Scala compilation time
  private val outputFile    = new File(OUTPUT_FILE)
  private val pluginEnabled = outputFile.exists && outputFile.isFile && outputFile.length == 0

  private val msgToLines = new mutable.HashMap[i18nKey, i18nValues] with mutable.MultiMap[i18nKey, i18nValue]

  // Avoid running ReduceComponent multiple times
  private var reduced = false

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

      else if (option.startsWith("ignoreNonLiteralStrings:"))
        ignoreNonLiteralStrings = option.stripPrefix("ignoreNonLiteralStrings:").toBoolean

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
    override val global: Xgettext.this.global.type = Xgettext.this.global

    override val runsAfter = List("refchecks")

    override val phaseName = "xgettext-map"

    override def newPhase(_prev: Phase) = new MapPhase(_prev)

    class MapPhase(prev: Phase) extends StdPhase(prev) {
      override def name: String = phaseName

      override def apply(unit: CompilationUnit) {
        val shouldExtract = pluginEnabled && !i18n_class.isEmpty
        if (shouldExtract) {
          val i18nType = rootMirror.getClassByName(TypeName(i18n_class)).tpe

          for (tree @ Apply(Select(x1, x2), list) <- unit.body) {
            if (x1.tpe <:< i18nType) {
              val methodName = x2.toString
              val pos        = tree.pos  // scala.tools.nsc.util.OffsetPosition
              val line       = (relPath(pos.source.path), pos.line)

              if (i18n_t.contains(methodName)) {
                for (msgid <- stringConstant(list.head, pos)) {
                  msgToLines.addBinding((None, formatString(msgid), None), line)
                }
              } else if (i18n_tn.contains(methodName)) {
                for (msgid <- stringConstant(list.head, pos);
                     msgidPlural <- stringConstant(list(1), pos)) {
                  msgToLines.addBinding((None, formatString(msgid), Some(formatString(msgidPlural))), line)
                }
              } else if (i18n_tc.contains(methodName)) {
                for (msgctxt <- stringConstant(list.head, pos);
                     msgid <- stringConstant(list(1), pos)) {
                  msgToLines.addBinding((Some(formatString(msgctxt)), formatString(msgid), None), line)
                }
              } else if (i18n_tcn.contains(methodName)) {
                for (msgctxt <- stringConstant(list.head, pos);
                     msgid <- stringConstant(list(1), pos);
                     msgidPlural <- stringConstant(list(2), pos)) {
                  msgToLines.addBinding((Some(formatString(msgctxt)), formatString(msgid),
                    Some(formatString(msgidPlural))), line)
                }
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

      private def stringConstant(tree: Tree, pos: Position): Option[String] = tree match {
        case Literal(Constant(s: String)) => Some(s)
        case _ if ignoreNonLiteralStrings => None
        case _ => throw new IllegalArgumentException(s"Not a literal constant string: '$tree' at ${pos.source.path} line ${pos.line}")
      }

      /**
        * t("Don't go") will be extracted as "Don\'t go"
        * (including the surrounding double quotes).
        *
        * Poedit will report "invalid control sequence" for key "Don\'t go", so
        * we should change it to just "Don't go".
        *
        * t("hi "name"") will be extracted as "hi \"name\""
        *
        * multi line format for .pot files:
        * lines == List("bar", "foo") =>
        * ""
        * "bar\n"
        * "foo"
       */
      private def formatString(s: String): String = {
        List(unEscapeSingleQuote, escapeDoubleQuote, mkXgettextNewlines).foldLeft(s)((v, f) => {
          if (v.contains("Marcus")){
            println(v)
          }
          f(v)
        })
      }

      // Replace \' with '
      private val unEscapeSingleQuote: String => String = s => {
        s.replaceAll("\\'", "'")
      }

      // Replace " with \"
      private val escapeDoubleQuote: String => String = s => {
        s.replaceAllLiterally("\"", "\\\"")
      }

      // bar\nfoo =>
      // ""
      // "bar\n"
      // "foo"
      private val mkXgettextNewlines: String => String = s => {
        val lines = s.split("\n")
        if(lines.length == 1)
          s""""$s""""
        else {
          val twoDoubleQuotes = "\"\""
          val potFormattedNewlines = lines.mkString("\"", "\\n\"\n\"", "\"")
          twoDoubleQuotes + "\n" + potFormattedNewlines
        }
      }
    }
  }

  private object ReduceComponent extends PluginComponent {
    override val global: Xgettext.this.global.type = Xgettext.this.global

    override val runsAfter = List("jvm")

    override val phaseName = "xgettext-reduce"

    override def newPhase(_prev: Phase) = new ReducePhase(_prev)

    class ReducePhase(prev: Phase) extends StdPhase(prev) {
      override def name: String = phaseName

      override def apply(unit: CompilationUnit) {
        val shouldExtract = pluginEnabled && !reduced && !i18n_class.isEmpty
        if (shouldExtract) {
          val builder = new StringBuilder(HEADER)

          rawPluralForm.map(pluralForm => builder.append(preparePluralForms(pluralForm)))

          builder.append("\n\n")

          // Sort by key (msgctxto, msgid, msgidPluralo)
          // so that it's easier too see diffs between versions of the .pot/.po file
          val sortedMsgToLines = msgToLines.toSeq.sortBy(_._1)

          for (((msgctxto, msgid, msgidPluralo), lines) <- sortedMsgToLines) {
            val sortedLines = lines.toSeq.sorted
            for ((srcPath, lineNo) <- sortedLines) {
              builder.append("#: " + srcPath + ":" + lineNo + "\n")
            }

            if (msgctxto.isDefined) builder.append("msgctxt " + msgctxto.get + "\n")
            builder.append("msgid " + msgid + "\n")
            msgidPluralo.map { msgidPlural =>
              builder.append(s"msgid_plural $msgidPlural\n")
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

      private def preparePluralForms(pluralForm: String) = s""""Plural-Forms: $pluralForm\\n""""
    }
  }
}
