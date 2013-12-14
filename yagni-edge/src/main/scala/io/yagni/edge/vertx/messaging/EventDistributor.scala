package io.yagni.edge.vertx.messaging

import io.yagni.edge.common.Path
import io.yagni.edge.vertx.event.StateChangeEvent
import org.vertx.java.core.json.JsonObject

trait EventDistributor {

  def distribute(event: StateChangeEvent): Unit

  def distributeEvent(path: Path, payload: JsonObject): Unit
}
