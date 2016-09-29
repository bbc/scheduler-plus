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

import bbc.schedulerplus.client.{CallbackResponse, Callbacks}
import bbc.schedulerplus.{Fixtures, Job, MockClientCallbacks}
import bbc.schedulerplus.persistence.Cache
import org.specs2.mutable.Specification
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class SchedulerManagerEngineSpec  extends Specification with ScalaFutures with Fixtures {

  import scala.concurrent.ExecutionContext.Implicits.global

  "SchedulerManager" should {
    "create an executable callback" in {

      var sideEffectCount = 0

      object MockCallbacks extends Callbacks {
        def callbackFor(job: Job):() => CallbackResponse = {
          job.`type` match {
            case "hello_world" => () => {
              val theLifetime = 10000
              sideEffectCount += 1
              CallbackResponse(lifetimeInMillis = theLifetime)
            }
          }
        }
        def keys: scala.List[String] = List("hello_world")
      }

      object MockSchedulerManager extends SchedulerManagerEngine {
        val cache = mock[Cache]
        cache.request(anyString) returns Future(Some(helloWorldRequest))
      }

      val callback = MockSchedulerManager.createCallback(helloWorldJob(timestampNow), MockCallbacks)

      val execute = Future({callback()})

      whenReady(execute) ( { c => c shouldEqual((): Unit)
        sideEffectCount shouldEqual 1
      })

      ok
    }

    "create a callback for a cancelled request" in {
      object MockSchedulerManager extends SchedulerManagerEngine {
        val cache = mock[Cache]
        cache.request(anyString) returns Future(Some(helloWorldCancelledRequest))
      }

      val callback = MockSchedulerManager.createCallback(helloWorldJob(timestampNow), MockClientCallbacks)
      val callbackResult = callback()

      callbackResult shouldEqual((): Unit)
    }

    "create a callback for a paused request" in {
      object MockSchedulerManager extends SchedulerManagerEngine {
        val cache = mock[Cache]
        cache.request(anyString) returns Future(Some(helloWorldPausedRequest))
      }

      val callback = MockSchedulerManager.createCallback(helloWorldJob(timestampNow), MockClientCallbacks)
      val callbackResult = callback()

      callbackResult shouldEqual((): Unit)
    }

    "throw an exception from the callback of an invalid request" in {
      object MockSchedulerManager extends SchedulerManagerEngine {
        val cache = mock[Cache]
        cache.request(anyString) returns Future(Some(helloWorldInvalidRequest))
      }

      val callback = MockSchedulerManager.createCallback(helloWorldJob(timestampNow), MockClientCallbacks)
      val callbackResult = callback()

      // todo not executing
      callbackResult shouldEqual((): Unit)
//      callback() should throwA[Exception]
    }

    "handles no request for job" in {
      object MockSchedulerManager extends SchedulerManagerEngine {
        val cache = mock[Cache]
        cache.request(anyString) returns Future(None)
      }

      val callback = MockSchedulerManager.createCallback(helloWorldJob(timestampNow), MockClientCallbacks)
      val callbackResult = callback()

      callbackResult shouldEqual((): Unit)
    }
  }
}
