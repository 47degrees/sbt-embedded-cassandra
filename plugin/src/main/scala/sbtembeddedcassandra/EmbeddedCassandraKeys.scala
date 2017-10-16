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

trait EmbeddedCassandraKeys extends EmbeddedCassandraSettingsKeys with EmbeddedCassandraTaskKeys

object EmbeddedCassandraKeys extends EmbeddedCassandraKeys

trait EmbeddedCassandraDefaultValues {

  lazy val defaultConfigFile: Option[File] = None

  lazy val defaultProperties: Map[String, String] = Map(
    "clusterName"         -> "TestCluster",
    "storagePort"         -> "7000",
    "storagePortSSL"      -> "7001",
    "listenAddress"       -> "127.0.0.1",
    "nativeTransportPort" -> "9042",
    "rpcAddress"          -> "localhost",
    "rpcPort"             -> "9160"
  )

  lazy val defaultWorkingDirectory: String = "target/cassandra"

}

sealed trait EmbeddedCassandraSettingsKeys extends EmbeddedCassandraDefaultValues {

  // (Settings keys are ordered alphabetically)

  private[this] lazy val prettyProperties: String = defaultProperties map {
    case (key, value) => s" * $key ($value)"
  } mkString "\n"

  val embeddedCassandraConfigFileSetting: SettingKey[Option[File]] =
    settingKey[Option[File]](
      s"Defines a custom template config file. Defaults to $defaultConfigFile")

  val embeddedCassandraPropertiesSetting: SettingKey[Map[String, String]] =
    settingKey[Map[String, String]](
      s"Properties to replace in the template. Available configuration properties and default values:\n $prettyProperties")

  val embeddedCassandraWorkingDirectorySetting: SettingKey[File] =
    settingKey[File](s"Output directory for Cassandra. Defaults to '$defaultWorkingDirectory")
}

sealed trait EmbeddedCassandraTaskKeys {

  // (Task keys are ordered alphabetically)

  val embeddedCassandraCreateConfigFile: TaskKey[Unit] =
    taskKey[Unit](
      "Task to create the Cassandra config file in the working directory. Usually, you don't invoke this directly because it's executed by 'embeddedCassandraStart'")

  val embeddedCassandraStart: TaskKey[Unit] = taskKey[Unit]("Task to start Cassandra.")

}
