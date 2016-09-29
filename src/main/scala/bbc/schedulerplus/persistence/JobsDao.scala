package bbc.schedulerplus.persistence

import scala.concurrent.Future
import akka.event.Logging
import bbc.AppContext
import bbc.persistence.{OverwritingCacheFactory, RedisCache}
import bbc.schedulerplus.{Job, JobRequest}
import bbc.schedulerplus.marshalling.AppJsonProtocol

/**
  * The data access object for jobs, providing an interface over the cache.
  */
object JobsDao extends AppJsonProtocol {

  import scala.concurrent.ExecutionContext.Implicits.global

  val jobCache = OverwritingCacheFactory[Job]()
  val jobRequestCache = OverwritingCacheFactory[JobRequest]()

  val log = Logging(AppContext.actorSystem, getClass)

  /**
    * Return the last part of a key, without the type prefix which the Redis library uses.
    * @param key A String, such as bbc.schedulerplus.JobRequest:the_job_type_123456
    * @return A String, such as the_job_type_123456
    */
  private def stripKeyType(key: String): String = key.split(":").last

  /**
    * Build a future Job from a key
    * @param key
    * @return
    */
  private def buildJob(key: String): Option[Future[Job]] = job(key)

  /**
    * Return all keys for job requests with the matching glob
    * @param glob The pattern to search for.
    * @return
    */
  private def jobRequestKeys(glob: String): Future[List[String]] =
  jobRequestCache.asInstanceOf[RedisCache[JobRequest]].keys(glob)


  /**
    * Return all keys for jobs with the matching glob
    * @param glob The pattern to search for.
    * @return
    */
  private def jobKeys(glob: String): Future[List[String]] = jobCache.asInstanceOf[RedisCache[Job]].keys(glob)

  /**
    * Obtain job request objects from a future list of keys
    * @param keysFuture
    * @return
    */
  private def jobRequestsFromKeys(keysFuture: Future[List[String]]): Future[List[JobRequest]] = {
    val jobRequestKeysFuture: Future[List[String]] = for { jobRequests <- keysFuture } yield jobRequests

    val jobRequestsFuture =
      for { jobRequestKeys <- jobRequestKeysFuture }
        yield for { jobRequestKey <- jobRequestKeys }
          yield jobRequest(stripKeyType(jobRequestKey)).get

    jobRequestsFuture flatMap { Future.traverse(_) { jobRequests => jobRequests }}
  }

  /**
    * Obtain job request objects from a future list of keys
    * @param keysFuture
    * @return
    */
  private def jobsFromKeys(keysFuture: Future[List[String]]): Future[List[Job]] = {
    val jobKeysFuture: Future[List[String]] = for { jobs <- keysFuture } yield jobs

    val jobsFuture =
      for { jobKeys <- jobKeysFuture }
        yield for { jobKey <- jobKeys }
          yield job(stripKeyType(jobKey)).get

    jobsFuture flatMap { Future.traverse(_) { jobs => jobs }}
  }

  /**
    * Inserts a Job into the cache.
    * @param key
    */
  def putJob(key: String, item: Job): Unit = jobCache(key) { item }

  /**
    * Inserts a JobRequest into the cache.
    * @param key
    */
  def putJob(key: String, item: JobRequest): Unit = jobRequestCache(key) { item }

  /**
    * Deletes an item from the cache by its key
    * @param key
    */
  def deleteJob(key: String): Unit = jobCache.remove(key)

  /**
    * Returns a single JobRequest given it's key, such as 'episode_summary_b07lf5sf'
    * @param key
    * @return
    */
  def jobRequest(key: String): Option[Future[JobRequest]] = jobRequestCache.get(key)

  /**
    * Returns all of the job requests for a particular type, such as 'the_job_type'.
    * @param jobType
    * @return
    */
  def jobRequests(jobType: String): Future[List[JobRequest]] =
  jobRequestsFromKeys(jobRequestKeys(s"bbc.schedulerplus.JobRequest:$jobType*"))

  /**
    * Returns a single Job given its key, such as 'the_job_type_123456'
    * @param key
    * @return
    */
  def job(key: String): Option[Future[Job]] = jobCache.get(key)

  /**
    * Returns all of the job requests for a particular type, such as 'the_job_type'.
    * @param jobType
    * @return
    */
  def jobs(jobType: String): Future[List[Job]] = jobsFromKeys(jobKeys(s"bbc.schedulerplus.Job:$jobType*"))
}



