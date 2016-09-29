package bbc.schedulerplus.marshalling

import bbc.schedulerplus.{Job, JobRequest}
import spray.json.{DefaultJsonProtocol, NullOptions}

/**
  * JSON format protocol for case classes
  */
trait AppJsonProtocol extends DefaultJsonProtocol with NullOptions with JodaDateTimeFormats {
  implicit val jobFormat = jsonFormat(Job, "createdAt", "lifetimeInMillis", "type", "id")
  implicit val jobRequestFormat = jsonFormat(JobRequest, "type", "id", "status")

}
