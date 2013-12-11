package io.yagni.edge.vertx.event

import io.yagni.edge.common.Path
import io.yagni.edge.vertx.event.changelog.ChangeLog
import io.yagni.edge.vertx.json.Node
import StateChangeEvent._
import scala.reflect.BeanProperty
import org.vertx.java.core.json.JsonObject

//remove if not needed


object StateChangeEvent {

  val PAYLOAD = "payload"

  val CREATION_DATE = "creationDate"

  val TYPE = "type"

  val PATH = "path"

  val NAME = "name"

  def extractPath(url: String): String = {
    var result = url
    if (url.startsWith("http://")) {
      result = if (url.indexOf("/", 7) != -1) url.substring(url.indexOf("/", 7)) else ""
    } else if (url.startsWith("https://")) {
      result = if (url.indexOf("/", 8) != -1) url.substring(url.indexOf("/", 8)) else ""
    }
    var workPath = if (result.startsWith("/")) result else "/" + result
    if (workPath.startsWith("/api/1")) {
      workPath = workPath.substring(6)
    } else if (workPath.startsWith("api/1")) {
      workPath = workPath.substring(5)
    }
    workPath
  }
}

class StateChangeEvent extends JsonObject {

  @BeanProperty
  var changeLog: ChangeLog = new ChangeLog()

  def this(json: String) {
    this()
    io.yagni.edge.vertx.json.Node.populate(this, json)
  }

  def this(`type`: StateChangeEventType.StateChangeEventType, path: String, payload: AnyRef) {
    this()
    putValue(StateChangeEvent.TYPE, `type`.toString)
    putValue(StateChangeEvent.PATH, path)
  }

  def this(`type`: StateChangeEventType.StateChangeEventType,
           path: String,
           data: AnyRef,
           priority: java.lang.Integer) {
    this()
    putValue(StateChangeEvent.TYPE, `type`.toString)
    putValue(StateChangeEvent.PATH, path)
    putValue(StateChangeEvent.PAYLOAD, data)
  }

  def this(`type`: StateChangeEventType.StateChangeEventType, path: String, priority: java.lang.Integer) {
    this()
    putValue(StateChangeEvent.TYPE, `type`.toString)
    putValue(StateChangeEvent.PATH, path)
  }

  def this(`type`: StateChangeEventType.StateChangeEventType, path: String) {
    this()
    putValue(StateChangeEvent.TYPE, `type`.toString)
    putValue(StateChangeEvent.PATH, path)
  }
  def extractNodePath(): Path = {
    if (!getFieldNames.contains(StateChangeEvent.PATH)) {
      return null
    }
    val requestPath = getField(StateChangeEvent.PATH).asInstanceOf[String]
    if (getFieldNames.contains(StateChangeEvent.NAME)) {
      if (getField(StateChangeEvent.NAME) != Nil) {
        return new Path(extractPath(requestPath)).append(getString(StateChangeEvent.NAME))
      }
    }
    new Path(extractPath(requestPath))
  }

  def getType(): StateChangeEventType.StateChangeEventType = {
    StateChangeEventType.withName(getField(StateChangeEvent.TYPE).asInstanceOf[String].toUpperCase())
  }

  def getPayload(): AnyRef = getField(StateChangeEvent.PAYLOAD)

  def getPath(): AnyRef = getField(StateChangeEvent.PATH)

  override def copy(): StateChangeEvent = {
    new StateChangeEvent(toString)
  }
}
