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

ThisBuild / baseVersion := "2.2"

ThisBuild / crossScalaVersions := List("0.25.0", "0.26.0-RC1", "2.12.11", "2.13.3")

ThisBuild / githubWorkflowJavaVersions := List("adopt@1.8", "adopt@14")

ThisBuild / strictSemVer := false

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

val CatsVersion = "2.1.1"
val ScalazVersion = "7.2.30"

val CatsEffectVersion = "2.2.0"

val Specs2Version = "4.10.5"
val ScalaCheckVersion = "1.15.1"
val DisciplineVersion = "1.1.1"

val testFrameworkSettings = Seq(
  libraryDependencies ++= Seq(
    "org.specs2"     %% "specs2-core"       % Specs2Version     % Test,
    "org.specs2"     %% "specs2-scalacheck" % Specs2Version     % Test,

    "org.scalacheck" %% "scalacheck"        % ScalaCheckVersion % Test))

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

    Compile / unmanagedSourceDirectories += {
      val component = if (scalaVersion.value.startsWith("0.") || scalaVersion.value.startsWith("3."))
        "scala-3.x"
      else
        "scala-2.x"

      baseDirectory.value / ".." / "src" / "main" / component
    },

    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core"   % CatsVersion,
      "org.typelevel" %%% "cats-free"   % CatsVersion,
      "org.scalaz"    %%% "scalaz-core" % ScalazVersion,

      "org.typelevel"  %%  "discipline-specs2" % DisciplineVersion % Test,
      "org.typelevel"  %%% "cats-laws"         % CatsVersion       % Test),

    // cribbed from shapeless
    libraryDependencies ++= {
      if (isDotty.value)
        Seq()
      else
        Seq(
          scalaOrganization.value % "scala-reflect" % scalaVersion.value % "provided",
          scalaOrganization.value % "scala-compiler" % scalaVersion.value % "provided")
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
    })
  .settings(dottyLibrarySettings)
  .settings(dottyJsSettings(ThisBuild / crossScalaVersions))

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

      "org.typelevel" %% "discipline-specs2" % DisciplineVersion % Test,
      "org.typelevel" %% "cats-effect-laws"  % CatsEffectVersion % Test))
  .settings(dottyLibrarySettings)

// intentionally not in the aggregation
lazy val scratch = project.dependsOn(coreJVM)
