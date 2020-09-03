/*
 * Copyright 2020 Daniel Spiewak
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
import cats.effect.laws.discipline.SyncTests
import cats.effect.laws.util.TestInstances.eqThrowable

import scalaz.effect.IO

import org.specs2.Specification
import org.typelevel.discipline.specs2.Discipline

object IOInstancesSpecs extends Specification with Discipline {
  import IOArbitrary._

  def is = checkAll("scalaz.effect.IO", SyncTests[IO].sync[Int, Int, Int])

  implicit def ioEq[A: Eq]: Eq[IO[A]] = Eq.by(_.catchLeft.unsafePerformIO())
}
