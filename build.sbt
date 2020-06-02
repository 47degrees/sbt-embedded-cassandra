ThisBuild / organization := "com.47deg"
ThisBuild / scalaVersion := "2.12.11"

addCommandAlias(
  "ci-test",
  "+scalafmtCheck; +scalafmtSbtCheck; +coverage; +test; +coverageReport; +coverageAggregate"
)
addCommandAlias("ci-docs", "documentation/mdoc; headerCreateAll")

lazy val plugin = project
  .in(file("."))
  .dependsOn(core)
  .aggregate(core)
  .settings(pluginSettings: _*)

lazy val core = project
  .in(file("core"))
  .settings(coreSettings: _*)

lazy val documentation = project
  .enablePlugins(MdocPlugin)
  .settings(mdocOut := file("."))
  .settings(skip in publish := true)
  .dependsOn(core, plugin)

lazy val pluginSettings: Seq[Def.Setting[_]] = Seq(
  moduleName := "sbt-embedded-cassandra",
  sbtPlugin := true,
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
