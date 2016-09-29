package bbc.client

import bbc.schedulerplus.client.{CallbackResponse, Callbacks}
import bbc.schedulerplus.domain.Job

/**
  * Mock callbacks for testing
  */
object MockClientCallbacks extends Callbacks {
  def callbackFor(job: Job):() => CallbackResponse = {

    job.`type` match {
      case "test_type" => () => {
        // do something

        CallbackResponse(lifetimeInMillis = 100)
      }
    }
  }

  def keys: scala.List[String] = List("test_type")
}