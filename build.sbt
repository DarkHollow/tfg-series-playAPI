name := """tfg-api"""

version := "1.0-SNAPSHOT"

lazy val ItTest = config("it") extend Test

lazy val root = project in file(".") enablePlugins PlayJava configs ItTest settings( inConfig(ItTest)(Defaults.testTasks) : _*)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.hibernate" % "hibernate-entitymanager" % "5.2.6.Final",
  "mysql" % "mysql-connector-java" % "5.1.40",
  "org.dbunit" % "dbunit" % "2.5.3",
  "org.mockito" % "mockito-core" % "2.7.5",
  "com.auth0" % "java-jwt" % "3.1.0",
  "commons-io" % "commons-io" % "2.5",
  javaJdbc,
  cache,
  javaWs,
  javaJpa,
  evolutions
)

def itTestFilter(name: String): Boolean = (name endsWith "ItTest") || (name endsWith "IntegrationTest")
def unitTestFilter(name: String): Boolean = (name endsWith "Test") && !itTestFilter(name)

testOptions in ItTest := Seq(Tests.Filter(itTestFilter))

testOptions in Test := Seq(Tests.Filter(unitTestFilter))