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

package bbc.config

import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification

/**
  * Config enrichment
  */
object RichConfigSpec extends Specification {

  import ConfigPlus._

  val integerConfig = ConfigFactory.parseString(("number=6"))
  val stringConfig = ConfigFactory.parseString(("name=six"))

  "RichConfig" should {
    "return the orElse if an integer doesn't exist" in {
      val orElse = integerConfig.getIntOrElse("foo", -1)
      orElse mustEqual -1
      ok
    }

    "return the value if an integer exists" in {
      val value = integerConfig.getIntOrElse("number", -1)
      value mustEqual 6
      ok
    }

    "return the orElse if a string doesn't exist" in {
      val orElse = stringConfig.getStringOrElse("foo", "bar")
      orElse mustEqual "bar"
      ok
    }

    "return the value if a string exists" in {
      val value = stringConfig.getStringOrElse("name", "unknown")
      value mustEqual "six"
      ok
    }
  }
}
