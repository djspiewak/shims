package shims

import cats.kernel.laws._
import scalaz.std.anyVal._

import org.specs2.mutable._
import org.typelevel.discipline.specs2.mutable.Discipline

object KernelConversionSpecs extends Specification with Discipline {

  "order conversion" >> {
    cats.Order[Int]
    scalaz.Order[Int]

    "scalaz -> cats" >> {
      checkAll("Int", OrderLaws[Int].eqv)
      checkAll("Int", OrderLaws[Int].order)
    }
  }

  "semigroup conversion" >> {
    cats.Semigroup[Int]
    scalaz.Semigroup[Int]

    "scalaz -> cats" >> checkAll("Int", GroupLaws[Int].semigroup)
  }

  "monoid conversion" >> {
    cats.Monoid[Int]
    scalaz.Monoid[Int]

    "scalaz -> cats" >> checkAll("Int", GroupLaws[Int].monoid)
  }
}
