organization := "tv.cntt"
name         := "xgettext"
version      := "1.5.2-SNAPSHOT"

crossScalaVersions := Seq("2.12.1", "2.11.8", "2.10.6")
scalaVersion       := "2.12.1"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

unmanagedSourceDirectories in Compile += baseDirectory.value / (scalaBinaryVersion.value match {
  case "2.10" => "src/main/scala-2_10"
  case _      => "src/main/scala-2_11"
})
