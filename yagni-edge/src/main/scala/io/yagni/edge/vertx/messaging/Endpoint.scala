package io.yagni.edge.vertx.messaging

import java.util.List
import io.yagni.edge.vertx.EdgeServer
import io.yagni.edge.vertx.event.StateChangeEvent
import io.yagni.edge.vertx.event.StateChangeEventType
import org.slf4j.LoggerFactory
import io.yagni.edge.common.Path
import io.yagni.edge.vertx.event.changelog.ChangeLog
import io.yagni.edge.vertx.event.changelog.ChildAddedLogEvent
import io.yagni.edge.vertx.event.changelog.ChildChangedLogEvent
import io.yagni.edge.vertx.event.changelog.ChildRemovedLogEvent
import io.yagni.edge.vertx.event.changelog.ValueChangedLogEvent
import io.yagni.edge.persistence.Persistence
import io.yagni.edge.persistence.queries.QueryEvaluator
import Endpoint._
import scala.reflect.BooleanBeanProperty
import io.yagni.edge.vertx.rpc.{Rpc, Param, Method}
import scala.collection.mutable
import java.util
import org.vertx.java.core.json.JsonObject

//remove if not needed

import scala.collection.JavaConversions._

object Endpoint {

  private val QUERY_CHILD_REMOVED = "query_child_removed"

  private val QUERY_CHILD_CHANGED = "query_child_changed"

  private val QUERY_CHILD_ADDED = "query_child_added"

  private val CHILD_REMOVED = "child_removed"

  private val CHILD_MOVED = "child_moved"

  private val VALUE = "value"

  private val CHILD_CHANGED = "child_changed"

  private val CHILD_ADDED = "child_added"

  private val LOGGER = LoggerFactory.getLogger(classOf[Endpoint])
}

