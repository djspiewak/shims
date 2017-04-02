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

  "ifunctor" >> {
    cats.functor.Invariant[Option]
    scalaz.InvariantFunctor[Option]

    "scalaz -> cats" >>
      checkAll("Option", InvariantTests[Option].invariant[Int, Int, Int])
  }

  /*"contravariant" >> {
    cats.functor.Contravariant[???]
    scalaz.Contravariant[???]

    "scalaz -> cats" >> ok
  }*/

  "functor" >> {
    cats.Functor[Option]
    scalaz.Functor[Option]

    "scalaz -> cats" >>
      checkAll("Option", FunctorTests[Option].functor[Int, Int, Int])
  }

  "apply" >> {
    cats.Apply[Option]
    scalaz.Apply[Option]

    "scalaz -> cats" >>
      checkAll("Option", ApplyTests[Option].apply[Int, Int, Int])
  }

  "applicative" >> {
    cats.Applicative[Option]
    scalaz.Applicative[Option]

    "scalaz -> cats" >>
      checkAll("Option", ApplicativeTests[Option].applicative[Int, Int, Int])
  }

  "foldable" >> {
    "scalaz -> cats" >> {
      "Option" >> {
        cats.Foldable[Option]
        scalaz.Foldable[Option]

        ok
      }

      "List" >> {
        import scalaz.std.list._

        cats.Foldable[List]
        scalaz.Foldable[List]

        ok
      }
    }
  }

  "traverse" >> {
    import scalaz.std.list._

    cats.Traverse[Option]
    scalaz.Traverse[Option]

    "scalaz -> cats" >>
      checkAll("Option", TraverseTests[Option].traverse[Int, Int, Int, Int, List, Option])
  }

  "monad" >> {
    "scalaz -> cats" >> {
      "Option" >> {
        cats.Monad[Option]
        scalaz.Monad[Option]

        checkAll("Option", MonadTests[Option].monad[Int, Int, Int])
      }

      "Free[Function0, ?]" >> {
        import scalaz.Free

        trait Foo[A]

        cats.Monad[Free[Foo, ?]]
        scalaz.Monad[Free[Foo, ?]]

        ok   // TODO
      }
    }
  }
}
