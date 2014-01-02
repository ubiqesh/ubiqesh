package io.ubiqesh.central

import io.ubiqesh.central.rest.RestEndpoint
import io.ubiqesh.central.ui.{WebUIHandler, AuthenticationHandler}
import io.ubiqesh.central.vertx.RouteMatcher
import org.vertx.scala.core.net.{NetSocket, NetServer}
import org.vertx.scala.core.http.HttpServerRequest
import io.ubiqesh.central.mqtt.{MqttServer}

object Boot extends App {
  val vertx = org.vertx.scala.core.newVertx()

  val rm = new RouteMatcher()
  // on /v1 we register our RestEndpoint (when implementing a new mayor version should be registered at v2)
  rm.all("/v1(.)*",new RestEndpoint("/v1"))
  // on /signin we register the AuthenticationHandler
  rm.post("/signin", new AuthenticationHandler());
  // Everything else runs into the UI Handler
  rm.noMatch(new WebUIHandler())
  // Start the HTTP server at port 8080, listening on localhost
  vertx.createHttpServer().requestHandler(rm).listen(8080, "localhost")

  val mqttServer = new MqttServer(vertx)
  vertx.createNetServer()
    .setClientAuthRequired(false)
    .setTCPKeepAlive(true)
    .setTCPNoDelay(true)
    .connectHandler({ socket:NetSocket => mqttServer.registerClient(socket)})
    .listen(1883)
}
