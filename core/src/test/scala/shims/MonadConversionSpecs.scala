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

import cats.Eq
import cats.laws.discipline._

import scalaz.\/
import scalaz.std.either._
import scalaz.std.option._
import scalaz.std.tuple._
import scalaz.std.anyVal._

import org.scalacheck._
import org.specs2.mutable._
import org.typelevel.discipline.specs2.mutable.Discipline

object MonadConversionSpecs extends Specification with Discipline {
  import Arbitrary.arbitrary

  "ifunctor" >> {
    cats.Invariant[Option]
    scalaz.InvariantFunctor[Option]

    "scalaz -> cats" >>
      checkAll("Option", InvariantTests[Option].invariant[Int, Int, Int])
  }

  /*"contravariant" >> {
    cats.Contravariant[???]
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

  "coflatmap" >> {
    import scalaz.\&/

    implicit def arbThese[A: Arbitrary, B: Arbitrary]: Arbitrary[A \&/ B] = {
      val g = for {
        a <- arbitrary[Option[A]]

        b <- if (a.isDefined)
          arbitrary[Option[B]]
        else
          arbitrary[B].map(Some(_))

        // we've defined things such that this is true, but keep the conditional anyway
        if a.isDefined || b.isDefined
      } yield {
        (a, b) match {
          case (Some(a), Some(b)) => \&/.Both(a, b)
          case (Some(a), None) => \&/.This(a)
          case (None, Some(b)) => \&/.That(b)
          case _ => ???
        }
      }

      Arbitrary(g)
    }

    implicit def cogenThese[A: Cogen, B: Cogen]: Cogen[A \&/ B] = Cogen { (s, t) =>
      t match {
        case \&/.Both(a, b) => Cogen.perturb(Cogen.perturb(s, a), b)
        case \&/.This(a) => Cogen.perturb(s, a)
        case \&/.That(b) => Cogen.perturb(s, b)
      }
    }

    cats.CoflatMap[Boolean \&/ ?]
    scalaz.Cobind[Boolean \&/ ?]

    "scalaz -> cats" >>
      checkAll("Boolean \\&/ ?", CoflatMapTests[Boolean \&/ ?].coflatMap[Int, Int, Int])
  }

  "comonad" >> {
    import scalaz.{NonEmptyList => NEL}

    implicit def arbNEL[A: Arbitrary]: Arbitrary[NEL[A]] = {
      val g = for {
        h <- arbitrary[A]
        t <- arbitrary[List[A]]
      } yield NEL(h, t: _*)

      Arbitrary(g)
    }

    implicit def cogenNEL[A: Cogen]: Cogen[NEL[A]] = Cogen { (s, nel) =>
      val s2 = Cogen.perturb(s, nel.head)
      Cogen.perturb(s2, nel.tail.toList)
    }

    cats.Comonad[NEL]
    scalaz.Cobind[NEL]

    "scalaz -> cats" >>
      checkAll("NonEmptyList", ComonadTests[NEL].comonad[Int, Int, Int])
  }

  "monad" >> {
    "scalaz -> cats" >> {
      "Option" >> {
        cats.Monad[Option]
        scalaz.Monad[Option]

        checkAll("Option", MonadTests[Option].monad[Int, Int, Int])
      }
    }
  }

  "monaderror" >> {
    "scalaz -> cats" >> {
      "Throwable \\/ ?" >> {
        implicit def arbEitherT[A: Arbitrary, B: Arbitrary]: Arbitrary[A \/ B] = {
          val genEitherT: Gen[A \/ B] =
            Arbitrary.arbitrary[Either[A, B]].map(\/.fromEither(_))

          Arbitrary(genEitherT)
        }

        implicit val eqThrowable: Eq[Throwable] = Eq.fromUniversalEquals[Throwable]

        cats.Monad[Throwable \/ ?]
        scalaz.Monad[Throwable \/ ?]

        /*
         * This is an interesting test btw because of where the Eq instances are coming from.
         * Namely, some are coming from scalaz, some are coming from cats, and some are coming
         * inductively from BOTH.  This approximates pretty closely a real-world inductive
         * coercion scenario.
         */
        checkAll("Option", MonadErrorTests[Throwable \/ ?, Throwable].monadError[Int, Int, Int])
      }
    }
  }
}
