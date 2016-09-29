package bbc.schedulerplus

import scala.concurrent.duration._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
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

  val schedulerPlusActor = TestActorRef[SchedulerPlusActor]

  override def afterAll {
    shutdown()
  }

  "A DataSyncActor" should {
    "Respond with STARTING when it recieves callbacks" in {
      within(500 millis) {
        schedulerPlusActor ! MockClientCallbacks
        expectMsg("STARTING")
      }
    }

    "Respond with FAILED when it recieves an unknown type" in {
      within(500 millis) {
        schedulerPlusActor ! Some
        expectMsg("FAILED")
      }
    }

    "Respond with STOPPING when it recieves a stop message" in {
      within(500 millis) {
        schedulerPlusActor ! "stop"
        expectMsg("STOPPING")
      }
    }

  }
}
