package bbc.schedulerplus.client

import bbc.schedulerplus.domain.Job

/**
  * Represents callbacks for a particular job, based on the supplied key, which relates to a job type. The
  * implementation is supplied by the client of the system and maps Job objects to the correct callback for that job.
  */
trait Callbacks {

  /**
    * Returns an anonymous function which should be executed when the system has to deal with a job matching the key.
    *
    * For example, the callback for the "expensive_task" key might be:
    * () => {
    *   log.info("Running expensive task for job " + job.toKey + "...")
    *   ExpensiveThing.runExpensiveTask(true)
    * }
    *
    * This would then be executed each time that job is run. The implementation must return an anonymous function (of
    * the form () => { ... }) which will be returned when the system calls this function, passing in a key.
    *
    * @param job
    * @return
    */
  def callbackFor(job: Job):() => CallbackResponse

  /**
    * Returns a list of keys for which there are callbacks for
    * @return
    */
  def keys: List[String]
}