class Endpoint(private var yagniSocket: OutboundSocket, private var persistence: Persistence, private var yagni: EdgeServer)
  extends EventDistributor with OutboundSocket {

  private var attached_listeners: mutable.MultiMap[String, String] = new mutable.HashMap[String, mutable.Set[String]] with mutable.MultiMap[String, String]

  private var basePath: String = _

  private var queryEvaluator: QueryEvaluator = new QueryEvaluator()

  private var disconnectEvents: List[StateChangeEvent] = new util.ArrayList[StateChangeEvent]()

  @BooleanBeanProperty
  var open: Boolean = true

  private var rpc: Rpc = new Rpc()

  this.rpc.register(this)

  override def distribute(event: StateChangeEvent) {
    if (open) {
      if (event.getType == StateChangeEventType.EVENT) {
        var jsonObject: JsonObject = null
        val `object` = event.getField(StateChangeEvent.PAYLOAD)
        if (`object`.isInstanceOf[JsonObject]) {
          jsonObject = event.getObject(StateChangeEvent.PAYLOAD)
          distributeEvent(event.extractNodePath(), jsonObject)
        } else if (`object`.isInstanceOf[String]) {
          val node = new JsonObject()
          io.yagni.edge.vertx.json.Node.populate(node, `object`.asInstanceOf[String])
          distributeEvent(event.extractNodePath(), node)
        }
      } else {
        processQuery(event)
        val changeLog = event.getChangeLog
        distributeChangeLog(changeLog)
      }
    }
  }

  def distributeChangeLog(changeLog: ChangeLog) {
    for (logE <- changeLog.getLog) {
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

  private def processQuery(event: StateChangeEvent) {
    var nodePath = event.extractNodePath()
    if (!(persistence.get(nodePath).isInstanceOf[JsonObject])) {
      nodePath = nodePath.getParent
    }
    if (hasQuery(nodePath.getParent)) {
      val queries:mutable.MultiMap[String, String] = queryEvaluator.getQueries
      for(key <- queries.keySet) {
        val queryStrs = queries.get(key).get
        for( queryStr <- queryStrs) {
          if (event.getPayload != null) {
            val nodeValue = persistence.getNode(nodePath)
            val parent = persistence.getNode(nodePath.getParent)
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
    broadcast.putValue(StateChangeEvent.PAYLOAD, checkPayload(path, node))
    broadcast.putBoolean("hasChildren", hasChildren)
    broadcast.putNumber("numChildren", numChildren)
    yagniSocket.send(broadcast.toString)
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
      broadcast.putValue(StateChangeEvent.PAYLOAD, checkPayload(path, node))
      broadcast.putBoolean("hasChildren", hasChildren)
      broadcast.putNumber("numChildren", numChildren)
      yagniSocket.send(broadcast.toString)
    }
  }

  def fireChildRemoved(path: Path, name: String, payload: AnyRef) {
    val broadcast = new JsonObject()
    broadcast.putValue(StateChangeEvent.TYPE, CHILD_REMOVED)
    broadcast.putValue(StateChangeEvent.NAME, name)
    broadcast.putValue(StateChangeEvent.PATH, createPath(path))
    broadcast.putValue(StateChangeEvent.PAYLOAD, checkPayload(path, payload))
    yagniSocket.send(broadcast.toString)
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
    broadcast.putValue(StateChangeEvent.PAYLOAD, checkPayload(path, value))
    yagniSocket.send(broadcast.toString)
  }

  def fireQueryChildAdded(path: Path, parent: JsonObject, value: AnyRef) {
    val broadcast = new JsonObject()
    broadcast.putValue(StateChangeEvent.TYPE, QUERY_CHILD_ADDED)
    broadcast.putValue("name", path.getLastElement)
    broadcast.putValue(StateChangeEvent.PATH, createPath(path.getParent))
    broadcast.putValue("parent", createPath(path.getParent.getParent))
    broadcast.putValue(StateChangeEvent.PAYLOAD, checkPayload(path, value))
    yagniSocket.send(broadcast.toString)
  }

  def fireQueryChildChanged(path: Path, parent: JsonObject, value: AnyRef) {
    if (value != null && value != Nil) {
      val broadcast = new JsonObject()
      broadcast.putValue(StateChangeEvent.TYPE, QUERY_CHILD_CHANGED)
      broadcast.putValue("name", path.getLastElement)
      broadcast.putValue(StateChangeEvent.PATH, createPath(path.getParent))
      broadcast.putValue("parent", createPath(path.getParent.getParent))
      broadcast.putValue(StateChangeEvent.PAYLOAD, checkPayload(path, value))
      yagniSocket.send(broadcast.toString)
    }
  }

  def fireQueryChildRemoved(path: Path, payload: AnyRef) {
    val broadcast = new JsonObject()
    broadcast.putValue(StateChangeEvent.TYPE, QUERY_CHILD_REMOVED)
    broadcast.putValue(StateChangeEvent.NAME, path.getLastElement)
    broadcast.putValue(StateChangeEvent.PATH, createPath(path.getParent))
    broadcast.putValue(StateChangeEvent.PAYLOAD, checkPayload(path, payload))
    yagniSocket.send(broadcast.toString)
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
      yagniSocket.send(broadcast.toString)
    }
  }

  private def checkPayload(path: Path, value: AnyRef): AnyRef = {
    if (value.isInstanceOf[JsonObject]) {
      val org = value.asInstanceOf[JsonObject]
      val node = new JsonObject()
      for (key <- org.getFieldNames) {
        node.putValue(key, checkPayload(path.append(key), org.getField(key)))
      }
      node
    } else {
      value
    }
  }

  private def createPath(path: String): String = {
    var workPath = path
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

  private def createPath(path: Path): String = createPath(path.toString)

  def addListener(path: Path, listenerType: String) {
    attached_listeners.addBinding(path.toString, listenerType)
  }

  def removeListener(path: Path, listenerType: String) {
    attached_listeners.removeBinding(path.toString, listenerType)
  }

  private def hasListener(path: Path, `type`: String): Boolean = {
    attached_listeners.entryExists("/", _ == `type`)
    if (path.isEmtpy) {
      attached_listeners.containsKey("/") && attached_listeners.entryExists("/", _ == `type`)
    } else {
      attached_listeners.containsKey(path.toString) &&
        attached_listeners.entryExists(path.toString, _ == `type`)
    }
  }

  def addQuery(path: Path, query: String) {
    queryEvaluator.addQuery(path, query)
  }

  def removeQuery(path: Path, query: String) {
    queryEvaluator.removeQuery(path, query)
  }

  def hasQuery(path: Path): Boolean = queryEvaluator.hasQuery(path)

  def registerDisconnectEvent(stateChangeEvent: StateChangeEvent) {
    disconnectEvents.add(stateChangeEvent.copy())
  }

  def executeDisconnectEvents() {
    for (event <- disconnectEvents) {
      yagni.handle(event)
    }
  }

  override def send(msg: String) {
    yagniSocket.send(msg)
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
    addListener(new Path(StateChangeEvent.extractPath(path)), eventType)
    if ("child_added" == eventType) {
      this.persistence.syncPath(new Path(StateChangeEvent.extractPath(path)), this)
    } else if ("value" == eventType) {
      this.persistence.syncPropertyValue(new Path(StateChangeEvent.extractPath(path)), this)
    }
  }

  @Method
  def detachListener(@Param("path") path: String, @Param("event_type") eventType: String) {
    LOGGER.trace("detachListener")
    removeListener(new Path(StateChangeEvent.extractPath(path)), eventType)
  }

  @Method
  def attachQuery(@Param("path") path: String, @Param("query") query: String) {
    LOGGER.trace("attachQuery")
    addQuery(new Path(StateChangeEvent.extractPath(path)), query)
    this.persistence.syncPathWithQuery(new Path(StateChangeEvent.extractPath(path)), this, new QueryEvaluator(),
      query)
  }

  @Method
  def detachQuery(@Param("path") path: String, @Param("query") query: String) {
    LOGGER.trace("detachQuery")
    removeQuery(new Path(StateChangeEvent.extractPath(path)), query)
  }

  @Method
  def event(@Param("path") path: String, @Param("data") data: JsonObject) {
    LOGGER.trace("event")
    this.yagni.getDistributor().distribute(path, data)
  }

  @Method
  def push(@Param("path") path: String, @Param("name") name: String, @Param("data") data: JsonObject) {
    LOGGER.trace("push")
    val event = new StateChangeEvent(StateChangeEventType.PUSH, path + "/" + name, data)
    this.yagni.handle(event)
  }

  @Method
  def set(@Param("path") path: String, @Param("data") data: AnyRef, @Param(value = "priority", defaultValue = "-1") priority: java.lang.Integer) {
    LOGGER.trace("set")
    val event = new StateChangeEvent(StateChangeEventType.SET, path, data, priority)
    this.yagni.handle(event)
  }

  @Method
  def update(@Param("path") path: String, @Param("data") data: JsonObject) {
    LOGGER.trace("update")
    val event = new StateChangeEvent(StateChangeEventType.UPDATE, path, data)
    this.yagni.handle(event)
  }

  @Method
  def setPriority(@Param("path") path: String, @Param("priority") priority: java.lang.Integer) {
    LOGGER.trace("setPriority")
    val event = new StateChangeEvent(StateChangeEventType.SETPRIORITY, path, priority)
    this.yagni.handle(event)
  }

  @Method
  def pushOnDisconnect(@Param("path") path: String, @Param("name") name: String, @Param("payload") payload: JsonObject) {
    LOGGER.trace("pushOnDisconnect")
    val event = new StateChangeEvent(StateChangeEventType.PUSH, path + "/" + name, payload)
    this.disconnectEvents.add(event)
  }

  @Method
  def setOnDisconnect(@Param("path") path: String, @Param("data") data: JsonObject, @Param(value = "priority", defaultValue = "-1") priority: java.lang.Integer) {
    LOGGER.trace("setOnDisconnect")
    val event = new StateChangeEvent(StateChangeEventType.SET, path, data, priority)
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

  def handle(msg: String) {
    rpc.handle(msg, this);
  }

  def childCount(node: AnyRef): Long = {
    if ((node.isInstanceOf[JsonObject])) node.asInstanceOf[JsonObject].getFieldNames().size else 0
  }

  def hasChildren(node: AnyRef): Boolean = {
    if ((node.isInstanceOf[JsonObject])) !node.asInstanceOf[JsonObject].getFieldNames().isEmpty else false
  }
}
