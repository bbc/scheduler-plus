package bbc.persistence

import spray.json.{JsonParser, JsonReader, JsonWriter}
import spray.json._

/**
  * Implementation of CacheParser which provides Spray JSON parsing support.
  */
object SprayJsonCacheParser {
  def apply[V](implicit reader: JsonReader[V],
               writer: JsonWriter[V]): SprayJsonCacheParser[V] = {
    new SprayJsonCacheParser[V]
  }
}

class SprayJsonCacheParser[V] (implicit reader: JsonReader[V], writer: JsonWriter[V]) extends CacheParser[V]{
  def serialise(candidate: V): String = {
    candidate.toJson.toString()
  }

  def deserialise(response: String): V = {
    JsonParser(response).convertTo[V]
  }
}
