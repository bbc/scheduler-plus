// scalastyle:off
lazy val schedulerplus = (project in file(".")).
  settings(
    organization := "bbc",
    name := "scheduler-plus",
    mainClass := Some("bbc.Boot"),
    scalaVersion := "2.11.8",
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),

    publishTo := Some(Resolver.file("file", new File("."))),

    libraryDependencies ++= {
      Seq(
        "joda-time"           %   "joda-time"                         % "2.9.4",
        "org.joda"            %   "joda-convert"                      % "1.2",
        "org.slf4j"           %   "slf4j-api"                         % "1.6.4",
        "org.slf4j"           %   "slf4j-api"                         % "1.6.4",
        "com.github.etaty"    %%  "rediscala"                         % "1.6.0",
        "com.typesafe.akka"   %%  "akka-actor"                        % "2.3.15",
        "com.typesafe.akka"   %%  "akka-testkit"                      % "2.4.10"    % "test",
        "org.scalatest"       %%  "scalatest"                         % "2.2.1"     % "test",
        "org.specs2"          %%  "specs2-core"                       % "3.8.5"     % "test",
        "org.specs2"          %%  "specs2-mock"                       % "3.8.5"     % "test"
      )
    }
)
// scalastyle:on
