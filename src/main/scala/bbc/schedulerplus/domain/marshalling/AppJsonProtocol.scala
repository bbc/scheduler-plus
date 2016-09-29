package bbc.schedulerplus.domain.marshalling

import spray.json.{DefaultJsonProtocol, NullOptions}
import bbc.schedulerplus.domain.{Job, JobRequest}

/**
  * JSON format protocol for case classes
  */
trait AppJsonProtocol extends DefaultJsonProtocol with NullOptions with JodaDateTimeFormats {
  implicit val jobFormat = jsonFormat(Job, "createdAt", "lifetimeInMillis", "type", "id")
  implicit val jobRequestFormat = jsonFormat(JobRequest, "type", "id", "status")

}
