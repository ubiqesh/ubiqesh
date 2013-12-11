package io.yagni.edge.disruptor

;

import java.util.Set
import io.yagni.edge.vertx.event.StateChangeEvent
import io.yagni.edge.vertx.messaging.EventDistributor
import io.yagni.edge.vertx.messaging.OutboundSocket
import org.slf4j.LoggerFactory
import com.lmax.disruptor.EventHandler
import io.yagni.edge.common.Path
import io.yagni.edge.vertx.json.Node
import Distributor._
import scala.reflect.BeanProperty
import scala.collection.mutable

import scala.collection.JavaConversions._
import org.vertx.java.core.json.JsonObject

object Distributor {

  private val logger = LoggerFactory.getLogger(classOf[Distributor])
}

class Distributor extends EventHandler[StateChangeEvent] {

  private var handlers: Set[EventDistributor] = new mutable.HashSet[EventDistributor]()

  @BeanProperty
  var sequence: Long = _

  override def onEvent(event: StateChangeEvent, sequence: Long, endOfBatch: Boolean) {
    this.sequence = sequence
    distribute(event)
  }

  def distribute(event: StateChangeEvent) {
    logger.trace("distributing event: " + event)
    for (handler <- handlers) {
      handler.distribute(event)
    }
  }

  def distribute(path: String, data: JsonObject) {
    for (handler <- handlers) {
      handler.distributeEvent(new Path(StateChangeEvent.extractPath(path)), data)
    }
  }

  def addHandler(handler: EventDistributor) {
    handlers.add(handler)
  }

  def removeHandler(handler: OutboundSocket) {
    handlers.remove(handler)
  }
}
