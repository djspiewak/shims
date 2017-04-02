package shims.conversions

import shims.util.{</<, Capture}

trait EqConversions {

  private[conversions] trait EqShimS2C[A] extends cats.kernel.Eq[A] with Synthetic {
    val A: scalaz.Equal[A]

    override def eqv(x: A, y: A): Boolean = A.equal(x, y)
  }

  implicit def equalToCats[A, T](implicit AC: Capture[scalaz.Equal[A], T], ev: T </< Synthetic): cats.kernel.Eq[A] with Synthetic =
    new EqShimS2C[A] { val A = AC.value }

  private[conversions] trait EqShimC2S[A] extends scalaz.Equal[A] with Synthetic {
    val A: cats.kernel.Eq[A]

    override def equal(x: A, y: A): Boolean = A.eqv(x, y)
  }

  implicit def eqToScalaz[A, T](implicit AC: Capture[cats.kernel.Eq[A], T], ev: T </< Synthetic): scalaz.Equal[A] with Synthetic =
    new EqShimC2S[A] { val A = AC.value }
}

trait OrderConversions extends EqConversions {

  private[conversions] trait OrderShimS2C[A] extends cats.kernel.Order[A] with EqShimS2C[A] {
    val A: scalaz.Order[A]

    override def compare(x: A, y: A): Int = A.order(x, y).toInt
  }

  implicit def orderToCats[A, T](implicit AC: Capture[scalaz.Order[A], T], ev: T </< Synthetic): cats.kernel.Order[A] with Synthetic =
    new OrderShimS2C[A] { val A = AC.value }

  private[conversions] trait OrderShimC2S[A] extends scalaz.Order[A] with EqShimC2S[A] {
    val A: cats.kernel.Order[A]

    override def order(x: A, y: A): scalaz.Ordering = scalaz.Ordering.fromInt(A.compare(x, y))
  }

  implicit def orderToScalaz[A, T](implicit AC: Capture[cats.kernel.Order[A], T], ev: T </< Synthetic): scalaz.Order[A] with Synthetic =
    new OrderShimC2S[A] { val A = AC.value }
}

trait SemigroupConversions {

  private[conversions] trait SemigroupShimS2C[A] extends cats.Semigroup[A] with Synthetic {
    val A: scalaz.Semigroup[A]

    override def combine(x: A, y: A): A = A.append(x, y)
  }

  implicit def semigroupToCats[A, T](implicit FC: Capture[scalaz.Semigroup[A], T], ev: T </< Synthetic): cats.Semigroup[A] with Synthetic =
    new SemigroupShimS2C[A] { val A = FC.value }

  private[conversions] trait SemigroupShimC2S[A] extends scalaz.Semigroup[A] with Synthetic {
    val A: cats.Semigroup[A]

    override def append(f1: A, f2: => A): A = A.combine(f1, f2)
  }

  implicit def semigroupToScalaz[A, T](implicit FC: Capture[cats.Semigroup[A], T], ev: T </< Synthetic): scalaz.Semigroup[A] with Synthetic =
    new SemigroupShimC2S[A] { val A = FC.value }
}

trait MonoidConversions extends SemigroupConversions {

  private[conversions] trait MonoidShimS2C[A] extends cats.Monoid[A] with SemigroupShimS2C[A] {
    val A: scalaz.Monoid[A]

    override def empty: A = A.zero
  }

  implicit def monoidToCats[A, T](implicit FC: Capture[scalaz.Monoid[A], T], ev: T </< Synthetic): cats.Monoid[A] with Synthetic =
    new MonoidShimS2C[A] { val A = FC.value }

  private[conversions] trait MonoidShimC2S[A] extends scalaz.Monoid[A] with SemigroupShimC2S[A] {
    val A: cats.Monoid[A]

    override def zero: A = A.empty
  }

  implicit def monoidToScalaz[A, T](implicit FC: Capture[cats.Monoid[A], T], ev: T </< Synthetic): scalaz.Monoid[A] with Synthetic =
    new MonoidShimC2S[A] { val A = FC.value }
}
