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

package bbc.schedulerplus.persistence

import bbc.schedulerplus.{Fixtures, Job, Request}
import org.scalatest.concurrent.ScalaFutures
import org.specs2.mutable.Specification

object CacheEngineSpec extends Specification with ScalaFutures with Fixtures {

  import scala.concurrent.ExecutionContext.Implicits.global

  "CacheData" should {
    "execute a set[Job] in the underlying client when putting a job" in {
      object MockCacheObj extends MockCache {}
      val cache = MockCacheObj
      val theJob = helloWorldJob(timestampNow)

      cache.putJob("bbc.schedulerplus.job:hello_world_abc123", theJob)

      there was one(cache.connection.redis).set[Job]("bbc.schedulerplus.job:hello_world_abc123", theJob)

      ok
    }

    "execute a set[Request] in the underlying client when putting a request" in {
      object MockCacheObj extends MockCache {}
      val cache = MockCacheObj
      cache.putRequest("bbc.schedulerplus.request:hello_world_abc123", helloWorldRequest)

      there was one(cache.connection.redis).set[Request]("bbc.schedulerplus.request:hello_world_abc123", helloWorldRequest)
      ok
    }

    "execute a get[Request] in the underlying client when calling request" in {
      object MockCacheObj extends MockCache {}
      val cache = MockCacheObj
      cache.request("bbc.schedulerplus.request:hello_world_abc123")

      there was one(cache.connection.redis).get[Request]("bbc.schedulerplus.request:hello_world_abc123")
      ok
    }

    "execute a del in the underlying client when deleting a job" in {
      object MockCacheObj extends MockCache {}
      val cache = MockCacheObj
      cache.deleteJob("bbc.schedulerplus.job:hello_world_abc123")

      there was one(cache.connection.redis).del("bbc.schedulerplus.job:hello_world_abc123")
      ok
    }

    "execute a keys in the underlying client when obtaining request keys" in {
      object MockCacheObj extends MockCache {}
      val cache = MockCacheObj
      cache.requestKeys("hello_world")

      there was one(cache.connection.redis).keys("bbc.schedulerplus.request*")
      ok
    }

    "execute a keys in the underlying client when obtaining job keys" in {
      object MockCacheObj extends MockCache {}
      val cache = MockCacheObj
      cache.jobKeys("hello_world")

      there was one(cache.connection.redis).keys("bbc.schedulerplus.job*")
      ok
    }

    "return requests for job type" in {
      object MockCacheObj extends MockCache {}
      val cache = MockCacheObj

      val requests = cache.requests("hello_world")

      whenReady(requests) { r => r should have size 1 }
      ok
    }

    "return a job for a cowboy" in {
      object MockCacheObj extends MockCache {}
      val cache = MockCacheObj

      val theJob = cache.job("cowboy")

      whenReady(theJob) ( { j => j.get.id shouldEqual "abc123" })

      ok
    }

    "return jobs for a type" in {
      object MockCacheObj extends MockCache {}
      val cache = MockCacheObj

      val theJob = cache.jobs("hello_world")

      whenReady(theJob) ( { js => js should have size 1 })

      ok
    }
  }
}
