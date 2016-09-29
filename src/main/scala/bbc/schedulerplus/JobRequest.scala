package bbc.schedulerplus

/**
  * Represents a job request which triggers a scheduled job
  */
case class JobRequest(
 `type`: String,
 id: String,
 status: String
) { def toKey: String = { `type` + "_" + id } }
