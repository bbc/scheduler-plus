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

package bbc.schedulerplus

import akka.actor.{Actor, ActorLogging}
import bbc.schedulerplus.client.Callbacks
import bbc.schedulerplus.system.Monitor

/**
  * Receives callbacks on application startup to begin the scheduler for jobs we wish to run
  */
class SchedulerPlusActor extends Actor with ActorLogging {

  /**
   * Start scheduler
   */
  private def start(callbacks: Callbacks): Unit = Monitor.startScheduling(callbacks)

  /**
   * Stop scheduler
   */
  private def stop(): Unit = Monitor.stopScheduling

  override def receive: PartialFunction[Any, Unit] = {
    case callbacks: Callbacks => {
      start(callbacks)
      sender ! "STARTING"
    }
    case "stop" => {
      stop()
      sender ! "STOPPING"
    }
    case message: Any => {
      log.info(message.getClass.getName + " isn't handled by " + this.getClass.getSimpleName)
      sender ! "FAILED"
    }
  }
}
