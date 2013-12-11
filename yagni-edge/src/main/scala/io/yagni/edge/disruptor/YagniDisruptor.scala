package io.yagni.edge.disruptor

import java.util.concurrent.Executors
import io.yagni.edge.disruptor.translator.YagniEventTranslator
import io.yagni.edge.vertx.event.StateChangeEvent
import io.yagni.edge.vertx.messaging.Endpoint
import org.slf4j.LoggerFactory
import com.lmax.disruptor.EventFactory
import com.lmax.disruptor.ExceptionHandler
import io.yagni.edge.persistence.Persistence
import YagniDisruptor._
import scala.reflect.BeanProperty
import com.lmax.disruptor.dsl.Disruptor

object YagniDisruptor {

  val EVENT_FACTORY = new EventFactory[StateChangeEvent]() {

    override def newInstance(): StateChangeEvent = new StateChangeEvent()
  }

  private val logger = LoggerFactory.getLogger(classOf[YagniDisruptor])

  private val RING_SIZE = 256
}

class YagniDisruptor(var persistence: Persistence) extends ExceptionHandler {

  var persistenceProcessor: PersistenceProcessor = new PersistenceProcessor(persistence)

  var distributionProcessor: Distributor = new Distributor()

  var currentSequence: Long = _

  val executor = Executors.newCachedThreadPool()

  @BeanProperty
  var disruptor = new Disruptor[StateChangeEvent](YagniDisruptor.EVENT_FACTORY, RING_SIZE, executor);
  {
    disruptor.handleExceptionsWith(this)
    disruptor.handleEventsWith(persistenceProcessor).then(distributionProcessor)
    disruptor.start()
  }

  def handleEvent(stateChangeEvent: StateChangeEvent) {
    val eventTranslator = new YagniEventTranslator(stateChangeEvent)
    logger.trace("handling event: " + stateChangeEvent)
    disruptor.publishEvent(eventTranslator)
    this.currentSequence = eventTranslator.getSequence
  }

  def shutdown() {
    disruptor.shutdown()
  }

  def addEndpoint(handler: Endpoint) {
    distributionProcessor.addHandler(handler)
  }

  def removeEndpoint(handler: Endpoint) {
    distributionProcessor.removeHandler(handler)
  }

  def getDistributor(): Distributor = distributionProcessor

  def hasBacklog(): Boolean = {
    currentSequence != distributionProcessor.getSequence
  }

  override def handleEventException(ex: Throwable, sequence: Long, event: AnyRef) {
    logger.error("Event Exception (msg: " + ex.getMessage + ", sequence: +" +
      sequence +
      ", event: " +
      event +
      ")", ex)
  }

  override def handleOnStartException(ex: Throwable) {
    logger.error("OnStart Exception (msg: " + ex.getMessage + ")", ex)
  }

  override def handleOnShutdownException(ex: Throwable) {
    logger.error("OnShutdown Exception (msg: " + ex.getMessage + ")", ex)
  }
}
