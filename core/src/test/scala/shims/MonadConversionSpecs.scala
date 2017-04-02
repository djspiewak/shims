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

package shims

import cats.laws.discipline._
import scalaz.std.option._
import scalaz.std.tuple._
import scalaz.std.anyVal._

import org.specs2.mutable._
import org.typelevel.discipline.specs2.mutable.Discipline

object MonadConversionSpecs extends Specification with Discipline {

  "functor conversion" >> {
    cats.Functor[Option]
    scalaz.Functor[Option]

    "scalaz -> cats" >>
      checkAll("Option", FunctorTests[Option].functor[Int, Int, Int])
  }

  "applicative conversion" >> {
    cats.Applicative[Option]
    scalaz.Applicative[Option]

    "scalaz -> cats" >>
      checkAll("Option", ApplicativeTests[Option].applicative[Int, Int, Int])
  }

  "monad conversion" >> {
    "Option" >> {
      cats.Monad[Option]
      scalaz.Monad[Option]

      "scalaz -> cats" >>
        checkAll("Option", MonadTests[Option].monad[Int, Int, Int])
    }

    "Free[Function0, ?]" >> {
      import scalaz.Free

      trait Foo[A]

      cats.Monad[Free[Foo, ?]]
      scalaz.Monad[Free[Foo, ?]]

      "scalaz -> cats" >> ok   // TODO
    }
  }
}