/*
 * Copyright 2017-2020 47 Degrees Open Source <https://www.47deg.com>
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
import java.net.InetSocketAddress
import cats.instances.either._
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import com.datastax.oss.driver.api.core.CqlSession
import org.apache.cassandra.config.DatabaseDescriptor
import org.apache.cassandra.db.commitlog.CommitLog
import org.apache.cassandra.service.CassandraDaemon
import sbtembeddedcassandra.syntax._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Either
import scala.util.control.NonFatal

object CassandraUtils {

  lazy val eitherDaemon: CResult[CassandraDaemon] =
    Either.catchNonFatal(new CassandraDaemon())

  def setCassandraProperties(
      yaml: File,
      workingDir: File,
      log4jFileName: String
  ): CResult[List[Option[String]]] = {

    def setProperty(nameAndValue: (String, String)): CResult[Option[String]] =
      Either.catchNonFatal(System.setProperty(nameAndValue._1, nameAndValue._2)).map(Option(_))

    val properties = List(
      ("cassandra.config", s"file:${yaml.getAbsolutePath}"),
      ("cassandra-foreground", "true"),
      ("cassandra.native.epoll.enabled", "false"),
      ("cassandra.unsafesystem", "true"),
      ("cassandra.storagedir", new File(workingDir, "storage").getAbsolutePath)
    )

    val log4jProperty = Option(System.getProperty("log4j.configuration")).map(_ => Nil).getOrElse {
      List(("log4j.configuration", s"file:${new File(workingDir, log4jFileName).getAbsolutePath}"))
    }

    (properties ++ log4jProperty).traverse(setProperty)
  }

  def startCassandra(timeout: Duration): CResult[Unit] = {

    def init: CResult[Int] =
      Either.catchNonFatal {
        DatabaseDescriptor.daemonInitialization()
        CommitLog.instance.resetUnsafe(true)
      }

    def startCassandra(daemon: CassandraDaemon): CResult[Unit] = {
      import scala.concurrent.ExecutionContext.Implicits.global
      val future: Future[CResult[Unit]] =
        Future(daemon.activate()).map(Right(_)).recover { case e =>
          Left(e)
        }
      try Await.result(future, timeout)
      catch {
        case NonFatal(e) => Left(e)
      }
    }

    for {
      _      <- init
      daemon <- eitherDaemon
      _      <- startCassandra(daemon)
    } yield ()
  }

  def executeCQLStatements(
      listenAddress: String,
      nativePort: Int,
      statements: List[String]
  ): CResult[Unit] = {

    CqlSession
      .builder()
      .addContactPoint(InetSocketAddress.createUnresolved(listenAddress, nativePort))
      .build()

    def buildSession(): CResult[CqlSession] = Either.catchNonFatal {
      CqlSession
        .builder()
        .addContactPoint(InetSocketAddress.createUnresolved(listenAddress, nativePort))
        .build()
    }

    def executeStatement(session: CqlSession, statement: String): CResult[Unit] =
      Either.catchNonFatal(session.execute(statement)).map(_ => (): Unit)

    def executeStatements(session: CqlSession): CResult[Unit] =
      for {
        _ <- statements.traverse(executeStatement(session, _)).map(_ => (): Unit)
        _ <- Either.catchNonFatal(session.close())
      } yield ()

    buildSession() flatMap { cluster => executeStatements(cluster).guarantee(cluster.close()) }
  }

}
