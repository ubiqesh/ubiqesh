package io.yagni.edge.disruptor.translator

import io.yagni.edge.vertx.event.StateChangeEvent
import scala.reflect.BeanProperty
import org.vertx.java.core.json.JsonObject

class YagniEventTranslator(private var stateChangeEvent: StateChangeEvent) extends com.lmax.disruptor.EventTranslator[StateChangeEvent] {

  @BeanProperty
  var sequence: Long = _

  override def translateTo(event: StateChangeEvent, sequence: Long) {
    io.yagni.edge.vertx.json.Node.clear(event)
    event.mergeIn(new JsonObject(stateChangeEvent.toString))
    this.sequence = sequence
  }
}
