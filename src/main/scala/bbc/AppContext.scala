package bbc

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object AppContext {
  val actorSystem = ActorSystem("scala-data-manager-lib")

  val cacheSystem = ActorSystem("scala-data-manager-lib-cache")
}
