organization := "tv.cntt"
name         := "xgettext"
version      := "1.5.4-SNAPSHOT"

crossScalaVersions := Seq("2.13.4", "2.12.13")
scalaVersion       := "2.13.4"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value
