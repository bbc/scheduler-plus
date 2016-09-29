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
import bbc.schedulerplus.client.Callbacks

/**
  * Manages data syncing of objects
  */
object Monitor {

  val system = AppContext.akkaSystem
  val log = Logging(system, getClass)

  /**
    * Entry-point to the manager which begins syncing of specified data
    * @return true if the sync starts ok, false otherwise
    */
  def startScheduling(callbacks: Callbacks): Boolean = {
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
