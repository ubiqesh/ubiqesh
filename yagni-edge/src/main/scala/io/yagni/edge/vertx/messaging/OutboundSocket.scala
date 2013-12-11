package io.yagni.edge.vertx.messaging

//remove if not needed


trait OutboundSocket {

  def send(string: String): Unit
}
