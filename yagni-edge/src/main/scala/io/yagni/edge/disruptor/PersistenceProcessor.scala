package io.yagni.edge.disruptor

import com.lmax.disruptor.EventHandler
import io.yagni.edge.disruptor.persistence.actions.PushAction
import io.yagni.edge.disruptor.persistence.actions.RemoveAction
import io.yagni.edge.disruptor.persistence.actions.SetAction
import io.yagni.edge.disruptor.persistence.actions.UpdateAction
import io.yagni.edge.vertx.event.{StateChangeEventType, StateChangeEvent}
import io.yagni.edge.persistence.Persistence

class PersistenceProcessor(persistence: Persistence) extends EventHandler[StateChangeEvent] {

  private var pushAction: PushAction = new PushAction(persistence)

  private var setAction: SetAction = new SetAction(persistence)

  private var removeAction: RemoveAction = new RemoveAction(persistence)

  private var updateAction: UpdateAction = new UpdateAction(persistence)

  override def onEvent(event: StateChangeEvent, sequence: Long, endOfBatch: Boolean) {
    if (event.getType() == StateChangeEventType.PUSH) {
      pushAction.handle(event)
    }
    if (event.getType() == StateChangeEventType.UPDATE) {
      updateAction.handle(event)
    }
    if (event.getType() == StateChangeEventType.SET) {
      setAction.handle(event)
    }
    if (event.getType() == StateChangeEventType.REMOVE) {
      removeAction.handle(event)
    }
  }
}
