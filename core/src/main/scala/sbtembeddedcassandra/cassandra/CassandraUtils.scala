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

import cats.instances.either._
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import com.datastax.driver.core.{Cluster, Session}
import org.apache.cassandra.config.DatabaseDescriptor
import org.apache.cassandra.db.commitlog.CommitLog
import org.apache.cassandra.service.CassandraDaemon
import sbtembeddedcassandra.syntax._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Either
import scala.util.control.NonFatal

object CassandraUtils {

  lazy val eitherDaemon: EitherThrowable[CassandraDaemon] =
    Either.catchNonFatal(new CassandraDaemon())

  def setCassandraProperties(
      yaml: File,
      workingDir: File,
      confFileName: String,
      log4jFileName: String): EitherThrowable[Unit] =
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

  def startCassandra(yaml: File, workingDir: File, timeout: Duration): EitherThrowable[Unit] = {

    def init: EitherThrowable[Unit] =
      Either.catchNonFatal {
        DatabaseDescriptor.forceStaticInitialization()
        CommitLog.instance.resetUnsafe(true)
      }

    def startCassandra(daemon: CassandraDaemon): EitherThrowable[Unit] = {
      import scala.concurrent.ExecutionContext.Implicits.global
      val future: Future[EitherThrowable[Unit]] =
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

  def executeCQLStatements(
      clusterName: String,
      listenAddress: String,
      nativePort: String,
      statements: List[String]): EitherThrowable[Unit] = {

    def buildCluster(): EitherThrowable[Cluster] = Either.catchNonFatal {
      new Cluster.Builder()
        .withClusterName(clusterName)
        .addContactPoint(listenAddress)
        .withPort(nativePort.toInt)
        .build()
    }

    def connectCluster(cluster: Cluster): EitherThrowable[Session] =
      Either.catchNonFatal(cluster.connect())

    def executeStatement(session: Session, statement: String): EitherThrowable[Unit] =
      Either.catchNonFatal(session.execute(statement)).map(_ => (): Unit)

    buildCluster() flatMap { cluster =>
      val tryExecuteStatements: EitherThrowable[Unit] = for {
        session <- connectCluster(cluster)
        _       <- statements.traverse(executeStatement(session, _)).map(_ => (): Unit)
        _       <- Either.catchNonFatal(session.close())
      } yield ()
      Either.catchNonFatal(cluster.close())
      tryExecuteStatements
    }
  }

}
