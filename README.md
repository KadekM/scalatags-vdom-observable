# Cats instances for Scala.js rx

[![Build Status](https://travis-ci.org/KadekM/scalatags-vdom-observable.svg?branch=dev)](https://travis-ci.org/KadekM/scalatags-vdom-observable)
[![Maven Central](https://img.shields.io/maven-central/v/com.marekkadek/scalatags-vdom-observable_sjs0.6_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.marekkadek/scalatags-vdom-observable_sjs0.6_2.12)

## Install

```scala
libraryDependencies += "com.marekkadek" %%% "scalatags-vdom-observable" % "0.1-SNAPSHOT"
```

It doesn't come bundled with the underlying `rx.js` file, so you'll need to either add them manually or specify them as `jsDependencies`:

```scala
jsDependencies += "org.webjars.npm" % "rxjs" % "5.0.0-rc.4" / "bundles/Rx.min.js" commonJSName "Rx"
```

## Usage

```scala
```

It uses [scalajs rx facade](https://github.com/LukaJCB/rxscala-js)
