package actors

import actors.DigraphActor._
import akka.actor.{Actor, ActorLogging, Props}
import akka.remote.{DisassociatedEvent, RemotingLifecycleEvent}
import controllers.DiGraphController
import domain.Node

class RemoteNodeActor(node: Node) extends Actor with ActorLogging {

  log.info(s"RemoteNodeActor for $node")

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[RemotingLifecycleEvent])

    DiGraphController.digraphActor ! AddNodes(Seq(node))
    DiGraphController.digraphActor ! StateChanges(Seq(NodeState(node.id, Running)))
  }

  override def postStop(): Unit =
    DiGraphController.digraphActor ! StateChanges(Seq(NodeState(node.id, Stopped)))

  override def receive = {

    case DisassociatedEvent(_, _, _) =>
      log.info("Client disconnected - DisassociatedEvent")
      DiGraphController.digraphActor ! StateChanges(Seq(NodeState(node.id, Stopped)))

    case msg@_ =>
      log.info(s"msg: $msg")
  }

}

object RemoteNodeActor {

  def props(node: Node) = Props(new RemoteNodeActor(node))

}