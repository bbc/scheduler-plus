package bbc

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.Props
import akka.pattern.ask
import akka.event.Logging
import akka.util.Timeout
import bbc.client.examples.HelloWorldCallbacks
import bbc.schedulerplus.SchedulerPlusActor

/**
  * Example Hello, World!
  */
object Boot extends App {
  implicit val system = AppContext.actorSystem
  val log = Logging(system, getClass)

  implicit val timeout = Timeout(5 seconds)

  val scheduler = system.actorOf(Props[SchedulerPlusActor], "scheduler-actor")
  val response = scheduler ? HelloWorldCallbacks

  val message = Await.result(response, timeout.duration).asInstanceOf[String]

  log.debug(s"Response: $message")
}
