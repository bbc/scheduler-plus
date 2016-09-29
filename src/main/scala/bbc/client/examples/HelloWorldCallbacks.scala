package bbc.client.examples

import akka.event.Logging
import bbc.AppContext
import bbc.schedulerplus.client.{CallbackResponse, Callbacks}
import bbc.schedulerplus.domain.Job

/**
  * Mock callbacks for testing
  */
object HelloWorldCallbacks extends Callbacks {
  val log = Logging(AppContext.actorSystem, getClass)

  def callbackFor(job: Job):() => CallbackResponse = {

    job.`type` match {
      case "hello_world" => () => {
        log.debug("Hello, World!")

        CallbackResponse(lifetimeInMillis = 10000)
      }
    }
  }

  def keys: scala.List[String] = List("hello_world")
}
