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

import shims._

object ScalazExamples {
  import scalaz.Functor
  import scalaz.syntax.functor._

  def liftedToString[F[_]: Functor](fa: F[Int]): F[String] = fa.map(_.toString)

  liftedToString(Box(42))

  {
    import cats.instances.list._

    liftedToString(List(1, 2, 3))
  }
}

final case class Box[A](a: A)

object Box {
  import cats.Functor

  implicit val functor: Functor[Box] = new Functor[Box] {
    def map[A, B](ba: Box[A])(f: A => B) = Box(f(ba.a))
  }
}

object CatsExamples {
  import scalaz.{\/, \/-}

  def extractFromEither[A, B](e: Either[A, B]): Option[B] = e.toOption

  val example: Boolean \/ Int = \/-(42)
  extractFromEither(example.asCats)
}

object MixedExamples {
  import cats.Eval
  import scalaz.std.list._
  import cats.syntax.traverse._

  List(1, 2, 3, 4).traverse(i => Eval.later(i * 2))
}
