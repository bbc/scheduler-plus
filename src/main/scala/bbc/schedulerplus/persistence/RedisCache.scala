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

import redis.RedisClient
import com.typesafe.config.ConfigFactory
import bbc.SchedulerPlusContext
import bbc.config.ConfigPlus

/**
  * The data access object for jobs, providing an interface over the cache.
  */
object RedisCache extends Cache with CacheEngine {

  import ConfigPlus._

  implicit val akkaSystem = SchedulerPlusContext.akkaSystem

  lazy val config = ConfigFactory.load()

  val defaultHost = "localhost"
  val defaultPort = 6379

  lazy val host = config.getStringOrElse("schedulerplus.cache.host", defaultHost)
  lazy val port = config.getIntOrElse("schedulerplus.cache.port", defaultPort)

  object RedisConnector extends Connection {
    val redis = RedisClient(
      host = host,
      port = port
    )
  }

  val connection = RedisConnector
}
