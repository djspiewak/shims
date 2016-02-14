val ReleaseTag = """^v([\d\.]+)$""".r

lazy val commonSettings = Seq(
  organization := "com.codecommit",

  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/")),

  scalaVersion := "2.11.7",

  crossScalaVersions := Seq(scalaVersion.value, "2.10.6"),

  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.7.1" cross CrossVersion.binary),

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
  .aggregate(core, scalaz72, scalaz71, cats)
  .settings(commonSettings: _*)
  .settings(
    name := "shims",

    publish := (),
    publishLocal := (),
    publishArtifact := false)

lazy val core = project.in(file("core")).settings(commonSettings: _*)

lazy val scalaz72 = project.in(file("scalaz72")).settings(commonSettings: _*).dependsOn(core)
lazy val scalaz71 = project.in(file("scalaz71")).settings(commonSettings: _*).dependsOn(core)
lazy val cats = project.in(file("cats")).settings(commonSettings: _*).dependsOn(core)

enablePlugins(GitVersioning)

git.baseVersion := "0.3"

git.gitTagToVersionNumber := {
  case ReleaseTag(version) => Some(version)
  case _ => None
}

git.formattedShaVersion := {
  val suffix = git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, git.uncommittedSignifier.value)

  git.gitHeadCommit.value map { _.substring(0, 7) } map { sha =>
    git.baseVersion.value + "-" + sha + suffix
  }
}

git.gitUncommittedChanges := "git status -s".!!.trim.length > 0
