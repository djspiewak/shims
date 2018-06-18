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

package shims.effect

import cats.StackSafeMonad
import cats.effect.{ExitCase, Sync}

import scalaz.{-\/, \/-}
import scalaz.effect.{IO => SzIO}
import scalaz.syntax.monad._

import shims.conversions.MonadErrorConversions

trait IOInstances extends MonadErrorConversions {

  implicit object ioSync extends Sync[SzIO] with StackSafeMonad[SzIO] {

    def pure[A](a: A): SzIO[A] = SzIO(a)

    def handleErrorWith[A](ioa: SzIO[A])(f: Throwable => SzIO[A]): SzIO[A] =
      ioa.catchSome(Some(_), f)

    def raiseError[A](e: Throwable): SzIO[A] = SzIO.throwIO(e)

    def bracketCase[A, B](acq: SzIO[A])(use: A => SzIO[B])(rel: (A, ExitCase[Throwable]) => SzIO[Unit]): SzIO[B] = {
      acq flatMap { a =>
        use(a).catchLeft flatMap { err =>
          err match {
            case -\/(e) => rel(a, ExitCase.error(e)) *> raiseError(e)
            case \/-(b) => rel(a, ExitCase.complete).as(b)
          }
        }
      }
    }

    def flatMap[A, B](fa: SzIO[A])(f: A => SzIO[B]): SzIO[B] = fa.flatMap(f)

    override def suspend[A](thunk: => SzIO[A]): SzIO[A] = SzIO(thunk).flatMap(a => a)

    override def delay[A](thunk: => A): SzIO[A] = SzIO(thunk)
  }
}
