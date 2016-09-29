package bbc.persistence

import scala.concurrent.duration.Duration
import spray.caching.Cache
import spray.json.{JsonReader, JsonWriter}
import bbc.AppContext

/**
  * Cache Factory which returns a spray compatible client for Redis.
  * This also supports always overwriting contents of cache with result of execution of a lambda.
  */
object OverwritingCacheFactory {

  implicit val system = AppContext.cacheSystem

  def apply[V](expiry: Duration = Duration.Inf, prefix: Option[String] = None)
              (implicit m: reflect.Manifest[V], reader: JsonReader[V], writer: JsonWriter[V]): Cache[V] = {
    RedisCacheFactory(system,
      expiry,
      SprayJsonCacheParser[V],
      prefix,
      None,
      None,
      CacheType.OverwritingRedisCache
    )
  }

  def apply[V](prefix: String)
              (implicit m: reflect.Manifest[V], reader: JsonReader[V], writer: JsonWriter[V]): Cache[V] = {
    apply(Duration.Inf, Some(prefix))
  }

}
