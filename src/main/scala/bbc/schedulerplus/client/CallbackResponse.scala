package bbc.schedulerplus.client

/**
  * Represents the result of a client callback, used to return the lifetime of the result of the callback (when the
  * callback should be executed again)
  *
  * @param lifetimeInMillis
  */
case class CallbackResponse(
 lifetimeInMillis: Long
)
