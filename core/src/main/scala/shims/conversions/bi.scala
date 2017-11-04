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

package shims.conversions

import cats.Eval

import shims.util.Capture

trait BifunctorConversions {

  private[conversions] trait BifunctorShimS2C[F[_, _]] extends cats.Bifunctor[F] with Synthetic {
    val F: scalaz.Bifunctor[F]

    override def bimap[A, B, C, D](fab: F[A, B])(f: A => C, g: B => D): F[C, D] =
      F.bimap(fab)(f, g)
  }

  implicit def bifunctorToCats[F[_, _]](implicit FC: Capture[scalaz.Bifunctor[F]]): cats.Bifunctor[F] with Synthetic =
    new BifunctorShimS2C[F] { val F = FC.value }

  private[conversions] trait BifunctorShimC2S[F[_, _]] extends scalaz.Bifunctor[F] with Synthetic {
    val F: cats.Bifunctor[F]

    override def bimap[A, B, C, D](fab: F[A, B])(f: A => C, g: B => D): F[C, D] =
      F.bimap(fab)(f, g)
  }

  implicit def bifunctorToScalaz[F[_, _]](implicit FC: Capture[cats.Bifunctor[F]]): scalaz.Bifunctor[F] with Synthetic =
    new BifunctorShimC2S[F] { val F = FC.value }
}

trait BifoldableConversions extends MonoidConversions {

  private[conversions] trait BifoldableShimS2C[F[_, _]] extends cats.Bifoldable[F] with Synthetic {
    val F: scalaz.Bifoldable[F]

    override def bifoldLeft[A, B, C](fab: F[A, B], c: C)(f: (C, A) => C, g: (C, B) => C): C =
      F.bifoldLeft(fab, c)(f)(g)

    override def bifoldRight[A, B, C](fab: F[A, B], c: Eval[C])(f: (A, Eval[C]) => Eval[C], g: (B, Eval[C]) => Eval[C]): Eval[C] =
      F.bifoldRight(fab, c)((a, c) => f(a, c))((b, c) => g(b, c))
  }

  implicit def bifoldableToCats[F[_, _]](implicit FC: Capture[scalaz.Bifoldable[F]]): cats.Bifoldable[F] with Synthetic =
    new BifoldableShimS2C[F] { val F = FC.value }

  private[conversions] trait BifoldableShimC2S[F[_, _]] extends scalaz.Bifoldable[F] with Synthetic {
    val F: cats.Bifoldable[F]

    override def bifoldMap[A, B, M: scalaz.Monoid](fa: F[A, B])(f: A => M)(g: B => M): M =
      F.bifoldMap(fa)(f, g)(monoidToCats(Capture(scalaz.Monoid[M])))

    override def bifoldRight[A, B, C](fa: F[A, B], z: => C)(f: (A, => C) => C)(g: (B, => C) => C): C =
      F.bifoldRight(fa, Eval.always(z))((a, c) => c.map(f(a, _)), (b, c) => c.map(g(b, _))).value
  }

  implicit def bifoldableToScalaz[F[_, _]](implicit FC: Capture[cats.Bifoldable[F]]): scalaz.Bifoldable[F] with Synthetic =
    new BifoldableShimC2S[F] { val F = FC.value }
}

trait BitraverseConversions extends BifunctorConversions with BifoldableConversions with ApplicativeConversions {

  private[conversions] trait BitraverseShimS2C[F[_, _]] extends cats.Bitraverse[F] with BifunctorShimS2C[F] with BifoldableShimS2C[F] {
    val F: scalaz.Bitraverse[F]

    override def bitraverse[G[_]: cats.Applicative, A, B, C, D](fab: F[A, B])(f: A => G[C], g: B => G[D]): G[F[C, D]] =
      F.bitraverse(fab)(f)(g)(applicativeToScalaz(Capture(cats.Applicative[G])))
  }

  implicit def bitraverseToCats[F[_, _]](implicit FC: Capture[scalaz.Bitraverse[F]]): cats.Bitraverse[F] with Synthetic =
    new BitraverseShimS2C[F] { val F = FC.value }

  private[conversions] trait BitraverseShimC2S[F[_, _]] extends scalaz.Bitraverse[F] with BifunctorShimC2S[F] with BifoldableShimC2S[F] {
    val F: cats.Bitraverse[F]

    override def bitraverseImpl[G[_]: scalaz.Applicative, A, B, C, D](fab: F[A, B])(f: A => G[C], g: B => G[D]): G[F[C, D]] =
      F.bitraverse(fab)(f, g)(applicativeToCats(Capture(scalaz.Applicative[G])))
  }

  implicit def bitraverseToScalaz[F[_, _]](implicit FC: Capture[cats.Bitraverse[F]]): scalaz.Bitraverse[F] with Synthetic =
    new BitraverseShimC2S[F] { val F = FC.value }
}
