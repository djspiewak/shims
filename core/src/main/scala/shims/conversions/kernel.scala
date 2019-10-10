/*
 * Copyright 2019 Daniel Spiewak
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

package shims.conversions

import shims.util.Capture

trait EqConversions {

  private[conversions] trait EqShimS2C[A] extends cats.kernel.Eq[A] with Synthetic {
    val A: scalaz.Equal[A]

    override def eqv(x: A, y: A): Boolean = A.equal(x, y)
  }

  implicit def equalToCats[A](implicit AC: Capture[scalaz.Equal[A]]): cats.kernel.Eq[A] with Synthetic =
    new EqShimS2C[A] { val A = AC.value }

  private[conversions] trait EqShimC2S[A] extends scalaz.Equal[A] with Synthetic {
    val A: cats.kernel.Eq[A]

    override def equal(x: A, y: A): Boolean = A.eqv(x, y)
  }

  implicit def eqToScalaz[A](implicit AC: Capture[cats.kernel.Eq[A]]): scalaz.Equal[A] with Synthetic =
    new EqShimC2S[A] { val A = AC.value }
}

trait OrderConversions extends EqConversions {

  private[conversions] trait OrderShimS2C[A] extends cats.kernel.Order[A] with EqShimS2C[A] {
    val A: scalaz.Order[A]

    override def compare(x: A, y: A): Int = A.order(x, y).toInt
  }

  implicit def orderToCats[A](implicit AC: Capture[scalaz.Order[A]]): cats.kernel.Order[A] with Synthetic =
    new OrderShimS2C[A] { val A = AC.value }

  private[conversions] trait OrderShimC2S[A] extends scalaz.Order[A] with EqShimC2S[A] {
    val A: cats.kernel.Order[A]

    override def order(x: A, y: A): scalaz.Ordering = scalaz.Ordering.fromInt(A.compare(x, y))
  }

  implicit def orderToScalaz[A](implicit AC: Capture[cats.kernel.Order[A]]): scalaz.Order[A] with Synthetic =
    new OrderShimC2S[A] { val A = AC.value }
}

trait SemigroupConversions extends OrderConversions {

  private[conversions] trait SemigroupShimS2C[A] extends cats.Semigroup[A] with Synthetic {
    val A: scalaz.Semigroup[A]

    override def combine(x: A, y: A): A = A.append(x, y)
  }

  implicit def semigroupToCats[A](implicit FC: Capture[scalaz.Semigroup[A]]): cats.Semigroup[A] with Synthetic =
    new SemigroupShimS2C[A] { val A = FC.value }

  private[conversions] trait SemigroupShimC2S[A] extends scalaz.Semigroup[A] with Synthetic {
    val A: cats.Semigroup[A]

    override def append(f1: A, f2: => A): A = A.combine(f1, f2)
  }

  implicit def semigroupToScalaz[A](implicit FC: Capture[cats.Semigroup[A]]): scalaz.Semigroup[A] with Synthetic =
    new SemigroupShimC2S[A] { val A = FC.value }
}

trait MonoidConversions extends SemigroupConversions {

  private[conversions] trait MonoidShimS2C[A] extends cats.Monoid[A] with SemigroupShimS2C[A] {
    val A: scalaz.Monoid[A]

    override def empty: A = A.zero
  }

  implicit def monoidToCats[A](implicit FC: Capture[scalaz.Monoid[A]]): cats.Monoid[A] with Synthetic =
    new MonoidShimS2C[A] { val A = FC.value }

  private[conversions] trait MonoidShimC2S[A] extends scalaz.Monoid[A] with SemigroupShimC2S[A] {
    val A: cats.Monoid[A]

    override def zero: A = A.empty
  }

  implicit def monoidToScalaz[A](implicit FC: Capture[cats.Monoid[A]]): scalaz.Monoid[A] with Synthetic =
    new MonoidShimC2S[A] { val A = FC.value }
}

trait SemigroupKConversions {

  private[conversions] trait SemigroupKShimS2C[F[_]] extends cats.SemigroupK[F] with Synthetic {
    val F: scalaz.Plus[F]

    def combineK[A](x: F[A], y: F[A]): F[A] = F.plus(x, y)
  }

  implicit def plusToCats[F[_]](implicit FC: Capture[scalaz.Plus[F]]): cats.SemigroupK[F] with Synthetic =
    new SemigroupKShimS2C[F] { val F = FC.value }

  private[conversions] trait PlusShimC2S[F[_]] extends scalaz.Plus[F] with Synthetic {
    val F: cats.SemigroupK[F]

    def plus[A](a: F[A], b: => F[A]): F[A] = F.combineK(a, b)
  }

  implicit def semigroupKToScalaz[F[_]](implicit FC: Capture[cats.SemigroupK[F]]): scalaz.Plus[F] with Synthetic =
    new PlusShimC2S[F] { val F = FC.value }
}

trait MonoidKConversions extends SemigroupKConversions {

  private[conversions] trait MonoidKShimS2C[F[_]] extends cats.MonoidK[F] with SemigroupKShimS2C[F] {
    val F: scalaz.PlusEmpty[F]

    def empty[A]: F[A] = F.empty[A]
  }

  implicit def plusEmptyToCats[F[_]](implicit FC: Capture[scalaz.PlusEmpty[F]]): cats.MonoidK[F] with Synthetic =
    new MonoidKShimS2C[F] { val F = FC.value }

  private[conversions] trait PlusEmptyShimC2S[F[_]] extends scalaz.PlusEmpty[F] with PlusShimC2S[F] {
    val F: cats.MonoidK[F]

    def empty[A]: F[A] = F.empty[A]
  }

  implicit def monoidKToScalaz[F[_]](implicit FC: Capture[cats.MonoidK[F]]): scalaz.PlusEmpty[F] with Synthetic =
    new PlusEmptyShimC2S[F] { val F = FC.value }
}

// "kernel" is such an ill-defined thing...
trait ShowConversions {

  private[conversions] trait ShowShimS2C[A] extends cats.Show[A] with Synthetic {
    val A: scalaz.Show[A]

    override def show(a: A): String = A.shows(a)
  }

  implicit def showToCats[A](implicit FC: Capture[scalaz.Show[A]]): cats.Show[A] with Synthetic =
    new ShowShimS2C[A] { val A = FC.value }

  private[conversions] trait ShowShimC2S[A] extends scalaz.Show[A] with Synthetic {
    val A: cats.Show[A]

    override def shows(a: A): String = A.show(a)
  }

  implicit def showToScalaz[A](implicit FC: Capture[cats.Show[A]]): scalaz.Show[A] with Synthetic =
    new ShowShimC2S[A] { val A = FC.value }
}
