import sbtassembly.MergeStrategy

val awsSdkVersion = "1.11.307"

name := "save-for-later"

organization := "com.gu"

version := "1.0"

scalaVersion := "2.12.4"

description:= "lambdas to implement save for later"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-dead-code"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
  "com.amazonaws" % "aws-java-sdk-cloudwatch" % "1.11.307",
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.307",
  "com.gu" %% "simple-configuration-ssm" % "1.4.1",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test",
  "org.specs2" %% "specs2-core" % "3.9.1" % "test",
  "org.specs2" %% "specs2-matcher-extra" % "3.9.1" % "test"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Guardian Platform Bintray" at "https://dl.bintray.com/guardian/platforms"
)

assemblyMergeStrategy in assembly := {
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

enablePlugins(RiffRaffArtifact)

assemblyJarName := s"${name.value}.jar"
riffRaffPackageType := assembly.value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffArtifactResources += (file("cfn.yaml"), s"${name.value}-cfn/cfn.yaml")
