package bbc.domain.marshalling

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._

trait JodaDateTimeFormats {

  implicit object JodaDateTimeFormat extends JsonFormat[DateTime] {
    val formatter = ISODateTimeFormat.dateTimeNoMillis

    def write(d: DateTime): JsValue = JsString(formatter.print(d))

    def read(json: JsValue): DateTime = json match {
      case JsString(s) => formatter.parseDateTime(s)
      case x => throw new DeserializationException(s"Expected a joda DateTime instead of $x")
    }
  }
}
