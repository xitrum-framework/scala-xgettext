.. image:: http://www.poedit.net/screenshots/osx.png

This is a Scala 2.10 and 2.11 compiler plugin that acts like GNU ``xgettext``
command to extract i18n strings in Scala source code files to Gettext .po file
on compilation.

More info on Scala compiler plugin:
http://www.scala-lang.org/node/140

Discussion group:
https://groups.google.com/group/scala-xgettext

Usage
-----

Create I18n trait or class
~~~~~~~~~~~~~~~~~~~~~~~~~~

You should have a trait or class
(see `example <https://github.com/xitrum-framework/xitrum/blob/master/src/main/scala/xitrum/I18n.scala>`_)
that has these i18n methods:

::

  t(singular: String): String
  tc(context: String, singular: String): String
  tn(singular: String, plural: String, n: Long): String
  tcn(context: String, singular: String, plural: String, n: Long): String

You can use `Scaposer <https://github.com/xitrum-framework/scaposer>`_ to implement the above.

Then in your Scala source code, use them like this:

::

  t("Hello World")

Extract i18n strings to .po file
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To extract i18n strings like "Hello World" in the above snippet:

* Create an empty i18n.pot file in the current working directory. It will be
  filled with i18n string resources extracted from compiled Scala source code files.
  (You may need to clean your Scala project to force the recompilation of all files.)
* Pass ``-P:xgettext:<i18n trait or class>`` option when compiling Scala source code.
  (Ex: ``-P:xgettext:xitrum.I18n``)

If you use `SBT <http://www.scala-sbt.org/>`_, build.sbt should looks like this:

::

  ...
  autoCompilerPlugins := true

  addCompilerPlugin("tv.cntt" %% "xgettext" % "1.0")

  scalacOptions += "-P:xgettext:xitrum.I18n"
  ...

This plugin can be used by frameworks like `Xitrum <http://xitrum-framework.github.io/>`_
to add i18n feature to them. For an example, see `this SBT project <https://github.com/xitrum-framework/comy>`_.

Load created .po file
~~~~~~~~~~~~~~~~~~~~~

Use `Scaposer <https://github.com/xitrum-framework/scaposer>`_.
