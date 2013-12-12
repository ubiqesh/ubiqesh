package io.yagni.edge.vertx.rpc

import io.yagni.edge.vertx.messaging.OutboundSocket
import scala.collection.mutable
import org.vertx.java.core.json.JsonObject

class Rpc {

  private var methods: mutable.Map[String, RpcMethodInstance] = new mutable.HashMap[String, RpcMethodInstance]()

  def register(obj: AnyRef) {
    try {
      for (method <- obj.getClass.getMethods if method.isAnnotationPresent(classOf[Method])) {
        methods.put(method.getName, new RpcMethodInstance(obj, method))
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def handle(message: String, socket: OutboundSocket) {
    val json = new JsonObject()
    io.yagni.edge.vertx.json.Node.populate(json, message)
    val id = json.getString("id")
    val method = json.getString("method")
    val args = json.getObject("args")
    if (methods.contains(method)) {
      val response = new JsonObject()
      response.putValue("id", id)
      try {
        val meth = methods.get(method).get
        response.putValue("resp", meth.invoke(args))
        response.putValue("state", "ok")
        response.putValue("type", "rpc")
      } catch {
        case e: Exception => {
          e.printStackTrace()
          response.putValue("resp", e.getMessage)
          response.putValue("state", "error")
          response.putValue("type", "rpc")
        }
      }
      socket.send(response.toString)
    } else {
      val response = new JsonObject()
      response.putValue("id", id)
      response.putValue("type", "rpc")
      response.putValue("state", "error")
      response.putValue("resp", method + " not found")
      socket.send(response.toString)
    }
  }
}
