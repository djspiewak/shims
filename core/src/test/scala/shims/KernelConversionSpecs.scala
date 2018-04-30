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

package shims

import cats.kernel.laws.discipline._
import scalaz.std.anyVal._
import cats.instances.option._

import org.specs2.mutable._
import org.typelevel.discipline.specs2.mutable.Discipline

object KernelConversionSpecs extends Specification with Discipline {

  "eq conversion" >> {
    cats.Eq[Int]
    scalaz.Equal[Int]

    "scalaz -> cats" >> {
      checkAll("Int", EqTests[Int].eqv)
    }
  }

  "order conversion" >> {
    cats.Order[Int]
    scalaz.Order[Int]

    "scalaz -> cats" >> {
      checkAll("Int", OrderTests[Int].order)
    }
  }

  "semigroup conversion" >> {
    cats.Semigroup[Int]
    scalaz.Semigroup[Int]

    "scalaz -> cats" >> checkAll("Int", SemigroupTests[Int].semigroup)
  }

  "monoid conversion" >> {
    cats.Monoid[Int]
    scalaz.Monoid[Int]

    "scalaz -> cats" >> checkAll("Int", MonoidTests[Int].monoid)
  }

  "show conversion" >> {
    import cats.syntax.show._

    cats.Show[Int]
    scalaz.Show[Int]

    "scalaz -> cats" >> {
      show"testing ${42}" mustEqual "testing 42"
    }
  }
}
