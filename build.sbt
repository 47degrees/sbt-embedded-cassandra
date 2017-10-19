import sbtorgpolicies.templates.badges._

pgpPassphrase := Some(getEnvVar("PGP_PASSPHRASE").getOrElse("").toCharArray)
pgpPublicRing := file(s"$gpgFolder/pubring.gpg")
pgpSecretRing := file(s"$gpgFolder/secring.gpg")

lazy val root = project
  .in(file("."))
  .dependsOn(core)
  .settings(name := "sbt-embedded-cassandra")
  .settings(sbtPlugin := true)
  .settings(scalacOptions := Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-unchecked"))
  .settings(
    orgBadgeListSetting := List(
      TravisBadge.apply,
      CodecovBadge.apply,
      MavenCentralBadge.apply,
      LicenseBadge.apply,
      GitHubIssuesBadge.apply
    ))

lazy val core = project
  .in(file("core"))
  .settings(sbtPlugin := false)
  .settings(name := "embedded-cassandra-core")
  .settings(scalaMetaSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      %%("org-policies-core"),
      %("cassandra-driver-core"),
      "org.apache.cassandra" % "cassandra-all" % "3.9"
    )
  )
