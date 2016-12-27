name := "akka-http-client"

organization := "net.fehmicansaglam"

version := "10.0_0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.1"

scalacOptions := Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import"
)

libraryDependencies ++= {
  val akkaHttpCoreV = "10.0.1"
  val sprayJsonV = "1.3.2"
  val scalaTestV = "3.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpCoreV,
    "io.spray" %%  "spray-json" % sprayJsonV % Test,
    "org.scalatest" %% "scalatest" % scalaTestV % Test
  )
}