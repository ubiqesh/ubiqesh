package io.ubiqesh.uplink.vertx.event

//remove if not needed


object StateChangeEventType extends Enumeration {

  val INIT = new StateChangeEventType()

  val PUSH = new StateChangeEventType()

  val SET = new StateChangeEventType()

  val REMOVE = new StateChangeEventType()

  val UPDATE = new StateChangeEventType()

  val EVENT = new StateChangeEventType()

  class StateChangeEventType extends Val

  implicit def convertValue(v: Value): StateChangeEventType = v.asInstanceOf[StateChangeEventType]
}
