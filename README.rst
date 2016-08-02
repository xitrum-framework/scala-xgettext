.. image:: poedit.png

This is a Scala 2.10 and 2.11 compiler plugin that acts like GNU ``xgettext``
command to extract i18n strings in Scala source code files to `Gettext <http://en.wikipedia.org/wiki/Gettext>`_
.po file, when you compile the Scala source code files.

More info on Scala compiler plugin:
http://www.scala-lang.org/node/140

Presentation:
`I18nize Scala programs à la gettext <http://www.slideshare.net/ngocdaothanh/i18nize-scala-program-a-la-gettext>`_

Discussion group:
https://groups.google.com/group/scala-xgettext

For `Play <https://www.playframework.com/>`_:
https://github.com/georgeOsdDev/play-xgettext

Usage
-----

This plugin can be used by frameworks like `Xitrum <http://xitrum-framework.github.io/>`_
to add i18n feature to them. For an example, see `this SBT project <https://github.com/xitrum-framework/comy>`_.

Create I18n trait or class
~~~~~~~~~~~~~~~~~~~~~~~~~~

In your Scala source code, you need to mark the strings you want to extract by
using a trait or class that has these method signatures:

::

  t(singular: String): String
  tn(singular: String, plural: String, n: Long): String

  tc(context: String, singular: String): String
  tcn(context: String, singular: String, plural: String, n: Long): String

The methods can also be:

::

  t(singular: String, args: Any*): String
  tn(singular: String, plural: String, n: Long, args: Any*): String

  tc(context: String, singular: String, args: Any*): String
  tcn(context: String, singular: String, plural: String, n: Long, args: Any*): String

That is, only the first arguments (1 first argument for ``t``, 3 first arguments
for ``tn`` etc.) are required, all the following arguments are ignored
(like `params` above).

You can use `Scaposer <https://github.com/xitrum-framework/scaposer>`_ to
implement the methods above. See `example <https://github.com/xitrum-framework/xitrum/blob/master/src/main/scala/xitrum/I18n.scala>`_.

Then in your Scala source code, use them like this:

::

  t("Hello World")
  t("Hello %s").format("World")

  t("%,.3f").format(1234.5678)                                // => 1,234.568
  t("%,.3f").formatLocal(java.util.Locale.FRANCE, 1234.5678)  // => 1 234,568

If you have more than one placeholder:

::

  // 1$ and 2$ are placeholders
  t("%1$s says hello to %2$s, then %2$s says hello back to %1$s").format("Bill", "Hillary")

  // {0} and {1} are placeholders
  java.text.MessageFormat.format(t("{0} says hello to {1}, then {1} says hello back to {0}"), "Bill", "Hillary")

Extract i18n strings to .pot file
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To extract i18n strings like "Hello World" in the above snippet:

* Clean your Scala project to force the recompilation of all Scala source code files.
* Create an empty i18n.pot file in the current working directory. It will be
  filled with i18n string resources extracted from compiled Scala source code files.
* Compile your Scala project with ``-P:xgettext:<i18n trait or class>`` option.
  Example: ``-P:xgettext:xitrum.I18n``.

If you use `SBT <http://www.scala-sbt.org/>`_, build.sbt should look like this:

::

  ...
  autoCompilerPlugins := true
  addCompilerPlugin("tv.cntt" %% "xgettext" % "1.3")
  scalacOptions += "-P:xgettext:xitrum.I18n"
  ...

Copy or rename the .pot file to a .po file, and translate the strings in it to
the language if want. "t" in ".pot" means "template".

You can use plain text editor to edit the .po file, or you can use
`Poedit <http://poedit.net/>`_. Poedit is very convenient, it can merge new .pot
file to existing translated .po file.

Content of the .pot file is sorted by msgid, so that it's easier too see diffs
between versions of the .pot/.po file.

Configure i18n marker method names
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``t``, ``tn``, ``tc``, and ``tcn`` above are the defaults.

If you want to use other names, you can change them to, for example,
``tr``, ``trn``, ``trc``, and ``trcn``, by adding options to Scala compiler:

::

  scalacOptions ++= Seq(
    "xitrum.I18n", "t:tr", "tn:trn", "tc:trc", "tcn:trcn"
  ).map("-P:xgettext:" + _)

If you skip an option, its default value will be used.

Multiple marker method names
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Multiple marker methods for ``t`` can be configured like this:

::

  scalacOptions ++= Seq(
    "xitrum.I18n", "t:tr", "t:notr"
  ).map("-P:xgettext:" + _)

Similar for ``tn``, ``tc``, and ``tcn``.

With this feature you can, for example, create an i18n library that can display
both original strings and translated strings.

Load .po file
~~~~~~~~~~~~~

Use `Scaposer <https://github.com/xitrum-framework/scaposer>`_.
