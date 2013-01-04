This is a Scala 2.10 compiler plugin that acts like GNU xgettext command to extract
i18n strings in Scala source code files to Gettext .po file.

To load the created .po file, you can use
`Scaposer <https://github.com/ngocdaothanh/scaposer>`_.

More info on Scala compiler plugin:
http://www.scala-lang.org/node/140

Usage
-----

* Pass ``-P:xgettext:<i18n trait or class>`` option when compiling Scala source code.
  (Ex: ``-P:xgettext:xitrum.I18n``)
* Create an empty i18n.pot file in the current working directory. It will be
  filled with i18n string resources extracted from compiled Scala source code files.

``<i18n trait or class>`` should have these methods:

::

  t(singular: String): String
  tc(context: String, singular: String): String
  tn(singular: String, plural: String, n: Long): String
  tcn(context: String, singular: String, plural: String, n: Long): String

If you use `SBT <http://www.scala-sbt.org/>`_, build.sbt should looks like this:

::

  ...
  autoCompilerPlugins := true

  addCompilerPlugin("tv.cntt" %% "xgettext" % "1.0")

  scalacOptions += "-P:xgettext:xitrum.I18n"
  ...

This plugin is used by `Xitrum web framework <http://ngocdaothanh.github.com/xitrum/>`_.
For an example, see `this SBT project <https://github.com/ngocdaothanh/comy>`_.
