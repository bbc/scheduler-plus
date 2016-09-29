lazy val datamanager = (project in file(".")).
  settings(
    organization := "bbc.rmp",
    name := "scala-data-manager-lib",
    mainClass := Some("bbc.Boot"),
    version := scala.util.Properties.envOrElse("BUILD_VERSION", "0.1-SNAPSHOT"),
    scalaVersion := "2.11.8",
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    makePomConfiguration ~= { (mpc: MakePomConfiguration) =>
      mpc.copy(file = file("pom.xml"))
    },
    libraryDependencies ++= {
      Seq(
        "bbc.rmp"             %%  "scala-redis-lib"                   % "2.0.1",
        "com.typesafe.akka"   %%  "akka-actor"                        % "2.3.15",
        "com.typesafe.akka"   %%  "akka-http-spray-json-experimental" % "2.4.10",
        "joda-time"           %   "joda-time"                         % "2.9.4",
        "org.joda"            %   "joda-convert"                      % "1.2",
        "org.slf4j"           %   "slf4j-api"                         % "1.6.4",
        "org.slf4j"           %   "slf4j-api"                         % "1.6.4",
        "com.typesafe.akka"   %%  "akka-testkit"                      % "2.4.10"    % "test",
        "org.scalatest"       %%  "scalatest"                         % "2.2.1"     % "test"
      )
    },
    resolvers ++= Seq(
      "BBC Forge Artifactory" at "https://dev.bbc.co.uk/artifactory/repo/",
      "BBC Forge Maven Releases" at "https://dev.bbc.co.uk/maven2/releases/",
      "BBC Forge Maven Snapshots" at "https://dev.bbc.co.uk/maven2/snapshots/"
    )
)
