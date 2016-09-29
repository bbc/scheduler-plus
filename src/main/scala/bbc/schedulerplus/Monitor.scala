package bbc.schedulerplus

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.actor.{Actor, Props}
import akka.event.Logging
import bbc.AppContext
import bbc.schedulerplus.client.Callbacks
import bbc.schedulerplus.domain.JobRequest
import bbc.schedulerplus.timing.ExecutionTimePoolManager
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

/**
  * Manages data syncing of objects
  */
object Monitor {

  private class JobSchedulerActor(callbacks: Callbacks) extends Actor {

    lazy val config = ConfigFactory.load()
    lazy val configInitialdelay = config.getInt("monitor.initial_delay_seconds")
    lazy val configInterval = config.getInt("monitor.interval_seconds")

    import context.dispatcher
    val tick = context.system.scheduler.schedule(
      initialDelay = configInitialdelay seconds,
      interval = configInterval seconds,
      receiver = self,
      message = "find-jobs"
    )

    val log = Logging(AppContext.actorSystem, getClass)

    override def postStop() = tick.cancel()

    def receive = {
      case "find-jobs" => {
        log.debug("Running job scheduler for [" + callbacks.keys.mkString(", ") + "]")

        val jobRequestsToRun = for (key <- callbacks.keys) yield JobManager.findJobRequestsToRun(key)

        Future.sequence(jobRequestsToRun) onComplete {
          case Success(jrs) => {
            val jobRequests: List[JobRequest] = jrs.flatten
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
    system.actorOf(Props(classOf[JobSchedulerActor], callbacks))
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