package io.yagni.edge.vertx

import io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE
import java.io.{InputStreamReader, IOException}
import io.yagni.edge.vertx.event.StateChangeEvent
import io.yagni.edge.vertx.event.StateChangeEventType
import io.yagni.edge.vertx.messaging.Endpoint
import io.yagni.edge.vertx.messaging.OutboundSocket
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.http.ServerWebSocket
import io.yagni.edge.common.Path
import java.nio.charset.{CodingErrorAction, StandardCharsets}
import java.net.URL
import org.vertx.java.core.json.JsonObject

object EdgeServerHandler {

  def readUrl(resource: URL):String = {
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

class EdgeServerHandler(var yagni: EdgeServer) {

  def getRestHttpHandler(): Handler[HttpServerRequest] = new YagniRestHttpHandler(yagni)

  def getWebsocketHandler(): Handler[ServerWebSocket] = new YagniWebsocketHandler()

  def getYagniFileHttpHandler(): Handler[HttpServerRequest] = new YagniFileHttpHandler()

  def getWebUIHandler(): Handler[HttpServerRequest] = new WebUiHttpHandler()

  private class YagniWebsocketHandler extends Handler[ServerWebSocket] {

    override def handle(socket: ServerWebSocket) {
      val endpoint = new Endpoint(new OutboundSocket() {

        override def send(msg: String) {
          socket.writeTextFrame(msg)
        }
      }, yagni.getPersistence, yagni)
      yagni.addEndpoint(endpoint)
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
          yagni.removeEndpoint(endpoint)
        }
      })
    }
  }

}

class YagniFileHttpHandler extends Handler[HttpServerRequest] {

  override def handle(request: HttpServerRequest) {
    var yagniJsFile: String = null
    try {
      yagniJsFile = loadJsFile()
      request.response().headers().set(CONTENT_TYPE, "application/javascript; charset=UTF-8")
      request.response().end(yagniJsFile)
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }

  private def loadJsFile(): String = {
    val uuid = Thread.currentThread().getContextClassLoader.getResource("uuid.js")
    val rpc = Thread.currentThread().getContextClassLoader.getResource("rpc.js")
    val reconnectingwebsocket = Thread.currentThread().getContextClassLoader.getResource("reconnecting-websocket.min.js")
    val yagni = Thread.currentThread().getContextClassLoader.getResource("yagni.js")

    EdgeServerHandler.readUrl(uuid) + "\r\n" +
    EdgeServerHandler.readUrl(reconnectingwebsocket) +"\r\n" +
    EdgeServerHandler.readUrl(rpc) + "\r\n" +
    EdgeServerHandler.readUrl(yagni)
  }
}

class YagniRestHttpHandler(var yagni: EdgeServer) extends Handler[HttpServerRequest] {

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
        val node = yagni.getPersistence.get(nodePath)
        request.response().end(node.toString)
      } else if (request.method().equalsIgnoreCase("PUT")) {
        request.bodyHandler(new Handler[Buffer]() {

          override def handle(buffer: Buffer) {
            val msg = new String(buffer.getBytes)
            if ("application/json".equalsIgnoreCase(request.headers().get("Content-Type"))) {
              val node = new JsonObject()
              io.yagni.edge.vertx.json.Node.populate(node, msg)
              yagni.handleEvent(StateChangeEventType.SET, request.uri(), node)
              request.response().end()
            } else {
              yagni.handleEvent(StateChangeEventType.SET, request.uri(), msg)
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
              io.yagni.edge.vertx.json.Node.populate(node, msg)
              yagni.handleEvent(StateChangeEventType.PUSH, request.uri(), node)
              request.response().end()
            } else {
              yagni.handleEvent(StateChangeEventType.PUSH, request.uri(), msg)
              request.response().end()
            }
          }
        })
      } else if (request.method().equalsIgnoreCase("DELETE")) {
        yagni.handleEvent(StateChangeEventType.SET, request.uri(), null)
        request.response().end()
      }
    }
  }
}