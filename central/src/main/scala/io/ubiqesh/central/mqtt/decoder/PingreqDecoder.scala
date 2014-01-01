package io.ubiqesh.central.mqtt.decoder

import io.ubiqesh.central.mqtt.commands._
import io.netty.buffer.ByteBuf

/**
 * Created by balu on 01.01.14.
 */
class PingreqDecoder extends Decoder{
  override def decode(startPos:Int, messageType: Int, dupFlag:Boolean, qosLevel: Int, retainFlag:Boolean, length:Int, stream: ByteBuf):Option[Pingreq] = {
    val pingreq = new Pingreq(CommandType(messageType),
      dupFlag,
      QosLevel(qosLevel),
      retainFlag,
      length)

    Some(pingreq)
  }
}
