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

import akka.util.ByteString
import org.specs2.mutable.Specification

object JobSpec extends Specification with Fixtures {

  "Job" should {
    "render toKey properly" in {
      // scalastyle:off
      val job = Job("abc123", "hello_world", "2016-10-06T11:30:21+0100", 1234)
      // scalastyle:on

      job.toKey mustEqual "hello_world_abc123"
    }

    "build a correct job from default constructor args" in {
      // scalastyle:off
      val job = Job("abc123", "hello_world", "2016-10-06T11:30:21+0100", 1234)
      // scalastyle:on

      job.id mustEqual "abc123"
      job.`type` mustEqual "hello_world"
      job.createdAt mustEqual "2016-10-06T11:30:21+0100"
      job.lifetimeInMillis mustEqual 1234
    }

    "deserialize correctly as a job from a string with any order values" in {
      val s1 = "id=abc123|type=hello_world|createdAt=2016-10-06T11:30:21+0100|lifetimeInMillis=3810"
      val s2 = "type=hello_world|createdAt=2016-10-06T11:30:21+0100|lifetimeInMillis=3810|id=abc123"
      val s3 = "createdAt=2016-10-06T11:30:21+0100|lifetimeInMillis=3810|id=abc123|type=hello_world"
      val s4 = "lifetimeInMillis=3810|id=abc123|type=hello_world|createdAt=2016-10-06T11:30:21+0100"

      val job1 = Job.byteStringFormatter.deserialize(ByteString(s1))
      job1.id mustEqual "abc123"
      job1.`type` mustEqual "hello_world"
      job1.createdAt mustEqual "2016-10-06T11:30:21+0100"
      job1.lifetimeInMillis mustEqual 3810

      val job2 = Job.byteStringFormatter.deserialize(ByteString(s2))
      job2.id mustEqual "abc123"
      job2.`type` mustEqual "hello_world"
      job2.createdAt mustEqual "2016-10-06T11:30:21+0100"
      job2.lifetimeInMillis mustEqual 3810

      val job3 = Job.byteStringFormatter.deserialize(ByteString(s3))
      job3.id mustEqual "abc123"
      job3.`type` mustEqual "hello_world"
      job3.createdAt mustEqual "2016-10-06T11:30:21+0100"
      job3.lifetimeInMillis mustEqual 3810

      val job4 = Job.byteStringFormatter.deserialize(ByteString(s4))
      job4.id mustEqual "abc123"
      job4.`type` mustEqual "hello_world"
      job4.createdAt mustEqual "2016-10-06T11:30:21+0100"
      job4.lifetimeInMillis mustEqual 3810

      ok
    }

    "throw an exception with a string containing the wrong number of values" in {
      val s1 = "type=hello_world|createdAt=2016-10-06T11:30:21+0100|lifetimeInMillis=3810"
      val s2 = "foo=bar|id=abc123|type=hello_world|createdAt=2016-10-06T11:30:21+0100|lifetimeInMillis=3810"

      Job.byteStringFormatter.deserialize(ByteString(s1)) should throwA[Exception]
      Job.byteStringFormatter.deserialize(ByteString(s2)) should throwA[Exception]
    }

    "serialize correctly as a byte string" in {
      // scalastyle:off
      val job = Job("abc123", "hello_world", "2016-10-06T11:30:21+0100", 3810)
      // scalastyle:on

      val jobByteString = Job.byteStringFormatter.serialize(job)

      jobByteString.utf8String mustEqual "id=abc123|type=hello_world|createdAt=2016-10-06T11:30:21+0100|lifetimeInMillis=3810"
      ok
    }
  }
}
