ThisBuild / organization := "com.47deg"
ThisBuild / scalaVersion := "2.12.11"

addCommandAlias(
  "ci-test",
  "+scalafmtCheck; +scalafmtSbtCheck; +coverage; +test; +coverageReport; +coverageAggregate"
)
addCommandAlias("ci-docs", "project-docs/mdoc; headerCreateAll")

lazy val plugin = project
  .in(file("."))
  .dependsOn(core)
  .aggregate(core)
  .settings(pluginSettings: _*)

lazy val core = project
  .in(file("core"))
  .settings(coreSettings: _*)

lazy val `project-docs` = (project in file(".docs"))
  .aggregate(core, plugin)
  .dependsOn(core, plugin)
  .settings(moduleName := "project-docs")
  .settings(mdocIn := file(".docs"))
  .settings(mdocOut := file("."))
  .settings(skip in publish := true)
  .enablePlugins(MdocPlugin)

lazy val pluginSettings: Seq[Def.Setting[_]] = Seq(
  moduleName := "sbt-embedded-cassandra",
  sbtPlugin := true,
  scalacOptions := Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-unchecked")
)

lazy val coreSettings: Seq[Def.Setting[_]] = Seq(
  moduleName := "embedded-cassandra-core",
  sbtPlugin := false,
  resolvers += Resolver.typesafeIvyRepo("releases").withName("typesafe-alt-ivy-releases"),
  libraryDependencies ++= Seq(
    "com.47deg"              %% "org-policies-core"    % "0.13.3",
    "com.datastax.cassandra" % "cassandra-driver-core" % "3.6.0",
    "org.apache.cassandra"   % "cassandra-all"         % "3.11.6"
  )
)
