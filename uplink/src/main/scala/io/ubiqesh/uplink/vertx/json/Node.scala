package io.ubiqesh.uplink.vertx.json

import io.ubiqesh.uplink.common.Path
import org.vertx.java.core.json.JsonObject
import io.ubiqesh.uplink.vertx.event.changelog.{ChangeLogBuilder, ChangeLog}

object Node {

  def getObjectForPath(currentNode: JsonObject, path: Path): AnyRef = {
    if (currentNode.getFieldNames.contains(path.getFirstElement)) {
      val obj = currentNode.toMap.get(path.getFirstElement)
      if (obj.isInstanceOf[JsonObject]) {
        return getObjectForPath(obj.asInstanceOf[JsonObject], path.getSubpath(1))
      }
      else {
        currentNode.getField(path.getFirstElement)
      }
    } else {
      if (path.getFirstElement == null) {
        return currentNode
      }
      val node = new JsonObject()
      currentNode.putValue(path.getFirstElement, node)
      return getObjectForPath(node.asInstanceOf[JsonObject], path.getSubpath(1))
    }
  }

  def getNodeForPath(currentNode: JsonObject, log: ChangeLog, path: Path): JsonObject = {
    var node: JsonObject = null
    val firstElement = path.getFirstElement
    if (firstElement == null || firstElement.isEmpty) {
      return currentNode
    }
    if (currentNode.getFieldNames.contains(firstElement)) {
      val element = currentNode.getElement(firstElement)
      if (element.isObject) {
        node = element.asObject()
      } else {
        node = new JsonObject()
        log.addChildAddedLogEntry(firstElement, path, path.getParent, node, false, 0)
        currentNode.putValue(firstElement, node)
      }
    } else {
      node = new JsonObject()
      log.addChildAddedLogEntry(firstElement, path, path.getParent, node, false, 0)
      currentNode.putValue(firstElement, node)
    }
    if (path.isSimple) {
      if (currentNode.getFieldNames.contains(firstElement)) {
        val element = currentNode.getElement(firstElement)
        if (element.isObject) {
          node = element.asObject()
        } else {
          node = new JsonObject()
          log.addChildAddedLogEntry(firstElement, path, path.getParent, node, false, 0)
          currentNode.putValue(firstElement, node)
        }
      } else {
        node = new JsonObject()
        log.addChildAddedLogEntry(firstElement, path, path.getParent, node, false, 0)
        currentNode.putValue(firstElement, node)
      }
      return node
    }
    val subpath = path.getSubpath(1)
    if (subpath.isEmtpy) {
      currentNode
    } else {
      getNodeForPath(node, log, subpath)
    }
  }

  def getNode(currentNode: JsonObject, key: String): JsonObject = {
    val `object` = currentNode.getField(key)
    if (`object`.isInstanceOf[JsonObject]) {
      return `object`.asInstanceOf[JsonObject]
    }
    throw new RuntimeException("Node[" + key + "] is not a Node. " + toString())
  }

  def putValue(node: JsonObject, key: String, value: AnyRef) {
    node.putValue(key, value)
  }

  def accept(node: JsonObject, path: Path, visitor: NodeVisitor) {
    visitor.visitNode(path, node)
    val it = node.getFieldNames.iterator()
    while (it.hasNext) {
      val key = it.next()
      val value = node.getField(key)
      if (value.isInstanceOf[JsonObject]) {
        accept(value.asInstanceOf[JsonObject], path.append(key), visitor)
      } else {
        visitor.visitProperty(path, node, key, value)
      }
    }
  }

  def clear(node: JsonObject) {
    val it = node.getFieldNames.iterator()
    while (it.hasNext) {
      val key = it.next()
      node.removeField(key)
    }
  }

  def pathExists(node: JsonObject, path: Path): Boolean = {
    if (path.isEmtpy) {
      true
    } else if (node.getFieldNames.contains(path.getFirstElement)) {
      val `object` = node.getField(path.getFirstElement)
      if (`object`.isInstanceOf[JsonObject]) {
        val node = `object`.asInstanceOf[JsonObject]
        pathExists(node, path.getSubpath(1))
      } else if (path.isSimple) {
        true
      } else {
        false
      }
    } else {
      false
    }
  }

  /**
   *
   * Populates the node with the handed json data
   *
   * @param node to populate
   * @param json JSON Data as String
   */
  def populate(node: JsonObject, json: String) {
    try {
      val obj = new JsonObject(json)
      node.mergeIn(obj)
    }
    catch {
      case e: Exception => System.out.println(json); e.printStackTrace()
    }
  }

  /**
   *
   * Populates the node with the handed json data. Also fills the changeLog with the changes done by the population.
   *
   * @param logBuilder log builder for the changelog
   * @param currentNode node to populate
   * @param payload data to prefill
   */
  def populate(logBuilder: ChangeLogBuilder, currentNode: JsonObject, payload: JsonObject) {
    val it = currentNode.getFieldNames.iterator()
    while (it.hasNext) {
      val key = it.next()
      val value = payload.getField(key)
      if (value.isInstanceOf[JsonObject]) {
        val childNode = new JsonObject()
        populate(logBuilder.getChildLogBuilder(key), childNode, value.asInstanceOf[JsonObject])
        if (currentNode.getFieldNames.contains(key)) {
          currentNode.putValue(key, childNode)
          logBuilder.addNew(key, childNode)
        } else {
          currentNode.putValue(key, childNode)
          logBuilder.addChangedNode(key, childNode)
        }
      } else {
        if (currentNode.getFieldNames.contains(key)) {
          logBuilder.addChange(key, value)
        } else {
          logBuilder.addNew(key, value)
        }
        if (value == null) {
          logBuilder.addRemoved(key, currentNode.getField(key))
        }
        currentNode.putValue(key, value)
      }
    }
  }
}
