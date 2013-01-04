This is a Scala 2.10 compiler plugin that acts like GNU xgettext command to extract
i18n strings in Scala source code files to Gettext .po file.

More info on Scala compiler plugin:
http://www.scala-lang.org/node/140

Usage
-----

* Pass ``-Dxgettext=<i18n trait or class>`` option when compiling Scala source code.
  (Ex: ``-Dxgettext=xitrum.I18n``)
* Create an empty i18n.pot file in the current working directory. It will be
  filled with i18n string resources extracted from compiled Scala source code files.

The ``<i18n trait or class>`` should have these methods:

::

  t(singular: String): String
  tc(context: String, singular: String): String
  tn(singular: String, plural: String, n: Long): String
  tcn(context: String, singular: String, plural: String, n: Long): String

If you use `SBT <http://www.scala-sbt.org/>`_, build.sbt should looks like this:

::

  ...
  scalacOptions += "-Dxgettext=<i18n trait or class>"

  autoCompilerPlugins := true

  addCompilerPlugin("tv.cntt" %% "scala-xgettext" % "1.0")
  ...

This plugin is used by `Xitrum web framework <http://ngocdaothanh.github.com/xitrum/>`_.
For an example, see `this SBT project <https://github.com/ngocdaothanh/comy>`_.
