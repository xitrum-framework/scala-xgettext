This is a Scala 2.10 compiler plugin that acts like GNU xgettext command to extract
i18n strings in Scala source code files to Gettext .po file.

See http://www.scala-lang.org/node/140

Usage
-----

This compiler plugin checks if there's an empty i18n.pot file in the current
working directory, then it will fill that file with string resources.
