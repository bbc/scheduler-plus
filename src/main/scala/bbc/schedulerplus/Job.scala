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
  * Represents a job which is the system response from a JobRequest
  */
case class Job (
  id: String,
  `type`: String,
  createdAt: String,
  lifetimeInMillis: Long
) { def toKey: String = { `type` + "_" + id } }

object Job {
  implicit val byteStringFormatter = new ByteStringFormatter[Job] {
    def serialize(data: Job): ByteString = {
      ByteString(
        "id=" + data.id +
        "|type=" + data.`type` +
        "|createdAt=" + data.createdAt +
        "|lifetimeInMillis=" + data.lifetimeInMillis
      )
    }

    def deserialize(bs: ByteString): Job = {
      val r = bs.utf8String.split('|').toList

      if (r.size == 4) {
        Job(
          id = r.filter(_.startsWith("id=")).head.split("=")(1),
          `type` = r.filter(_.startsWith("type=")).head.split("=")(1),
          createdAt = r.filter(_.startsWith("createdAt=")).head.split("=")(1),
          lifetimeInMillis = r.filter(_.startsWith("lifetimeInMillis=")).head.split("=")(1).toLong
        )
      } else {
        throw new Exception("Job cannot be created with: " + bs.utf8String)
      }
    }
  }
}
