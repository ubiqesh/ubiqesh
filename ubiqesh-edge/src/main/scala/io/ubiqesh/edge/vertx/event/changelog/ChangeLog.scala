package io.ubiqesh.edge.vertx.event.changelog

import java.util.List
import io.ubiqesh.edge.common.Path
import scala.reflect.BeanProperty
import java.util

class ChangeLog {

  @BeanProperty
  var log: List[ChangeLogEvent] = new util.ArrayList[ChangeLogEvent]()

  def addLog(event: ChangeLogEvent) {
    log.add(event)
  }

  def addChildAddedLogEntry(name: String,
                            path: Path,
                            parent: Path,
                            value: AnyRef,
                            hasChildren: Boolean,
                            numChildren: Long) {
    log.add(new ChildAddedLogEvent(name, path, parent, value, numChildren))
  }

  def addChildChangedLogEntry(name: String,
                              path: Path,
                              parent: Path,
                              value: AnyRef,
                              hasChildren: Boolean,
                              numChildren: Long) {
    if (name != null) {
      log.add(new ChildChangedLogEvent(name, path, parent, value, numChildren))
    }
  }

  def addValueChangedLogEntry(name: String,
                              path: Path,
                              parent: Path,
                              value: AnyRef) {
    log.add(new ValueChangedLogEvent(name, path, parent, value))
  }

  def addChildRemovedLogEntry(path: Path, name: String, value: AnyRef) {
    log.add(new ChildRemovedLogEvent(path, name, value))
  }

  def clear() {
    log.clear()
  }
}
