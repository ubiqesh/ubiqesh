package io.ubiqesh.edge.vertx.messaging

import java.util.List
import io.ubiqesh.edge.vertx.EdgeServer
import io.ubiqesh.edge.vertx.event.StateChangeEvent
import io.ubiqesh.edge.vertx.event.StateChangeEventType
import org.slf4j.LoggerFactory
import io.ubiqesh.edge.common.Path
import io.ubiqesh.edge.vertx.event.changelog._
import io.ubiqesh.edge.persistence.Persistence
import io.ubiqesh.edge.persistence.queries.QueryEvaluator
import Endpoint._
import scala.reflect.BooleanBeanProperty
import io.ubiqesh.edge.vertx.rpc.{Rpc, Param, Method}
import scala.collection.mutable
import java.util
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.http.ServerWebSocket

//remove if not needed

import scala.collection.JavaConversions._

/**
 * Endpoint abstraction ( At the moment this class encapsulates an websocket client)
 */
object Endpoint {
  val QUERY_CHILD_REMOVED = "query_child_removed"

  val QUERY_CHILD_CHANGED = "query_child_changed"

  val QUERY_CHILD_ADDED = "query_child_added"

  val CHILD_REMOVED = "child_removed"

  val CHILD_CHANGED = "child_changed"

  val CHILD_ADDED = "child_added"

  val VALUE = "value"

  val LOGGER = LoggerFactory.getLogger(classOf[Endpoint])
}

/**
 * This class is a Endpoint abstraction ( At the moment this class encapsulates an websocket client)
 * @param socket the outbound socket for sending data important for this socket
 * @param persistence the persistence for storing state changes
 * @param server the
 */
