package io.ubiqesh.central.mqtt.decoder

import io.netty.buffer.ByteBuf
import io.ubiqesh.central.mqtt.commands._
import scala.annotation.tailrec
import java.io.DataInputStream
import scala.Some
import io.ubiqesh.central.mqtt.commands.Topic

/**
 * Created by balu on 01.01.14.
 */
class UnsubscribeDecoder extends Decoder {
  override def decode(startPos:Int, messageType: Int, dupFlag:Boolean, qosLevel: Int, retainFlag:Boolean, length:Int, stream: ByteBuf):Option[Unsubscribe] = {
    val startPos = stream.readerIndex()
    // --- VARIABLE HEADER

    //  Message ID
    //  The variable header contains a Message ID because a
    //  SUBSCRIBE message has a QoS level of 1.
    // (MQTT V3.1 Protocol Specification - section 3.8)
    val messageId = stream.readUnsignedShort() // 16-bit unsigned integer

    // --- PAYLOAD
    // The payload of a UNSUBSCRIBE message contains a list of
    // topic names to which the client wants to subscribe. The topic
    // strings may contain special Topic wildcard characters to represent a set
    // of topics. These topic/QoS pairs are packed contiguously (...)
    // (MQTT V3.1 Protocol Specification - section 3.8)
    val topics = readTopicsUnsubscribe(startPos,length, stream)

    val unsubscribe = new Unsubscribe(CommandType(messageType),
      dupFlag,
      QosLevel(qosLevel),
      retainFlag,
      length,
      messageId,
      topics)

    Some(unsubscribe)
  }

  private def readTopicsUnsubscribe(startPos:Int,remainingLength:Int, stream:ByteBuf): List[Topic] = {
    @tailrec def readTopics0(topics:List[Topic]): List[Topic] = {
      if (canReadMore(startPos,remainingLength, stream)) {
        val pattern = DataInputStream.readUTF(new BufferAsDataInput(stream))
        readTopics0(Topic(pattern, null) :: topics)
      }
      else
        topics
    }
    readTopics0(Nil)
  }
}