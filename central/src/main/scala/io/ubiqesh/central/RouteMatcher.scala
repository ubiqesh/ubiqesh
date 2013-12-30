package io.ubiqesh.central

import org.vertx.java.core.Handler
import org.vertx.java.core.http.RouteMatcher
import org.vertx.java.core.http.{ RouteMatcher => JRouteMatcher }
import org.vertx.scala.core.FunctionConverters._
import org.vertx.scala.Wrap
import org.vertx.java.core.Handler
import org.vertx.scala.Wrap
import org.vertx.scala.core.http.{HttpServerRequest, RouteMatcher}
import org.vertx.scala.core.FunctionConverters._

/**
 * Not sure whether this kind of RouteMatcher should stay in Scala...
 *
 * @author swilliams
 * @author <a href="http://www.campudus.com/">Joern Bernhardt</a>
 */
class RouteMatcher(val internal: JRouteMatcher = new JRouteMatcher()) extends Handler[HttpServerRequest] with (HttpServerRequest => Unit) with Wrap {

  def all(uri: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.all(uri, wrapHandler(handler)))

  def allWithRegEx(regex: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.allWithRegEx(regex, wrapHandler(handler)))

  def apply(request: HttpServerRequest): Unit = handle(request)

  def connect(uri: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.connect(uri, wrapHandler(handler)))

  def connectWithRegEx(regex: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.connectWithRegEx(regex, wrapHandler(handler)))

  def delete(uri: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.delete(uri, wrapHandler(handler)))

  def deleteWithRegEx(regex: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.deleteWithRegEx(regex, wrapHandler(handler)))

  def get(uri: String, handler: Handler[HttpServerRequest]): RouteMatcher =
    wrap(internal.get(uri, wrapHandler(handler)))

  def get(uri: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.get(uri, wrapHandler(handler)))

  def getWithRegEx(regex: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.getWithRegEx(regex, wrapHandler(handler)))

  def handle(request: HttpServerRequest): Unit = internal.handle(request.toJava)

  def head(uri: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.head(uri, wrapHandler(handler)))

  def headWithRegEx(regex: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.headWithRegEx(regex, wrapHandler(handler)))

  def options(uri: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.options(uri, wrapHandler(handler)))

  def optionsWithRegEx(regex: String, handler: HttpServerRequest => Unit): RouteMatcher =

    wrap(internal.optionsWithRegEx(regex, wrapHandler(handler)))

  def patch(uri: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.patch(uri, wrapHandler(handler)))

  def patchWithRegEx(regex: String, handler: HttpServerRequest => Unit): RouteMatcher =

    wrap(internal.patchWithRegEx(regex, wrapHandler(handler)))

  def post(uri: String, handler: Handler[HttpServerRequest]): RouteMatcher =
    wrap(internal.post(uri, wrapHandler(handler)))

  def post(uri: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.post(uri, wrapHandler(handler)))

  def postWithRegEx(regex: String, handler: HttpServerRequest => Unit): RouteMatcher =

    wrap(internal.postWithRegEx(regex, wrapHandler(handler)))

  def put(uri: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.put(uri, wrapHandler(handler)))

  def putWithRegEx(regex: String, handler: HttpServerRequest => Unit): RouteMatcher =

    wrap(internal.putWithRegEx(regex, wrapHandler(handler)))

  def trace(uri: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.trace(uri, wrapHandler(handler)))

  def traceWithRegEx(regex: String, handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.traceWithRegEx(regex, wrapHandler(handler)))

  private def wrapHandler(handler: HttpServerRequest => Unit) =
    fnToHandler(handler.compose(HttpServerRequest.apply))

  def noMatch(handler: Handler[HttpServerRequest]): RouteMatcher =
    wrap(internal.noMatch(wrapHandler(handler)))

  def noMatch(handler: HttpServerRequest => Unit): RouteMatcher =
    wrap(internal.noMatch(wrapHandler(handler)))
}