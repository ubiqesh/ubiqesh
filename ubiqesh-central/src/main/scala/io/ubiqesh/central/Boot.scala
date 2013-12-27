package io.ubiqesh.central

import org.vertx.scala.core.http.{HttpServerRequest, RouteMatcher}

import akka.actor.{ActorSystem, Props}

object Boot extends App {
  implicit val system = ActorSystem("central")
  val supervisor = system.actorOf(Props[Supervisor], "Supervisor")
  val vertx = org.vertx.scala.core.newVertx()
  val rm = new RouteMatcher()
  rm.all("/v1(.)*", {
    request:HttpServerRequest => {
      supervisor.tell(request, null)
    }
  })
  vertx.createHttpServer().requestHandler(rm).listen(8080, "localhost")
}
