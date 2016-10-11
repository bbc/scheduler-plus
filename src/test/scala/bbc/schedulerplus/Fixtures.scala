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

import bbc.schedulerplus.persistence.{Cache, CacheEngine, Connection}
import org.mockito.Matchers.anyString
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.specs2.mock.Mockito
import redis.RedisClient

import scala.concurrent.Future

/**
  * Provides fixtures for the Spec tests
  */
trait Fixtures extends Mockito {
  import scala.concurrent.ExecutionContext.Implicits.global

  val tenMinutes = 600000

  val helloWorldRequest = Request("abc123", "hello_world", "live")
  val helloWorldPausedRequest = Request("abc123", "hello_world", "paused")
  val helloWorldCancelledRequest = Request("abc123", "hello_world", "cancelled")
  val helloWorldInvalidRequest = Request("abc123", "hello_world", "foo")

  def timestampOneDayAgo: String = {
    val timestamp = new DateTime()
    val format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
    timestamp.minusDays(1).toString(format)
  }

  def timestampNow: String = {
    val timestamp = new DateTime()
    val format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
    timestamp.toString(format)
  }

  def helloWorldJob(timestamp: String): Job = Job(helloWorldRequest.id, helloWorldRequest.`type`, timestamp, tenMinutes)

  trait MockCache extends Cache with CacheEngine {
    object MockConnection extends Connection {
      val redis = mock[RedisClient]
      redis.keys("bbc.schedulerplus.request*") returns Future(Seq("bbc.schedulerplus.request:hello_world_abc123"))
      redis.keys("bbc.schedulerplus.job*") returns Future(Seq("bbc.schedulerplus.job:hello_world_abc123"))
      redis.get[Request]("bbc.schedulerplus.request:hello_world_abc123") returns Future(Option(helloWorldRequest))
      redis.get[Job]("cowboy") returns Future(Some(helloWorldJob(timestampNow)))
      redis.get[Job]("bbc.schedulerplus.job:hello_world_abc123") returns Future(Some(helloWorldJob(timestampNow)))
    }

    val connection = MockConnection
  }

  val mockCacheWithValidJob = mock[Cache]
  mockCacheWithValidJob.job(anyString) returns Future(Some(helloWorldJob(timestampNow)))
  mockCacheWithValidJob.jobs(anyString) returns Future(Seq(helloWorldJob(timestampNow)))
  mockCacheWithValidJob.request(anyString) returns Future(Some(helloWorldRequest))
  mockCacheWithValidJob.requests(anyString) returns Future(Seq(helloWorldRequest))

  val mockCacheWithNoJob = mock[Cache]
  mockCacheWithNoJob.job(anyString) returns Future(None)
  mockCacheWithNoJob.jobs(anyString) returns Future(Seq())
  mockCacheWithNoJob.request(anyString) returns Future(Some(helloWorldRequest))
  mockCacheWithNoJob.requests(anyString) returns Future(Seq(helloWorldRequest))

  val mockCacheWithStalledJob = mock[Cache]
  mockCacheWithStalledJob.job(anyString) returns Future(Some(helloWorldJob(timestampOneDayAgo)))
  mockCacheWithStalledJob.jobs(anyString) returns Future(Seq(helloWorldJob(timestampOneDayAgo)))
  mockCacheWithStalledJob.request(anyString) returns Future(Some(helloWorldRequest))
  mockCacheWithStalledJob.requests(anyString) returns Future(Seq(helloWorldRequest))

  val mockCacheWithPausedRequest = mock[Cache]
  mockCacheWithPausedRequest.job(anyString) returns Future(Some(helloWorldJob(timestampNow)))
  mockCacheWithPausedRequest.jobs(anyString) returns Future(Seq(helloWorldJob(timestampNow)))
  mockCacheWithPausedRequest.request(anyString) returns Future(Some(helloWorldPausedRequest))
  mockCacheWithPausedRequest.requests(anyString) returns Future(Seq(helloWorldPausedRequest))

  val mockCacheWithCancelledRequest = mock[Cache]
  mockCacheWithCancelledRequest.job(anyString) returns Future(Some(helloWorldJob(timestampNow)))
  mockCacheWithCancelledRequest.jobs(anyString) returns Future(Seq(helloWorldJob(timestampNow)))
  mockCacheWithCancelledRequest.request(anyString) returns Future(Some(helloWorldCancelledRequest))
  mockCacheWithCancelledRequest.requests(anyString) returns Future(Seq(helloWorldCancelledRequest))

  val mockCacheWithInvalidRequest = mock[Cache]
  mockCacheWithInvalidRequest.job(anyString) returns Future(Some(helloWorldJob(timestampNow)))
  mockCacheWithInvalidRequest.jobs(anyString) returns Future(Seq(helloWorldJob(timestampNow)))
  mockCacheWithInvalidRequest.request(anyString) returns Future(Some(helloWorldInvalidRequest))
  mockCacheWithInvalidRequest.requests(anyString) returns Future(Seq(helloWorldInvalidRequest))

  val mockCacheWithOldJob = mock[Cache]
  mockCacheWithOldJob.job(anyString) returns Future(Some(helloWorldJob(timestampOneDayAgo)))
  mockCacheWithOldJob.jobs(anyString) returns Future(Seq(helloWorldJob(timestampOneDayAgo)))
  mockCacheWithOldJob.request(anyString) returns Future(Some(helloWorldRequest))
  mockCacheWithOldJob.requests(anyString) returns Future(Seq(helloWorldRequest))
}
