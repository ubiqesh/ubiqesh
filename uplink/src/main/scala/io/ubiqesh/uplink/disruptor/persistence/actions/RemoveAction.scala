package io.ubiqesh.uplink.disruptor.persistence.actions

import io.ubiqesh.uplink.vertx.event.StateChangeEvent
import io.ubiqesh.uplink.persistence.Persistence

class RemoveAction(private var persistence: Persistence) {

  def handle(event: StateChangeEvent) {
    val path = event.extractNodePath()
    persistence.remove(event.getChangeLog, path)
  }
}
