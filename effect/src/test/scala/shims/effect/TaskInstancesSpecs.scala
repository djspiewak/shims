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

package shims.effect

import cats.Eq

import cats.effect.laws.discipline.EffectTests
import cats.effect.laws.discipline.arbitrary.catsEffectLawsArbitraryForIO
import cats.effect.laws.util.{TestContext, TestInstances}, TestInstances.{eqIO, eqThrowable}
import cats.effect.syntax.effect._

import cats.instances.either._
import cats.instances.int._
import cats.instances.tuple._
import cats.instances.unit._

import scalaz.concurrent.Task

import org.specs2.Specification
import org.specs2.scalacheck.Parameters
import org.specs2.specification.core.Fragments

import org.typelevel.discipline.Laws
import org.typelevel.discipline.specs2.Discipline

object TaskInstancesSpecs extends Specification with Discipline {
  import TaskArbitrary._

  def is =
    checkAllAsync("Task", implicit ctx => EffectTests[Task].effect[Int, Int, Int])

  def checkAllAsync(name: String, f: TestContext => Laws#RuleSet)(implicit p: Parameters) = {
    val context = TestContext()
    val ruleSet = f(context)

    Fragments.foreach(ruleSet.all.properties) {
      case (id, prop) =>
        id ! check(prop, p, defaultFreqMapPretty) ^ br
    }
  }

  implicit def taskEq[A: Eq](implicit ctx: TestContext): Eq[Task[A]] = Eq.by(_.toIO)
}
