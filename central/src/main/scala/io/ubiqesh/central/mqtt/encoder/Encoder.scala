package io.ubiqesh.central.mqtt.encoder

import org.vertx.java.core.buffer.Buffer
import scala.annotation.tailrec
import io.ubiqesh.central.mqtt.commands._

/**
 * Created by balu on 31.12.13.
 */
class Encoder {

  def encodeConnack(connackCode:ConnackCode.Value):Buffer = {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  0     0     1     0  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the CONNACK message.
    // MQTT V3.1 Protocol Specification - sections 3.2

    val buffer = new Buffer(4)

    // byte 1: 0b_0010_0000 = 0x20
    buffer.appendByte(0x20.asInstanceOf[Byte])
    // byte 2: remaining length = 2 => 0x02
    buffer.appendByte(0x02.asInstanceOf[Byte])
    // 1st byte; unused => 0x00
    buffer.appendByte(0x00.asInstanceOf[Byte])
    // 2nd byte: connack return code
    buffer.appendByte(connackCode.id.asInstanceOf[Byte])

    buffer
  }

  def encodePuback(messageId: Int):Buffer = {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  0     1     0     0  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the PUBACK message.
    // MQTT V3.1 Protocol Specification - sections 3.4

    val buffer = new Buffer(4)

    // byte 1: 0b_0100_0000 = 0x40
    buffer.appendByte(0x40.asInstanceOf[Byte])
    // byte 2: remaining length = 2 => 0x02
    buffer.appendByte(0x02.asInstanceOf[Byte])

    buffer.appendShort((messageId & 0xFFFF).asInstanceOf[Short])

    buffer
  }

  def encodePubrec(messageId: Int):Buffer = {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  0     1     0     1  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the PUBREC message.
    // MQTT V3.1 Protocol Specification - sections 3.5

    val buffer = new Buffer(4)

    // byte 1: 0b_0101_0000 = 0x50
    buffer.appendByte(0x50.asInstanceOf[Byte])
    // byte 2: remaining length = 2 => 0x02
    buffer.appendByte(0x02.asInstanceOf[Byte])

    buffer.appendShort((messageId & 0xFFFF).asInstanceOf[Short])

    buffer
  }

  def encodeSuback(messageId:Int, grantedQos:List[QosLevel.Value]):Buffer = {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  1     0     0     1  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the SUBACK message.
    // MQTT V3.1 Protocol Specification - sections 3.9

    // write payload first to calculate the 'remaining length'
    val content = new Buffer(grantedQos.size + 2)

    content.appendShort((messageId & 0xFFFF).asInstanceOf[Short])

    grantedQos.foreach({ qos => content.appendByte( (qos.id & 0xF).asInstanceOf[Byte] )})

    val len = content.length()

    val header = new Buffer(2)
    // byte 1: 0b_1001_0000 = 0x90
    header.appendByte(0x90.asInstanceOf[Byte])

    encodeRemainingLength(len, header)

    val buffer = new Buffer()
    buffer.appendBuffer(header)
    buffer.appendBuffer(content)
    buffer
  }

  def encodePingresp():Buffer = {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  1     0     0     1  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the SUBACK message.
    // MQTT V3.1 Protocol Specification - sections 3.9

    val header = new Buffer(2)
    // byte 1: 0b_1101_0000 = 0x90
    header.appendByte(0xD0.asInstanceOf[Byte])
    // byte 2: remaining length = 2 => 0x02
    header.appendByte(0x00.asInstanceOf[Byte])

    val buffer = new Buffer()
    buffer.appendBuffer(header)
    buffer
  }

  def writePuback(messageId: Int) {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  0     1     0     0  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the PUBACK message.
    // MQTT V3.1 Protocol Specification - sections 3.4

    val buffer = new Buffer(4)

    // byte 1: 0b_0100_0000 = 0x40
    buffer.appendByte(0x40.asInstanceOf[Byte])
    // byte 2: remaining length = 2 => 0x02
    buffer.appendByte(0x02.asInstanceOf[Byte])

    // variable header:
    // Contains the Message Identifier (Message ID) for the PUBLISH
    // message that is being acknowledged.
    buffer.appendShort((messageId & 0xFFFF).asInstanceOf[Short]) // 16-bit unsigned integer

    buffer
  }

  def encodeUnsuback(messageId: Int):Buffer = {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  0     1     0     1  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the PUBREC message.
    // MQTT V3.1 Protocol Specification - sections 3.5

    val buffer = new Buffer(4)

    // byte 1: 0b_1011_0000 = 0x50
    buffer.appendByte(0xB0.asInstanceOf[Byte])
    // byte 2: remaining length = 2 => 0x02
    buffer.appendByte(0x02.asInstanceOf[Byte])

    // variable header:
    // Contains the Message Identifier (Message ID) for the PUBLISH
    // message that is being acknowledged.
    buffer.appendShort((messageId & 0xFFFF).asInstanceOf[Short]) // 16-bit unsigned integer

    buffer
  }

  def encodePublish(messageId: Option[Int], topic:String, payload: Array[Byte]):Buffer = {
    val content = new Buffer()

    content.appendShort((messageId.getOrElse(0) & 0xFFFF).asInstanceOf[Short])
    content.appendString(topic)
    content.appendBytes(payload)

    val len = content.length()

    val header = new Buffer(2)
    // byte 1: 0b_0011_0000 = 0x90
    header.appendByte(0x30.asInstanceOf[Byte])

    encodeRemainingLength(len, header)

    val buffer = new Buffer()
    buffer.appendBuffer(header)
    buffer.appendBuffer(content)
    buffer
  }

  def encodeRemainingLength(remainingLength:Long, buffer:Buffer) {
    @tailrec def encodeRemainingLength0(x:Long) {
      val digit = (x % 128).asInstanceOf[Int]
      val newX  = (x / 128)
      if (newX > 0) {
        buffer.appendByte( (digit | 0x80 ).asInstanceOf[Byte] )
        encodeRemainingLength0(newX)
      }
      else {
        buffer.appendByte( digit.asInstanceOf[Byte] )
      }
    }
    encodeRemainingLength0(remainingLength)
  }
}
