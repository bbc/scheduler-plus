package bbc.persistence.sync

import scala.concurrent.duration._
import akka.actor.{Actor, Props}
import akka.event.Logging
import bbc.AppContext
import bbc.scheduler.SchedulerManager

import scala.util.{Failure, Success}

/**
  * Manages data syncing of objects
  */
object SyncManager {

  private class JobScheduler(callbacks: Callbacks) extends Actor {

    import context.dispatcher
    val tick = context.system.scheduler.schedule(
      initialDelay = 10 seconds,
      interval = 15 seconds,
      receiver = self,
      message = "find-jobs"
    )

    val log = Logging(AppContext.actorSystem, getClass)

    override def postStop() = tick.cancel()

    def receive = {
      case "find-jobs" => {
        log.debug("Running job scheduler...")

        val jobRequestsToRun = JobManager.findJobRequestsToRun("episode_summary")

        jobRequestsToRun onComplete {
          case Success(jobRequests) => {
            log.debug("Found " + jobRequests.size + " job requests to run")

            for(jobRequest <- jobRequests) {
              // run quickly at first so lifetime is 0; subsequent lifetimes come from the callback response
              val executionDelay = ExecutionTimePoolManager.nextMillis(`type` = jobRequest.`type`, lifetimeInMillis = 0)
              val job = JobManager.createJob(jobRequest, executionDelay)
              SchedulerManager.schedule(job, callbacks)
            }
          }
          case Failure(e) => log.debug("Error getting job requests: " + e.getMessage)
        }
      }
    }
  }

  val system = AppContext.actorSystem
  val log = Logging(system, getClass)

  /**
    * Entry-point to the manager which begins syncing of specified data
    * @return true if the sync starts ok, false otherwise
    */
  def startScheduling(callbacks: Callbacks): Boolean = {
    log.info("Starting scheduler...")
    system.actorOf(Props(classOf[JobScheduler], callbacks))
    true
  }

  /**
    * Entry-point to the manager which begins syncing of specified data
    * @return true if the sync stops ok, false otherwise
    */
  def stopScheduling: Boolean = {

    log.info("Stopping scheduler...")
    true
  }

}