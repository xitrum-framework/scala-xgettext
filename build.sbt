organization := "tv.cntt"
name         := "xgettext"
version      := "1.4-SNAPSHOT"

crossScalaVersions := Seq("2.10.6", "2.11.8")
scalaVersion       := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

// http://www.scala-sbt.org/release/docs/Detailed-Topics/Java-Sources
// Avoid problem when this lib is built with Java 7 but the projects that use it
// are run with Java 6
// java.lang.UnsupportedClassVersionError: Unsupported major.minor version 51.0
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

unmanagedSourceDirectories in Compile += baseDirectory.value / (scalaBinaryVersion.value match {
  case "2.10" => "src/main/scala-2_10"
  case _      => "src/main/scala-2_11"
})
