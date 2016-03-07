name := "shims-core"

// shamelessly copied from shapeless
libraryDependencies ++= Seq(
  "org.typelevel" %% "macro-compat" % "1.1.1",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
    case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
    // in Scala 2.10, quasiquotes are provided by macro paradise
    case Some((2, 10)) =>
      Seq("org.scalamacros" %% "quasiquotes" % "2.1.0" cross CrossVersion.binary)
  }
}

libraryDependencies += "org.specs2" %% "specs2-core" % "3.7" % "test"

scalacOptions in Test ++= Seq("-Yrangepos")