package bbc.domain

import bbc.domain.marshalling.JodaDateTimeFormats
import bbc.persistence.sync.{Job, JobRequest}
import spray.json.{DefaultJsonProtocol, JsonFormat, NullOptions, RootJsonFormat}

/**
  * JSON format protocol for case classes
  */
trait AppJsonProtocol extends DefaultJsonProtocol with NullOptions with JodaDateTimeFormats {
  implicit val jobFormat = jsonFormat(Job, "createdAt", "lifetimeInMillis", "type", "id")
  implicit val jobRequestFormat = jsonFormat(JobRequest, "type", "id", "status")

}
