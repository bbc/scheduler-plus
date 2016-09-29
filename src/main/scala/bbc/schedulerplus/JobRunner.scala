package bbc.schedulerplus

import scala.concurrent.duration._
import akka.actor.Actor
import akka.event.Logging
import bbc.AppContext
import bbc.schedulerplus.domain.Job

/**
  * AKKA Scheduler Actor which will execute the anonymous function 'callback()' after the job.lifetimeInMills has elapsed
  */
class JobRunner(job: Job, callback: () => Unit) extends Actor {
  import context.dispatcher
  val tick = context.system.scheduler.scheduleOnce(
    job.lifetimeInMillis milliseconds,
    self,
    "run-job"
  )

  //todo get time from manager

  val log = Logging(AppContext.actorSystem, getClass)

  override def postStop() = tick.cancel()

  def receive = {
    case "run-job" => {
      log.debug("Running job runner for " + job.toKey + "  ...")

      callback()
    }
  }
}
