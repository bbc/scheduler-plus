package bbc.client.examples

import akka.event.Logging
import bbc.AppContext
import bbc.schedulerplus.Job
import bbc.schedulerplus.client.{CallbackResponse, Callbacks}

/**
  * Sample Hello, World! callbacks
  */
object HelloWorldCallbacks extends Callbacks {
  val log = Logging(AppContext.actorSystem, getClass)

  def callbackFor(job: Job):() => CallbackResponse = {

    job.`type` match {
      case "hello_world" => () => {
        log.debug("Hello, World! [" + job.id + "]")
        // do something with the ID here
        CallbackResponse(lifetimeInMillis = 10000)
      }
    }
  }

  def keys: scala.List[String] = List("hello_world")
}
