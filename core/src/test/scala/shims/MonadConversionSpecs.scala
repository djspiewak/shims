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

    "scalaz -> cats" >> checkAll("Option", FunctorTests[Option].functor[Int, Int, Int])
  }

  "applicative conversion" >> {
    cats.Applicative[Option]
    scalaz.Applicative[Option]

    "scalaz -> cats" >> checkAll("Option", ApplicativeTests[Option].applicative[Int, Int, Int])
  }

  "monad conversion" >> {
    cats.Monad[Option]
    scalaz.Monad[Option]

    "scalaz -> cats" >> checkAll("Option", MonadTests[Option].monad[Int, Int, Int])
  }
}
