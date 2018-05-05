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

baseVersion in ThisBuild := "1.2"

val CatsVersion = "1.1.0"
val ScalazVersion = "7.2.20"

val Specs2Version = "4.0.3"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.specs2"     %% "specs2-core"       % Specs2Version % Test,
    "org.specs2"     %% "specs2-scalacheck" % Specs2Version % Test,

    "org.scalacheck" %% "scalacheck"        % "1.13.5"      % Test),

  developers += Developer("alissapajer", "Alissa Pajer", "@alissapajer", url("https://github.com/alissapajer")),

  homepage := Some(url("https://github.com/djspiewak/shims")),

  scmInfo := Some(ScmInfo(url("https://github.com/djspiewak/shims"),
    "git@github.com:djspiewak/shims.git")))

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS)
  .settings(commonSettings: _*)
  .settings(noPublishSettings)
  .settings(name := "root")

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

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

// intentionally not in the aggregation
lazy val scratch = project.dependsOn(coreJVM).settings(commonSettings: _*)
