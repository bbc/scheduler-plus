package bbc

import akka.event.Logging
import bbc.persistence.sync.{CallbackResponse, Callbacks, Job}

/**
  * Mock callbacks for testing
  */
object HelloWorldCallbacks extends Callbacks {
  val log = Logging(AppContext.actorSystem, getClass)

  def callbackFor(job: Job):() => CallbackResponse = {

    job.`type` match {
      case "episode_summary" => () => {
        log.debug("Hello, World!")

        CallbackResponse(lifetimeInMillis = 10000)
      }
    }
  }

  def keys: scala.List[String] = List("episode_summary")
}
