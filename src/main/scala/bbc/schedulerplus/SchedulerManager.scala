package bbc.schedulerplus

import akka.actor.Props
import akka.event.Logging
import bbc.AppContext
import bbc.schedulerplus.client.Callbacks
import bbc.schedulerplus.domain.Job
import bbc.schedulerplus.persistence.JobsDao
import bbc.schedulerplus.timing.ExecutionTimePoolManager

/**
  * Manages the actual scheduling of jobs, creating an actor which will execute when the lifetimeInMillis elapses
  */
object SchedulerManager {
  val system = AppContext.actorSystem
  val log = Logging(system, getClass)

  import scala.concurrent.ExecutionContext.Implicits.global

  /**
    * Creates an anonymous function to make the actual calls to blur and insert into cache etc
    * @param job
    * @return
    */
  def createCallback(job: Job, callbacks: Callbacks):() => Unit = {
    () => {
      log.debug("Executing job " + job.toKey)
      val request = JobsDao.jobRequest(job.toKey)

      request match {
        case Some(r) => {
          for { req <- r } yield {
            req.status match {
              case "live" => {
                log.debug(job.toKey + " is LIVE so running and rescheduling for " + job.lifetimeInMillis + "ms")

                val response = callbacks.callbackFor(job)()

                val executionDelay =
                  ExecutionTimePoolManager.nextMillis(
                    `type` = job.`type`,
                    lifetimeInMillis = response.lifetimeInMillis
                  )

                val newJob = JobManager.updateJob(job, executionDelay)
                SchedulerManager.schedule(newJob, callbacks)
              }
              case "cancelled" => {
                log.debug(job.toKey + " is CANCELLED so ignoring")
                JobManager.deleteJob(job)
              }
              case "paused" => {
                log.debug(job.toKey + " is PAUSED so rescheduling for " + job.lifetimeInMillis + "ms")
                SchedulerManager.schedule(job, callbacks)
              }
            }
          }
        }
      }
    } : Unit
  }

  def schedule(job: Job, callbacks: Callbacks): Unit = {
    log.debug("Scheduling " + job.id + " for " + job.lifetimeInMillis + "ms")
    system.actorOf(Props(classOf[JobRunner], job, createCallback(job, callbacks)))
  }
}
