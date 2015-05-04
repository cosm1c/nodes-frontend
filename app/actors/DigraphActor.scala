package actors

import actors.DigraphActor._
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import domain.Node
import domain.Node.NodeId
import play.api.libs.json.{JsValue, Json, Writes}

class DigraphActor(private var digraph: Set[Node]) extends Actor with ActorLogging {

  private var nodesStateMap: Map[NodeId, NodeState] = digraph.map(node => (node.id, NodeState(node.id, Unknown))).toMap

  private var listeners: Set[ActorRef] = Set.empty

  override def receive = {

    case Subscribe =>
      log.info(s"Subscribe ${sender()}")
      sender() ! AddNodes(digraph.toSeq)
      sender() ! StateChanges(nodesStateMap.values.toSeq)
      listeners += sender()

    case Unsubscribe =>
      log.info(s"Unsubscribe ${sender()}")
      listeners -= sender()

    case stateChange: StateChanges =>
      //log.info(s"StateChange $nodeState")
      listeners.foreach(_ ! stateChange)
      nodesStateMap ++= stateChange.nodeStates.map(nodeState => nodeState.id -> nodeState)

    case addNodes@AddNodes(nodes: Seq[Node]) =>
      log.info(s"AddNode $nodes")
      listeners.foreach(_ ! addNodes)
      digraph ++= nodes
      nodesStateMap ++= nodes.map(node => node.id -> NodeState(node.id, Unknown))
  }
}

object DigraphActor {

  private val initialDigraph: Set[Node] = Set(
    Node("root", Seq("a", "g")),
    Node("a", Seq("b", "d", "h")),
    Node("b", Seq("c")),
    Node("c"),
    Node("d", Seq("e", "f")),
    Node("e"),
    Node("f"),
    Node("g", Seq("h")),
    Node("h")
  )

  def props() = Props(new DigraphActor(initialDigraph))

  /*
   * Messages
   */
  case object Subscribe

  case object Unsubscribe

  case class NodeState(id: NodeId, state: State)

  case class StateChanges(nodeStates: Seq[NodeState])

  case class AddNodes(nodes: Seq[Node])


  /*
   * State - a simple example.
   */
  sealed abstract class State(val name: String)

  case object Unknown extends State("Unknown")

  case object Running extends State("Running")

  case object Stopped extends State("Stopped")

  /*
   * JSON
   */

  implicit val addNodesWrites = new Writes[AddNodes] {
    override def writes(addNodes: AddNodes): JsValue = Json.obj(
      "type" -> "add",
      "nodes" -> Json.toJson(addNodes.nodes)
    )
  }

  implicit val nodeStateWrites = new Writes[NodeState] {
    override def writes(nodeState: NodeState): JsValue = Json.obj(
      "id" -> nodeState.id,
      "state" -> nodeState.state.name
    )
  }

  implicit val stateChangeWrites = new Writes[StateChanges] {
    override def writes(stateChange: StateChanges): JsValue = Json.obj(
      "type" -> "change",
      "changes" -> Json.toJson(stateChange.nodeStates)
    )
  }

}
