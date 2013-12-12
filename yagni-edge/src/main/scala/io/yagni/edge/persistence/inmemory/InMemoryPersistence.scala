package io.yagni.edge.persistence.inmemory

import io.yagni.edge.vertx.EdgeServer
import io.yagni.edge.vertx.messaging.Endpoint
import org.slf4j.LoggerFactory
import io.yagni.edge.common.Path
import io.yagni.edge.vertx.event.changelog.ChangeLog
import io.yagni.edge.vertx.event.changelog.ChangeLogBuilder
import io.yagni.edge.vertx.json.NodeVisitor
import io.yagni.edge.persistence.Persistence
import io.yagni.edge.persistence.queries.QueryEvaluator

import scala.collection.JavaConversions._
import org.vertx.java.core.json.JsonObject

class InMemoryPersistence(private var yagni: EdgeServer) extends Persistence {
  val logger = LoggerFactory.getLogger(classOf[InMemoryPersistence])

  private class ChildRemovedSubTreeVisitor(private var log: ChangeLog) extends NodeVisitor {

    override def visitProperty(path: Path,
                               node: JsonObject,
                               key: String,
                               value: AnyRef) {
      log.addChildRemovedLogEntry(path, key, value)
    }

    override def visitNode(path: Path, node: JsonObject) {
      log.addChildRemovedLogEntry(path.getParent, path.getLastElement, null)
    }
  }

  private val model: JsonObject = new JsonObject()

  override def get(path: Path): AnyRef = {
    if (path == null || path.isEmtpy) {
      model
    } else {
      io.yagni.edge.vertx.json.Node.getObjectForPath(model, path)
    }
  }

  override def getNode(path: Path): JsonObject = {
    val log = new ChangeLog()
    val nodeForPath = io.yagni.edge.vertx.json.Node.getNodeForPath(model, log, path)
    yagni.distributeChangeLog(log)
    nodeForPath
  }

  override def remove(log: ChangeLog, path: Path) {
    val nodeName = path.getLastElement
    val parentPath = path.getParent
    val parentNode = io.yagni.edge.vertx.json.Node.getNodeForPath(model, log, parentPath)
    val node = io.yagni.edge.vertx.json.Node.getNode(parentNode, nodeName)
    val parent = io.yagni.edge.vertx.json.Node.getNodeForPath(model, log, parentPath)
    io.yagni.edge.vertx.json.Node.accept(node, path, new ChildRemovedSubTreeVisitor(log))
    parent.removeField(nodeName)
    log.addChildRemovedLogEntry(parentPath, nodeName, node)
    yagni.distributeChangeLog(log)
  }

  override def syncPath(path: Path, handler: Endpoint) {
    val log = new ChangeLog()
    val node = io.yagni.edge.vertx.json.Node.getNodeForPath(model, log, path)
    for (childNodeKey <- node.getFieldNames) {
      val element = node.getElement(childNodeKey)
      if(element != null)
      {
        if(element.isObject)
        {
          val obj = element.asObject()
          val hasChildren = !obj.getFieldNames().isEmpty
          val numChildren = obj.size()
          handler.fireChildAdded(childNodeKey, path, path.getParent, obj, hasChildren, numChildren)
        }
        else
        {
          handler.fireChildAdded(childNodeKey, path, path.getParent, node.getValue(childNodeKey), false, 0)
        }
      }
    }
    yagni.distributeChangeLog(log)
  }

  override def syncPathWithQuery(path: Path,
                                 handler: Endpoint,
                                 queryEvaluator: QueryEvaluator,
                                 query: String) {
    val log = new ChangeLog()
    val node = io.yagni.edge.vertx.json.Node.getNodeForPath(model, log, path)
    for (childNodeKey <- node.getFieldNames) {
      val `object` = node.getField(childNodeKey)
      if (queryEvaluator.evaluateQueryOnValue(`object`, query)) {
        if (`object` != null && `object` != Nil) {
          handler.fireQueryChildAdded(path, node, `object`)
        }
      }
    }
    yagni.distributeChangeLog(log)
  }

  override def syncPropertyValue(path: Path, handler: Endpoint) {
    val log = new ChangeLog()
    val node = io.yagni.edge.vertx.json.Node.getNodeForPath(model, log, path.getParent)
    val childNodeKey = path.getLastElement
    if (node.getFieldNames.contains(path.getLastElement)) {
      val `object` = node.getField(path.getLastElement)
      handler.fireValue(childNodeKey, path, path.getParent, `object`)
    } else {
      handler.fireValue(childNodeKey, path, path.getParent, "")
    }
    yagni.distributeChangeLog(log)
  }

