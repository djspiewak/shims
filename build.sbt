/*
 * Copyright 2018 Daniel Spiewak
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

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

baseVersion in ThisBuild := "1.3"

developers in ThisBuild ++= List(
  Developer(
    "christopherdavenport",
    "ChristopherDavenport",
    "@christopherdavenport",
    url("https://github.com/christopherdavenport")))

homepage in ThisBuild := Some(url("https://github.com/djspiewak/shims"))

scmInfo in ThisBuild := Some(ScmInfo(url("https://github.com/djspiewak/shims"),
  "git@github.com:djspiewak/shims.git"))

val CatsVersion = "1.1.0"
val ScalazVersion = "7.2.24"

val CatsEffectVersion = "1.0.0-RC2"

val Specs2Version = "4.0.3"
val DisciplineVersion = "0.8"

val testFrameworkSettings = Seq(
  libraryDependencies ++= Seq(
    "org.specs2"     %% "specs2-core"       % Specs2Version % Test,
    "org.specs2"     %% "specs2-scalacheck" % Specs2Version % Test,

    "org.scalacheck" %% "scalacheck"        % "1.13.5"      % Test))

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, effect)
  .settings(noPublishSettings)
  .settings(name := "root")

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(testFrameworkSettings)
  .settings(
    name := "shims",

    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core"   % CatsVersion,
      "org.typelevel" %%% "cats-free"   % CatsVersion,
      "org.scalaz"    %%% "scalaz-core" % ScalazVersion,

      "org.typelevel"  %%  "discipline"       % DisciplineVersion % Test,
      "org.typelevel"  %%% "cats-laws"        % CatsVersion       % Test),

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
    },

    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      import com.typesafe.tools.mima.core.ProblemFilters._

      Seq(
        // we don't care about macro incompatibilities
        exclude[DirectMissingMethodProblem]("shims.util.CaptureMacros.secondOpenImplicitTpe"),
        exclude[DirectMissingMethodProblem]("shims.util.OpenImplicitMacros.secondOpenImplicitTpe"),
        exclude[ReversedMissingMethodProblem]("shims.util.OpenImplicitMacros.rightImplicitTpeParam"),
        exclude[ReversedMissingMethodProblem]("shims.util.OpenImplicitMacros.leftImplicitTpeParam"),
        exclude[DirectMissingMethodProblem]("shims.util.CaptureMacros.secondOpenImplicitTpe"),
        exclude[DirectMissingMethodProblem]("shims.util.OpenImplicitMacros.secondOpenImplicitTpe"),
        exclude[ReversedMissingMethodProblem]("shims.util.OpenImplicitMacros.rightImplicitTpeParam"),
        exclude[ReversedMissingMethodProblem]("shims.util.OpenImplicitMacros.leftImplicitTpeParam"))
    }
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

// scalaz-concurrent isn't published for scalajs
lazy val effect = project
  .in(file("effect"))
  .dependsOn(coreJVM)
  .settings(testFrameworkSettings)
  .settings(
    name := "shims-effect",

    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"       % CatsEffectVersion,
      "org.scalaz"    %% "scalaz-concurrent" % ScalazVersion,

      "org.typelevel" %% "discipline"        % DisciplineVersion % Test,
      "org.typelevel" %% "cats-effect-laws"  % CatsEffectVersion % Test))

// intentionally not in the aggregation
lazy val scratch = project.dependsOn(coreJVM)
