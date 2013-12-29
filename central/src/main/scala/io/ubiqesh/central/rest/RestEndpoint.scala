package io.ubiqesh.central.rest

import org.vertx.scala.core.http.{HttpServerRequest, RouteMatcher}

class RestEndpoint(prefix: String) extends RouteMatcher{
  all(prefix+"/devices(.)*", new DevicesEndpoint(prefix))
  all(prefix+"/semantics(.)*", new DevicesEndpoint(prefix))
  all(prefix+"/users(.)*", new DevicesEndpoint(prefix))
  all(prefix+"(.)*", {
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
