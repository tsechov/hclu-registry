import java.text.SimpleDateFormat
import java.util.Date

import bintry.Attr
import com.earldouglas.xsbtwebplugin.PluginKeys._
import com.earldouglas.xsbtwebplugin.WebPlugin.webSettings
import sbt.Keys._
import sbt.{ScalaVersion, _}
import sbtassembly.AssemblyPlugin.autoImport._
import sbtdocker.DockerPlugin.autoImport._
import sbtrelease._

import scala.util.Try
import scalariform.formatter.preferences._

val slf4jVersion = "1.7.9"
val logBackVersion = "1.1.2"
val scalatraVersion = "2.3.1"
val scalaLoggingVersion = "3.1.0"
val jettyVersion = "9.2.6.v20141205"
val json4sVersion = "3.2.11"
val seleniumVersion = "2.46.0"
val akkaVersion = "2.3.12"
val camelVersion = "2.15.3"
val awsSdkVersion = "1.10.15"
val liquibaseVersion = "3.4.1"

val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaCamel ="com.typesafe.akka" %% "akka-camel" % akkaVersion
val camelDropbox = "org.apache.camel" % "camel-dropbox" % camelVersion
val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
val awsSdk = "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion
val liquiBase = "org.liquibase" % "liquibase-core" % liquibaseVersion
val liquiBaseLog = "com.mattbertolini" % "liquibase-slf4j" % "1.2.1"

lazy val integrationStack = Seq(akka, akkaCamel, camelDropbox, akkaTestKit, awsSdk)
val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
lazy val loggingStack = Seq(slf4jApi, logBackClassic, scalaLogging)

val bugsnag = "com.bugsnag" % "bugsnag" % "1.2.8"

val typesafeConfig = "com.typesafe" % "config" % "1.2.1"

val jetty = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
val jettyContainer = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container"
val jettyTest = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "test"

val jodaTime = "joda-time" % "joda-time" % "2.6"
val jodaConvert = "org.joda" % "joda-convert" % "1.7"
lazy val jodaDependencies = Seq(jodaTime, jodaConvert)

lazy val commonDependencies = unitTestingStack ++ loggingStack

val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
val scalatraScalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
val scalatraJson = "org.scalatra" %% "scalatra-json" % scalatraVersion
val scalatraCommands = "org.scalatra" %% "scalatra-commands" % scalatraVersion
val scalatraSwagger = "org.scalatra" %% "scalatra-swagger" % scalatraVersion
val json4s = "org.json4s" %% "json4s-native" % json4sVersion
val scalatraAuth = "org.scalatra" %% "scalatra-auth" % scalatraVersion exclude("commons-logging", "commons-logging")
lazy val scalatraStack = Seq(scalatra, scalatraScalatest, scalatraJson, scalatraSwagger, json4s, scalatraAuth, scalatraCommands)

val javaxMailSun = "com.sun.mail" % "javax.mail" % "1.5.3"

val awaitility = "com.jayway.awaitility" % "awaitility-scala" % "1.6.3" % "test"

val slick = "com.typesafe.slick" %% "slick" % "3.0.0"
val h2 = "com.h2database" % "h2" % "1.3.176"
val postgres = "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
val hikari = "com.zaxxer" % "HikariCP-java6" % "2.3.8"
val flyway = "org.flywaydb" % "flyway-core" % "3.1"
lazy val slickStack = Seq(slick, h2, postgres, hikari, flyway, liquiBase, liquiBaseLog)

val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
val scalatest = "org.scalatest" %% "scalatest" % "2.2.3" % "test"
lazy val unitTestingStack = Seq(mockito, scalatest)

val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % "test"
val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion % "test"
val fest = "org.easytesting" % "fest-assert" % "1.4" % "test"
lazy val seleniumStack = Seq(seleniumJava, seleniumFirefox, fest)

// If the scope is provided;test, as in scalatra examples then gen-idea generates the incorrect scope (test).
// As provided implies test, so is enough here.
lazy val servletApiProvided = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts Artifact("javax.servlet", "jar", "jar")




