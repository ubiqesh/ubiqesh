package io.ubiqesh.central.rest

import akka.actor.{Props, ActorSystem, ActorLogging, UntypedActor}
import org.vertx.scala.core.http.{RouteMatcher, HttpServerRequest}

class UserEndpoint(prefix: String) extends RouteMatcher {
  implicit val system = ActorSystem("central")

  val usersRestEndpoint = system.actorOf(Props(classOf[UsersRestEndpoint],prefix), "UsersRestEndpoint")

  all(prefix+"/users", {
    request:HttpServerRequest => {
      usersRestEndpoint.tell(request, null)
    }
  })
  all(prefix+"/users/:userId", {
    request:HttpServerRequest => {
      usersRestEndpoint.tell(request, null)
    }
  })

}

class UsersRestEndpoint(prefix: String) extends UntypedActor with ActorLogging {

  def onReceive(message: Any) = {
    if (message.isInstanceOf[HttpServerRequest]) {
      val req = message.asInstanceOf[HttpServerRequest]
      val serverName = req.absoluteURI().getScheme +"://"+req.absoluteURI().getHost+":"+req.absoluteURI().getPort+prefix
      req.response()
        .putHeader("Content-Type","application/json")
        .end("""{
             "users": """" + serverName + """/users"
         }""")
    }
  }
}
