

name := """nodes-frontend"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  // WebJars (i.e. client-side) dependencies
  "org.webjars" % "requirejs" % "2.1.17",
  "org.webjars" % "lodash" % "3.6.0",
  "org.webjars" % "jquery" % "2.1.4",
  "org.webjars" % "bootstrap" % "3.3.4" exclude("org.webjars", "jquery"),
  "org.webjars" % "angularjs" % "1.3.15" exclude("org.webjars", "jquery")
)

pipelineStages := Seq(rjs, digest, gzip)

RjsKeys.paths += ("jsRoutes" -> ("/jsroutes" -> "empty:"))
