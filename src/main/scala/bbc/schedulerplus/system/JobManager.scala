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

import scala.concurrent.Future
import bbc.schedulerplus.persistence.Cache
import bbc.schedulerplus.{Job, Request}

/**
  * Represents the interface to a JobManager
  */
trait JobManager {
  val cache: Cache

  /**
    * Find all of the current Requests that should be run. Looks for a
    * @return
    */
  def findRequestsToRun(jobType: String): Future[Seq[Request]]

  /**
    * Creates a Job object and also creates a job item in the cache with the key [jobRequest.`type` + "_" + jobRequest.id], such as
    * the key the_key_type_123456
    *
    * @param jobRequest
    * @return
    */
  def createJob(jobRequest: Request, lifetimeInMillis: Long): Job

  /**
    * Updates a job by replacing it with one with a new createdAt timestamp and
    * @param job
    * @param lifetimeInMillis
    * @return
    */
  def updateJob(job: Job, lifetimeInMillis: Long): Job

  /**
    * Removes a job from the cache by its key
    * @param job
    * @return
    */
  def deleteJob(job: Job): Boolean
}