name := "hreg"

// factor out common settings into a sequence
lazy val commonSettings = scalariformSettings ++ Seq(
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(PreserveSpaceBeforeArguments, true)
    .setPreference(CompactControlReadability, true)
    .setPreference(SpacesAroundMultiImports, false),

  organization := "io.drain.hreg",
  version := (version in ThisBuild).value,
  releaseNextVersion := {
    ver => Version(ver).map(versionBump(_).asSnapshot.string).getOrElse(versionFormatError)
  },
  publishMavenStyle := true,
  scalaVersion := "2.11.6",
  scalacOptions ++= Seq("-unchecked", "-deprecation"),
  classpathTypes ~= (_ + "orbit"),
  libraryDependencies ++= commonDependencies,
  licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
)

def versionBump(version: Version) = sys.props.get("versionBump") match {
  case Some("major") => version.bumpMajor
  case Some("minor") => version.bumpMinor
  case _ => version.bumpBugfix
}

def haltOnCmdResultError(result: Int) {
  if (result != 0) {
    throw new Exception("Build failed.")
  }
}

lazy val buildSha = Try(Process("git rev-parse HEAD").!!.stripLineEnd).toOption
lazy val branch = Try(Process("git rev-parse --abbrev-ref HEAD").!!.stripLineEnd).toOption
lazy val buildUrl = sys.env.get("BUILD_URL")
lazy val buildNumber = sys.env.get("BUILD_NUMBER")
lazy val gitDirty = Try("git diff --shortstat" #| "wc -l" !!).toOption.map(_ != "0")



val updateNpm = baseDirectory map { bd =>
  println("Updating NPM dependencies")
  haltOnCmdResultError(Process("npm install", bd / ".." / "ui") !)
}

def gruntTask(taskName: String) = (baseDirectory, streams) map { (bd, s) =>
  val localGruntCommand = "./node_modules/.bin/grunt " + taskName
  def buildGrunt() = {
    Process(localGruntCommand, bd / ".." / "ui").!
  }
  println("Building with Grunt.js : " + taskName)
  haltOnCmdResultError(buildGrunt())
} dependsOn updateNpm

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    publish := {},
    bintrayUnpublish := {}
  )
  .aggregate(backend, ui, hreg)

