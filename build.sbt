ThisBuild / organization := "com.47deg"
ThisBuild / scalaVersion := "2.12.11"

addCommandAlias("ci-test", "scalafmtCheck; scalafmtSbtCheck; mdoc")
addCommandAlias("ci-docs", "mdoc; headerCreateAll")
addCommandAlias("ci-publish", "ci-release")

lazy val `sbt-embedded-cassandra` = project
  .enablePlugins(SbtPlugin)
  .dependsOn(`embedded-cassandra-core`)

lazy val `embedded-cassandra-core` = project
  .settings(resolvers += Resolver.typesafeIvyRepo("releases").withName("typesafe-alt-ivy-releases"))
  .settings(
    libraryDependencies ++= Seq(
      "com.47deg"              %% "org-policies-core"    % "0.13.3",
      "com.datastax.cassandra" % "cassandra-driver-core" % "3.6.0",
      "org.apache.cassandra"   % "cassandra-all"         % "3.11.6"
    )
  )

lazy val documentation = project
  .enablePlugins(MdocPlugin)
  .settings(mdocOut := file("."))
  .settings(skip in publish := true)
  .dependsOn(`embedded-cassandra-core`, `sbt-embedded-cassandra`)
