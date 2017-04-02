val ReleaseTag = """^v([\d\.]+)$""".r

val CatsVersion = "0.9.0"
val ScalazVersion = "7.2.10"

val Specs2Version = "3.8.6"

lazy val commonSettings = Seq(
  organization := "com.codecommit",

  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/")),

  libraryDependencies ++= Seq(
    "org.specs2"     %% "specs2-core"       % Specs2Version % "test",
    "org.specs2"     %% "specs2-scalacheck" % Specs2Version % "test",

    "org.scalacheck" %% "scalacheck"        % "1.13.5"      % "test"),

  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.3" cross CrossVersion.binary),

  // Adapted from Rob Norris' post at https://tpolecat.github.io/2014/04/11/scalac-flags.html
  scalacOptions ++= Seq(
    "-language:_",
    "-deprecation",
    "-encoding", "UTF-8", // yes, this is 2 args
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code"
  ),

  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, major)) if major >= 11 => Seq(
        "-Ywarn-unused-import", // Not available in 2.10
        "-Ywarn-numeric-widen" // In 2.10 this produces a some strange spurious error
      )
      case _ => Seq.empty
    }
  },

  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, major)) if major >= 12 || scalaVersion.value == "2.11.9" =>
        Seq("-Ypartial-unification")

      case _ => Seq.empty
    }
  },

  scalacOptions in Test += "-Yrangepos",

  scalacOptions in (Compile, console) ~= (_ filterNot (Set("-Xfatal-warnings", "-Ywarn-unused-import").contains)),

  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,

  libraryDependencies ++= {
    scalaVersion.value match {
      case "2.11.8" => Seq(compilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full))
      case "2.10.6" => Seq(compilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full))
      case _ => Seq.empty
    }
  },

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
  .aggregate(coreJVM, coreJS)
  .settings(commonSettings: _*)
  .settings(
    name := "shims",

    publish := (),
    publishLocal := (),
    publishArtifact := false)

lazy val core = crossProject
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "shims-core",

    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core"   % CatsVersion,
      "org.typelevel" %%% "cats-free"   % CatsVersion,
      "org.scalaz"    %%% "scalaz-core" % ScalazVersion,

      "org.typelevel" %%  "discipline"  % "0.7.3"     % "test",
      "org.typelevel" %%% "cats-laws"   % CatsVersion % "test"))

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

enablePlugins(GitVersioning)

git.baseVersion := "1.0.0"

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
