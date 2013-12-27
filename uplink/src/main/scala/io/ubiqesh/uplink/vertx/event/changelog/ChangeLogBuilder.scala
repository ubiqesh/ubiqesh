package io.ubiqesh.uplink.vertx.event.changelog

import io.ubiqesh.uplink.common.Path
import org.vertx.java.core.json.JsonObject

//remove if not needed


class ChangeLogBuilder(private var log: ChangeLog,
                       private var path: Path,
                       private var parentPath: Path,
                       private var node: JsonObject) {

  def getChildLogBuilder(childName: String): ChangeLogBuilder = {
    new ChangeLogBuilder(log, path.append(childName), path, io.ubiqesh.uplink.vertx.json.Node.getNode(node,childName))
  }

  def addChange(name: String, value: AnyRef) {
    log.addChildChangedLogEntry(name, path, parentPath, value, hasChildren(value), childCount(value))
    log.addValueChangedLogEntry(name, path.append(name), path, value)
  }

  def addNew(name: String, value: AnyRef) {
    log.addChildAddedLogEntry(name, path, parentPath, value, hasChildren(value), childCount(value))
  }

  def addChangedNode(name: String, value: JsonObject) {
   log.addChildChangedLogEntry(name, path, parentPath, value, hasChildren(value), childCount(value))
  }

  def addRemoved(name: String, value: AnyRef) {
    log.addChildRemovedLogEntry(path, name, value)
  }

  private def childCount(node: AnyRef): Long = {
    if ((node.isInstanceOf[JsonObject])) node.asInstanceOf[JsonObject].getFieldNames().size else 0
  }

  private def hasChildren(node: AnyRef): Boolean = {
    if ((node.isInstanceOf[JsonObject])) node.asInstanceOf[JsonObject].getFieldNames.isEmpty else false
  }
}
