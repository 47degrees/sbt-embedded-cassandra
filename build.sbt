import sbtorgpolicies.templates.badges._

pgpPassphrase := Some(getEnvVar("PGP_PASSPHRASE").getOrElse("").toCharArray)
pgpPublicRing := file(s"$gpgFolder/pubring.gpg")
pgpSecretRing := file(s"$gpgFolder/secring.gpg")

lazy val root = project
  .in(file("."))
  .settings(name := "sbt-embedded-cassandra-plugin")
  .settings(noPublishSettings)
  .dependsOn(plugin, core)
  .aggregate(plugin, core)

lazy val plugin = project
  .in(file("plugin"))
  .dependsOn(core)
  .settings(moduleName := "sbt-embedded-cassandra")
  .settings(sbtPlugin := true)
  .settings(scalacOptions := Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-unchecked"))
  .settings(
    orgBadgeListSetting := List(
      TravisBadge.apply,
      CodecovBadge.apply,
      ScalaLangBadge.apply,
      LicenseBadge.apply,
      GitHubIssuesBadge.apply
    ))

lazy val core = project
  .in(file("core"))
  .settings(moduleName := "embedded-cassandra-core")
  .settings(scalaMetaSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.47deg"              %% "org-policies-core"    % "0.8.0",
      "org.apache.cassandra"   % "cassandra-all"         % "3.9",
      "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.0"
    )
  )
