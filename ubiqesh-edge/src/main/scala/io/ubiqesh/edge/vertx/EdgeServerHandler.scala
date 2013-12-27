package io.ubiqesh.edge.vertx

import io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE
import java.io.{InputStreamReader, IOException}
import io.ubiqesh.edge.vertx.event.StateChangeEvent
import io.ubiqesh.edge.vertx.event.StateChangeEventType
import io.ubiqesh.edge.vertx.messaging.Endpoint
import org.vertx.java.core.{Vertx, Handler}
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.http.ServerWebSocket
import io.ubiqesh.edge.common.Path
import java.nio.charset.{CodingErrorAction, StandardCharsets}
import java.net.URL
import org.vertx.java.core.json.JsonObject

object EdgeServerHandler {

  def readUrl(resource: URL): String = {
    val in = resource.openStream()
    val decoder = StandardCharsets.UTF_8.newDecoder()
    decoder.onMalformedInput(CodingErrorAction.IGNORE)
    val reader = new InputStreamReader(in, decoder)
    val buf = new StringBuffer()

    var read = reader.read()
    while (read > -1) {
      buf.append(read.asInstanceOf[Char])
      read = reader.read()
    }
    in.close()
    reader.close()
    buf.toString()
  }

}

class EdgeServerHandler(var vertx: Vertx, var edgeServer: EdgeServer) {

  def getRestHttpHandler(): Handler[HttpServerRequest] = new UbiqeshRestHttpHandler(edgeServer)

  def getWebsocketHandler(): Handler[ServerWebSocket] = new UbiqeshWebsocketHandler()

  def getUbiqeshFileHttpHandler(): Handler[HttpServerRequest] = new UbiqeshFileHttpHandler()

  def getWebUIHandler(): Handler[HttpServerRequest] = new WebUiHttpHandler(vertx)

  private class UbiqeshWebsocketHandler extends Handler[ServerWebSocket] {

    override def handle(socket: ServerWebSocket) {
      val endpoint = new Endpoint(socket, edgeServer.getPersistence, edgeServer)
      edgeServer.addEndpoint(endpoint)

      socket.dataHandler(new Handler[Buffer]() {

        override def handle(event: Buffer) {
          val msg = event.toString
          endpoint.handle(msg)
        }
      })
      socket.closeHandler(new Handler[Void]() {

        override def handle(arg0: Void) {
          endpoint.setOpen(false)
          endpoint.executeDisconnectEvents()
          edgeServer.removeEndpoint(endpoint)
        }
      })
    }
  }

}

class UbiqeshFileHttpHandler extends Handler[HttpServerRequest] {

  override def handle(request: HttpServerRequest) {
    var ubiqeshJsFile: String = null
    try {
      ubiqeshJsFile = loadJsFile()
      request.response().headers().set(CONTENT_TYPE, "application/javascript; charset=UTF-8")
      request.response().end(ubiqeshJsFile)
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }

  private def loadJsFile(): String = {

    val uuid = Thread.currentThread().getContextClassLoader.getResource("uuid.js")
    val rpc = Thread.currentThread().getContextClassLoader.getResource("rpc.js")
    val reconnectingwebsocket = Thread.currentThread().getContextClassLoader.getResource("reconnecting-websocket.min.js")
    val ubiqesh = Thread.currentThread().getContextClassLoader.getResource("ubiqesh.js")

    EdgeServerHandler.readUrl(uuid) + "\r\n" +
      EdgeServerHandler.readUrl(reconnectingwebsocket) + "\r\n" +
      EdgeServerHandler.readUrl(rpc) + "\r\n" +
      EdgeServerHandler.readUrl(ubiqesh)
  }
}

class UbiqeshRestHttpHandler(var ubiqesh: EdgeServer) extends Handler[HttpServerRequest] {

  override def handle(request: HttpServerRequest) {
    try {
      internalHandle(request)
    }
    catch {
      case e: IOException => request.response().end()
    }

    def internalHandle(request: HttpServerRequest) {
      request.response().headers().set(CONTENT_TYPE, "application/json; charset=UTF-8")
      val nodePath = new Path(StateChangeEvent.extractPath(request.uri().replaceAll("\\.json", "")))
      if (request.method().equalsIgnoreCase("GET")) {
        val node = ubiqesh.getPersistence.get(nodePath)
        request.response().end(node.toString)
      } else if (request.method().equalsIgnoreCase("PUT")) {
        request.bodyHandler(new Handler[Buffer]() {

          override def handle(buffer: Buffer) {
            val msg = new String(buffer.getBytes)
            if ("application/json".equalsIgnoreCase(request.headers().get("Content-Type"))) {
              val node = new JsonObject()
              io.ubiqesh.edge.vertx.json.Node.populate(node, msg)
              ubiqesh.handleEvent(StateChangeEventType.SET, request.uri(), node)
              request.response().end()
            } else {
              ubiqesh.handleEvent(StateChangeEventType.SET, request.uri(), msg)
              request.response().end()
            }
          }
        })
      } else if (request.method().equalsIgnoreCase("POST")) {
        request.bodyHandler(new Handler[Buffer]() {

          override def handle(buffer: Buffer) {
            val msg = new String(buffer.getBytes)
            if ("application/json".equalsIgnoreCase(request.headers().get("Content-Type"))) {
              val node = new JsonObject()
              io.ubiqesh.edge.vertx.json.Node.populate(node, msg)
              ubiqesh.handleEvent(StateChangeEventType.PUSH, request.uri(), node)
              request.response().end()
            } else {
              ubiqesh.handleEvent(StateChangeEventType.PUSH, request.uri(), msg)
              request.response().end()
            }
          }
        })
      } else if (request.method().equalsIgnoreCase("DELETE")) {
        ubiqesh.handleEvent(StateChangeEventType.SET, request.uri(), null)
        request.response().end()
      }
    }
  }
}