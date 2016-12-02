val scalaV = "2.11.8"
val crossScalaV = Seq("2.11.8", "2.12.0")

// ---- formatting ----
scalaVersion in ThisBuild := scalaV
scalafmtConfig in ThisBuild := Some(file(".scalafmt.conf"))

// ---- common settings ----

val commonSettings = Seq(
  organization := "com.marekkadek",
  scalaVersion := scalaV,
  crossScalaVersions := crossScalaV,
  releaseCrossBuild := true,
  scalacOptions := Seq(
    // following two lines must be "together"
    "-encoding",
    "UTF-8",
    "-Xlint",
    "-Xlint:missing-interpolator",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Ywarn-dead-code",
    "-Yno-adapted-args",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Ywarn-value-discard",
    "-Ywarn-unused-import",
    "-Ywarn-unused",
    "-Ywarn-numeric-widen"
  )
)

// ---- publising ----

val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

val publishSettings = Seq(
  homepage := Some(url("https://github.com/KadekM/scalatags-vdom-observable")),
  organizationHomepage := Some(
    url("https://github.com/KadekM/scalatags-vdom-observable")),
  licenses += ("MIT license", url(
    "http://www.opensource.org/licenses/mit-license.php")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { _ =>
    false
  },
  pomExtra :=
    <scm>
      <url>git@github.com:kadekm/scalatags-vdom-observable.git</url>
      <connection>scm:git:git@github.com:kadekm/scalatags-vdom-observable.git</connection>
    </scm>
      <developers>
        <developer>
          <id>kadekm</id>
          <name>Marek Kadek</name>
          <url>https://github.com/KadekM</url>
        </developer>
      </developers>
)

val libsDeps = Seq(
  libraryDependencies ++= Seq(
    "com.github.lukajcb" %%% "rxscala-js" % "0.10.0",
    //"com.marekkadek" %%% "rxscala-js-cats" % "0.1-SNAPSHOT",
    "com.marekkadek" %%% "scalatags-vdom" % "0.3.0-SNAPSHOT" changing(),

    "com.chuusai" %%% "shapeless" % "2.3.2",

    "org.scalatest" %%% "scalatest" % "3.0.1" % Test,
    "org.scalacheck" %%% "scalacheck" % "1.13.4" % Test
  ),
  jsDependencies += "org.webjars.npm" % "rxjs" % "5.0.0-rc.4" / "bundles/Rx.min.js" commonJSName "Rx"
)

// ---- modules ----

lazy val vdom_observable = Project(id = "scalatags-vdom-observable", base = file("modules/vdom-observable"))
  .settings(
    commonSettings,
    publishSettings,
    libsDeps
  )
  .enablePlugins(ScalaJSPlugin)

lazy val sample = Project(id = "sample", base = file("modules/sample"))
  .settings(commonSettings, noPublishSettings, libsDeps)
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(vdom_observable)
  .aggregate(vdom_observable)

lazy val root = Project(id = "vdom-observable-root", base = file("."))
  .settings(
    commonSettings,
    noPublishSettings
  )
  .dependsOn(vdom_observable)
  .aggregate(vdom_observable)
