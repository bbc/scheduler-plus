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

package bbc.matchers

import org.joda.time.DateTime
import org.specs2.matcher.{Expectable, Matcher, MatchersImplicits}
import org.specs2.mutable.Specification

/**
  * Contains custom matchers.
  */
trait CustomMatchers extends Specification with MatchersImplicits {

  // scalastyle:off
  /**
    * Matches like `date1 must beNearTo(date2, 60000)` which will match if date2 is within ten minutes of date1
    * @param date
    * @param limit
    * @return
    */
  def beNearTo(date: DateTime, limit: Long) = new Matcher[DateTime] {
    def apply[D <: DateTime](e: Expectable[D]) =
      result((e.value.getMillis - date.getMillis) < limit, "Dates are close", "Dates are far apart", e)
  }
  // scalastyle:on
}
