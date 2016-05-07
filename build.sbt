organization := "tv.cntt"

name := "xgettext"

version := "1.4-SNAPSHOT"

crossScalaVersions := Seq("2.11.6", "2.11.7", "2.11.8")

crossVersion := CrossVersion.full

// In src/main/scala/scala/Xgettext.scala, see the lines that are marked with
// "Scala 2.10" and "Scala 2.11".
//
scalaVersion := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

// http://www.scala-sbt.org/release/docs/Detailed-Topics/Java-Sources
// Avoid problem when this lib is built with Java 7 but the projects that use it
// are run with Java 6
// java.lang.UnsupportedClassVersionError: Unsupported major.minor version 51.0
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

libraryDependencies <+= scalaVersion { sv =>
  "org.scala-lang" % "scala-compiler" % sv
}