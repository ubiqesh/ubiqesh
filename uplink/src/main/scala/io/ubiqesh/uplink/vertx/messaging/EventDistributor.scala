package io.ubiqesh.uplink.vertx.messaging

import io.ubiqesh.uplink.common.Path
import io.ubiqesh.uplink.vertx.event.StateChangeEvent
import org.vertx.java.core.json.JsonObject

trait EventDistributor {

  def distribute(event: StateChangeEvent): Unit

  def distributeEvent(path: Path, payload: JsonObject): Unit
}
