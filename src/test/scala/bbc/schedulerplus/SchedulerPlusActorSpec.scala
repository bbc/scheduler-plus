package bbc.schedulerplus

import scala.concurrent.duration._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import bbc.client.MockClientCallbacks
import org.scalatest._

/**
  * TestKit for DataSyncActor
  */
class SchedulerPlusActorSpec extends TestKit(ActorSystem("testSystem"))
    with DefaultTimeout
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  val dataSyncActor = system.actorOf(Props(classOf[SchedulerPlusActor], testActor))

  override def afterAll {
    shutdown()
  }

  "A DataSyncActor" should {
    "Start up when it recieves a callback message" in {
      within(500 millis) {
        dataSyncActor ! MockClientCallbacks
        expectMsg("STARTING")
      }
    }
  }

}
