package io.yagni.central

import org.vertx.scala.core.http.{HttpServerRequest, RouteMatcher}

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("yagni-central")

  // create and start our service actor
  val service = system.actorOf(Props[MyServiceActor], "demo-service")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}

/*
object Boot extends App {
  implicit val system = ActorSystem("central")
  val service = system.actorOf(Props[Supervisor], "Supervisor")
  val vertx = org.vertx.scala.core.newVertx()
  val rm = new RouteMatcher()
  rm.all("/api/1(.)*", new RestEndpoint())
  vertx.createHttpServer().requestHandler(rm).listen(8080)
}*/

class RestEndpoint extends RouteMatcher {
  get("/api/1", {
    case req: HttpServerRequest => {
      val resp = req.response()
      resp.setStatusCode(204).setStatusMessage("ok").setChunked(true)
      resp.headers.add("Content-Type", "application/json")
      resp.write("{'devices': '" + req.uri + "/devices'}").end()
    }
  })

  allWithRegEx("/api/1/devices(.)*", new DeviceEndpoint())
}

class DeviceEndpoint extends RouteMatcher {
  val devices = Map()

  get("/api/1/devices", {
    case req: HttpServerRequest => {
      val resp = req.response()
      resp.setStatusCode(204).setStatusMessage("ok").setChunked(true)
      resp.headers.add("Content-Type", "application/json")
      resp.write(devices.values.mkString).end()
    }
  })
  post("/api/1/devices", {
    case req: HttpServerRequest => req.response().setStatusCode(202).setStatusMessage("ok").end()
  })
  options("/api/1/devices", {
    case req: HttpServerRequest => req.response().setStatusCode(202).setStatusMessage("ok").end()
  })
  get("/api/1/devices/:id", {
    case req: HttpServerRequest => req.response().setStatusCode(202).setStatusMessage("ok").end()
  })
  put("/api/1/devices/:id", {
    case req: HttpServerRequest => req.response().setStatusCode(202).setStatusMessage("ok").end()
  })
  patch("/api/1/devices/:id", {
    case req: HttpServerRequest => req.response().setStatusCode(202).setStatusMessage("ok").end()
  })
  delete("/api/1/devices/:id", {
    case req: HttpServerRequest => req.response().setStatusCode(202).setStatusMessage("ok").end()
  })
  options("/api/1/devices/:id", {
    case req: HttpServerRequest => req.response().setStatusCode(202).setStatusMessage("ok").end()
  })

}