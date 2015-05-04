package controllers

import actors.DigraphActor._
import actors.{ClientWebSocketActor, DigraphActor}
import akka.util.Timeout
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.duration._
import scala.util.Random

object DiGraphController extends Controller {

  implicit val timeout = Timeout(5.seconds)

  val digraphActor = Akka.system.actorOf(DigraphActor.props(), name = "digraphActor")

  def websocket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    Logger.info("Client connected")
    ClientWebSocketActor.props(out)
  }

  // Random state changes
  Akka.system.scheduler.schedule(0.microsecond, 333.millisecond) {
    def randomStateChange: StateChanges = {
      val nextChar: Char = ('a'.toInt + Random.nextInt(8)).toChar
      StateChanges(Seq(NodeState(nextChar.toString, if (Random.nextBoolean()) Running else Stopped)))
    }

    digraphActor ! randomStateChange
  }

}
