/*
 * Copyright 2020 Daniel Spiewak
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

import cats.{Functor, Monad, Monoid}
import cats.effect.{
  Async,
  Bracket,
  CancelToken,
  Concurrent,
  ConcurrentEffect,
  ContextShift,
  Effect,
  ExitCase,
  Fiber,
  IO,
  LiftIO,
  Sync,
  SyncIO
}
import cats.syntax.all._

import scalaz.{\/, -\/, \/-, EitherT, IndexedStateT, Kleisli, OptionT, StateT, WriterT}

import shims.AsSyntaxModule
import shims.conversions.{EitherConverters, MonadConversions, MonadErrorConversions}

import scala.concurrent.ExecutionContext

trait MTLBracket
    extends MonadErrorConversions
    with EitherConverters
    with AsSyntaxModule {

  implicit def scalazKleisliBracket[F[_]: Bracket[?[_], E], R, E]: Bracket[Kleisli[F, R, ?], E] =
    new KleisliBracket[F, R, E] { def F = Bracket[F, E] }

  protected[this] trait KleisliBracket[F[_], R, E] extends Bracket[Kleisli[F, R, ?], E] {
    protected implicit def F: Bracket[F, E]

    def pure[A](x: A): Kleisli[F, R, A] =
      Kleisli.kleisliApplicative[F, R].point(x)

    def handleErrorWith[A](fa: Kleisli[F, R, A])(f: E => Kleisli[F, R, A]): Kleisli[F, R, A] =
      Kleisli.kleisliMonadError[F, E, R].handleError(fa)(f)

    def raiseError[A](e: E): Kleisli[F, R, A] =
      Kleisli.kleisliMonadError[F, E, R].raiseError(e)

    def bracketCase[A, B](
        acquire: Kleisli[F, R, A])(
        use: A => Kleisli[F, R, B])(
        release: (A, ExitCase[E]) => Kleisli[F, R, Unit])
        : Kleisli[F, R, B] = {

      Kleisli { r =>
        F.bracketCase(acquire.run(r))(a => use(a).run(r)) { (a, br) =>
          release(a, br).run(r)
        }
      }
    }

    def flatMap[A, B](fa: Kleisli[F, R, A])(f: A => Kleisli[F, R, B]): Kleisli[F, R, B] =
      fa.flatMap(f)

    def tailRecM[A, B](a: A)(f: A => Kleisli[F, R, Either[A, B]]): Kleisli[F, R, B] =
      Kleisli.kleisliBindRec[F, R].tailrecM(a)(f.andThen(_.map(_.asScalaz)))
  }
}

trait MTLSync extends MTLBracket {

  implicit def scalazOptionTSync[F[_]: Sync]: Sync[OptionT[F, ?]] =
    new OptionTSync[F] { def F = Sync[F] }

  implicit def scalazKleisliSync[F[_]: Sync, R]: Sync[Kleisli[F, R, ?]] =
    new KleisliSync[F, R] { def F = Sync[F] }

  implicit def scalazEitherTSync[F[_]: Sync, L]: Sync[EitherT[F, L, ?]] =
    new EitherTSync[F, L] { def F = Sync[F] }

  implicit def scalazStateTSync[F[_]: Sync, S]: Sync[StateT[F, S, ?]] =
    new StateTSync[F, S] { def F = Sync[F] }

  implicit def scalazWriterTSync[F[_]: Sync, L: Monoid]: Sync[WriterT[F, L, ?]] =
    new WriterTSync[F, L] { def F = Sync[F]; def L = Monoid[L] }

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
      OptionT.optionTBindRec[F].tailrecM[A, B](a)(a => f(a).map(_.asScalaz))

    def suspend[A](thunk: => OptionT[F, A]): OptionT[F, A] =
      OptionT(F.suspend(thunk.run))

    override def uncancelable[A](fa: OptionT[F, A]): OptionT[F, A] =
      OptionT(F.uncancelable(fa.run))
  }

  protected[this] trait KleisliSync[F[_], R]
      extends KleisliBracket[F, R, Throwable]
      with Sync[Kleisli[F, R, ?]] {

    protected implicit def F: Sync[F]

    override def handleErrorWith[A](fa: Kleisli[F, R, A])(f: Throwable => Kleisli[F, R, A]): Kleisli[F, R, A] =
      Kleisli(r => F.suspend(F.handleErrorWith(fa.run(r))(e => f(e).run(r))))

    override def flatMap[A, B](fa: Kleisli[F, R, A])(f: A => Kleisli[F, R, B]): Kleisli[F, R, B] =
      Kleisli(r => F.suspend(fa.run(r).flatMap(f.andThen(_.run(r)))))

    def suspend[A](thunk: => Kleisli[F, R, A]): Kleisli[F, R, A] =
      Kleisli(r => F.suspend(thunk.run(r)))

    override def uncancelable[A](fa: Kleisli[F, R, A]): Kleisli[F, R, A] =
      Kleisli(r => F.suspend(F.uncancelable(fa.run(r))))
  }

  protected[this] trait EitherTSync[F[_], L] extends Sync[EitherT[F, L, ?]] {
    protected implicit def F: Sync[F]

    def pure[A](x: A): EitherT[L, F, A] = EitherT.pure[F, L, A](x)

    def handleErrorWith[A](fa: EitherT[L, F, A])(f: Throwable => EitherT[L, F, A]): EitherT[L, F, A] =
      EitherT.eitherT(F.handleErrorWith(fa.run)(f.andThen(_.run)))

    def raiseError[A](e: Throwable): EitherT[L, F, A] =
      EitherT.rightT(F.raiseError(e))

    def bracketCase[A, B](
        acquire: EitherT[L, F, A])(
        use: A => EitherT[L, F, B])(
        release: (A, ExitCase[Throwable]) => EitherT[L, F, Unit])
        : EitherT[L, F, B] = {

      EitherT(F.bracketCase(acquire.run) {
        case \/-(a) => use(a).run
        case e @ -\/(_) => F.pure(e: \/[L, B])
      } { (ea, br) =>
        ea match {
          case \/-(a) => release(a, br).run.void
          case -\/(_) => F.unit // nothing to release
        }
      })
    }

    def flatMap[A, B](fa: EitherT[L, F, A])(f: A => EitherT[L, F, B]): EitherT[L, F, B] =
      fa.flatMap(f)

    def tailRecM[A, B](a: A)(f: A => EitherT[L, F, Either[A, B]]): EitherT[L, F, B] =
      EitherT.eitherTBindRec[F, L].tailrecM(a)(f.andThen(_.map(_.asScalaz)))

    def suspend[A](thunk: => EitherT[L, F, A]): EitherT[L, F, A] =
      EitherT.eitherT(F.suspend(thunk.run))

    override def uncancelable[A](fa: EitherT[L, F, A]): EitherT[L, F, A] =
      EitherT.eitherT(F.uncancelable(fa.run))
  }

  protected[this] trait StateTSync[F[_], S] extends Sync[StateT[F, S, ?]] {
    protected implicit def F: Sync[F]

    def pure[A](x: A): StateT[S, F, A] = StateT.stateT(x)

    def handleErrorWith[A](fa: StateT[S, F, A])(f: Throwable => StateT[S, F, A]): StateT[S, F, A] =
      StateT(s => F.handleErrorWith(fa.run(s))(e => f(e).run(s)))

    def raiseError[A](e: Throwable): StateT[S, F, A] =
      StateT.liftM(F.raiseError(e))

    def bracketCase[A, B](acquire: StateT[S, F, A])
      (use: A => StateT[S, F, B])
      (release: (A, ExitCase[Throwable]) => StateT[S, F, Unit]): StateT[S, F, B] = {

      StateT { startS =>
        F.bracketCase(acquire.run(startS)) { case (s, a) =>
          use(a).run(s)
        } { case ((s, a), br) =>
          release(a, br).run(s).void
        }
      }
    }

    override def uncancelable[A](fa: StateT[S, F, A]): StateT[S, F, A] =
      fa.mapT(F.uncancelable)

    def flatMap[A, B](fa: StateT[S, F, A])(f: A => StateT[S, F, B]): StateT[S, F, B] =
      fa.flatMap(f)

    // overwriting the pre-existing one, since flatMap is guaranteed stack-safe
    def tailRecM[A, B](a: A)(f: A => StateT[S, F, Either[A, B]]): StateT[S, F, B] =
      IndexedStateT.stateTBindRec[S, F].tailrecM(a)(f.andThen(_.map(_.asScalaz)))

    def suspend[A](thunk: => StateT[S, F, A]): StateT[S, F, A] =
      StateT(s => F.suspend(thunk.run(s)))
  }

  protected[this] trait WriterTSync[F[_], L] extends Sync[WriterT[F, L, ?]] {
    protected implicit def F: Sync[F]
    protected implicit def L: Monoid[L]

    def pure[A](x: A): WriterT[L, F, A] =
      WriterT.put(F.pure(x))(L.empty)

    def handleErrorWith[A](fa: WriterT[L, F, A])(f: Throwable => WriterT[L, F, A]): WriterT[L, F, A] =
      WriterT.writerTMonadError[F, Throwable, L].handleError(fa)(f)

    def raiseError[A](e: Throwable): WriterT[L, F, A] =
      WriterT.writerTMonadError[F, Throwable, L].raiseError(e)

    def bracketCase[A, B](acquire: WriterT[L, F, A])
      (use: A => WriterT[L, F, B])
      (release: (A, ExitCase[Throwable]) => WriterT[L, F, Unit]): WriterT[L, F, B] = {

      uncancelable(acquire).flatMap { a =>
        WriterT(
          F.bracketCase(F.pure(a))(use.andThen(_.run)){ (a, res) =>
            release(a, res).value
          }
        )
      }
    }

    override def uncancelable[A](fa: WriterT[L, F, A]): WriterT[L, F, A] =
      WriterT(F.uncancelable(fa.run))

    def flatMap[A, B](fa: WriterT[L, F, A])(f: A => WriterT[L, F, B]): WriterT[L, F, B] =
      fa.flatMap(f)

    def tailRecM[A, B](a: A)(f: A => WriterT[L, F, Either[A, B]]): WriterT[L, F, B] =
      WriterT.writerTBindRec[F, L].tailrecM(a)(f.andThen(_.map(_.asScalaz)))

    def suspend[A](thunk: => WriterT[L, F, A]): WriterT[L, F, A] =
      WriterT(F.suspend(thunk.run))
  }
}

trait MTLLiftIO extends MonadConversions {

  implicit def scalazOptionTLiftIO[F[_]: LiftIO: Functor]: LiftIO[OptionT[F, ?]] =
    new OptionTLiftIO[F] { def F = LiftIO[F]; def FF = Functor[F] }

  implicit def scalazKleisliLiftIO[F[_]: LiftIO, R]: LiftIO[Kleisli[F, R, ?]] =
    new KleisliLiftIO[F, R] { def F = LiftIO[F] }

  implicit def scalazEitherTLiftIO[F[_]: LiftIO: Functor, L]: LiftIO[EitherT[F, L, ?]] =
    new EitherTLiftIO[F, L] { def F = LiftIO[F]; def FF = Functor[F] }

  implicit def scalazStateTLiftIO[F[_]: LiftIO: Monad, S]: LiftIO[StateT[F, S, ?]] =
    new StateTLiftIO[F, S] { def F = LiftIO[F]; def FF = Monad[F] }

  implicit def scalazWriterTLiftIO[F[_]: LiftIO: Functor, L: Monoid]: LiftIO[WriterT[F, L, ?]] =
    new WriterTLiftIO[F, L] { def F = LiftIO[F]; def L = Monoid[L]; def FF = Functor[F] }

  protected[this] trait OptionTLiftIO[F[_]] extends LiftIO[OptionT[F, ?]] {
    protected implicit def F: LiftIO[F]
    protected implicit def FF: Functor[F]

    def liftIO[A](ioa: IO[A]): OptionT[F, A] =
      OptionT(F.liftIO(ioa).map(Option(_)))
  }

  protected[this] trait KleisliLiftIO[F[_], R] extends LiftIO[Kleisli[F, R, ?]] {
    protected implicit def F: LiftIO[F]

    def liftIO[A](ioa: IO[A]): Kleisli[F, R, A] =
      Kleisli(_ => F.liftIO(ioa))
  }

  protected[this] trait EitherTLiftIO[F[_], L] extends LiftIO[EitherT[F, L, ?]] {
    protected implicit def F: LiftIO[F]
    protected implicit def FF: Functor[F]

    def liftIO[A](ioa: IO[A]): EitherT[L, F, A] =
      EitherT.rightT(F.liftIO(ioa))
  }

  protected[this] trait StateTLiftIO[F[_], S] extends LiftIO[StateT[F, S, ?]] {
    protected implicit def F: LiftIO[F]
    protected implicit def FF: Monad[F]

    def liftIO[A](ioa: IO[A]): StateT[S, F, A] =
      StateT.liftM(F.liftIO(ioa))
  }

  protected[this] trait WriterTLiftIO[F[_], L] extends LiftIO[WriterT[F, L, ?]] {
    protected implicit def F: LiftIO[F]
    protected implicit def FF: Functor[F]
    protected implicit def L: Monoid[L]

    def liftIO[A](ioa: IO[A]): WriterT[L, F, A] =
      WriterT.put(F.liftIO(ioa))(L.empty)
  }
}

trait MTLAsync extends MTLSync with MTLLiftIO {

  implicit def scalazOptionTAsync[F[_]: Async]: Async[OptionT[F, ?]] =
    new OptionTAsync[F] { def F = Async[F] }

  implicit def scalazKleisliAsync[F[_]: Async, R]: Async[Kleisli[F, R, ?]] =
    new KleisliAsync[F, R] { def F = Async[F] }

  implicit def scalazEitherTAsync[F[_]: Async, L]: Async[EitherT[F, L, ?]] =
    new EitherTAsync[F, L] { def F = Async[F] }

  implicit def scalazStateTAsync[F[_]: Async, S]: Async[StateT[F, S, ?]] =
    new StateTAsync[F, S] { def F = Async[F] }

  implicit def scalazWriterTAsync[F[_]: Async, L: Monoid]: Async[WriterT[F, L, ?]] =
    new WriterTAsync[F, L] { def F = Async[F]; def L = Monoid[L] }

  protected[this] trait OptionTAsync[F[_]]
      extends OptionTSync[F]
      with OptionTLiftIO[F]
      with Async[OptionT[F, ?]] {

    protected implicit def F: Async[F]
    protected final def FF = F

    def asyncF[A](k: (Either[Throwable, A] => Unit) => OptionT[F, Unit]): OptionT[F, A] =
      OptionT.optionTMonadTrans.liftM(F.asyncF((cb: Either[Throwable, A] => Unit) => k(cb).run.void))

    def async[A](k: (Either[Throwable, A] => Unit) => Unit): OptionT[F, A] =
      OptionT.optionTMonadTrans.liftM(F.async(k))
  }

  protected[this] trait KleisliAsync[F[_], R]
      extends KleisliSync[F, R]
      with KleisliLiftIO[F, R]
      with Async[Kleisli[F, R, ?]] {

    protected implicit def F: Async[F]

    override def asyncF[A](k: (Either[Throwable, A] => Unit) => Kleisli[F, R, Unit]): Kleisli[F, R, A] =
      Kleisli(a => F.asyncF(cb => k(cb).run(a)))

    override def async[A](k: (Either[Throwable, A] => Unit) => Unit): Kleisli[F, R, A] =
      Kleisli(_ => F.async(k))
  }

  protected[this] trait EitherTAsync[F[_], L]
      extends EitherTSync[F, L]
      with EitherTLiftIO[F, L]
      with Async[EitherT[F, L, ?]] {

    protected implicit def F: Async[F]
    protected final def FF = F

    def async[A](k: (Either[Throwable, A] => Unit) => Unit): EitherT[L, F, A] =
      EitherT.rightT(F.async(k))

    def asyncF[A](k: (Either[Throwable, A] => Unit) => EitherT[L, F, Unit]): EitherT[L, F, A] =
      EitherT.rightT(F.asyncF(cb => k(cb).run.void))
  }

  protected[this] trait StateTAsync[F[_], S]
      extends StateTSync[F, S]
      with StateTLiftIO[F, S]
      with Async[StateT[F, S, ?]] {

    protected implicit def F: Async[F]
    protected final def FF = F

    override def asyncF[A](k: (Either[Throwable, A] => Unit) => StateT[S, F, Unit]): StateT[S, F, A] =
      StateT(s => F.asyncF[A](cb => k(cb).eval(s)).map(a => (s, a)))

    override def async[A](k: (Either[Throwable, A] => Unit) => Unit): StateT[S, F, A] =
      StateT.liftM(F.async(k))
  }

  protected[this] trait WriterTAsync[F[_], L]
      extends WriterTSync[F, L]
      with WriterTLiftIO[F, L]
      with Async[WriterT[F, L, ?]] {

    protected implicit def F: Async[F]
    protected final def FF = F

    override def asyncF[A](k: (Either[Throwable, A] => Unit) => WriterT[L, F, Unit]): WriterT[L, F, A] =
      WriterT.put(F.asyncF((cb: Either[Throwable, A] => Unit) => k(cb).run.void))(L.empty)

    override def async[A](k: (Either[Throwable, A] => Unit) => Unit): WriterT[L, F, A] =
      WriterT.put(F.async(k))(L.empty)
  }
}

trait MTLEffect extends MTLAsync {

  implicit def scalazEitherTEffect[F[_]: Effect]: Effect[EitherT[F, Throwable, ?]] =
    new EitherTEffect[F] { def F = Effect[F] }

  implicit def scalazWriterTEffect[F[_]: Effect, L: Monoid]: Effect[WriterT[F, L, ?]] =
    new WriterTEffect[F, L] { def F = Effect[F]; def L = Monoid[L] }

  protected[this] trait EitherTEffect[F[_]] extends EitherTAsync[F, Throwable] with Effect[EitherT[F, Throwable, ?]] {
    protected implicit def F: Effect[F]

    def runAsync[A](fa: EitherT[Throwable, F, A])(cb: Either[Throwable, A] => IO[Unit]): SyncIO[Unit] =
      F.runAsync(fa.run)(cb.compose(_.flatMap(x => x.asCats)))

    override def toIO[A](fa: EitherT[Throwable, F, A]): IO[A] =
      F.toIO(F.rethrow(fa.run.map(_.asCats)))
  }

  protected[this] trait WriterTEffect[F[_], L] extends WriterTAsync[F, L] with Effect[WriterT[F, L, ?]] {
    protected implicit def F: Effect[F]

    def runAsync[A](fa: WriterT[L, F, A])(cb: Either[Throwable, A] => IO[Unit]): SyncIO[Unit] =
      F.runAsync(fa.run)(cb.compose(_.map(_._2)))

    override def toIO[A](fa: WriterT[L, F, A]): IO[A] =
      F.toIO(fa.value)
  }
}

trait MTLConcurrent extends MTLAsync {

  implicit def scalazOptionTConcurrent[F[_]: Concurrent]: Concurrent[OptionT[F, ?]] =
    new OptionTConcurrent[F] { def F = Concurrent[F] }

  implicit def scalazKleisliConcurrent[F[_]: Concurrent, R]: Concurrent[Kleisli[F, R, ?]] =
    new KleisliConcurrent[F, R] { def F = Concurrent[F] }

  implicit def scalazWriterTConcurrent[F[_]: Concurrent, L: Monoid]: Concurrent[WriterT[F, L, ?]] =
    new WriterTConcurrent[F, L] { def F = Concurrent[F]; def L = Monoid[L] }

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

  protected[this] trait KleisliConcurrent[F[_], R] extends KleisliAsync[F, R] with Concurrent[Kleisli[F, R, ?]] {
    protected implicit def F: Concurrent[F]

    override def cancelable[A](k: (Either[Throwable, A] => Unit) => CancelToken[Kleisli[F, R, ?]]): Kleisli[F, R, A] =
      Kleisli(r => F.cancelable(k.andThen(_.run(r))))

    override def start[A](fa: Kleisli[F, R, A]): Kleisli[F, R, Fiber[Kleisli[F, R, ?], A]] =
      Kleisli(r => F.start(fa.run(r)).map(fiberT))

    override def racePair[A, B](fa: Kleisli[F, R, A], fb: Kleisli[F, R, B]) =
      Kleisli { r =>
        F.racePair(fa.run(r), fb.run(r)) map {
          case Left((a, fiber)) => Left((a, fiberT[B](fiber)))
          case Right((fiber, b)) => Right((fiberT[A](fiber), b))
        }
      }

    protected def fiberT[A](fiber: Fiber[F, A]): Fiber[Kleisli[F, R, ?], A] =
      Fiber(Kleisli(_ => fiber.join), Kleisli(_ => fiber.cancel))
  }

  protected[this] trait EitherTConcurrent[F[_], L] extends EitherTAsync[F, L] with Concurrent[EitherT[F, L, ?]] {
    protected implicit def F: Concurrent[F]

    override def cancelable[A](k: (Either[Throwable, A] => Unit) => CancelToken[EitherT[F, L, ?]]): EitherT[L, F, A] =
      EitherT.rightT(F.cancelable(k.andThen(_.run.void)))

    override def start[A](fa: EitherT[L, F, A]) =
      EitherT.rightT(F.start(fa.run).map(fiberT))

    override def racePair[A, B](
        fa: EitherT[L, F, A],
        fb: EitherT[L, F, B])
        : EitherT[L, F, Either[(A, Fiber[EitherT[F, L, ?], B]), (Fiber[EitherT[F, L, ?], A], B)]] = {

      EitherT.eitherT(F.racePair(fa.run, fb.run) flatMap {
        case Left((\/-(r), fiberB)) =>
          F.pure(\/-(Left((r, fiberT[B](fiberB)))))

          case Left((-\/(l), fiberB)) =>
            fiberB.cancel.as(-\/(l))

        case Right((fiberA, \/-(r))) =>
          F.pure(\/-(Right((fiberT[A](fiberA), r))))

        case Right((fiberA, -\/(l))) =>
          fiberA.cancel.as(-\/(l))
      })
    }

    protected def fiberT[A](fiber: Fiber[F, L \/ A]): Fiber[EitherT[F, L, ?], A] =
      Fiber(EitherT.eitherT(fiber.join), EitherT.rightT(fiber.cancel))
  }

  protected[this] trait WriterTConcurrent[F[_], L] extends WriterTAsync[F, L] with Concurrent[WriterT[F, L, ?]] {
    protected implicit def F: Concurrent[F]

    override def cancelable[A](k: (Either[Throwable, A] => Unit) => CancelToken[WriterT[F, L, ?]]): WriterT[L, F, A] =
      WriterT.put(F.cancelable(k.andThen(_.run.void)))(L.empty)

    override def start[A](fa: WriterT[L, F, A]) = {
      WriterT(F.start(fa.run) map { fiber =>
        (L.empty, fiberT[A](fiber))
      })
    }

    override def racePair[A, B](
        fa: WriterT[L, F, A],
        fb: WriterT[L, F, B])
        : WriterT[L, F, Either[(A, Fiber[WriterT[F, L, ?], B]), (Fiber[WriterT[F, L, ?], A], B)]] = {

      WriterT(F.racePair(fa.run, fb.run) map {
        case Left(((l, value), fiber)) =>
          (l, Left((value, fiberT(fiber))))

        case Right((fiber, (l, value))) =>
          (l, Right((fiberT(fiber), value)))
      })
    }

    protected def fiberT[A](fiber: Fiber[F, (L, A)]): Fiber[WriterT[F, L, ?], A] =
      Fiber(WriterT(fiber.join), WriterT.put(fiber.cancel)(L.empty))
  }
}

trait MTLConcurrentEffect extends MTLEffect with MTLConcurrent {

  implicit def scalazEitherTConcurrentEffect[F[_]: ConcurrentEffect]: ConcurrentEffect[EitherT[F, Throwable, ?]] =
    new EitherTConcurrentEffect[F] { def F = ConcurrentEffect[F] }

  implicit def scalazWriterTConcurrentEffect[F[_]: ConcurrentEffect, L: Monoid]: ConcurrentEffect[WriterT[F, L, ?]] =
    new WriterTConcurrentEffect[F, L] { def F = ConcurrentEffect[F]; def L = Monoid[L] }

  protected[this] trait EitherTConcurrentEffect[F[_]]
      extends EitherTEffect[F]
      with EitherTConcurrent[F, Throwable]
      with ConcurrentEffect[EitherT[F, Throwable, ?]] {

    protected implicit def F: ConcurrentEffect[F]

    override def runCancelable[A](
        fa: EitherT[Throwable, F, A])(
        cb: Either[Throwable, A] => IO[Unit])
        : SyncIO[CancelToken[EitherT[F, Throwable, ?]]] =
      F.runCancelable(fa.run)(cb.compose(_.flatMap(x => x.asCats))).map(EitherT.rightT(_))
  }

  protected[this] trait WriterTConcurrentEffect[F[_], L]
      extends WriterTEffect[F, L]
      with WriterTConcurrent[F, L]
      with ConcurrentEffect[WriterT[F, L, ?]] {

    protected implicit def F: ConcurrentEffect[F]

    override def runCancelable[A](
        fa: WriterT[L, F, A])(
        cb: Either[Throwable, A] => IO[Unit])
        : SyncIO[CancelToken[WriterT[F, L, ?]]] =
      F.runCancelable(fa.run)(cb.compose(_.map(_._2))).map(WriterT.put(_)(L.empty))
  }
}

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

  implicit def scalazKleisliContextShift[F[_], R](
      implicit cs: ContextShift[F])
      : ContextShift[Kleisli[F, R, ?]] = {

    new ContextShift[Kleisli[F, R, ?]] {
      def shift: Kleisli[F, R, Unit] =
        Kleisli(_ => cs.shift)

      def evalOn[A](ec: ExecutionContext)(fa: Kleisli[F, R, A]): Kleisli[F, R, A] =
        Kleisli(a => cs.evalOn(ec)(fa.run(a)))
    }
  }

  implicit def scalazEitherTContextShift[F[_]: Functor, L](
      implicit cs: ContextShift[F])
      : ContextShift[EitherT[F, L, ?]] = {

    new ContextShift[EitherT[F, L, ?]] {
      def shift: EitherT[L, F, Unit] =
        EitherT.rightT(cs.shift)

      def evalOn[A](ec: ExecutionContext)(fa: EitherT[L, F, A]): EitherT[L, F, A] =
        EitherT.eitherT(cs.evalOn(ec)(fa.run))
    }
  }

  implicit def scalazStateTContextShift[F[_]: Monad, S](
      implicit cs: ContextShift[F])
      : ContextShift[StateT[F, S, ?]] = {

    new ContextShift[StateT[F, S, ?]] {
      def shift: StateT[S, F, Unit] =
        StateT.liftM(cs.shift)

      def evalOn[A](ec: ExecutionContext)(fa: StateT[S, F, A]): StateT[S, F, A] =
        StateT(s => cs.evalOn(ec)(fa.run(s)))
    }
  }

  implicit def scalazWriterTContextShift[F[_]: Monad, L: Monoid](
      implicit cs: ContextShift[F])
      : ContextShift[WriterT[F, L, ?]] = {

    new ContextShift[WriterT[F, L, ?]] {
      def shift: WriterT[L, F, Unit] =
        WriterT.put(cs.shift)(Monoid[L].empty)

      def evalOn[A](ec: ExecutionContext)(fa: WriterT[L, F, A]): WriterT[L, F, A] =
        WriterT(cs.evalOn(ec)(fa.run))
    }
  }
}
