package io.ubiqesh.central.mqtt.decoder

import io.netty.buffer.ByteBuf
import io.ubiqesh.central.mqtt.commands._

/**
 * Created by balu on 01.01.14.
 */
class DisconnectDecoder extends Decoder {
  override def decode(startPos:Int, messageType: Int, dupFlag:Boolean, qosLevel: Int, retainFlag:Boolean, length:Int, stream: ByteBuf):Option[Disconnect] = {
    val disconnect = new Disconnect(CommandType(messageType),
      dupFlag,
      QosLevel(qosLevel),
      retainFlag,
      length)

    Some(disconnect)
  }
}
