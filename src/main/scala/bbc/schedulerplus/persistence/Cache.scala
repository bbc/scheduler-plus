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

import bbc.schedulerplus.{Job, Request}
import scala.concurrent.Future

/**
  * Represents the interface for a Cache object
  */
trait Cache {
  val connection: Connection

  def putJob(key: String, item: Job): Unit

  /**
    * Inserts a JobRequest into the cache.
    * @param key
    */
  def putRequest(key: String, item: Request): Unit

  /**
    * Deletes an item from the cache by its key
    * @param key
    */
  def deleteJob(key: String): Unit

  /**
    * Returns a single JobRequest given it's key, such as 'episode_summary_b07lf5sf'
    * @param key
    * @return
    */
  def request(key: String): Future[Option[Request]]


  /**
    * Returns all of the job requests for a particular type, such as 'the_job_type'.
    * @param jobType
    * @return
    */
  def requests(jobType: String): Future[Seq[Request]]

  /**
    * Returns a single Job given its key, such as 'the_job_type_123456'
    * @param key
    * @return
    */
  def job(key: String): Future[Option[Job]]

  /**
    * Returns all of the job requests for a particular type, such as 'the_job_type'.
    * @param jobType
    * @return
    */
  def jobs(jobType: String): Future[Seq[Job]]
}
