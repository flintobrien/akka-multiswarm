import sbt._
import Keys._

object BuildSettings {
//  val buildScalaVersion = "2.11.5"
  val buildScalaVersion = "2.10.4"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization       := "com.hungrylearner.akka-multiswarm",
    version            := "0.1-SNAPSHOT",
    scalaVersion       := buildScalaVersion,
    scalacOptions in Test ++= Seq("-Yrangepos"),
    resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)
  )
}

object Dependencies {
  val akkaActor   = "com.typesafe.akka" %% "akka-actor"   % "2.3.2"
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % "2.3.2"
  // Native libraries are not included by default. add this if you want them (as of 0.7)
  // Native libraries greatly improve performance, but increase jar sizes.
  val breeze        = "org.scalanlp"    %%  "breeze"         % "0.11.2"
  val breezeNatives = "org.scalanlp"    %%  "breeze-natives" % "0.11.2"
  val specs2        = "org.specs2"      %% "specs2"              % "2.3.12" % "test"
}

object AkkaMultiswarmBuild extends Build {
  import BuildSettings._
  import Dependencies._

  def akkaMultiswarmProject( dir: String) =
    Project(
      dir,
      file( dir),
      settings = buildSettings ++ Seq(
        libraryDependencies ++= Seq( akkaActor, akkaTestkit, breeze, breezeNatives, specs2)
      )
    )

  lazy val core    = akkaMultiswarmProject("core")

  lazy val samples = akkaMultiswarmProject("samples") dependsOn( core)

  lazy val root    = Project("root", base = file(".")) aggregate (core, samples)

}