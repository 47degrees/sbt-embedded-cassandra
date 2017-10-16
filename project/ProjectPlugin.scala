import sbt.Keys._
import sbt._
import sbtorgpolicies.OrgPoliciesPlugin
import sbtorgpolicies.OrgPoliciesPlugin.autoImport._
import sbtorgpolicies.model._
import sbtorgpolicies.runnable.syntax._

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = OrgPoliciesPlugin

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    orgProjectName := "sbt-embedded-cassandra",
    orgGithubSetting := GitHubSettings(
      organization = "47deg",
      project = "sbt-embedded-cassandra",
      organizationName = "47 Degrees",
      groupId = "com.47deg",
      organizationHomePage = url("http://47deg.com"),
      organizationEmail = "hello@47deg.com"
    ),
    orgScriptTaskListSetting := List(
      orgCheckSettings.asRunnableItem,
      "clean".asRunnableItemFull,
      "compile".asRunnableItemFull,
      "test".asRunnableItemFull
    ),
    startYear := Option(2017),
    sbtPlugin := true,
    scalaVersion := scalac.`2.12`,
    scalaOrganization := "org.scala-lang",
    crossSbtVersions := Seq(sbtV.`1.0`),
    crossScalaVersions := Seq(scalac.`2.12`)
  )

}
