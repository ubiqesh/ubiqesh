package io.ubiqesh.edge.vertx.messaging

import io.ubiqesh.edge.common.Path
import io.ubiqesh.edge.vertx.event.StateChangeEvent
import org.vertx.java.core.json.JsonObject

trait EventDistributor {

  def distribute(event: StateChangeEvent): Unit

  def distributeEvent(path: Path, payload: JsonObject): Unit
}
