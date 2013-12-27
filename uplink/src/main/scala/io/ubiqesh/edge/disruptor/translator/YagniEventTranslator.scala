package io.ubiqesh.edge.disruptor.translator

import io.ubiqesh.edge.vertx.event.StateChangeEvent
import scala.reflect.BeanProperty
import org.vertx.java.core.json.JsonObject

class UbiqeshEventTranslator(private var stateChangeEvent: StateChangeEvent) extends com.lmax.disruptor.EventTranslator[StateChangeEvent] {

  @BeanProperty
  var sequence: Long = _

  override def translateTo(event: StateChangeEvent, sequence: Long) {
    io.ubiqesh.edge.vertx.json.Node.clear(event)
    event.mergeIn(new JsonObject(stateChangeEvent.toString))
    this.sequence = sequence
  }
}
