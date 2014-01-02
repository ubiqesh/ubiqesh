package io.ubiqesh.uplink.disruptor

import java.util.Set
import io.ubiqesh.uplink.vertx.event.StateChangeEvent
import io.ubiqesh.uplink.vertx.messaging.EventDistributor
import org.slf4j.LoggerFactory
import com.lmax.disruptor.EventHandler
import io.ubiqesh.uplink.common.Path
import Distributor._
import scala.reflect.BeanProperty
import scala.collection.mutable

import scala.collection.JavaConversions._
import org.vertx.java.core.json.JsonObject

object Distributor {
  val logger = LoggerFactory.getLogger(classOf[Distributor])
}

class Distributor extends EventHandler[StateChangeEvent] {
  val handlers: Set[EventDistributor] = new mutable.HashSet[EventDistributor]()

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

  def removeHandler(handler: EventDistributor) {
    handlers.remove(handler)
  }
}