lazy val backend: Project = (project in file("backend"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings ++ webSettings: _*)
  .settings(
    libraryDependencies ++= (jodaDependencies ++ slickStack ++ scalatraStack ++ integrationStack :+ javaxMailSun) ++
      Seq(jettyContainer, typesafeConfig, servletApiProvided, bugsnag),
    buildInfoPackage := "hclu.hreg.version",
    buildInfoObject := "BuildInfo",
    buildInfoKeys := Seq[BuildInfoKey](
      BuildInfoKey.action("buildDate")(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())),
      // if the build is done outside of a git repository, we still want it to succeed
      BuildInfoKey.action("buildSha")(buildSha.getOrElse("?")),
      BuildInfoKey.action("version")((version in ThisBuild).value),
      BuildInfoKey.action("branch")(branch.getOrElse("?")),
      BuildInfoKey.action("buildNumber")(buildNumber.getOrElse("?")),
      BuildInfoKey.action("buildUrl")(buildUrl.getOrElse("?"))),

    buildInfoOptions += BuildInfoOption.ToJson,
    Seq(
      artifactName := { (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        "hreg." + artifact.extension // produces nice war name -> http://stackoverflow.com/questions/8288859/how-do-you-remove-the-scala-version-postfix-from-artifacts-builtpublished-wi
      },
      // We need to include the whole webapp, hence replacing the resource directory
      webappResources in Compile <<= baseDirectory { bd =>
        val restResources = bd.getParentFile / backend.base.getName / "src" / "main" / "webapp"
        val uiResources = bd.getParentFile / ui.base.getName / "dist" / "webapp"
        // "dist" may not yet exist, as it will be created by grunt later. However, we still need to include it, and
        // if it doesn't exist, SBT will complain
        if (!uiResources.exists() && !uiResources.mkdirs()) {
          throw new RuntimeException(s"$uiResources directory doesn't exist, and cannot be created!")
        }
        List(restResources, uiResources)
      },
      packageWar in DefaultConf <<= (packageWar in DefaultConf) dependsOn gruntTask("build"),
      compile in Compile := {
        val compilationResult = (compile in Compile).value
        IO.touch(target.value / "compilationFinished")

        compilationResult
      },
      assembly <<= assembly dependsOn (packageWar in DefaultConf),
      publish := {},
      bintrayUnpublish := {},
      parallelExecution in Test := false
    )
  )

lazy val ui = (project in file("ui"))
  .settings(commonSettings: _*)
  .settings(
    publish := {},
    bintrayUnpublish := {}
  )
  .settings(test in Test <<= (test in Test) dependsOn gruntTask("test"))


lazy val hreg = (project in file("hreg"))
  .settings(commonSettings: _*)
  .settings(DeployToHeroku.settings: _*)
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(
    libraryDependencies += jetty,
    mainClass in assembly := Some("hclu.hreg.AppRunner"),
    // We need to include the whole webapp, hence replacing the resource directory
    unmanagedResourceDirectories in Compile <<= baseDirectory { bd => {
      List(bd.getParentFile / backend.base.getName / "src" / "main", bd.getParentFile / ui.base.getName / "dist")
    }
    },

    artifact in (assembly) := {
      val art = (artifact in (assembly)).value
      art.copy(`classifier` = Some("assembly"))
    },
    //    assemblyJarName in assembly := "hreg.jar",
    addArtifact(artifact in (assembly), assembly),
    //    def copy(finder: PathFinder, dst: File) = {
    //      IO.copy(finder.get map {f => (f, dst / f.getName)})
    //    }

    assembly <<= assembly map { artifactFile: File =>

      IO.copyFile(artifactFile, new File(artifactFile.getParent + "/hreg.jar"))
      // post-compile work

      artifactFile
    },

    assembly <<= assembly dependsOn gruntTask("build"),

    bintrayOrganization := Some("drain-io"),
    bintrayRepository := "generic",
    bintrayPackage := "hclu-registry",
    bintrayVersionAttributes := {
      val sv = Map("scala_version" -> Seq(Attr.String(scalaVersion.value)))

      val attrs = Map(
        "build_url" -> buildUrl,
        "build_number" -> buildNumber,
        "build_sha" -> buildSha.map(_.take(7)),
        "branch" -> branch,
        "commit_url" -> buildSha.map("https://github.com/tsechov/hclu-registry/commit/" + _.take(7)),
        "git_dirty" -> gitDirty.map(_.toString)
      )

      println("gitDirty: " + gitDirty)

      sv ++ attrs.collect {
        case (key, value) if (value.isDefined) => (key, Seq(Attr.String(value.get)))
      }

      //      buildUrl.foldLeft(sv) { (m, u) =>
      //        m ++ Map("build_url" -> Seq(Attr.String(u)))
      //      }


    },


    docker <<= (docker dependsOn assembly),

    dockerfile in docker := {
      val jarFile = (assemblyOutputPath in assembly).value
      val appDirPath = "/app"
      val jarTargetPath = s"$appDirPath/${jarFile.name}"

      new Dockerfile {
        from("drainio/server:1.0")
        add(jarFile, jarTargetPath)
        workDir(appDirPath)
        expose(8080)
        entryPoint("java", "-jar", jarTargetPath)
      }
    },

    imageNames in docker := Seq(
      ImageName(namespace = Some("drainio"),
        repository = name.value,
        tag = Some(version.value))),

    buildOptions in docker := BuildOptions(cache = false)


  ) dependsOn(ui, backend)



lazy val uiTests = (project in file("ui-tests"))
  .settings(commonSettings: _*)
  .settings(
    parallelExecution := false,
    libraryDependencies ++= seleniumStack ++ Seq(awaitility, jettyTest, servletApiProvided),
    test in Test <<= (test in Test) dependsOn (Keys.`package` in Compile in backend)
  ) dependsOn hreg

RenameProject.settings



