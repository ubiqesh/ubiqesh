package io.yagni.edge.disruptor.persistence.actions

import io.yagni.edge.vertx.event.StateChangeEvent
import io.yagni.edge.persistence.Persistence

class RemoveAction(private var persistence: Persistence) {

  def handle(event: StateChangeEvent) {
    val path = event.extractNodePath()
    persistence.remove(event.getChangeLog, path)
  }
}
