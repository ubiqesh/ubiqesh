package io.ubiqesh.central.mqtt.decoder

import io.netty.buffer.ByteBuf
import io.ubiqesh.central.mqtt.commands.{CommandType, QosLevel, Connect}
import java.io.{ByteArrayInputStream, DataInputStream}

/**
 * Created by balu on 01.01.14.
 */
class ConnectDecoder extends Decoder {
  override def decode(startPos:Int, messageType: Int, dupFlag:Boolean, qosLevel: Int, retainFlag:Boolean, length:Int, stream: ByteBuf):Option[Connect] = {

    val protocolName = DataInputStream.readUTF(new DataInputStream(new ByteArrayInputStream(stream.readBytes(8).array())))
    val protocolVersion = stream.readUnsignedByte()

    // Connect flags
    // ------+------+-------+-------+-------+-------+-------+-------+---------+
    // bit   |   7  |   6   |   5   |   4   |   3   |   2   |   1   |    O    |
    // ------+------+-------+-------+-------+-------+-------+-------+---------+
    //       | User | Pass- | Will  |     Will      | Will  | Clean | Reserved|
    //       | Name |  word | Retain|     QoS       | Flag  |Session|         |
    // ------+------+-------+-------+---------------+-------+-------+---------+
    // 0b0000_0010: 0x02
    // 0b0000_0100: 0x04
    // 0b0001_1000: 0x18
    // 0b0010_0000: 0x20
    // 0b0100_0000: 0x40
    // 0b1000_0000: 0x80
    val b = stream.readByte()
    val hasUsername = (((b & 0x80) >> 7) == 1)
    val hasPassword = (((b & 0x40) >> 6) == 1)
    val willRetain = (((b & 0x20) >> 5) == 1)
    val willQoS = (((b & 0x18) >> 3))
    val willFlag = (((b & 0x04) >> 2) == 1)
    val cleanSession = (((b & 0x02) >> 1) == 1)

    // The Keep Alive timer is a 16-bit value that represents the number of seconds for the time period.
    val keepAlive = (stream.readByte() << 8)+stream.readByte()

    // --- PAYLOAD
    val clientId = DataInputStream.readUTF(new BufferAsDataInput(stream))

    /*val willMessage =
      if (willFlag) {
        val willTopic = DataInputStream.readUTF(new BufferAsDataInput(stream))
        val willMessageBody = DataInputStream.readUTF(new BufferAsDataInput(stream))
        Some(Message(willTopic, willMessageBody, QosLevel(willQoS), willRetain))
      }
      else
        None
    */

    /*
    Note that, for compatibility with the original MQTT V3 specification,
    the Remaining Length field from the fixed header takes precedence over
    the User Name flag. Server implementations must allow for the possibility
    that the User Name flag is set, but the User Name string is missing.
    This is valid, and connections should be allowed to continue.

    Same for password...

    (MQTT V3.1 Protocol Specification - section 3.1)

    => one need to check if there is enough bytes remaining to read them both
     */
    val username =
      if (hasUsername && canReadMore(startPos, length, stream))
        Some(DataInputStream.readUTF(new BufferAsDataInput(stream)))
      else
        None

    val password =
      if (hasPassword && canReadMore(startPos, length, stream))
        Some(DataInputStream.readUTF(new BufferAsDataInput(stream)))
      else
        None

    val connect = new Connect(CommandType(messageType),
      dupFlag,
      QosLevel(qosLevel),
      retainFlag,
      length,
      protocolName,
      protocolVersion,
      cleanSession,
      keepAlive,
      clientId, username, password)

    Some(connect)
  }
}
