package shims

import cats.laws.discipline._
import scalaz.std.option._
import scalaz.std.anyVal._

import org.specs2.mutable._
import org.typelevel.discipline.specs2.mutable.Discipline

object ConversionSpecs extends Specification with Discipline {

  "functor conversion" >> {
    cats.Functor[Option]
    scalaz.Functor[Option]

    "cats -> scalaz" >> checkAll("Option", FunctorTests[Option].functor[Int, Int, Int])
  }
}
