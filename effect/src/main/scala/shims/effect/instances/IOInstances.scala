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

import cats.StackSafeMonad
import cats.effect.{ExitCase, Sync}

import scalaz.effect.{IO => SzIO}

import shims.conversions.MonadErrorConversions

trait IOInstances extends MonadErrorConversions {

  implicit def liftIOToScalaz[F[_]: cats.effect.LiftIO]: scalaz.effect.LiftIO[F] =
    new scalaz.effect.LiftIO[F] {
      def liftIO[A](ioa: SzIO[A]): F[A] =
        cats.effect.LiftIO[F].liftIO(cats.effect.IO(ioa.unsafePerformIO()))
    }

  implicit object ioSync extends Sync[SzIO] with StackSafeMonad[SzIO] {

    def pure[A](a: A): SzIO[A] = SzIO(a)

    def handleErrorWith[A](ioa: SzIO[A])(f: Throwable => SzIO[A]): SzIO[A] =
      ioa.catchSome(Some(_), f)

    def raiseError[A](e: Throwable): SzIO[A] = SzIO.throwIO(e)

    def flatMap[A, B](fa: SzIO[A])(f: A => SzIO[B]): SzIO[B] = fa.flatMap(f)

    // emulates using catchLeft
    def bracketCase[A, B](acquire: SzIO[A])(use: A => SzIO[B])(release: (A, ExitCase[Throwable]) => SzIO[Unit]): SzIO[B] = {
      for {
        a <- acquire
        bOr <- use(a).catchLeft
        ec = bOr.fold(ExitCase.Error(_), _ => ExitCase.Completed)
        _ <- release(a, ec)
        b <- bOr.fold(SzIO.throwIO(_), SzIO(_))
      } yield b
    }

    override def suspend[A](thunk: => SzIO[A]): SzIO[A] = SzIO(thunk).flatMap(a => a)

    override def delay[A](thunk: => A): SzIO[A] = SzIO(thunk)
  }
}
