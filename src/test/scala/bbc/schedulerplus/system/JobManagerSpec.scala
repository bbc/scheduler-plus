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

import bbc.matchers.CustomMatchers
import bbc.schedulerplus.Fixtures
import org.joda.time.format.ISODateTimeFormat
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.specs2.mutable.Specification

object JobManagerSpec extends Specification with Fixtures with ScalaFutures with CustomMatchers {

  // scalastyle:off
  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))
  // scalastyle:on

  "JobManager" should {
    "find a job to run with valid request and no matching jobs" in {
      object MockJobManager extends JobManagerEngine { val cache = mockCacheWithNoJob }
      val manager = MockJobManager

      val requests = manager.findRequestsToRun("hello_world")

      whenReady(requests) { r => r should have size 1 }
      ok
    }

    "find a job to run with a valid request and a stalled job" in {
      object MockJobManager extends JobManagerEngine { val cache = mockCacheWithStalledJob }
      val manager = MockJobManager

      val requests = manager.findRequestsToRun("hello_world")

      whenReady(requests) { r => r should have size 1 }
      ok
    }

    "not find any jobs to run with a valid request and a valid job" in {
      object MockJobManager extends JobManagerEngine { val cache = mockCacheWithValidJob }
      val manager = MockJobManager

      val requests = manager.findRequestsToRun("hello_world")

      whenReady(requests) { r => r should have size 0 }
      ok
    }

    "not find jobs to run with a PAUSED request" in {
      object MockJobManager extends JobManagerEngine { val cache = mockCacheWithPausedRequest }
      val manager = MockJobManager

      val requests = manager.findRequestsToRun("hello_world")

      whenReady(requests) { r => r should have size 0 }
      ok
    }

    "not find jobs to run with a CANCELLED request" in {
      object MockJobManager extends JobManagerEngine { val cache = mockCacheWithCancelledRequest }
      val manager = MockJobManager

      val requests = manager.findRequestsToRun("hello_world")

      whenReady(requests) { r => r should have size 0 }
      ok
    }

    "not find jobs to run if the request has an invalid status" in {
      object MockJobManager extends JobManagerEngine { val cache = mockCacheWithInvalidRequest }
      val manager = MockJobManager

      val requests = manager.findRequestsToRun("hello_world")

      whenReady(requests) { r => r should have size 0 }
      ok
    }

    "create a job correctly using the cache" in {
      object MockJobManager extends JobManagerEngine { val cache = mockCacheWithValidJob }
      val manager = MockJobManager
      val parser = ISODateTimeFormat.dateTimeNoMillis()
      val timeNow = timestampNow

      val job = manager.createJob(helloWorldRequest, tenMinutes)

      there was one(manager.cache).putJob("bbc.schedulerplus.job:hello_world_abc123", helloWorldJob(timeNow))

      job.id shouldEqual helloWorldRequest.id
      job.lifetimeInMillis shouldEqual tenMinutes
      parser.parseDateTime(job.createdAt) must beNearTo(parser.parseDateTime(timeNow), tenMinutes)

      ok
    }

    "update a job correctly using the cache" in {
      object MockJobManager extends JobManagerEngine { val cache = mockCacheWithOldJob }
      val manager = MockJobManager
      val parser = ISODateTimeFormat.dateTimeNoMillis()
      val timeNow = timestampNow

      val job = manager.updateJob(helloWorldJob(timeNow), tenMinutes)

      there was one(manager.cache).putJob("bbc.schedulerplus.job:hello_world_abc123", helloWorldJob(timeNow))

      job.id shouldEqual helloWorldRequest.id
      job.lifetimeInMillis shouldEqual tenMinutes
      parser.parseDateTime(job.createdAt) must beNearTo(parser.parseDateTime(timeNow), tenMinutes)

      ok
    }

    "delete a job correctly using the cache" in {
      object MockJobManager extends JobManagerEngine { val cache = mockCacheWithOldJob }
      val manager = MockJobManager

      manager.deleteJob(helloWorldJob(timestampNow))

      there was one(manager.cache).deleteJob("bbc.schedulerplus.job:hello_world_abc123")

      ok
    }
  }
}
