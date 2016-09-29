package bbc

import akka.actor.ActorSystem

object AppContext {
  val actorSystem = ActorSystem("scala-data-manager-lib")

  val cacheSystem = ActorSystem("scala-data-manager-lib-cache")
}
