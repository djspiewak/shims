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

import cats.{Applicative, Monad, Parallel, StackSafeMonad, ~>}
import cats.effect.{Effect, ExitCase, IO, SyncIO}

import scalaz.{Tag, -\/, \/, \/-}
import scalaz.concurrent.Task.ParallelTask
import scalaz.concurrent.{Future, Task}

import shims.conversions.MonadErrorConversions

import java.util.concurrent.atomic.AtomicBoolean

import scala.util.control.NonFatal

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
    def async[A](k: (Either[Throwable, A] => Unit) => Unit): Task[A] = Task.async { registered =>
      val a = new AtomicBoolean(true)
      try k(e => if (a.getAndSet(false)) registered(\/.fromEither(e)) else ())
      catch { case NonFatal(t) => registered(-\/(t)) }
    }

    def asyncF[A](k: (Either[Throwable, A] => Unit) => Task[Unit]): Task[A] =
      async(k.andThen(_.unsafePerformAsync(forget)))

    // emulates using attempt
    def bracketCase[A, B](acquire: Task[A])(use: A => Task[B])(release: (A, ExitCase[Throwable]) => Task[Unit]): Task[B] = {
      for {
        a <- acquire
        bOr <- use(a).attempt
        ec = bOr.fold(ExitCase.Error(_), _ => ExitCase.Completed)
        _ <- release(a, ec)
        b <- bOr.fold(Task.fail, Task.now)
      } yield b
    }

    /*
     * runAsync takes the final callback to something that
     * summarizes the effects in an IO[Unit] as such this
     * takes the Task and executes the internal IO callback
     * into the task asynchronous execution all delayed
     * within the outer IO, discarding any error that might
     * occur
     */
    def runAsync[A](fa: Task[A])(cb: Either[Throwable, A] => IO[Unit]): SyncIO[Unit] =
      SyncIO {
        fa unsafePerformAsync { disjunction =>
          cb(disjunction.toEither).unsafeRunAsync(forget)
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

  implicit val taskParallel: Parallel.Aux[Task, ParallelTask] = new Parallel[Task] {
    import Task.taskParallelApplicativeInstance

    type F[A] = ParallelTask[A]

    val monad: Monad[Task] = taskEffect
    val applicative: Applicative[ParallelTask] = Applicative[ParallelTask]
    val sequential: ParallelTask ~> Task = λ[ParallelTask ~> Task](Tag.unwrap(_))
    val parallel: Task ~> ParallelTask = λ[Task ~> ParallelTask](Tag(_))
  }

  private def functionToPartial[A, B](f: A => B): PartialFunction[A, B] = {
    case a => f(a)
  }

  private def forget[A](x: A): Unit = ()
}
