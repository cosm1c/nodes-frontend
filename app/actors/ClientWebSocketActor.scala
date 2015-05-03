package actors

import actors.DigraphActor._
import akka.actor._
import controllers.DiGraph
import play.api.libs.json._

class ClientWebSocketActor(out: ActorRef) extends Actor with ActorLogging {

  log.info("ClientWebSocketActor started")

  override def preStart(): Unit = {
    log.info("preStart")
    DiGraph.digraphActor ! Subscribe
  }


  override def postStop(): Unit = {
    log.info("postStop")
    DiGraph.digraphActor ! Unsubscribe
  }

  override def receive = {

    case digraphState: DigraphState =>
      //      log.info(s"Sending DigraphState ${digraphState.nodeStates}")
      out ! Json.toJson(digraphState.nodeStates)

    case nodeState: NodeState =>
      //      log.info(s"Sending State $nodeState")
      out ! Json.arr(nodeState)
  }
}

object ClientWebSocketActor {

  def props(out: ActorRef) = Props(new ClientWebSocketActor(out))

}
