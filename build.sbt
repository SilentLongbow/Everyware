name := "SENG302 TEAM 100 Everyware"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.8"

lazy val myProject = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += "com.h2database" % "h2" % "1.4.197"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-core" % "2.3.0.1"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.2"

// For Cucumber
libraryDependencies += "io.cucumber" % "cucumber-core" % "4.2.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-jvm" % "4.2.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-junit" % "4.2.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-java" % "4.2.0"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.25.2"
//libraryDependencies += "com.waioeka.sbt" %% "cucumber-runner" % "0.1.5"

// For database
// mySQL
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.15"

// SQLite
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.23.1"

libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.1.0" % Test
testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")


//val framework = new TestFramework("com.waioeka.sbt.runner.CucumberFramework")
//testFrameworks += framework
//
//// Configure the arguments.
//testOptions in Test += Tests.Argument(framework,"--glue","")
//testOptions in Test += Tests.Argument(framework,"--plugin","html:/tmp/html")
//testOptions in Test += Tests.Argument(framework,"--plugin","json:/tmp/json")
//
///** can remove pretty printing if running in parallel. */
//parallelExecution in Test := true
//
//unmanagedClasspath in Test += baseDirectory.value / "test/features"