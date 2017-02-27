name := "akka-http-client"

description := "Simple Akka HTTP Client DSL for Scala."

organization := "net.fehmicansaglam"

version := "10.0_0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

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
  val akkaHttpCoreV = "10.0.4"
  val sprayJsonV = "1.3.2"
  val scalaTestV = "3.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpCoreV,
    "io.spray" %%  "spray-json" % sprayJsonV % Test,
    "org.scalatest" %% "scalatest" % scalaTestV % Test
  )
}

// Publish settings
crossScalaVersions := Seq("2.11.8", "2.12.1")

publishMavenStyle := true

publishArtifact in Test := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra :=
  <url>http://github.com/fehmicansaglam/akka-http-client</url>
  <licenses>
    <license>
      <name>MIT License</name>
      <url>http://www.opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <developers>
    <developer>
      <id>fehmicansaglam</id>
      <name>Fehmi Can Saglam</name>
      <url>http://github.com/fehmicansaglam</url>
    </developer>
  </developers>
  <scm>
    <connection>scm:git:git://github.com/fehmicansaglam/akka-http-client.git</connection>
    <developerConnection>scm:git:ssh://github.com:fehmicansaglam/akka-http-client.git</developerConnection>
    <url>http://github.com/fehmicansaglam/akka-http-client/tree/master</url>
  </scm>

