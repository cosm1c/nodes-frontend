package controllers

import actors.DigraphActor.{NodeState, Running, StateChange, Stopped}
import actors.{ClientWebSocketActor, DigraphActor}
import domain.Node
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.duration._
import scala.util.Random

object DiGraph extends Controller {

  val digraphActor = Akka.system.actorOf(DigraphActor.props(DiGraph.digraph()), name = "digraphActor")

  Akka.system.scheduler.schedule(0.microsecond, 333.millisecond, new Runnable {
    override def run(): Unit = {
      digraphActor ! randomStateChange
    }
  })

  private def randomStateChange: StateChange = {
    val nextChar: Char = ('a'.toInt + Random.nextInt(8)).toChar
    StateChange(NodeState(nextChar.toString, if (Random.nextBoolean()) Running else Stopped))
  }

  def digraph() = Seq(
    Node("root", Seq("a", "g")),
    Node("a", Seq("b", "d")),
    Node("b", Seq("c")),
    Node("c"),
    Node("d", Seq("e", "f")),
    Node("e"),
    Node("f"),
    Node("g", Seq("h")),
    Node("h")
  )

  def digraphJson = Action {
    Ok(Json.toJson(digraph()))
  }

  def websocket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    println("Client connected")
    ClientWebSocketActor.props(out)
  }

}
