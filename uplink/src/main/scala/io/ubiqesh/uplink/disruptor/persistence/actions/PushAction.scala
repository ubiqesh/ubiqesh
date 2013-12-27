package io.ubiqesh.uplink.disruptor.persistence.actions

import java.util.UUID
import io.ubiqesh.uplink.common.Path
import io.ubiqesh.uplink.vertx.event.StateChangeEvent
import io.ubiqesh.uplink.persistence.Persistence
import org.vertx.java.core.json.JsonObject

class PushAction(private var persistence: Persistence) {

  def handle(event: StateChangeEvent) {
    val path = event.extractNodePath()
    var payload: AnyRef = null
    payload = if (event.getFieldNames.contains(StateChangeEvent.PAYLOAD)) event.getField(StateChangeEvent.PAYLOAD) else new JsonObject()
    var nodeName: String = null
    nodeName = if (event.getFieldNames.contains("name")) event.getString("name")
    else UUID.randomUUID().toString.replaceAll("-", "")
    if (path.isEmtpy) {
      persistence.applyNewValue(event.getChangeLog, new Path(nodeName), payload)
    } else {
      persistence.applyNewValue(event.getChangeLog, path, payload)
    }
  }
}
