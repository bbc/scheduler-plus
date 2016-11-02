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

package bbc.schedulerplus.client

import bbc.schedulerplus.Job

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
    *   log.info(s"Running expensive task for job ${job.toKey}...")
    *   val millisToRunAgain = ExpensiveThing.runExpensiveTask(true)
    *
    *   CallbackResponse(lifetimeInMillis = millisToRunAgain)
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