  override def updateValue(log: ChangeLog,
                           path: Path,
                           payload: AnyRef) {
    var node: JsonObject = null
    var created = false
    if (!io.yagni.edge.vertx.json.Node.pathExists(model, path)) {
      created = true
    }
    val parent = io.yagni.edge.vertx.json.Node.getNodeForPath(model, log, path.getParent)
    if (payload.isInstanceOf[JsonObject]) {
      if (parent.getFieldNames.contains(path.getLastElement)) {
      } else {
        node = new JsonObject()
        parent.putValue(path.getLastElement, node)
      }
      io.yagni.edge.vertx.json.Node.populate(new ChangeLogBuilder(log, path, path.getParent, node), node, payload.asInstanceOf[JsonObject])
      if (created) {
        log.addChildAddedLogEntry(path.getLastElement, path.getParent, path.getParent.getParent, payload,
          false, 0)
      } else {
        log.addChildChangedLogEntry(path.getLastElement, path.getParent, path.getParent.getParent, payload,
          false, 0)
      }
    } else {
      parent.putValue(path.getLastElement, payload)
      if (created) {
        log.addChildAddedLogEntry(path.getLastElement, path.getParent, path.getParent.getParent, payload,
          false, 0)
      } else {
        log.addChildChangedLogEntry(path.getLastElement, path.getParent, path.getParent.getParent, payload,
          false, 0)
        log.addValueChangedLogEntry(path.getLastElement, path, path.getParent, payload)
      }
      log.addChildChangedLogEntry(path.getParent.getLastElement, path.getParent.getParent, path.getParent.getParent.getParent,
        parent, false, 0)
    }
    logger.trace("Model changed: " + model)
  }

  override def applyNewValue(log: ChangeLog,
                             path: Path,
                             payload: AnyRef) {
    try {
      var created = false
      if (!io.yagni.edge.vertx.json.Node.pathExists(model,path)) {
        created = true
      }
      val parent = io.yagni.edge.vertx.json.Node.getNodeForPath(model, log, path.getParent)
      if (payload.isInstanceOf[JsonObject]) {
        val node = new JsonObject()
        populate(new ChangeLogBuilder(log, path, path.getParent, node), path, node, payload.asInstanceOf[JsonObject])
        parent.putValue(path.getLastElement, node)
      } else {
        parent.putValue(path.getLastElement, payload)
      }
      if (created) {
        log.addChildAddedLogEntry(path.getLastElement, path.getParent, path.getParent.getParent, payload,
          false, 0)
      } else {
        addChangeEvent(log, path)
      }
      var currentPath = path
      while (!currentPath.isSimple) {
        log.addValueChangedLogEntry(currentPath.getLastElement, currentPath, currentPath.getParent, io.yagni.edge.vertx.json.Node.getObjectForPath(model, currentPath))
        currentPath = currentPath.getParent
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()

      }
    }
  }

  def populate(logBuilder: ChangeLogBuilder,
               path: Path,
               node: JsonObject,
               payload: JsonObject) {
    for (key <- payload.getFieldNames) {
      val obj: AnyRef = payload.toMap.get(key)
      if (obj.isInstanceOf[JsonObject]) {
        val childNode = new JsonObject()
        populate(logBuilder.getChildLogBuilder(key), path.append(key), childNode, payload.getObject(key))
        if (node.getFieldNames.contains(key)) {
          node.putValue(key, childNode)
          logBuilder.addNew(key, childNode)
        } else {
          node.putValue(key, childNode)
          logBuilder.addChangedNode(key, childNode)
        }
      } else {
        val value: AnyRef = payload.getField(key)
        logBuilder.addNew(key, value)
        logBuilder.addChange(key, value)
        if (value == null) {
          logBuilder.addRemoved(key, node.getField(key))
        }
        node.putValue(key, value)
      }
    }
  }

  private def addChangeEvent(log: ChangeLog, path: Path) {
    val payload = io.yagni.edge.vertx.json.Node.getObjectForPath(model, path)
    val parent = io.yagni.edge.vertx.json.Node.getNodeForPath(model, log, path.getParent)
    log.addChildChangedLogEntry(path.getLastElement, path.getParent, path.getParent.getParent, payload,
      hasChildren(payload), childCount(payload))
    log.addValueChangedLogEntry(path.getLastElement, path.getParent, path.getParent.getParent, payload)
    if (!path.isEmtpy) {
      addChangeEvent(log, path.getParent)
    }
  }
  def childCount(node: AnyRef): Long = {
    if ((node.isInstanceOf[JsonObject])) node.asInstanceOf[JsonObject].getFieldNames().size else 0
  }

  def hasChildren(node: AnyRef): Boolean = {
    if ((node.isInstanceOf[JsonObject])) !node.asInstanceOf[JsonObject].getFieldNames.isEmpty else false
  }
}
