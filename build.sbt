/*
 * Copyright 2019 Daniel Spiewak
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

ThisBuild / baseVersion := "1.8"

ThisBuild / strictSemVer := false     // 😢 maybe in 2.0...

ThisBuild / developers ++= List(
  Developer(
    "christopherdavenport",
    "ChristopherDavenport",
    "@christopherdavenport",
    url("https://github.com/christopherdavenport")))

ThisBuild / organization := "com.codecommit"
ThisBuild / publishGithubUser := "djspiewak"
ThisBuild / publishFullName := "Daniel Spiewak"

ThisBuild / homepage := Some(url("https://github.com/djspiewak/shims"))
ThisBuild / scmInfo := Some(ScmInfo(homepage.value.get,
  "git@github.com:djspiewak/shims.git"))

val CatsVersion = "1.6.1"
val ScalazVersion = "7.2.27"

val CatsEffectVersion = "1.3.0"

val Specs2Version = Def setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 12 => "4.1.2"
    case _ => "4.3.5"
  }
}

val ScalaCheckVersion = Def setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 12 => "1.13.5"
    case _ => "1.14.0"
  }
}

val DisciplineVersion = Def setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 12 => "0.9.0"
    case _ => "0.10.0"
  }
}

val testFrameworkSettings = Seq(
  libraryDependencies ++= Seq(
    "org.specs2"     %% "specs2-core"       % Specs2Version.value     % Test,
    "org.specs2"     %% "specs2-scalacheck" % Specs2Version.value     % Test,

    "org.scalacheck" %% "scalacheck"        % ScalaCheckVersion.value % Test))

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

      "org.typelevel"  %%  "discipline"       % DisciplineVersion.value % Test,
      "org.typelevel"  %%% "cats-laws"        % CatsVersion             % Test),

    // cribbed from shapeless
    libraryDependencies ++= Seq(
      scalaOrganization.value % "scala-reflect" % scalaVersion.value % "provided",
      scalaOrganization.value % "scala-compiler" % scalaVersion.value % "provided"),

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

      "org.typelevel" %% "discipline"        % DisciplineVersion.value % Test,
      "org.typelevel" %% "cats-effect-laws"  % CatsEffectVersion       % Test))

// intentionally not in the aggregation
lazy val scratch = project.dependsOn(coreJVM)
