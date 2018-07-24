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

import cats.StackSafeMonad
import cats.effect.{Effect, IO}

import scalaz.{\/, -\/, \/-}
import scalaz.concurrent.{Future, Task}

import shims.conversions.MonadErrorConversions

import java.util.concurrent.atomic.AtomicBoolean

trait TaskInstances extends MonadErrorConversions {

  // cribbed from quasar, where it was mostly cribbed from scalaz-task-effect
  implicit object taskEffect extends Effect[Task] with StackSafeMonad[Task] {

    def pure[A](x: A): Task[A] = Task.now(x)

    def handleErrorWith[A](fa: Task[A])(f: Throwable => Task[A]): Task[A] =
      fa.handleWith(functionToPartial(f))

    def raiseError[A](e: Throwable): Task[A] = Task.fail(e)

    // In order to comply with `repeatedCallbackIgnored` law
    // on async, a custom AtomicBoolean is required to ignore
    // second callbacks.
    def async[A](k: (Either[Throwable, A] => Unit) => Unit): Task[A] =
      Task.async(singleUseCallback(k))

    def asyncF[A](k: (Either[Throwable, A] => Unit) => Task[Unit]): Task[A] =
      Task.async(cb => singleUseCallback(k)(cb).unsafePerformSync)

    /*
     * runAsync takes the final callback to something that
     * summarizes the effects in an IO[Unit] as such this
     * takes the Task and executes the internal IO callback
     * into the task asynchronous execution all delayed
     * within the outer IO, discarding any error that might
     * occur
     */
    def runAsync[A](fa: Task[A])(cb: Either[Throwable, A] => IO[Unit]): IO[Unit] =
      IO {
        fa.unsafePerformAsync { disjunction =>
          cb(disjunction.toEither).unsafeRunAsync(_ => ())
        }
      }

    def runSyncStep[A](fa: Task[A]): IO[Either[Task[A], A]] =
      IO {
        fa.get match {
          case Future.Now(-\/(_)) => Left(fa)

          case other => other.step match {
            case Future.Now(\/-(a)) => Right(a)
            case other => Left(new Task(other))
          }
        }
      }

    override def map[A, B](fa: Task[A])(f: A => B): Task[B] =
      fa.map(f)

    def flatMap[A, B](fa: Task[A])(f: A => Task[B]): Task[B] = fa.flatMap(f)

    override def delay[A](thunk: => A): Task[A] = Task.delay(thunk)

    def suspend[A](thunk: => Task[A]): Task[A] = Task.suspend(thunk)
  }

  private def functionToPartial[A, B](f: A => B): PartialFunction[A, B] = _ match {
    case a => f(a)
  }

  private def singleUseCallback[A, B](f: (Either[Throwable, A] => Unit) => B): (Throwable \/ A => Unit) => B = { registered =>
    val a = new AtomicBoolean(true)
    f(e => if (a.getAndSet(false)) { registered(\/.fromEither(e)) } else ())
  }
}
