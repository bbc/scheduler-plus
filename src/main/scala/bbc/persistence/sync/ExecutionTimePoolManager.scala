package bbc.persistence.sync

import org.joda.time.DateTime

object ExecutionTimePoolManager {

  /**
    * Returns a number of milliseconds until this type of job can be executed, so the job must be scheduled for this
    * time. The returned number of milliseconds is at least the lifetimeInMillis and may be either very close to that
    * number (when the system is not under heavy load) or it may be longer than lifetimeInMillis if the system needs
    * this job to be held for a while as it has other jobs to complete first. This means jobs can be scheduled to be
    * run when the system is ready and not have processes hanging around.
    *
    * @param `type`
    * @param lifetimeInMillis
    * @return
    */
  def nextMillis(`type`: String, lifetimeInMillis: Long): Long = {
    // todo return actual next second after request.lifeTimeInMillis taking weighting into account and with some 'jitter'
    val random = scala.util.Random
    lifetimeInMillis + random.nextInt(10)
  }
}
