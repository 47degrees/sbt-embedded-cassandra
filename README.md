
[![Maven Central](https://img.shields.io/badge/maven%20central-0.0.6-green.svg)](https://repo1.maven.org/maven2/com/47deg/sbt-embedded-cassandra_2.12_1.0) [![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/47degrees/sbt-embedded-cassandra/master/LICENSE) [![GitHub Issues](https://img.shields.io/github/issues/47degrees/sbt-embedded-cassandra.svg)](https://github.com/47degrees/sbt-embedded-cassandra/issues)

# sbt-embedded-cassandra

**sbt-embedded-cassandra** is an SBT plugin that allows to you to start an embedded Cassandra instance.
 
The goal of this project is to provide a way to add integration tests using Cassandra, supporting the latest versions and keeping the plugin simple as possible. 

## Usage

### Add to plugins.sbt

```scala
addSbtPlugin("com.47deg" % "sbt-embedded-cassandra" % "0.0.7")
```

### Start Cassandra from SBT

```bash
> embeddedCassandraStart
```

## Customization

* `embeddedCassandraWorkingDirectorySetting: File`: Output directory for Cassandra. `target/Cassandra` by default.

* `embeddedCassandraPropertiesSetting: Map[String, String]`: Properties to replace in the `cassandra.yml` template. Internally, the plugin will add a new property to the map named `workingDirectory` with the value of the previous setting.

* `embeddedCassandraConfigFileSetting: Option[File]`: Defines a custom template config file, `None` by default. When this setting is `None` it will use the following [template from the resources](embedded-cassandra-core/src/main/resources/basic-cassandra-conf.yml) 

* `embeddedCassandraCQLFileSetting: Option[File]`: Defines a CQL file with statements ended with `;` that will be executed after starting the service.

## Supported Cassandra version

Currently, the plugin uses the version `3.9` of [`cassandra-all`](http://mvnrepository.com/artifact/org.apache.cassandra/cassandra-all) library. Take a look to the [issues](https://github.com/47degrees/sbt-embedded-cassandra/issues) to stay informed about latest improvements and limitations. 

## sbt-embedded-cassandra in the wild not found

If you wish to add your library here please consider a PR to include it.

# Copyright

sbt-embedded-cassandra is designed and developed by 47 Degrees

Copyright (C) 2017-2018 47 Degrees. <http://47deg.com>
