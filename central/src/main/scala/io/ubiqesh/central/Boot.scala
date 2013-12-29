package io.ubiqesh.central

import org.vertx.scala.core.http.{HttpServerRequest, RouteMatcher}

import akka.actor.{ActorSystem, Props}
import io.netty.handler.codec.http.{QueryStringDecoder, HttpHeaders}
import org.vertx.java.core.http.impl.MimeMapping
import java.io.{InputStreamReader, IOException}
import java.net.URL
import java.nio.charset.{CodingErrorAction, StandardCharsets}
import org.vertx.scala.core.buffer.Buffer
import io.ubiqesh.central.rest.{RestEndpoint, UsersRestEndpoint, SemanticsRestEndpoint, DevicesRestEndpoint}

object Boot extends App {
  implicit val system = ActorSystem("central")

  val devicesRestEndpoint = system.actorOf(Props(classOf[DevicesRestEndpoint],"/v1"), "DevicesRestEndpoint")
  val usersRestEndpoint = system.actorOf(Props(classOf[UsersRestEndpoint],"/v1"), "UsersRestEndpoint")
  val semanticsRestEndpoint = system.actorOf(Props(classOf[SemanticsRestEndpoint],"/v1"), "SemanticsRestEndpoint")
  val vertx = org.vertx.scala.core.newVertx()

  val rm = new RouteMatcher()
  rm.all("/v1(.)*",new RestEndpoint("/v1"))
  rm.post("/signin", { request:HttpServerRequest => {
    request.bodyHandler({
      buffer: Buffer => {
        val contentType = request.headers().get("Content-Type");
        if ("application/x-www-form-urlencoded".equals(contentType))
        {
          val qsd = new QueryStringDecoder(buffer.toString(), false);
          val params = qsd.parameters()
          System.out.println(params);
        }
        request.response()
          .setStatusCode(302)
          .putHeader("location","/index.html")
          .end()
    }})
  }});
  rm.get("/(.)*",  {
    request:HttpServerRequest => {
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
        case e: IOException => {
          request.response().end()
          e.printStackTrace()
        }
      }
    }
  })
  vertx.createHttpServer().requestHandler(rm).listen(8080, "localhost")

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
