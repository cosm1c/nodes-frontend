package actors

import actors.DigraphActor._
import akka.actor.{Actor, ActorLogging, Props}
import akka.remote.{DisassociatedEvent, RemotingLifecycleEvent}
import controllers.DiGraphController
import domain.Node

class RemoteNodeActor(node: Node) extends Actor with ActorLogging {

  log.info(s"RemoteNodeActor for $node - ${self.path}")

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[RemotingLifecycleEvent])

    DiGraphController.digraphActor ! AddNodes(Seq(node))
    DiGraphController.digraphActor ! StateChanges(Seq(NodeState(node.id, Running)))
  }

  override def postStop(): Unit =
    DiGraphController.digraphActor ! StateChanges(Seq(NodeState(node.id, Stopped)))

  override def receive = {

    case evt@DisassociatedEvent(localAddress, remoteAddress, inbound) =>
      log.info(s"Client disconnected: $evt")

      // TODO: Implement cleaner means of determining if DisassociatedEvent is for this Actor
      if (self.path.toString.contains(remoteAddress.toString.replace("://", "/"))) {
        log.info("remoteAddress DOES MATCH")
        DiGraphController.digraphActor ! StateChanges(Seq(NodeState(node.id, Stopped)))
      } else {
        log.info("remoteAddress NO MATCH")
      }

    case msg@_ =>
      log.info(s"Unhandled message: $msg")
  }
}

object RemoteNodeActor {

  def props(node: Node) = Props(new RemoteNodeActor(node))

}