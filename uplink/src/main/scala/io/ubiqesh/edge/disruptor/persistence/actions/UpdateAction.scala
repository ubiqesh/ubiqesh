package io.ubiqesh.edge.disruptor.persistence.actions

import io.ubiqesh.edge.vertx.event.StateChangeEvent
import io.ubiqesh.edge.vertx.json.Node
import io.ubiqesh.edge.persistence.Persistence
import org.vertx.java.core.json.JsonObject

class UpdateAction(private var persistence: Persistence) {

  def handle(event: StateChangeEvent) {
    val path = event.extractNodePath()
    var payload: JsonObject = null
    if (event.getFieldNames.contains(StateChangeEvent.PAYLOAD)) {
      val obj = event.getField(StateChangeEvent.PAYLOAD)
      if (obj.isInstanceOf[JsonObject]) {
        payload = obj.asInstanceOf[JsonObject]
        if (payload.isInstanceOf[JsonObject]) {
          persistence.updateValue(event.getChangeLog, path, obj)
        }
      }
    } else {
      persistence.remove(event.getChangeLog, path)
    }
  }
}
