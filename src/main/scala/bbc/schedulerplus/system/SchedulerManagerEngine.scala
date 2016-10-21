/*
 * Copyright (c) 2016 BBC Design and Engineering
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package bbc.schedulerplus.system

import akka.actor.Props
import akka.event.Logging
import bbc.AppContext
import bbc.schedulerplus.Job
import bbc.schedulerplus.client.Callbacks
import bbc.schedulerplus.persistence.{Cache, RedisCache}
import bbc.schedulerplus.timing.ExecutionTimeManager
import com.typesafe.config.ConfigFactory

/**
  * Provides the implementation of scheduling
  */
trait SchedulerManagerEngine {
  lazy val system = AppContext.akkaSystem
  lazy val log = Logging(system, getClass)
  lazy val config = ConfigFactory.load()
  lazy val configInterval = config.getInt("monitor.interval_seconds")

  val cache: Cache

  import scala.concurrent.ExecutionContext.Implicits.global

  /**
    * Creates an anonymous function to make the actual calls to blur and insert into cache etc
    * @param job
    * @return
    */
  def createCallback(job: Job, callbacks: Callbacks):() => Unit = {
    () => {
      val jobRequest = cache.request("bbc.schedulerplus.request:" + job.toKey)

      for {
        request <- jobRequest
      } yield {
        request match {
          case Some(req) => {
            req.status match {
              case "live" => {
                val response = callbacks.callbackFor(job)()

                val executionDelay =
                  ExecutionTimeManager.nextMillis(`type` = job.`type`, lifetimeInMillis = response.lifetimeInMillis)

                log.debug(job.toKey + " is LIVE so running and rescheduling for " + executionDelay + "ms")
                val newJob = RedisJobManager.updateJob(job, executionDelay)
                RedisSchedulerManager.schedule(newJob, callbacks)
              }
              case "cancelled" => {
                log.debug(job.toKey + " is CANCELLED so ignoring")
                RedisJobManager.deleteJob(job)
              }
              case "paused" => {
                // pause this for at least one polling cycle, avoids 'paused job thrashing'.
                val executionDelay = ExecutionTimeManager.nextMillis(`type` = job.`type`, configInterval * 1000)

                log.debug(job.toKey + " is PAUSED so rescheduling for " + executionDelay + "ms")
                val newJob = RedisJobManager.updateJob(job, executionDelay)
                RedisSchedulerManager.schedule(newJob, callbacks)
              }
              case _ => {
                log.error(req.toKey + " has unknown status of " + req.status)
                throw new Exception(req.toKey + " has unknown status of " + req.status)
              }
            }
          }
          case None => {
            log.debug("Job request supplied for job " + job.id + " is not supported.")
          }
        }
      }
    } : Unit
  }

  /**
    * Schedule a job with callbacks to run in job.lifetimeInMills milliseconds
    * @param job
    * @param callbacks
    */
  def schedule(job: Job, callbacks: Callbacks): Unit =
  system.actorOf(Props(classOf[JobRunnerActor], job, createCallback(job, callbacks)))
}
