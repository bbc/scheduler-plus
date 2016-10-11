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

package bbc.schedulerplus.timing

object ExecutionTimeManager {

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
    val tenSeconds = 10000
    lifetimeInMillis + random.nextInt(tenSeconds)
  }
}
