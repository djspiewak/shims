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
