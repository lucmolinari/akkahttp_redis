organization := "com.lucianomolinari"

name := "akkahttp_redis"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "2.4.7",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.7",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.7",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.7" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.github.etaty" %% "rediscala" % "1.6.0"
)