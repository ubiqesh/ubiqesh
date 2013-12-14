package io.yagni.edge.vertx.event

import org.vertx.java.core.json.JsonObject

object EventBuilder {

  def start(): EventBuilder = new EventBuilder()
}

class EventBuilder private() {

  private var underConstruction: StateChangeEvent = new StateChangeEvent()

  def build(): StateChangeEvent = this.underConstruction

  def withNode(no: JsonObject): JsonObject = {
    val node = new JsonObject()
    node.mergeIn(no)
    underConstruction.putValue(StateChangeEvent.PAYLOAD, node)
    node
  }

  def withPayload(payload: AnyRef): EventBuilder = {
    underConstruction.putValue(StateChangeEvent.PAYLOAD, payload)
    this
  }

  def `type`(`type`: StateChangeEventType.StateChangeEventType): EventBuilder = {
    underConstruction.putValue(StateChangeEvent.TYPE, `type`.toString)
    this
  }

  def path(path: String): EventBuilder = {
    underConstruction.putValue(StateChangeEvent.PATH, path)
    this
  }

  def name(name: String): EventBuilder = {
    underConstruction.putValue(StateChangeEvent.NAME, name)
    this
  }
}
