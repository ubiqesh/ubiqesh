package io.ubiqesh.central.rest

import akka.actor.{ActorLogging, UntypedActor}
import org.vertx.scala.core.http.HttpServerRequest

class SemanticsRestEndpoint(prefix: String) extends UntypedActor with ActorLogging {

  def onReceive(message: Any) = {
    if (message.isInstanceOf[HttpServerRequest]) {
      val req = message.asInstanceOf[HttpServerRequest]
      val serverName = req.absoluteURI().getScheme +"://"+req.absoluteURI().getHost+":"+req.absoluteURI().getPort+prefix

      req.response()
        .putHeader("Content-Type","application/json")
        .end("""{
             "aggregations": """" + serverName + """/semantics/aggregations",
             "types": """" + serverName + """/semantics/types",
             "systems": """" + serverName + """/semantics/systems",
             "refresh_strategies": """" + serverName + """/semantics/refresh_strategies"
         }""")
    }
  }
}