package bbc.schedulerplus.system

import akka.event.Logging
import bbc.AppContext
import bbc.schedulerplus.persistence.JobsDao
import bbc.schedulerplus.{Job, JobRequest}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}

import scala.concurrent.Future

/**
  * Manages all of the jobs
  */
object JobManager {

  import scala.concurrent.ExecutionContext.Implicits.global

  val log = Logging(AppContext.actorSystem, getClass)

  /**
    * Return all Jobs that has an id that matches the supplied JobRequest's id
    * @param jobs
    * @param jobRequest
    * @return
    */
  private def matchingJobs(jobs: List[Job], jobRequest: JobRequest): List[Job] = jobs.filter(j => j.id == jobRequest.id)

  /**
    * Return all Jobs which are 'active', where the createdAt time plus the lifeTimeInMillis is before the current system time.
    * @param jobs
    * @return
    */
  private def activeJobs(jobs: List[Job]): List[Job] = {
    val now = System.currentTimeMillis()
    val parser = ISODateTimeFormat.dateTimeNoMillis()

    jobs.filter(j => parser.parseDateTime(j.createdAt).plus(j.lifetimeInMillis).isAfter(now))
  }

  /**
    * Return a Set of all of the ids from a List of JobRequests
    * @param jobRequests
    * @return
    */
  private def jobRequestIds(jobRequests: List[JobRequest]): Set[String] = { for(r <- jobRequests) yield r.id }.toSet

  /**
    * Return a Set of all of the ids from a List of Jobs
    * @param jobs
    * @return
    */
  private def jobIds(jobs: List[Job]): Set[String] = { for(a <- jobs) yield a.id}.toSet

  /**
    * Return job requests which should be run. Will return a job request which doesn't have a matching job or if the job
    * has stalled (i.e. the `createdAt` + `livetimeInMillis` is less than the current time.
    *
    * @param jobRequests
    * @param jobs
    * @return
    */
  private def jobRequestsToRun(jobRequests: List[JobRequest], jobs: List[Job]): List[JobRequest] = {
    if (jobs.size <= 0) jobRequests else { // no jobs so just return all requests (bootstrap)
    val matched: List[Job] = { for (jobRequest <- jobRequests) yield matchingJobs(jobs, jobRequest) }.flatten
      val active = activeJobs(matched)

      if (active.size <= 0) jobRequests else {
        val toRunIds = jobRequestIds(jobRequests).filterNot(jobIds(active))
        val toRun = for (jobRequest <- jobRequests if toRunIds.contains(jobRequest.id)) yield jobRequest

        log.debug("jobRequestsToRun[toRun]: " + toRun)

        toRun
      }
    }
  }

  /**
    * Find all of the current JobRequests that should be run. Looks for a
    * @return
    */
  def findJobRequestsToRun(jobType: String): Future[List[JobRequest]] = {
    val jobRequestsFuture = JobsDao.jobRequests(jobType)
    val jobsFuture = JobsDao.jobs(jobType)

    // Synchronise job requests and jobs
    for {
      jobRequests <- jobRequestsFuture
      jobs <- jobsFuture
    } yield jobRequestsToRun(jobRequests, jobs)
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
    * Creates a Job object and also creates a job item in the cache with the key [jobRequest.`type` + "_" + jobRequest.id], such as
    * the key the_key_type_123456
    *
    * @param jobRequest
    * @return
    */
  def createJob(jobRequest: JobRequest, lifetimeInMillis: Long): Job = {
    val job = Job(
      createdAt = now,
      lifetimeInMillis = lifetimeInMillis,
      `type` = jobRequest.`type`,
      id = jobRequest.id
    )

    JobsDao.putJob(key = job.toKey, item = job)

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

    // overwrite the old job by using the same key
    JobsDao.putJob(key = job.toKey, item = newJob)

    newJob
  }

  def deleteJob(job: Job): Boolean = {
    JobsDao.deleteJob(key = job.toKey)
    true
  }
}
