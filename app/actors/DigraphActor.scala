package actors

import actors.DigraphActor._
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import domain.Node
import domain.Node.NodeId
import play.api.libs.json.{Json, JsValue, Writes}

class DigraphActor(digraph: Seq[Node]) extends Actor with ActorLogging {

  private var nodesStateMap: Map[NodeId, NodeState] = digraph.map(node => (node.id, NodeState(node.id, Unknown))).toMap

  private var listeners: Set[ActorRef] = Set.empty

  override def receive = {

    case Subscribe =>
      log.info(s"Subscribe ${sender()}")
      sender() ! DigraphState(nodesStateMap.values.toSeq)
      listeners += sender()

    case Unsubscribe =>
      log.info(s"Unsubscribe ${sender()}")
      listeners -= sender()

    case StateChange(nodeState) =>
//      log.info(s"StateChange $nodeState")
      listeners.foreach(_ ! nodeState)
      nodesStateMap += nodeState.id -> nodeState
  }
}

object DigraphActor {

  def props(digraph: Seq[Node]) = Props(new DigraphActor(digraph))

  case object Subscribe

  case object Unsubscribe

  case class NodeState(id: NodeId, state: State)

  case class StateChange(nodeState: NodeState)

  case class DigraphState(nodeStates: Seq[NodeState])

  implicit val nodeStateWrites = new Writes[NodeState] {
    override def writes(nodeState: NodeState): JsValue = Json.obj(
      "id" -> nodeState.id,
      "state" -> nodeState.state.name
    )
  }

  /*
   * State - a simple example.
   */
  sealed abstract class State(val name: String)

  case object Unknown extends State("Unknown")

  case object Running extends State("Running")

  case object Stopped extends State("Stopped")

}
