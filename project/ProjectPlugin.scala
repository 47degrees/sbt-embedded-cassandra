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
    crossSbtVersions := Seq(sbtV.`0.13`, sbtV.`1.0`),
    crossScalaVersions := Seq(scalac.`2.12`)
  )

}
