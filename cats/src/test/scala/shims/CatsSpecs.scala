package shims

import shims.cats._

import _root_.cats.{Applicative => CApplicative, FlatMap => CFlatMap, Functor => CFunctor, Monad => CMonad, Traverse => CTraverse}
import _root_.cats.std.option._

import org.specs2.mutable._

object CatsSpecs extends Specification {

  "functors" >> {
    "forward" >> {
      Functor[Option]

      ok
    }

    "backward" >> {
      import shims.util._

      implicit val ev: Functor.Aux[List, Unit] = new Functor[List] {
        type Tag = Unit

        def map[A, B](xs: List[A])(f: A => B) = xs map f
      }

      CFunctor[List]

      ok
    }

    "remain unambiguous" >> {
      CFunctor[Option]

      ok
    }
  }
}