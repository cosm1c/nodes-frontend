import com.typesafe.sbt.SbtNativePackager.NativePackagerKeys._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging

name := """nodes-frontend"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

maintainer := "Cory Prowse <cory@prowse.com>"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-remote" % "2.3.4",
  // WebJars (i.e. client-side) dependencies
  "org.webjars" % "requirejs" % "2.1.17",
  "org.webjars" % "lodash" % "3.6.0",
  "org.webjars" % "jquery" % "2.1.4",
  "org.webjars" % "bootstrap" % "3.3.4" exclude("org.webjars", "jquery"),
  "org.webjars" % "angularjs" % "1.3.15" exclude("org.webjars", "jquery")
)

scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)

pipelineStages := Seq(rjs, digest, gzip)

RjsKeys.paths += ("jsRoutes" -> ("/jsroutes" -> "empty:"))

// This overwrites Play settings - docs say to use enablePlugins(JavaServerAppPackaging)
//packageArchetype.java_server

maintainer in Docker := "Cory Prowse <cory@prowse.com>"

dockerBaseImage in Docker := "dockerfile/java:oracle-java8"

dockerExposedPorts in Docker := Seq(9000, 2552, 9443)

packageSummary in Docker := "Frontend Node"

packageDescription := "Spiking technology"

emojiLogs
