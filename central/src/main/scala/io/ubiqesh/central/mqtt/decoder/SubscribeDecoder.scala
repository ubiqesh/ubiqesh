package io.ubiqesh.central.mqtt.decoder

import io.netty.buffer.ByteBuf
import io.ubiqesh.central.mqtt.commands._
import scala.Some
import scala.annotation.tailrec
import java.io.DataInputStream

/**
 * Created by balu on 01.01.14.
 */
class SubscribeDecoder extends Decoder {
  override def decode(startPos:Int, messageType: Int, dupFlag:Boolean, qosLevel: Int, retainFlag:Boolean, length:Int, stream: ByteBuf):Option[Subscribe] = {
    val startPos = stream.readerIndex()
      // --- VARIABLE HEADER

      //  Message ID
      //  The variable header contains a Message ID because a
      //  SUBSCRIBE message has a QoS level of 1.
      // (MQTT V3.1 Protocol Specification - section 3.8)
      val messageId = stream.readUnsignedShort() // 16-bit unsigned integer

      // --- PAYLOAD
      // The payload of a SUBSCRIBE message contains a list of
      // topic names to which the client wants to subscribe, and
      // the QoS level at which the client wants to receive the
      // messages. The strings are UTF-encoded, and the QoS level
      // occupies 2 bits of a single byte. The topic strings may
      // contain special Topic wildcard characters to represent a set
      // of topics. These topic/QoS pairs are packed contiguously (...)
      // (MQTT V3.1 Protocol Specification - section 3.8)
      val topics = readTopics(startPos,length, stream)

      val subscribe = new Subscribe(CommandType(messageType),
        dupFlag,
        QosLevel(qosLevel),
        retainFlag,
        length,
        messageId,
        topics)

      Some(subscribe)
  }

  private def readTopics(startPos:Int,remainingLength:Int, stream:ByteBuf): List[Topic] = {
    @tailrec def readTopics0(topics:List[Topic]): List[Topic] = {
      if (canReadMore(startPos,remainingLength, stream)) {
        val pattern = DataInputStream.readUTF(new BufferAsDataInput(stream))
        val qos     = stream.readByte()
        readTopics0(Topic(pattern, QosLevel(qos)) :: topics)
      }
      else
        topics
    }
    readTopics0(Nil)
  }
}
