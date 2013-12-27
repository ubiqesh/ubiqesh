package io.ubiqesh.edge.vertx

import java.util.Set
import io.ubiqesh.edge.disruptor.{Distributor, UbiqeshDisruptor}
import io.ubiqesh.edge.persistence.inmemory.InMemoryPersistence
import io.ubiqesh.edge.vertx.event.StateChangeEvent
import io.ubiqesh.edge.vertx.event.StateChangeEventType
import io.ubiqesh.edge.vertx.event.changelog.ChangeLog
import io.ubiqesh.edge.vertx.messaging.Endpoint
import org.vertx.java.core.VertxFactory
import org.vertx.java.core.http.RouteMatcher
import scala.reflect.BeanProperty
import scala.collection.mutable

//remove if not needed

import scala.collection.JavaConversions._

object UbiqeshEdgeServer extends App {
  new EdgeServer(8080).run()
  while (true) {
  }
}

class EdgeServer(private var port: Int) {

  @BeanProperty
  var persistence: InMemoryPersistence = new InMemoryPersistence(this)

  var disruptor: UbiqeshDisruptor = new UbiqeshDisruptor(persistence)

  var endpoints: Set[Endpoint] = new mutable.HashSet[Endpoint]()

  def run() {
    val vertx = VertxFactory.newVertx()
    val server = vertx.createHttpServer()
    val rsh = new EdgeServerHandler(vertx, this)
    server.requestHandler(createRoutMatcher(rsh))
    server.websocketHandler(rsh.getWebsocketHandler)
    server.listen(8080)
  }

  private def createRoutMatcher(rsh: EdgeServerHandler): RouteMatcher = {
    val rm = new RouteMatcher()
    rm.getWithRegEx("(.)*ubiqesh.js", rsh.getUbiqeshFileHttpHandler)
    rm.allWithRegEx("\\/api\\/1(.)*", rsh.getRestHttpHandler)
    rm.noMatch(rsh.getWebUIHandler)
    rm
  }

  def handle(stateChangeEvent: StateChangeEvent) {
    this.disruptor.handleEvent(stateChangeEvent)
  }

  def handleEvent(`type`: StateChangeEventType.StateChangeEventType, nodePath: String, value: AnyRef) {
    val stateChangeEvent = new StateChangeEvent(`type`, nodePath, value)
    handle(stateChangeEvent)
  }

  def distributeChangeLog(changeLog: ChangeLog) {
    for (endpoint <- this.endpoints) {
      endpoint.distributeChangeLog(changeLog.getLog)
    }
  }

  def addEndpoint(endpoint: Endpoint) {
    this.disruptor.addEndpoint(endpoint)
    this.endpoints.add(endpoint)
  }

  def removeEndpoint(endpoint: Endpoint) {
    this.disruptor.removeEndpoint(endpoint)
    this.endpoints.remove(endpoint)
  }

  def getDistributor(): Distributor = this.disruptor.getDistributor
}
