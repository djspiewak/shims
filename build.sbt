val ReleaseTag = """^v([\d\.]+)$""".r

lazy val commonSettings = Seq(
  organization := "com.codecommit",

  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/")),

  scalaVersion := "2.11.7",

  crossScalaVersions := Seq(scalaVersion.value, "2.10.6"),

  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.7.1" cross CrossVersion.binary),

  scalacOptions += "-language:_",      // I really can't be bothered with SIP-18

  scalacOptions in Test += "-Yrangepos")

lazy val core = project.in(file("core")).settings(commonSettings: _*)
lazy val scalaz = project.in(file("scalaz")).settings(commonSettings: _*).dependsOn(core)
lazy val cats = project.in(file("cats")).settings(commonSettings: _*).dependsOn(core)

enablePlugins(GitVersioning)

git.baseVersion := "0.1"

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
