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

import scala.concurrent.Promise
import scala.concurrent.duration._
import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatest.concurrent.ScalaFutures
import bbc.schedulerplus.{Fixtures, Job}

class JobRunnerActorSpec extends TestKit(ActorSystem("testSystem"))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with ScalaFutures
  with Fixtures {

  "JobRunner" should {
    "execute a callback" in {
      var sideEffectCount = 0
      val job = Job("123", "test", timestampNow, 0)

      // because we don't want to check the side-effect before it's happened, wrap in a promise/future latent execution
      val p = Promise[Unit]()
      val f = p.future

      val callback = () => { p.success { sideEffectCount += 1 } }

      // create the actor which executes immediately
      TestActorRef(Props(classOf[JobRunnerActor], job, callback))

      whenReady(f) ( { c => c shouldEqual((): Unit)
        sideEffectCount shouldEqual 1
      })
    }

    "should not respond after stop" in {
      val callback = () => { val i = 1 }
      val job = Job("123", "test", timestampNow, 0)

      val jobRunnerActor = TestActorRef(Props(classOf[JobRunnerActor], job, callback))

      within(1000 millis) {
        jobRunnerActor ! PoisonPill
        expectNoMsg()
      }
    }
  }
}
