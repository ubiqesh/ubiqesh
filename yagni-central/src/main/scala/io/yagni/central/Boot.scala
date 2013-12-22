package io.yagni.central

import org.vertx.scala.core.http.{HttpServerRequest, RouteMatcher}

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import org.vertx.java.core.Handler

object Boot extends App {
  implicit val system = ActorSystem("central")
  val supervisor = system.actorOf(Props[Supervisor], "Supervisor")
  val vertx = org.vertx.scala.core.newVertx()
  val rm = new RouteMatcher()
  rm.all("/v1(.)*", {
    request:HttpServerRequest => {
      supervisor.tell(request, supervisor)
    }
  })
  vertx.createHttpServer().requestHandler(rm).listen(8080, "localhost")
}
