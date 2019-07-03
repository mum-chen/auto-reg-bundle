import sbt._
import Keys._

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // If we're building with Scala > 2.11, enable the compile option
    //  switch to support our anonymous Bundle definitions:
    //  https://github.com/scala/bug/issues/10047
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.11")
    }
  }
}

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // Scala 2.12 requires Java 8. We continue to generate
    //  Java 7 compatible code for Scala 2.11
    //  for compatibility with old clients.
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
val defaultVersions = Map(
  "chisel3" -> "3.1.+",
  "chisel-iotesters" -> "[1.2.5,1.3-SNAPSHOT["
  )

val paradiseVersion = "2.1.0"
lazy val buildSettings = Seq(
  organization := "org.macrotest",
  version := "1.0.0",
  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.12.7"),
  resolvers += Resolver.sonatypeRepo("snapshots"),
  resolvers += Resolver.sonatypeRepo("releases"),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  libraryDependencies += scalaVersion("org.scala-lang" % "scala-reflect" % _).value,
  libraryDependencies ++= (
    if (scalaVersion.value.startsWith("2.10")) List("org.scalamacros" %% "quasiquotes" % paradiseVersion)
    else Nil
  )
)

lazy val coreSettings = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  libraryDependencies ++= Seq("chisel3","chisel-iotesters").map {
    dep: String => "edu.berkeley.cs" %% dep %
                   sys.props.getOrElse(dep + "Version", defaultVersions(dep))
  },

  scalacOptions ++= scalacOptionsVersion(scalaVersion.value),
  javacOptions ++= javacOptionsVersion(scalaVersion.value),
)

lazy val autoRegBundle: Project = Project("auto-reg-bundle", file("."))
  .settings(buildSettings)
  .settings(Seq(run := (run in Compile in core).evaluated))
  .settings(
    addCommandAlias("testAutoReg",
      "; clean " +
      "; core/test:runMain autoreg.AutoRegMain "
        + "--target-dir genstuff --top-name AutoRegDemo"
    )
  )
  .aggregate(macros, core)

lazy val core: Project = Project("core", file("core"))
  .settings(buildSettings, coreSettings)
  .dependsOn(macros)

lazy val macros: Project = Project("macros", file("macros"))
  .settings(buildSettings)
