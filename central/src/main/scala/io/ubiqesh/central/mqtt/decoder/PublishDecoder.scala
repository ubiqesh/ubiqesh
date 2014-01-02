package io.ubiqesh.central.mqtt.decoder

import io.netty.buffer.ByteBuf
import io.ubiqesh.central.mqtt.commands.{CommandType, QosLevel, Publish, Unsubscribe}
import java.io.DataInputStream

/**
 * Created by balu on 01.01.14.
 */
class PublishDecoder extends Decoder {
  override def decode(startPos:Int, messageType: Int, dupFlag:Boolean, qosLevel: Int, retainFlag:Boolean, length:Int, stream: ByteBuf):Option[Publish] = {

    val start = stream.readerIndex()
    val topic = DataInputStream.readUTF(new BufferAsDataInput(stream))

    val messageId: Option[Int] =
      if (qosLevel == 1 || qosLevel == 2)
        Some(stream.readUnsignedShort()) // 16-bit unsigned integer
      else
        None

    val payloadPos = stream.readerIndex()
    val payloadLen = length - (payloadPos - start)
    val payload = stream.readBytes(payloadLen)

    val publish = new Publish(CommandType(messageType),
      dupFlag,
      QosLevel(qosLevel),
      retainFlag,
      length,
      messageId,
      topic,
      payload.array())

    Some(publish)
  }
}