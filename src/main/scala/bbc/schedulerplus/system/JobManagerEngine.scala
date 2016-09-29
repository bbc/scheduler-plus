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

import bbc.schedulerplus.persistence.Cache
import bbc.schedulerplus.{Job, Request}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}

import scala.concurrent.Future

/**
  * Encloses the business logic to for job and job request data
  */
trait JobManagerEngine {

  import scala.concurrent.ExecutionContext.Implicits.global

  val cache: Cache

  /**
    * Return all Jobs that has an id that matches the supplied JobRequest's id
    * @param jobs
    * @param jobRequest
    * @return
    */
  private def matchingJobs(jobs: Seq[Job], jobRequest: Request): Seq[Job] = jobs.filter(j => j.id == jobRequest.id)

  /**
    * Return all Jobs which are 'active', where the createdAt time plus the lifeTimeInMillis is before the current system time.
    * @param jobs
    * @return
    */
  private def activeJobs(jobs: Seq[Job]): Seq[Job] = {
    val now = System.currentTimeMillis()
    val parser = ISODateTimeFormat.dateTimeNoMillis()

    jobs.filter(j => parser.parseDateTime(j.createdAt).plus(j.lifetimeInMillis).isAfter(now))
  }

  /**
    * Return a Set of all of the ids from a List of Requests
    * @param requests
    * @return
    */
  private def requestIds(requests: Seq[Request]): Set[String] = { for(r <- requests) yield r.id }.toSet

  /**
    * Return a Set of all of the ids from a List of Jobs
    * @param jobs
    * @return
    */
  private def jobIds(jobs: Seq[Job]): Set[String] = { for(a <- jobs) yield a.id}.toSet

  /**
    * Return job requests which should be run. Will return a job request which doesn't have a matching job or if the job
    * has stalled (i.e. the `createdAt` + `livetimeInMillis` is less than the current time.
    *
    * @param requests
    * @param jobs
    * @return
    */
  private def requestsToRun(requests: Seq[Request], jobs: Seq[Job]): Seq[Request] = {
    if (jobs.size <= 0) requests else {
      val matched: Seq[Job] = { for (jobRequest <- requests) yield matchingJobs(jobs, jobRequest) }.flatten
      val active = activeJobs(matched)

      if (active.size <= 0) requests else {
        val toRunIds = requestIds(requests).filterNot(jobIds(active))
        for (jobRequest <- requests if toRunIds.contains(jobRequest.id)) yield jobRequest
      }
    }
  }

  /**
    * Return the current system time as an ISO8601-format string
    * @return
    */
  private def now: String = {
    val timestamp = new DateTime()
    val format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
    timestamp.toString(format)
  }

  /**
    * Find all of the current Requests that should be run. Looks for a
    * @return
    */
  def findRequestsToRun(jobType: String): Future[Seq[Request]] = {
    val requestsFuture = cache.requests(jobType)
    val jobsFuture = cache.jobs(jobType)

    for {
      requests <- requestsFuture
      jobs <- jobsFuture
    } yield requestsToRun(requests, jobs)
  }

  /**
    * Creates a Job object and also creates a job item in the cache with the key [jobRequest.`type` + "_" + jobRequest.id], such as
    * the key the_key_type_123456
    *
    * @param jobRequest
    * @return
    */
  def createJob(jobRequest: Request, lifetimeInMillis: Long): Job = {
    val job = Job(
      createdAt = now,
      lifetimeInMillis = lifetimeInMillis,
      `type` = jobRequest.`type`,
      id = jobRequest.id
    )

    cache.putJob(key = "bbc.schedulerplus.job:" + job.toKey, item = job)

    job
  }

  /**
    * Updates a job by replacing it with one with a new createdAt timestamp and
    * @param job
    * @param lifetimeInMillis
    * @return
    */
  def updateJob(job: Job, lifetimeInMillis: Long): Job = {
    val newJob = Job(
      createdAt = now,
      lifetimeInMillis = lifetimeInMillis,
      `type` = job.`type`,
      id = job.id
    )

    cache.putJob(key = "bbc.schedulerplus.job:" + job.toKey, item = newJob)

    newJob
  }

  /**
    * Removes a job from the cache by its key
    * @param job
    * @return
    */
  def deleteJob(job: Job): Boolean = {
    cache.deleteJob(key = "bbc.schedulerplus.job:" + job.toKey)
    true
  }
}