class Endpoint(var socket: ServerWebSocket, var persistence: Persistence, var server: EdgeServer)
  extends EventDistributor {

  /**
   * map of the listeners attached. Key is the path, and the value are the events the listeners are attached to
   */
  val attached_listeners: mutable.MultiMap[String, String] = new mutable.HashMap[String, mutable.Set[String]] with mutable.MultiMap[String, String]

  /**
   *
   * the basePath of this endpoint. e.g. the leading part of the url, via which the client connects to the server.
   *
   */
  var basePath: String = _

  /**
   * Queries are evaluated via the Java Scripting API Javascript implementation
   */
  val queryEvaluator: QueryEvaluator = new QueryEvaluator()

  /**
   * Disconnect Events are fired when the client connected to this endpoint is disconnecting
   */
  val disconnectEvents: List[StateChangeEvent] = new util.ArrayList[StateChangeEvent]()

  @BooleanBeanProperty
  var open: Boolean = true

  /**
   * This method is called internally from the disruptor for processing an event
   * @param event
   */
  override def distribute(event: StateChangeEvent) {
    // is the client still connected?
    if (open) {
      // events are simply distributed to the endpoint if a listener is attached
      if (event.getType == StateChangeEventType.EVENT) {
        var jsonObject: JsonObject = null
        val `object` = event.getField(StateChangeEvent.PAYLOAD)
        if (`object`.isInstanceOf[JsonObject]) {
          jsonObject = event.getObject(StateChangeEvent.PAYLOAD)
          distributeEvent(event.extractNodePath(), jsonObject)
        } else if (`object`.isInstanceOf[String]) {
          val node = new JsonObject()
          io.ubiqesh.edge.vertx.json.Node.populate(node, `object`.asInstanceOf[String])
          distributeEvent(event.extractNodePath(), node)
        }
      } else {
        // Data Changes are first evaluated against a query for continuous queries
        processQuery(event)
        // afterwards the changelog is distributed
        distributeChangeLog(event.getChangeLog.getLog)
      }
    }
  }

  /**
   * This method iterates the given log and distributes the log entries depending of registered
   * @param log
   */
  def distributeChangeLog(log: List[ChangeLogEvent]) {
    for (logE <- log) {
      if (logE.isInstanceOf[ChildAddedLogEvent]) {
        val logEvent = logE.asInstanceOf[ChildAddedLogEvent]
        if (hasListener(logEvent.getPath, CHILD_ADDED)) {
          fireChildAdded(logEvent.getName, logEvent.getPath, logEvent.getParent, logEvent.getValue, logEvent.getHasChildren,
            logEvent.getNumChildren)
        }
      }
      if (logE.isInstanceOf[ChildChangedLogEvent]) {
        val logEvent = logE.asInstanceOf[ChildChangedLogEvent]
        if (hasListener(logEvent.getPath, CHILD_CHANGED)) {
          fireChildChanged(logEvent.getName, logEvent.getPath, logEvent.getParent, logEvent.getValue,
            logEvent.getHasChildren, logEvent.getNumChildren)
        }
      }
      if (logE.isInstanceOf[ValueChangedLogEvent]) {
        val logEvent = logE.asInstanceOf[ValueChangedLogEvent]
        if (hasListener(logEvent.getPath, VALUE)) {
          fireValue(logEvent.getName, logEvent.getPath, logEvent.getParent, logEvent.getValue)
        }
      }
      if (logE.isInstanceOf[ChildRemovedLogEvent]) {
        val logEvent = logE.asInstanceOf[ChildRemovedLogEvent]
        if (hasListener(logEvent.getPath, CHILD_REMOVED)) {
          fireChildRemoved(logEvent.getPath, logEvent.getName, logEvent.getValue)
        }
      }
    }
  }

  def processQuery(event: StateChangeEvent) {
    var nodePath = event.extractNodePath()
    if (!(persistence.get(nodePath).isInstanceOf[JsonObject])) {
      nodePath = nodePath.getParent
    }
    if (hasQuery(nodePath.getParent)) {
      val queries: mutable.MultiMap[String, String] = queryEvaluator.getQueries
      for (key <- queries.keySet) {
        val queryStrs = queries.get(key).get
        for (queryStr <- queryStrs) {
          if (event.getPayload != null) {
            val nodeValue = persistence.getJsonObject(nodePath)
            val parent = persistence.getJsonObject(nodePath.getParent)
            val matches = queryEvaluator.evaluateQueryOnValue(nodeValue, queryStr)
            val containsNode = queryEvaluator.queryContainsNode(new Path(key), queryStr, nodePath)
            if (matches) {
              if (!containsNode) {
                fireQueryChildAdded(nodePath, parent, nodeValue)
                queryEvaluator.addNodeToQuery(nodePath.getParent, queryStr, nodePath)
              } else {
                fireQueryChildChanged(nodePath, parent, nodeValue)
              }
            } else if (containsNode) {
              fireQueryChildRemoved(nodePath, nodeValue)
              queryEvaluator.removeNodeFromQuery(nodePath.getParent, queryStr, nodePath)
            }
          } else {
            fireQueryChildRemoved(nodePath, null)
            queryEvaluator.removeNodeFromQuery(nodePath.getParent, queryStr, nodePath)
          }
        }
      }
    }
  }

  def fireChildAdded(name: String,
                     path: Path,
                     parent: Path,
                     node: AnyRef,
                     hasChildren: Boolean,
                     numChildren: Long) {
    val broadcast = new JsonObject()
    broadcast.putValue(StateChangeEvent.TYPE, CHILD_ADDED)
    broadcast.putValue("name", name)
    broadcast.putValue(StateChangeEvent.PATH, createPath(path))
    broadcast.putValue("parent", createPath(parent))
    broadcast.putValue(StateChangeEvent.PAYLOAD, node)
    broadcast.putBoolean("hasChildren", hasChildren)
    broadcast.putNumber("numChildren", numChildren)
    socket.writeTextFrame(broadcast.toString)
  }

  def fireChildChanged(name: String,
                       path: Path,
                       parent: Path,
                       node: AnyRef,
                       hasChildren: Boolean,
                       numChildren: Long) {
    if (node != null && node != Nil) {
      val broadcast = new JsonObject()
      broadcast.putValue(StateChangeEvent.TYPE, CHILD_CHANGED)
      broadcast.putValue("name", name)
      broadcast.putValue(StateChangeEvent.PATH, createPath(path))
      broadcast.putValue("parent", createPath(parent))
      broadcast.putValue(StateChangeEvent.PAYLOAD, node)
      broadcast.putBoolean("hasChildren", hasChildren)
      broadcast.putNumber("numChildren", numChildren)
      socket.writeTextFrame(broadcast.toString)
    }
  }

  def fireChildRemoved(path: Path, name: String, payload: AnyRef) {
    val broadcast = new JsonObject()
    broadcast.putValue(StateChangeEvent.TYPE, CHILD_REMOVED)
    broadcast.putValue(StateChangeEvent.NAME, name)
    broadcast.putValue(StateChangeEvent.PATH, createPath(path))
    broadcast.putValue(StateChangeEvent.PAYLOAD, payload)
    socket.writeTextFrame(broadcast.toString)
  }

  def fireValue(name: String,
                path: Path,
                parent: Path,
                value: AnyRef) {
    val broadcast = new JsonObject()
    broadcast.putValue(StateChangeEvent.TYPE, VALUE)
    broadcast.putValue("name", name)
    broadcast.putValue(StateChangeEvent.PATH, createPath(path))
    broadcast.putValue("parent", createPath(parent))
    broadcast.putValue(StateChangeEvent.PAYLOAD, value)
    socket.writeTextFrame(broadcast.toString)
  }

  def fireQueryChildAdded(path: Path, parent: JsonObject, value: AnyRef) {
    val broadcast = new JsonObject()
    broadcast.putValue(StateChangeEvent.TYPE, QUERY_CHILD_ADDED)
    broadcast.putValue("name", path.getLastElement)
    broadcast.putValue(StateChangeEvent.PATH, createPath(path.getParent))
    broadcast.putValue("parent", createPath(path.getParent.getParent))
    broadcast.putValue(StateChangeEvent.PAYLOAD, value)
    socket.writeTextFrame(broadcast.toString)
  }

  def fireQueryChildChanged(path: Path, parent: JsonObject, value: AnyRef) {
    if (value != null && value != Nil) {
      val broadcast = new JsonObject()
      broadcast.putValue(StateChangeEvent.TYPE, QUERY_CHILD_CHANGED)
      broadcast.putValue("name", path.getLastElement)
      broadcast.putValue(StateChangeEvent.PATH, createPath(path.getParent))
      broadcast.putValue("parent", createPath(path.getParent.getParent))
      broadcast.putValue(StateChangeEvent.PAYLOAD, value)
      socket.writeTextFrame(broadcast.toString)
    }
  }

  def fireQueryChildRemoved(path: Path, payload: AnyRef) {
    val broadcast = new JsonObject()
    broadcast.putValue(StateChangeEvent.TYPE, QUERY_CHILD_REMOVED)
    broadcast.putValue(StateChangeEvent.NAME, path.getLastElement)
    broadcast.putValue(StateChangeEvent.PATH, createPath(path.getParent))
    broadcast.putValue(StateChangeEvent.PAYLOAD, payload)
    socket.writeTextFrame(broadcast.toString)
  }

  override def distributeEvent(path: Path, payload: JsonObject) {
    if (hasListener(path, "event")) {
      val broadcast = new JsonObject()
      broadcast.putValue(StateChangeEvent.TYPE, "event")
      broadcast.putValue(StateChangeEvent.PATH, createPath(path))
      broadcast.putValue(StateChangeEvent.PAYLOAD, payload)
      LOGGER.trace("Distributing Message (basePath: '" + basePath + "',path: '" +
        path +
        "') : " +
        broadcast.toString)
      socket.writeTextFrame(broadcast.toString)
    }
  }

  /**
   *
   * transforms a given Path into a complete Url of the resource addressed by the path
   *
   * @param path
   * @return
   */
  def createPath(path: Path): String = {
    var workPath = path.toString()
    if (workPath.startsWith("/api/1")) {
      workPath = workPath.substring(6)
    } else if (workPath.startsWith("api/1")) {
      workPath = workPath.substring(6)
    }
    if (basePath.endsWith("/") && workPath.startsWith("/")) {
      basePath + workPath.substring(1)
    } else {
      basePath + workPath
    }
  }

  // RPC Helper
  val rpc: Rpc = new Rpc()
  rpc.register(this)

  // Handle the given Message as a RPC Call
  def handle(msg: String) {
    rpc.handle(msg, socket);
  }

  @Method
  def init(@Param("path") path: String) {
    LOGGER.trace("init")
    if (!path.contains("/api/1")) {
      throw new RuntimeException("Path(" + path + ") doesn't show to API endpoint")
    }
    basePath = path.substring(0, path.indexOf("/api/1") + 6)
  }

  @Method
  def attachListener(@Param("path") path: String, @Param("event_type") eventType: String) {
    LOGGER.trace("attachListener")
    attached_listeners.addBinding(new Path(StateChangeEvent.extractPath(path)).toString(), eventType)
    if ("child_added" == eventType) {
      this.persistence.syncPath(new Path(StateChangeEvent.extractPath(path)), this)
    } else if ("value" == eventType) {
      this.persistence.syncPropertyValue(new Path(StateChangeEvent.extractPath(path)), this)
    }
  }

  @Method
  def detachListener(@Param("path") path: String, @Param("event_type") eventType: String) {
    LOGGER.trace("detachListener")
    attached_listeners.removeBinding(new Path(StateChangeEvent.extractPath(path)).toString(), eventType)
  }

  def hasListener(path: Path, `type`: String): Boolean = {
    attached_listeners.entryExists("/", _ == `type`)
    if (path.isEmtpy) {
      attached_listeners.containsKey("/") && attached_listeners.entryExists("/", _ == `type`)
    } else {
      attached_listeners.containsKey(path.toString) &&
        attached_listeners.entryExists(path.toString, _ == `type`)
    }
  }

  @Method
  def attachQuery(@Param("path") path: String, @Param("query") query: String) {
    LOGGER.trace("attachQuery")
    queryEvaluator.addQuery(new Path(StateChangeEvent.extractPath(path)), query)
    this.persistence.syncPathWithQuery(new Path(StateChangeEvent.extractPath(path)), this, new QueryEvaluator(),
      query)
  }

  @Method
  def detachQuery(@Param("path") path: String, @Param("query") query: String) {
    LOGGER.trace("detachQuery")
    queryEvaluator.removeQuery(new Path(StateChangeEvent.extractPath(path)), query)
  }

  def hasQuery(path: Path): Boolean = queryEvaluator.hasQuery(path)

  @Method
  def event(@Param("path") path: String, @Param("data") data: JsonObject) {
    LOGGER.trace("event")
    this.server.getDistributor().distribute(path, data)
  }

  @Method
  def push(@Param("path") path: String, @Param("name") name: String, @Param("data") data: JsonObject) {
    LOGGER.trace("push")
    val event = new StateChangeEvent(StateChangeEventType.PUSH, path + "/" + name, data)
    this.server.handle(event)
  }

  @Method
  def set(@Param("path") path: String, @Param("data") data: AnyRef) {
    LOGGER.trace("set")
    val event = new StateChangeEvent(StateChangeEventType.SET, path, data)
    this.server.handle(event)
  }

  @Method
  def update(@Param("path") path: String, @Param("data") data: JsonObject) {
    LOGGER.trace("update")
    val event = new StateChangeEvent(StateChangeEventType.UPDATE, path, data)
    this.server.handle(event)
  }

  @Method
  def pushOnDisconnect(@Param("path") path: String, @Param("name") name: String, @Param("payload") payload: JsonObject) {
    LOGGER.trace("pushOnDisconnect")
    val event = new StateChangeEvent(StateChangeEventType.PUSH, path + "/" + name, payload)
    this.disconnectEvents.add(event)
  }

  @Method
  def setOnDisconnect(@Param("path") path: String, @Param("data") data: JsonObject) {
    LOGGER.trace("setOnDisconnect")
    val event = new StateChangeEvent(StateChangeEventType.SET, path, data)
    this.disconnectEvents.add(event)
  }

  @Method
  def updateOnDisconnect(@Param("path") path: String, @Param("data") data: JsonObject) {
    LOGGER.trace("updateOnDisconnect")
    val event = new StateChangeEvent(StateChangeEventType.UPDATE, path, data)
    this.disconnectEvents.add(event)
  }

  @Method
  def removeOnDisconnect(@Param("path") path: String) {
    LOGGER.trace("removeOnDisconnect")
    val event = new StateChangeEvent(StateChangeEventType.REMOVE, path)
    this.disconnectEvents.add(event)
  }

  def executeDisconnectEvents() {
    for (event <- disconnectEvents) {
      server.handle(event)
    }
  }
}
