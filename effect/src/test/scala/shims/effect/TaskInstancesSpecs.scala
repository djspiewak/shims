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

package shims.effect

import cats.Eq
import cats.laws.discipline.{ApplicativeTests, ParallelTests}

import cats.effect.{Async, IO}
import cats.effect.laws.discipline.{arbitrary, EffectTests}, arbitrary._
import cats.effect.laws.util.{TestContext, TestInstances}, TestInstances._

import cats.instances.either._
import cats.instances.int._
import cats.instances.tuple._
import cats.instances.unit._

import scalaz.Tag
import scalaz.concurrent.Task
import scalaz.concurrent.Task.ParallelTask

import org.specs2.Specification
import org.specs2.scalacheck.Parameters
import org.specs2.specification.core.Fragments

import org.typelevel.discipline.Laws
import org.typelevel.discipline.specs2.Discipline

import java.util.concurrent.RejectedExecutionException

import scala.concurrent.ExecutionContext

object TaskInstancesSpecs extends Specification with Discipline {
  import TaskArbitrary._
  import Task.taskParallelApplicativeInstance

  def is = br ^ taskEff ^ br ^ taskPar ^ br ^ parTaskApp ^ br ^ asyncShiftTask

  def taskEff = checkAllAsync("Effect[Task]",
    implicit ctx => EffectTests[Task].effect[Int, Int, Int])

  def taskPar = checkAllAsync("Parallel[Task, ParallelTask]",
    implicit ctx => ParallelTests[Task, ParallelTask].parallel[Int, Int])

  def parTaskApp = checkAllAsync("Parallel[Task, ParallelTask]", { implicit ctx =>
    val tests = ApplicativeTests[ParallelTask]
    tests.applicative[Int, Int, Int]
  })

  def asyncShiftTask = {
    implicit val context: TestContext = TestContext()
    val boom = new RejectedExecutionException("Boom")
    val rejectingEc = new ExecutionContext {
      def execute(runnable: Runnable): Unit = throw boom
      def reportFailure(cause: Throwable): Unit = ()
    }

    "async.shift on rejecting execution context" ! {
      Eq[Task[Unit]].eqv(Async.shift[Task](rejectingEc), Task.fail(boom)) must beTrue
    }
  }

  def checkAllAsync(name: String, f: TestContext => Laws#RuleSet)(implicit p: Parameters) = {
    val context = TestContext()
    val ruleSet = f(context)

    Fragments.foreach(ruleSet.all.properties.toList) {
      case (id, prop) =>
        id ! check(prop, p, defaultFreqMapPretty) ^ br
    }
  }

  implicit def taskEq[A: Eq](implicit ctx: TestContext): Eq[Task[A]] =
    Eq.by(ta => IO.async[A](k => ta.unsafePerformAsync(e => k(e.toEither))))

  implicit def parallelTaskEq[A: Eq](implicit ctx: TestContext): Eq[ParallelTask[A]] =
    Tag.subst(taskEq[A])
}
