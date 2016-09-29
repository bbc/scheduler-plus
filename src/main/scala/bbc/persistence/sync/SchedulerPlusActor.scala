package bbc.persistence.sync

import akka.actor.{Actor, ActorLogging}

/**
  * Receives callbacks on application startup to begin the scheduler for jobs we wish to run
  */
class SchedulerPlusActor extends Actor with ActorLogging {

  /**
   * Start scheduler
   */
  private def start(callbacks: Callbacks): Unit = SyncManager.startScheduling(callbacks)

  /**
   * Stop scheduler
   */
  private def stop(): Unit = SyncManager.stopScheduling

  override def receive: PartialFunction[Any, Unit] = {
    case callbacks: Callbacks => {
      start(callbacks)
      sender ! "STARTING"
    }
    case "stop" => {
      stop()
      sender ! "STOPPING"
    }
    case message: Any => {
      log.info(message.getClass.getName + " isn't handled by " + this.getClass.getSimpleName)
      sender ! "FAILED"
    }
  }
}
