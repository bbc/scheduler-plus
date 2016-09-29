package bbc.schedulerplus.domain

/**
  * Represents a job which is the system response from a JobRequest
  */
case class Job (
 createdAt: String,
 lifetimeInMillis: Long,
 `type`: String,
 id: String
) { def toKey: String = { `type` + "_" + id } }
