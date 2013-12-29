package io.ubiqesh.central.rest

import org.vertx.scala.core.http.{HttpServerRequest, RouteMatcher}
import akka.actor.{Props, ActorSystem}

class RestEndpoint(prefix: String) extends RouteMatcher{
  implicit val system = ActorSystem("central")

  val devicesRestEndpoint = system.actorOf(Props(classOf[DevicesRestEndpoint],prefix), "DevicesRestEndpoint")
  val usersRestEndpoint = system.actorOf(Props(classOf[UsersRestEndpoint],prefix), "UsersRestEndpoint")
  val semanticsRestEndpoint = system.actorOf(Props(classOf[SemanticsRestEndpoint],prefix), "SemanticsRestEndpoint")

  all("/v1/devices", {
    request:HttpServerRequest => {
      devicesRestEndpoint.tell(request, null)
    }
  })
  all("/v1/devices/:deviceId", {
    request:HttpServerRequest => {
      devicesRestEndpoint.tell(request, null)
    }
  })
  all("/v1/devices/:deviceId/sensors", {
    request:HttpServerRequest => {
      devicesRestEndpoint.tell(request, null)
    }
  })
  all("/v1/devices/:deviceId/sensors/:sensorId", {
    request:HttpServerRequest => {
      devicesRestEndpoint.tell(request, null)
    }
  })
  all("/v1/devices/:deviceId/sensors/:sensorId/current", {
    request:HttpServerRequest => {
      devicesRestEndpoint.tell(request, null)
    }
  })
  all("/v1/devices/:deviceId/sensors/:sensorId/subscriptions", {
    request:HttpServerRequest => {
      devicesRestEndpoint.tell(request, null)
    }
  })
  all("/v1/devices/:deviceId/subscriptions", {
    request:HttpServerRequest => {
      devicesRestEndpoint.tell(request, null)
    }
  })
  all("/v1/semantics", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all("/v1/semantics/:semanticId", {
    request:HttpServerRequest => {
      semanticsRestEndpoint.tell(request, null)
    }
  })
  all("/v1/users", {
    request:HttpServerRequest => {
      usersRestEndpoint.tell(request, null)
    }
  })
  all("/v1/users/:userId", {
    request:HttpServerRequest => {
      usersRestEndpoint.tell(request, null)
    }
  })
  all("/v1(.)*", {
    req:HttpServerRequest => {
      val serverName = req.absoluteURI().getScheme +"://"+req.absoluteURI().getHost+":"+req.absoluteURI().getPort+prefix

      req.response()
        .putHeader("Content-Type","application/json")
        .end("""{
             "name": "ubiqesh",
             "version": "1.0.0",
             "devices": """"+serverName+"""/devices",
             "semantics": """" + serverName + """/semantics",
             "users": """" + serverName + """/users"
         }""")
    }
  })

}
