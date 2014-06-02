name := "akka-multiswarm"

version := "0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.2",
  "org.scalanlp" % "breeze_2.10" % "0.7",
  // native libraries are not included by default. add this if you want them (as of 0.7)
  // native libraries greatly improve performance, but increase jar sizes.
  "org.scalanlp" % "breeze-natives_2.10" % "0.7",
  "org.specs2" %% "specs2" % "2.3.12" % "test")
  //"org.mockito" % "mockito-core" % "1.9.5")

//  "org.scalatest" %% "scalatest" % "2.1.4" % "test",
//  "org.mockito" % "mockito-core" % "1.9.5"
//  "junit" % "junit" % "4.11" % "test",
//  "com.novocode" % "junit-interface" % "0.10" % "test"
//)
//
//testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

scalacOptions in Test ++= Seq("-Yrangepos")


//resolvers ++= Seq(
//  // other resolvers here
//  // if you want to use snapshot builds (currently 0.8-SNAPSHOT), use this.
//  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
//  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
//)

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

