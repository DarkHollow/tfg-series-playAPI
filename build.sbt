name := """tfg-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.hibernate" % "hibernate-entitymanager" % "5.2.6.Final",
  "mysql" % "mysql-connector-java" % "5.1.40",
  "org.dbunit" % "dbunit" % "2.5.3",
  "org.mockito" % "mockito-core" % "2.7.5",
  javaJdbc,
  cache,
  javaWs,
  javaJpa
)
