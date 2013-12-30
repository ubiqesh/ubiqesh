package io.ubiqesh.central

import org.vertx.java.core.Handler
import org.vertx.scala.core.http.HttpServerRequest
import org.vertx.scala.core.buffer.Buffer
import io.netty.handler.codec.http.QueryStringDecoder

/**
 * Created by balu on 30.12.13.
 */
class AuthenticationHandler extends Handler[HttpServerRequest] {
  def handle(request: HttpServerRequest){
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
  }
}
