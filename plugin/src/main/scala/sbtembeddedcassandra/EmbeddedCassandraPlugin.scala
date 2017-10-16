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

package sbtembeddedcassandra

import sbt._
import sbt.Keys._
import sbtembeddedcassandra.cassandra.CassandraUtils
import sbtembeddedcassandra.io.IOUtils
import sbtembeddedcassandra.syntax._

import scala.concurrent.duration._

object EmbeddedCassandraPlugin extends AutoPlugin {

  object autoImport extends EmbeddedCassandraKeys

  import autoImport._

  private[this] val cassandraConfInput   = "/basic-cassandra-conf.yml"
  private[this] val cassandraConfOutput  = "cassandra-conf.yml"
  private[this] val cassandraLog4jInput  = "/basic-cassandra-log4j.properties"
  private[this] val cassandraLog4jOutput = "log4j.properties"

  def sbtLogger(logger: Logger): SyntaxLogger = new SyntaxLogger {
    override def error(msg: String): Unit = logger.err(msg)
    override def info(msg: String): Unit  = logger.info(msg)
  }

  lazy val embeddedCassandraDefaultSettings: Seq[Def.Setting[_]] = Seq(
    embeddedCassandraPropertiesSetting := defaultProperties,
    embeddedCassandraConfigFileSetting := defaultConfigFile,
    embeddedCassandraWorkingDirectorySetting := file(defaultWorkingDirectory)
  ) ++ embeddedCassandraKeysDef

  lazy val embeddedCassandraKeysDef: Seq[Def.Setting[_]] = Seq(
    embeddedCassandraStart := {
      val workingDir: File                = embeddedCassandraWorkingDirectorySetting.value
      val properties: Map[String, String] = embeddedCassandraPropertiesSetting.value
      val variables: Map[String, String]  = properties + ("workingDirectory" -> workingDir.getAbsolutePath)
      val statementsFile: Option[File]    = embeddedCassandraCQLFileSetting.value
      val logger: Logger                  = streams.value.log

      import CassandraUtils._
      import IOUtils._

      (for {
        _    <- verifyProperties(properties)
        _    <- deleteDir(workingDir)
        yaml <- copyFile(workingDir, cassandraConfInput, variables, cassandraConfOutput)
        _    <- copyFile(workingDir, cassandraLog4jInput, Map.empty, cassandraLog4jOutput)
        _    <- setCassandraProperties(yaml, workingDir, cassandraConfOutput, cassandraLog4jOutput)
        _    <- startCassandra(yaml, workingDir, 60.seconds)
        _ <- statementsFile match {
          case Some(file) => executeStatements(properties, file)
          case None       => Right((): Unit)
        }
      } yield ()).logErrorOr(sbtLogger(logger), "Cassandra started")
    }
  )

  private[this] def verifyProperties(variables: Map[String, String]): EitherThrowable[Unit] =
    variables.keys.toList.filterNot(defaultProperties.keys.toList.contains(_)) match {
      case Nil => Right((): Unit)
      case missing =>
        Left(
          new IllegalArgumentException(
            s"""You need to define these required properties in `embeddedCassandraPropertiesSetting` setting:
             | - ${missing.mkString(",")}""".stripMargin))
    }

  private[this] def executeStatements(
      variables: Map[String, String],
      statementsFile: File): EitherThrowable[Unit] =
    for {
      clusterName   <- variables.get(clusterNameProp).toEither
      listenAddress <- variables.get(listenAddressProp).toEither
      nativePort    <- variables.get(nativeTransportPortProp).toEither
      statements    <- IOUtils.readStatements(statementsFile)
      _             <- CassandraUtils.executeCQLStatements(clusterName, listenAddress, nativePort, statements)
    } yield ()

  override def projectSettings: Seq[Def.Setting[_]] = embeddedCassandraDefaultSettings

}
