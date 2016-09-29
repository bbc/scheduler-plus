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
import redis.ByteStringFormatter

/**
  * Represents a job request which triggers a scheduled job
  */
case class Request(
  id: String,
  `type`: String,
  status: String
) { def toKey: String = { `type` + "_" + id } }

object Request {
  implicit val byteStringFormatter = new ByteStringFormatter[Request] {
    def serialize(data: Request): ByteString = {
      ByteString(
        "id=" + data.id +
        "|type=" + data.`type` +
        "|status=" + data.status
      )
    }

    def deserialize(bs: ByteString): Request = {
      val r = bs.utf8String.split('|').toList

      if (r.size == 3) {
        Request(
          id = r.filter(_.startsWith("id=")).head.split("=")(1),
          `type` = r.filter(_.startsWith("type=")).head.split("=")(1),
          status = r.filter(_.startsWith("status=")).head.split("=")(1)
        )
      } else {
        throw new Exception("Request cannot be created with " + bs.utf8String)
      }
    }
  }
}
