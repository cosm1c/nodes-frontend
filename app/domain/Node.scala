package domain

import domain.Node.NodeId
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Node(id: NodeId, depends: Seq[NodeId]) {
  def this(label: NodeId) = this(label, Seq())
}

object Node {

  type NodeId = String

  def apply(is: NodeId) = new Node(is)

  implicit lazy val nodeWrites: Writes[Node] = (
    (__ \ "id").write[NodeId] and
      (__ \ "depends").write[Seq[NodeId]]
    )(unlift(Node.unapply))
}
