organization := "tv.cntt"

name := "xgettext"

version := "1.4-SNAPSHOT"

// In src/main/scala/scala/Xgettext.scala, see the lines that are marked with
// "Scala 2.10" and "Scala 2.11".
//
// When doing publish-signed, change the version below and those line accordingly.
scalaVersion := "2.11.2"
//scalaVersion := "2.10.4"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

// http://www.scala-sbt.org/release/docs/Detailed-Topics/Java-Sources
// Avoid problem when this lib is built with Java 7 but the projects that use it
// are run with Java 6
// java.lang.UnsupportedClassVersionError: Unsupported major.minor version 51.0
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

libraryDependencies <+= scalaVersion { sv =>
  "org.scala-lang" % "scala-compiler" % sv
}
