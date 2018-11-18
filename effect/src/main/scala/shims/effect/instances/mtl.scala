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

package shims.effect.instances

import cats.Monad
import cats.effect.{Async, CancelToken, Concurrent, ContextShift, ExitCase, Fiber, Sync}
import cats.syntax.all._

import scalaz.OptionT

import shims.AsSyntaxModule
import shims.conversions.{EitherConverters, MonadConversions, MonadErrorConversions}

import scala.concurrent.ExecutionContext

trait MTLSync extends MonadErrorConversions with EitherConverters with AsSyntaxModule {

  implicit def scalazOptionTSync[F[_]: Sync]: Sync[OptionT[F, ?]] =
    new OptionTSync[F] { def F = Sync[F] }

  protected[this] trait OptionTSync[F[_]] extends Sync[OptionT[F, ?]] {
    protected implicit def F: Sync[F]

    def pure[A](x: A): OptionT[F, A] =
      OptionT.optionTMonadPlus[F].point(x)

    def handleErrorWith[A](fa: OptionT[F, A])(f: Throwable => OptionT[F, A]): OptionT[F, A] =
      OptionT.optionTMonadError[F, Throwable].handleError(fa)(f)

    def raiseError[A](e: Throwable): OptionT[F, A] =
      OptionT.optionTMonadError[F, Throwable].raiseError(e)

    def bracketCase[A, B](
        acquire: OptionT[F, A])(
        use: A => OptionT[F, B])(
        release: (A, ExitCase[Throwable]) => OptionT[F, Unit])
        : OptionT[F, B] = {

      OptionT(F.bracketCase(acquire.run) {
        case Some(a) => use(a).run
        case None => F.pure[Option[B]](None)
      } {
        case (Some(a), br) => release(a, br).run.map(_ => ())
        case _ => F.unit
      })
    }

    def flatMap[A, B](fa: OptionT[F, A])(f: A => OptionT[F, B]): OptionT[F, B] =
      fa.flatMap(f)

    def tailRecM[A, B](a: A)(f: A => OptionT[F, Either[A, B]]): OptionT[F, B] =
      OptionT.optionTBindRec[F].tailrecM[A, B](a => f(a).map(_.asScalaz))(a)

    def suspend[A](thunk: => OptionT[F, A]): OptionT[F, A] =
      OptionT(F.suspend(thunk.run))
  }
}

trait MTLAsync extends MTLSync {

  implicit def scalazOptionTAsync[F[_]: Async]: Async[OptionT[F, ?]] =
    new OptionTAsync[F] { def F = Async[F] }

  protected[this] trait OptionTAsync[F[_]] extends OptionTSync[F] with Async[OptionT[F, ?]] {
    protected implicit def F: Async[F]

    def asyncF[A](k: (Either[Throwable, A] => Unit) => OptionT[F, Unit]): OptionT[F, A] =
      OptionT.optionTMonadTrans.liftM(F.asyncF((cb: Either[Throwable, A] => Unit) => F.as(k(cb).run, ())))

    def async[A](k: (Either[Throwable, A] => Unit) => Unit): OptionT[F, A] =
      OptionT.optionTMonadTrans.liftM(F.async(k))
  }
}

trait MTLEffect extends MTLAsync

trait MTLConcurrent extends MTLAsync {

  implicit def scalazOptionTConcurrent[F[_]: Concurrent]: Concurrent[OptionT[F, ?]] =
    new OptionTConcurrent[F] { def F = Concurrent[F] }

  protected[this] trait OptionTConcurrent[F[_]] extends OptionTAsync[F] with Concurrent[OptionT[F, ?]] {
    protected implicit def F: Concurrent[F]

    override def cancelable[A](
        k: (Either[Throwable, A] => Unit) => CancelToken[OptionT[F, ?]])
        : OptionT[F, A] =
      OptionT.optionTMonadTrans.liftM(F.cancelable(k.andThen(_.run.void)))

    override def start[A](fa: OptionT[F, A]) =
      OptionT.optionTMonadTrans.liftM(F.start(fa.run).map(fiberT))

    override def racePair[A, B](
        fa: OptionT[F, A],
        fb: OptionT[F, B])
        : OptionT[F, Either[(A, Fiber[OptionT[F, ?], B]), (Fiber[OptionT[F, ?], A], B)]] = {
      OptionT(F.racePair(fa.run, fb.run) flatMap {
        case Left((None, fiberB)) =>
          fiberB.cancel.map(_ => None)

        case Left((Some(r), fiberB)) =>
          F.pure(Some(Left((r, fiberT[B](fiberB)))))

        case Right((fiberA, None)) =>
          fiberA.cancel.map(_ => None)

        case Right((fiberA, Some(r))) =>
          F.pure(Some(Right((fiberT[A](fiberA), r))))
      })
    }

    protected def fiberT[A](fiber: Fiber[F, Option[A]]): Fiber[OptionT[F, ?], A] =
      Fiber(OptionT(fiber.join), OptionT.optionTMonadTrans.liftM(fiber.cancel))
  }
}

trait MTLConcurrentEffect extends MTLEffect with MTLConcurrent

trait MTLContextShift extends MonadConversions {

  implicit def scalazOptionTContextShift[F[_]: Monad](
      implicit cs: ContextShift[F])
      : ContextShift[OptionT[F, ?]] = {
    new ContextShift[OptionT[F, ?]] {
      def shift: OptionT[F, Unit] =
        OptionT.optionTMonadTrans.liftM(cs.shift)

      def evalOn[A](ec: ExecutionContext)(fa: OptionT[F, A]): OptionT[F, A] =
        OptionT(cs.evalOn(ec)(fa.run))
    }
  }
}
