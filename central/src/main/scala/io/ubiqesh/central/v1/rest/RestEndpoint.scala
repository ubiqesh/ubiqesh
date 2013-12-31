package io.ubiqesh.central.rest

import org.vertx.scala.core.http.{HttpServerRequest, RouteMatcher}

/** The rest endpoint gets an prefix, which allows later version upgrades of the REST API
  *
  * @param prefix
  */
class RestEndpoint(prefix: String) extends RouteMatcher{
  // Everything under devices is handled in the DeviceEndpoint
  all(prefix+"/devices(.)*", new DevicesEndpoint(prefix))
  // Everything under semantics is handled in the SematincsEndpoint
  all(prefix+"/semantics(.)*", new SemanticsEndpoint(prefix))
  // Everything in the users endpoint is handled in the UsersEndpoint
  all(prefix+"/users(.)*", new UsersEndpoint(prefix))
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
