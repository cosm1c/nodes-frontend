package actors

import actors.DigraphActor._
import akka.actor._
import controllers.DiGraphController
import play.api.libs.json._

class ClientWebSocketActor(out: ActorRef) extends Actor with ActorLogging {

  log.info(s"ClientWebSocketActor started $out")

  override def preStart(): Unit = {
    log.info("preStart")
    DiGraphController.digraphActor ! Subscribe
  }


  override def postStop(): Unit = {
    log.info("postStop")
    DiGraphController.digraphActor ! Unsubscribe
  }

  override def receive = {

    case addNodes: AddNodes =>
      out ! Json.toJson(addNodes)

    case stateChanges: StateChanges =>
      out ! Json.toJson(stateChanges)
  }
}

object ClientWebSocketActor {

  def props(out: ActorRef) = Props(new ClientWebSocketActor(out))

}
