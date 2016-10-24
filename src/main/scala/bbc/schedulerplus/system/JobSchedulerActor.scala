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

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.actor.Actor
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import bbc.AppContext
import bbc.schedulerplus.Request
import bbc.schedulerplus.client.Callbacks
import bbc.schedulerplus.timing.ExecutionTimeManager

class JobSchedulerActor(callbacks: Callbacks) extends Actor {

  lazy val jobManager: JobManager = RedisJobManager

  lazy val schedulerManager: SchedulerManager = RedisSchedulerManager

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

  val log = Logging(AppContext.akkaSystem, getClass)

  // scalastyle:off
  override def postStop() = tick.cancel()
  // scalastyle:on

  // scalastyle:off
  def receive = {
    // scalastyle:on
    case "find-jobs" => {
      log.debug("Running scheduler for [" + callbacks.keys.mkString(", ") + "]")

      val requestsToRun = for (key <- callbacks.keys) yield jobManager.findRequestsToRun(key)

      Future.sequence(requestsToRun) onComplete {
        case Success(jrs) => {
          val requests: List[Request] = jrs.flatten
          log.debug("Found " + requests.size + " requests to run")

          for(request <- requests) {
            // run quickly at first so lifetime is 0; subsequent lifetimes come from the callback response
            val executionDelay = ExecutionTimeManager.nextMillis(`type` = request.`type`, lifetimeInMillis = 0)
            val job = RedisJobManager.createJob(request, executionDelay)
            schedulerManager.schedule(job, callbacks)
          }
          sender ! requests.size
        }
        case Failure(e) => {
          log.debug("Error getting requests: " + e.getMessage)
          sender ! 0
        }
      }
    }
  }
}
