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

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import bbc.schedulerplus.client.Callbacks
import bbc.schedulerplus.{Fixtures, Job, MockClientCallbacks}
import org.specs2.mock.Mockito
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.Future

/**
  * TestKit for JobSchedulerActor
  */
class JobSchedulerActorSpec extends TestKit(ActorSystem("testSystem"))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with Mockito
  with Fixtures {

  import scala.concurrent.ExecutionContext.Implicits.global

  "JobSchedulerActor" should {
    "schedule a job when it has a valid request" in {

      val callbacks = MockClientCallbacks

      val sm = mock[SchedulerManager]
      sm.schedule(any[Job], any[Callbacks])

      val jobSchedulerActor = TestActorRef(Props(
        new JobSchedulerActor(callbacks) {
          override lazy val jobManager = mock[JobManager]
          jobManager.findRequestsToRun(anyString) returns Future(Seq(helloWorldRequest))

          override lazy val schedulerManager = sm
        }))

      within(1000 millis) {
        jobSchedulerActor ! "find-jobs"
        expectMsg(1)
      }

      there was one(sm).schedule(any[Job], any[Callbacks])
    }

    "return no items if finding requests yields a failure" in {
      val callbacks = MockClientCallbacks

      val sm = mock[SchedulerManager]
      sm.schedule(any[Job], any[Callbacks])

      val jobSchedulerActor = TestActorRef(Props(
        new JobSchedulerActor(callbacks) {
          override lazy val jobManager = mock[JobManager]
          jobManager.findRequestsToRun(anyString) returns Future.failed(new Exception("mock"))

          override lazy val schedulerManager = sm
        }))

      within(1000 millis) {
        jobSchedulerActor ! "find-jobs"
        expectMsg(0)
      }
    }

    "should not respond after stop" in {
      val callbacks = MockClientCallbacks
      val jobSchedulerActor = TestActorRef(Props(classOf[JobSchedulerActor], callbacks))

      within(1000 millis) {
        jobSchedulerActor ! PoisonPill
        expectNoMsg()
      }
    }
  }
}
