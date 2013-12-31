package io.ubiqesh.central.rest

import akka.actor.{Props, ActorSystem, ActorLogging, UntypedActor}
import org.vertx.scala.core.http.{RouteMatcher, HttpServerRequest}

class SemanticsEndpoint(prefix: String) extends RouteMatcher {
  implicit val system = ActorSystem("central")
  val semanticsRestEndpoint = system.actorOf(Props(classOf[SemanticsRestEndpoint],prefix), "SemanticsRestEndpoint")

  all(prefix+"/semantics", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/semantics/aggregations", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/semantics/aggregations/:semanticId", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/semantics/aggregations", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/semantics/aggregations/:semanticId", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/semantics/types", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/semantics/types/:semanticId", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/semantics/systems", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/semantics/systems/:semanticId", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/semantics/refresh_strategies", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/semantics/refresh_strategies/:semanticId", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
}

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