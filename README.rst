.. image:: poedit.png

This is a Scala 2.10 and 2.11 compiler plugin that acts like GNU ``xgettext``
command to extract i18n strings in Scala source code files to `Gettext <http://en.wikipedia.org/wiki/Gettext>`_
.po file on compilation.

More info on Scala compiler plugin:
http://www.scala-lang.org/node/140

Discussion group:
https://groups.google.com/group/scala-xgettext

Usage
-----

This plugin can be used by frameworks like `Xitrum <http://xitrum-framework.github.io/>`_
to add i18n feature to them. For an example, see `this SBT project <https://github.com/xitrum-framework/comy>`_.

Create I18n trait or class
~~~~~~~~~~~~~~~~~~~~~~~~~~

You should have a trait or class
(see `example <https://github.com/xitrum-framework/xitrum/blob/master/src/main/scala/xitrum/I18n.scala>`_)
that has these i18n methods:

::

  t(singular: String): String
  tn(singular: String, plural: String, n: Long): String

  tc(context: String, singular: String): String
  tcn(context: String, singular: String, plural: String, n: Long): String

The methods can also be:

::

  t(singular: String, params: Any*): String
  tn(singular: String, plural: String, n: Long, params: Any*): String

  tc(context: String, singular: String, params: Any*): String
  tcn(context: String, singular: String, plural: String, n: Long, params: Any*): String

That is, only the first arguments are required, arguments after those
(like `params` above) are ignored.

You can use `Scaposer <https://github.com/xitrum-framework/scaposer>`_ to implement the above.

Then in your Scala source code, use them like this:

::

  t("Hello World")

Extract i18n strings to .pot file
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To extract i18n strings like "Hello World" in the above snippet:

* Clean your Scala project to force the recompilation of all files (see below).
* Create an empty i18n.pot file in the current working directory. It will be
  filled with i18n string resources extracted from compiled Scala source code files.
* Compile your Scala project with ``-P:xgettext:<i18n trait or class>`` option.
  (Example: ``-P:xgettext:xitrum.I18n``)

If you use `SBT <http://www.scala-sbt.org/>`_, build.sbt should look like this:

::

  ...
  autoCompilerPlugins := true

  addCompilerPlugin("tv.cntt" %% "xgettext" % "1.2")

  scalacOptions += "-P:xgettext:xitrum.I18n"
  ...

Copy the .pot file to .po file and translate it to the language if want
("t" in .pot means "template").

If you have existing .po file, use tools like `Poedit <http://poedit.net/>`_ to
merge the .pot file to the .po file.

Content of the .pot file is sorted by msgid, so that it's easier too see diffs
between versions of the .pot/.po file.

Configure i18n method names
~~~~~~~~~~~~~~~~~~~~~~~~~~~

``t``, ``tn``, ``tc``, and ``tcn`` above are the defaults.

If you want to use other names, for example if you want to change them to
``tr``, ``trn``, ``trc``, and ``trcn`` respectively,
you can add options to Scala compiler like this:

::

  scalacOptions ++= Seq("xitrum.I18n", "t:tr", "tn:trn", "tc:trc", "tcn:trcn").map("-P:xgettext:" + _)

If you skip an option, its default value will be used.

Load .po file
~~~~~~~~~~~~~

Use `Scaposer <https://github.com/xitrum-framework/scaposer>`_.
