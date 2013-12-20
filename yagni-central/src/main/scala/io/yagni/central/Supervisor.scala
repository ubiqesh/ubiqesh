package io.yagni.central

import akka.actor.{UntypedActor, ActorLogging}
import org.vertx.scala.core.http.HttpServerRequest

class Supervisor extends UntypedActor with ActorLogging {
  def onReceive(message: Any) = {
    if (message.isInstanceOf[HttpServerRequest]) {
      val req = message.asInstanceOf[HttpServerRequest]

      req.response().end()
    }
  }
}