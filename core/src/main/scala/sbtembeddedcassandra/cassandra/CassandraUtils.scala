/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
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

package sbtembeddedcassandra.cassandra

import java.io.File

import cats.syntax.either._
import org.apache.cassandra.config.DatabaseDescriptor
import org.apache.cassandra.db.commitlog.CommitLog
import org.apache.cassandra.service.CassandraDaemon

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Either
import scala.util.control.NonFatal

object CassandraUtils {

  lazy val eitherDaemon: Either[Throwable, CassandraDaemon] =
    Either.catchNonFatal(new CassandraDaemon())

  def setCassandraProperties(
      yaml: File,
      workingDir: File,
      confFileName: String,
      log4jFileName: String): Either[Throwable, Unit] =
    Either.catchNonFatal {
      System.setProperty("cassandra.config", s"file:${yaml.getAbsolutePath}")
      System.setProperty("cassandra-foreground", "true")
      System.setProperty("cassandra.native.epoll.enabled", "false")
      System.setProperty("cassandra.unsafesystem", "true")
      System.setProperty("cassandra.storagedir", new File(workingDir, "storage").getAbsolutePath)
      if (System.getProperty("log4j.configuration") == null) {
        System.setProperty(
          "log4j.configuration",
          s"file:${new File(workingDir, log4jFileName).getAbsolutePath}")
      }
    }

  def startCassandra(yaml: File, workingDir: File, timeout: Duration): Either[Throwable, Unit] = {

    def init: Either[Throwable, Unit] =
      Either.catchNonFatal {
        DatabaseDescriptor.forceStaticInitialization()
        CommitLog.instance.resetUnsafe(true)
      }

    def startCassandra(daemon: CassandraDaemon): Either[Throwable, Unit] = {
      import scala.concurrent.ExecutionContext.Implicits.global
      val future: Future[Either[Throwable, Unit]] =
        Future(daemon.activate()).map(Right(_)).recover {
          case e => Left(e)
        }
      try {
        Await.result(future, timeout)
      } catch {
        case NonFatal(e) => Left(e)
      }
    }

    for {
      _      <- init
      daemon <- eitherDaemon
      _      <- startCassandra(daemon)
    } yield ()
  }

}
