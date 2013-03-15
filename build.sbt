organization := "tv.cntt"

name := "xgettext"

version := "1.1-SNAPSHOT"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked"
)

scalaVersion := "2.10.1"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.10.1"
