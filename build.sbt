ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := Dependencies.scalaVersion

lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "akka-http-native-image",
    libraryDependencies := Dependencies.all,

    conflictWarning := ConflictWarning.disable,

    assembly / assemblyMergeStrategy := {
      case x if x.endsWith("module-info.class") => MergeStrategy.discard
      case x if x.endsWith(".proto") => MergeStrategy.last
      case "reference.conf" => MergeStrategy.concat
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    assembly / test := None, // run tests manually via `nativeImageTestRunAgent`

    Compile / mainClass := Some("vu.naya.test.Main"),

    nativeImageVersion := "22.3.2",
    nativeImageJvm := "graalvm-java17",
    nativeImageOptions ++= Seq(
      "--no-fallback",
      "--static", // statically link libc to run the binary on different Linux distributions
      "--verbose",
      "--allow-incomplete-classpath",
      "--report-unsupported-elements-at-runtime",
      s"-H:ReflectionConfigurationFiles=${target.value / "native-image-configs" / "reflect-config.json"}",
      s"-H:ConfigurationFileDirectories=${target.value / "native-image-configs"}",
    ),

    nativeImageTestRunOptions ++= Seq("-o", "-R", (Test / classDirectory).value.absolutePath),
    nativeImageTestOptions ++= Seq(
      s"-H:ReflectionConfigurationFiles=${target.value / "native-image-configs" / "reflect-config.json"}",
    ),
    nativeImageTestAgentMerge := true,

    Test / mainClass := Some("org.scalatest.tools.Runner"),
  )
