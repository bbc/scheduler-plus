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

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import org.scalatest._

/**
  * TestKit for SchedulerPlusActorSpec
  */
class SchedulerPlusActorSpec extends TestKit(ActorSystem("testSystem"))
    with DefaultTimeout
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  val schedulerPlusActor = TestActorRef[SchedulerPlusActor]

  override def afterAll {
    shutdown()
  }

  "A DataSyncActor" should {
    "Respond with STARTING when it recieves callbacks" in {
      within(1000 millis) {
        schedulerPlusActor ! MockClientCallbacks
        expectMsg("STARTING")
      }
    }

    "Respond with FAILED when it recieves an unknown type" in {
      within(1000 millis) {
        schedulerPlusActor ! Some
        expectMsg("FAILED")
      }
    }

    "Respond with STOPPING when it recieves a stop message" in {
      within(1000 millis) {
        schedulerPlusActor ! "stop"
        expectMsg("STOPPING")
      }
    }
  }
}
