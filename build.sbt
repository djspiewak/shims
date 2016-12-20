val ReleaseTag = """^v([\d\.]+)$""".r

val CatsVersion = "0.8.1"

lazy val commonSettings = Seq(
  organization := "com.codecommit",

  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/")),

  libraryDependencies += "org.specs2" %% "specs2-core" % "3.8.6" % "test",

  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.3" cross CrossVersion.binary),

  scalacOptions += "-language:_",      // I really can't be bothered with SIP-18
  scalacOptions in Test += "-Yrangepos",

  isSnapshot := version.value endsWith "SNAPSHOT",      // so… sonatype doesn't like git hash snapshots

  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },

  sonatypeProfileName := "com.codecommit",

  pomExtra :=
    <developers>
      <developer>
        <id>djspiewak</id>
        <name>Daniel Spiewak</name>
        <url>http://www.codecommit.com</url>
      </developer>
      <developer>
        <id>alissapajer</id>
        <name>Alissa Pajer</name>
      </developer>
    </developers>,

  homepage := Some(url("https://github.com/djspiewak/shims")),

  scmInfo := Some(ScmInfo(url("https://github.com/djspiewak/shims"),
    "git@github.com:djspiewak/shims.git")))

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, scalaz72JVM, scalaz72JS, catsJVM, catsJS)
  .settings(commonSettings: _*)
  .settings(
    name := "shims",

    publish := (),
    publishLocal := (),
    publishArtifact := false)

val extraCoreSettings = Seq(
  name := "shims-core",

  // shamelessly copied from shapeless
  libraryDependencies ++= Seq(
    "org.typelevel" %% "macro-compat" % "1.1.1",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)),

  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
      case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
      // in Scala 2.10, quasiquotes are provided by macro paradise
      case Some((2, 10)) =>
        Seq("org.scalamacros" %% "quasiquotes" % "2.1.0" cross CrossVersion.binary)
    }
  },

  libraryDependencies += "org.specs2" %% "specs2-core" % "3.7" % "test",

  scalacOptions in Test ++= Seq("-Yrangepos")
)

lazy val core = crossProject
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings ++ extraCoreSettings: _*)
lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val scalaz72 = crossProject
  .crossType(CrossType.Pure)
  .in(file("scalaz72"))
  .settings(
    (commonSettings :+ (name := "shims-scalaz-72")) :+
      (libraryDependencies += "org.scalaz" %%% "scalaz-core" % "7.2.7"): _*
  ).dependsOn(core)
lazy val scalaz72JVM = scalaz72.jvm
lazy val scalaz72JS = scalaz72.js

lazy val cats = crossProject
  .crossType(CrossType.Pure)
  .in(file("cats"))
  .settings(
    (commonSettings :+ (name := "shims-cats")) :+
      (libraryDependencies ++= Seq(
        "org.typelevel" %%% "cats-core" % CatsVersion,
        "org.typelevel" %%% "cats-macros" % CatsVersion)): _*
  ).dependsOn(core)
lazy val catsJVM = cats.jvm
lazy val catsJS = cats.js

enablePlugins(GitVersioning)

git.baseVersion := "0.4"

git.gitTagToVersionNumber := {
  case ReleaseTag(version) => Some(version)
  case _ => None
}

git.formattedShaVersion := {
  val suffix = git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, git.uncommittedSignifier.value)

  git.gitHeadCommit.value map {
    _.substring(0, 7)
  } map { sha =>
    git.baseVersion.value + "-" + sha + suffix
  }
}

git.gitUncommittedChanges := "git status -s".!!.trim.length > 0
