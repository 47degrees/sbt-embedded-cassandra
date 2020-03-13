/*
 * Copyright 2017-2020 47 Degrees, LLC. <http://www.47deg.com>
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

package sbtembeddedcassandra

import cats.MonadError
import cats.syntax.either._

import scala.util._

object syntax {

  type CResult[T] = Either[Throwable, T]

  trait SyntaxLogger {
    def info(msg: String): Unit
    def error(msg: String): Unit
  }

  object StdoutLogger extends SyntaxLogger {
    override def info(msg: String): Unit  = println(msg)
    override def error(msg: String): Unit = System.err.println(msg)
  }

  implicit class EitherOps[T](either: CResult[T]) {

    def logErrorOr(logger: SyntaxLogger, message: String): Unit =
      either match {
        case Left(e) =>
          logger.error(e.getMessage)
          e.printStackTrace()
        case Right(_) => logger.info(message)
      }

    def guarantee(f: => Unit)(implicit ME: MonadError[CResult, Throwable]): CResult[T] =
      ME.attempt(either).flatMap { e =>
        Either.catchNonFatal(f).flatMap(_ => e.fold(Left(_), Right(_)))
      }

  }

  implicit class OptionOps[T](option: Option[T]) {

    def toEither: CResult[T] =
      option.map(Right(_)).getOrElse(Left(new NoSuchElementException))

  }

}
