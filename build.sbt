

lazy val root = (project in file("."))
  .enablePlugins(PlayService, PlayLayoutPlugin, Common)
  .settings(
    name := "sticky-playlist",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      guice,
      jdbc,
      "org.joda" % "joda-convert" % "2.2.1",
      "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
      "io.lemonlabs" %% "scala-uri" % "1.5.1",
      "net.codingwell" %% "scala-guice" % "4.2.6",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      // https://mvnrepository.com/artifact/se.michaelthelin.spotify/spotify-web-api-java
      "se.michaelthelin.spotify" % "spotify-web-api-java" % "6.4.0",
      // https://mvnrepository.com/artifact/org.postgresql/postgresql
      "org.postgresql" % "postgresql" % "42.3.2"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )

lazy val gatlingVersion = "3.3.1"
lazy val gatling = (project in file("gatling"))
  .enablePlugins(GatlingPlugin)
  .settings(
    scalaVersion := "2.12.13",
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % Test,
      "io.gatling" % "gatling-test-framework" % gatlingVersion % Test
    )
  )
