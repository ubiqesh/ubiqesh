package io.ubiqesh.central

import org.vertx.scala.core.http.{HttpServerRequest}

import akka.actor.{ActorSystem}
import io.netty.handler.codec.http.{HttpHeaders, QueryStringDecoder}
import org.vertx.scala.core.buffer.Buffer
import org.vertx.java.core.Handler
import io.ubiqesh.central.rest.{RestEndpoint}
import org.vertx.java.core.http.impl.MimeMapping
import java.net.URL
import java.nio.charset.{CodingErrorAction, StandardCharsets}
import java.io.InputStreamReader

class WebUIHandler extends Handler[HttpServerRequest] {
  def handle(request: HttpServerRequest){
    try {
      var path = request.uri()
      if (path == "" || path == "/") {
        path = "index.html"
      }
      if (path.startsWith("/")) {
        path = path.substring(1)
      }
      if("signin".equals(path))
      {
        request.response()
          .setStatusCode(302)
          .putHeader("location","/login.html")
          .end()
      }
      var resource = Thread.currentThread().getContextClassLoader.getResource("ui/" + path)
      if (resource != null) {
        val content = readUrl(resource)
        request.response().putHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(content.length))
        val li = path.lastIndexOf('.')
        if (li != -1 && li != path.length - 1) {
          val ext = path.substring(li + 1, path.length)
          val contentType = MimeMapping.getMimeTypeForExtension(ext)
          if (contentType != null) {
            request.response().putHeader(HttpHeaders.Names.CONTENT_TYPE, contentType)
          }
        }
        request.response().end(content)
      } else {
        resource = this.getClass.getClassLoader.getResource("ui/404.html")
        val content = readUrl(resource)
        request.response().putHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(content.length))
        val li = path.lastIndexOf('.')
        if (li != -1 && li != path.length - 1) {
          val ext = path.substring(li + 1, path.length)
          val contentType = MimeMapping.getMimeTypeForExtension(ext)
          if (contentType != null) {
            request.response().putHeader(HttpHeaders.Names.CONTENT_TYPE, contentType)
          }
        }
        request.response().end(content)
      }
    } catch {
      case e: Exception => {
        request.response().end()
        e.printStackTrace()
      }
    }
  }

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
