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

object RequestSpec extends Specification with Fixtures {
  "Job" should {
    "render toKey properly" in {
      val request = Request("abc123", "hello_world", "live")

      request.toKey mustEqual "hello_world_abc123"
    }

    "build a correct request from default constructor args" in {
      val request = Request("abc123", "hello_world", "live")

      request.id mustEqual "abc123"
      request.`type` mustEqual "hello_world"
      request.status mustEqual "live"
    }

    "deserialize correctly as a job from a string with any order values" in {
      val s1 = "id=abc123|type=hello_world|status=live"
      val s2 = "type=hello_world|status=live|id=abc123"
      val s3 = "status=live|id=abc123|type=hello_world"

      val request1 = Request.byteStringFormatter.deserialize(ByteString(s1))
      request1.id mustEqual "abc123"
      request1.`type` mustEqual "hello_world"
      request1.status mustEqual "live"

      val request2 = Request.byteStringFormatter.deserialize(ByteString(s2))
      request2.id mustEqual "abc123"
      request2.`type` mustEqual "hello_world"
      request2.status mustEqual "live"

      val request3 = Request.byteStringFormatter.deserialize(ByteString(s3))
      request3.id mustEqual "abc123"
      request3.`type` mustEqual "hello_world"
      request3.status mustEqual "live"

      ok
    }

    "throw an exception with a string containing the wrong number of values" in {
      val s1 = "foo=bar|id=abc123|type=hello_world|status=live"
      val s2 = "type=hello_world|status=live"

      Request.byteStringFormatter.deserialize(ByteString(s1)) should throwA[Exception]
      Request.byteStringFormatter.deserialize(ByteString(s2)) should throwA[Exception]
    }

    "serialize correctly as a byte string" in {
      val request = Request("abc123", "hello_world", "live")

      val requestByteString = Request.byteStringFormatter.serialize(request)

      requestByteString.utf8String mustEqual "id=abc123|type=hello_world|status=live"

      ok
    }
  }
}

