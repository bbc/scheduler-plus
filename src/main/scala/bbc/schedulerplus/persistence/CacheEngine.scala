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

import scala.concurrent.Future
import bbc.schedulerplus.{Job, Request}

/**
  * Encloses the construction of data.
  */
trait CacheEngine {
  import scala.concurrent.ExecutionContext.Implicits.global

  val connection: Connection

  /**
    * Return all keys for job requests with the matching glob
    * @param glob The pattern to search for.
    * @return
    */
  private[schedulerplus] def requestKeys(glob: String): Future[Seq[String]] = connection.redis.keys("bbc.schedulerplus.request*")

  /**
    * Return all keys for jobs with the matching glob
    * @param glob The pattern to search for.
    * @return
    */
  private[schedulerplus] def jobKeys(glob: String): Future[Seq[String]] = connection.redis.keys("bbc.schedulerplus.job*")

  /**
    * Obtain job request objects from a future list of keys
    * @param keysFuture
    * @return
    */
  private[schedulerplus] def requestsFromKeys(keysFuture: Future[Seq[String]]): Future[Seq[Request]] = {

    val requestsFuture =
      for { requestKeys <- keysFuture }
        yield for { requestKey <- requestKeys }
          yield {
            val fjr = request(requestKey)
            for { jr <- fjr } yield jr.get
          }

    requestsFuture flatMap { Future.traverse(_) { jobRequests => jobRequests }}
  }

  /**
    * Obtain job request objects from a future list of keys
    * @param keysFuture
    * @return
    */
  private[schedulerplus] def jobsFromKeys(keysFuture: Future[Seq[String]]): Future[Seq[Job]] = {

    val jobsFuture =
      for { jobKeys <- keysFuture }
        yield
          for {jobKey <- jobKeys}
            yield {
              val fj = job(jobKey)
              for {j <- fj} yield j.get
            }

    jobsFuture flatMap { Future.traverse(_) { jobs => jobs }}
  }

  /**
    * Inserts a Job into the cache.
    * @param key
    */
  def putJob(key: String, item: Job): Unit = connection.redis.set[Job](key, item)

  /**
    * Inserts a JobRequest into the cache.
    * @param key
    */
  def putRequest(key: String, item: Request): Unit = connection.redis.set[Request](key, item)

  /**
    * Deletes an item from the cache by its key
    * @param key
    */
  def deleteJob(key: String): Unit = connection.redis.del(key)

  /**
    * Returns a single JobRequest given it's key, such as 'episode_summary_b07lf5sf'
    * @param key
    * @return
    */
  def request(key: String): Future[Option[Request]] = connection.redis.get[Request](key)


  /**
    * Returns all of the job requests for a particular type, such as 'the_job_type'.
    * @param jobType
    * @return
    */
  def requests(jobType: String): Future[Seq[Request]] =
    requestsFromKeys(requestKeys(s"bbc.schedulerplus.request:$jobType*"))

  /**
    * Returns a single Job given its key, such as 'the_job_type_123456'
    * @param key
    * @return
    */
  def job(key: String): Future[Option[Job]] = connection.redis.get[Job](key)

  /**
    * Returns all of the job requests for a particular type, such as 'the_job_type'.
    * @param jobType
    * @return
    */
  def jobs(jobType: String): Future[Seq[Job]] = jobsFromKeys(jobKeys(s"bbc.schedulerplus.job:$jobType*"))
}
