package io.ubiqesh.edge.disruptor.persistence.actions

import io.ubiqesh.edge.vertx.event.StateChangeEvent
import io.ubiqesh.edge.persistence.Persistence

class RemoveAction(private var persistence: Persistence) {

  def handle(event: StateChangeEvent) {
    val path = event.extractNodePath()
    persistence.remove(event.getChangeLog, path)
  }
}
