/*
 * Copyright 2017 Daniel Spiewak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import scala.sys.process._

// version scheme described here: https://github.com/djspiewak/parseback/blob/30ee45e411a66297167a6e45e0e874fa23d8cc6d/project.sbt#L23-L53
val BaseVersion = "1.1"
val ReleaseTag = """^v([\d\.]+)$""".r

val CatsVersion = "1.0.1"
val ScalazVersion = "7.2.18"

val Specs2Version = "4.0.0"

addCommandAlias("ci", ";test ;mimaReportBinaryIssues")
addCommandAlias("release", "; project root; reload; +publishSigned; sonatypeReleaseAll")

organizationName in ThisBuild := "Daniel Spiewak"
startYear in ThisBuild := Some(2017)
licenses in ThisBuild += ("Apache-2.0", url("http://www.apache.org/licenses/"))

lazy val commonSettings = Seq(
  organization := "com.codecommit",

  useGpg := true,

  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging),

  libraryDependencies ++= Seq(
    "org.specs2"     %% "specs2-core"       % Specs2Version % "test",
    "org.specs2"     %% "specs2-scalacheck" % Specs2Version % "test",

    "org.scalacheck" %% "scalacheck"        % "1.13.5"      % "test"),

  libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % "1.1.1",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)),

  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.3" cross CrossVersion.binary),

  // Adapted from Rob Norris' post at https://tpolecat.github.io/2014/04/11/scalac-flags.html
  scalacOptions ++= Seq(
    "-language:_",
    "-deprecation",
    "-encoding", "UTF-8", // yes, this is 2 args
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    // "-Xlint",
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
      case Some((2, major)) if major >= 12 || scalaVersion.value == "2.11.11" || scalaVersion.value == "2.11.12" =>
        Seq("-Ypartial-unification")

      case _ => Seq.empty
    }
  },

  scalacOptions in Test += "-Yrangepos",

  scalacOptions in (Compile, console) ~= (_ filterNot (Set("-Xfatal-warnings", "-Ywarn-unused-import", "-Xlint").contains)),

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

val mimaSettings = Seq(
  mimaPreviousArtifacts := {
    val TagBase = """^(\d+)\.(\d+).*"""r
    val TagBase(major, minor) = BaseVersion

    val tags = "git tag --list".!! split "\n" map { _.trim }

    val versions =
      tags filter { _ startsWith s"v$major.$minor" } map { _ substring 1 }

    versions map { v => organization.value %% name.value % v } toSet
  }
)

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS)
  .settings(commonSettings: _*)
  .settings(
    name := "root",

    publish := (()),
    publishLocal := (()),
    publishArtifact := false)

lazy val core = crossProject
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "shims",

    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core"   % CatsVersion,
      "org.typelevel" %%% "cats-free"   % CatsVersion,
      "org.scalaz"    %%% "scalaz-core" % ScalazVersion,

      "org.typelevel" %%  "discipline"  % "0.7.3"     % "test",
      "org.typelevel" %%% "cats-laws"   % CatsVersion % "test"),

    // cribbed from shapeless
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % "1.1.1",
      scalaOrganization.value % "scala-reflect" % scalaVersion.value % "provided",
      scalaOrganization.value % "scala-compiler" % scalaVersion.value % "provided",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)),

    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
        case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
        // in Scala 2.10, quasiquotes are provided by macro paradise
        case Some((2, 10)) =>
          Seq("org.scalamacros" %% "quasiquotes" % "2.1.0" cross CrossVersion.binary)
      }
    }
  )
  .enablePlugins(AutomateHeaderPlugin)

lazy val coreJVM = core.jvm.settings(mimaSettings)
lazy val coreJS = core.js

// intentionally not in the aggregation
lazy val scratch = project.dependsOn(coreJVM)

enablePlugins(GitVersioning)

git.baseVersion := BaseVersion

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
