package bbc

import scala.concurrent.duration._
import akka.actor.Props
import akka.pattern.ask
import akka.event.Logging
import akka.util.Timeout
import bbc.persistence.sync.SchedulerPlusActor

import scala.concurrent.Await

/**
  * Example Hello, World!
  */
object Boot extends App {
  implicit val system = AppContext.actorSystem
  val log = Logging(system, getClass)

  implicit val timeout = Timeout(5 seconds)
  import scala.concurrent.ExecutionContext.Implicits.global

  val scheduler = system.actorOf(Props[SchedulerPlusActor], "scheduler-actor")
  val response = scheduler ? HelloWorldCallbacks

  val message = Await.result(response, timeout.duration).asInstanceOf[String]

  log.debug(s"Response: $message")
}
