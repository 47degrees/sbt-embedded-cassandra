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

package sbtembeddedcassandra

import sbt._

trait EmbeddedCassandraKeys extends EmbeddedCassandraSettingsKeys with EmbeddedCassandraTaskKeys

object EmbeddedCassandraKeys extends EmbeddedCassandraKeys

trait EmbeddedCassandraDefaultValues {

  lazy val defaultConfigFile: Option[File] = None

  val clusterNameProp: String         = "clusterName"
  val listenAddressProp: String       = "listenAddress"
  val nativeTransportPortProp: String = "nativeTransportPort"
  val storagePortProp: String         = "storagePort"
  val storagePortSSLProp: String      = "storagePortSSL"
  val rpcAddressProp: String          = "rpcAddress"
  val rpcPortProp: String             = "rpcPort"

  lazy val defaultProperties: Map[String, String] = Map(
    clusterNameProp         -> "TestCluster",
    storagePortProp         -> "7000",
    storagePortSSLProp      -> "7001",
    listenAddressProp       -> "127.0.0.1",
    nativeTransportPortProp -> "9042",
    rpcAddressProp          -> "localhost",
    rpcPortProp             -> "9160"
  )

  lazy val defaultCQLFile: Option[File] = None

  lazy val defaultWorkingDirectory: String = "target/cassandra"

}

sealed trait EmbeddedCassandraSettingsKeys extends EmbeddedCassandraDefaultValues {

  // (Settings keys are ordered alphabetically)

  val embeddedCassandraConfigFileSetting: SettingKey[Option[File]] =
    settingKey[Option[File]](
      s"Defines a custom template config file. Defaults to $defaultConfigFile"
    )

  val embeddedCassandraCQLFileSetting: SettingKey[Option[File]] =
    settingKey[Option[File]](
      s"Defines a CQL file with statements ended with ';' that will be executed after start the service. Defaults to $defaultCQLFile"
    )

  val embeddedCassandraPropertiesSetting: SettingKey[Map[String, String]] =
    settingKey[Map[String, String]](
      s"Properties to replace in the `cassandra.yml` template. Available configuration properties and default values:\n $prettyProperties"
    )

  val embeddedCassandraWorkingDirectorySetting: SettingKey[File] =
    settingKey[File](s"Output directory for Cassandra. Defaults to '$defaultWorkingDirectory")

  private[this] lazy val prettyProperties: String = defaultProperties map { case (key, value) =>
    s" * $key ($value)"
  } mkString "\n"
}

sealed trait EmbeddedCassandraTaskKeys {

  // (Task keys are ordered alphabetically)

  val embeddedCassandraStart: TaskKey[Unit] =
    taskKey[Unit]("Starts an embedded Cassandra instance.")

}
