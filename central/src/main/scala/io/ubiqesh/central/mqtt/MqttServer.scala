package io.ubiqesh.central.mqtt

import org.vertx.scala.core.Vertx
import org.vertx.java.core.{Handler => JHandler}
import org.vertx.java.core.buffer._
import org.vertx.scala.core.net.NetSocket
import io.netty.buffer.{Unpooled, ByteBuf}
import java.io.{ByteArrayInputStream, DataInputStream, DataInput}
import io.ubiqesh.central.mqtt.Disconnect
import io.ubiqesh.central.mqtt.Disconnect

/**
 * Created by Christoph Grotz on 30.12.13.
 */
class MqttServer(vertx: Vertx) {
  def registerClient = (client: NetSocket) => {
    client.closeHandler({
      // TODO close of client
    })
    new MqttSocket(client)
  }
}

class MqttSocket(client: NetSocket) {
  val stream = Unpooled.buffer()
  client.internal.dataHandler(new JHandler[Buffer]{
    def handle(event: Buffer)
    {
      stream.writeBytes(event.getBytes)
      val message = tryDecode()
      print(message)
      if( message.isInstanceOf[Connect])
      {
        val connack = writeConnack(ConnackCode.Accepted)
        client.internal.write(connack)
        print(connack)
      }
    }
  })

  def tryDecode(): AnyRef = {
    if(stream.readableBytes() < 2)
    {
      // TODO noComplete Header
    }
    else
    {
      val startPos = stream.readerIndex()
      val b1 = stream.readByte()
      val messageType = ((b1 & 0x00F0) >> 4)
      val dupFlag = (((b1 & 0x0008) >> 3) == 1)
      val qosLevel = ((b1 & 0x0006) >> 1)
      val retainFlag = ((b1 & 0x0001) == 1)
      val length = extractLength(stream)

      while( stream.readableBytes() < length)
      {
        // Waiting on rest of package
      }

      CommandType.apply(messageType) match {
        case CommandType.CONNECT => {
          return extractConnect(startPos,messageType, dupFlag, qosLevel, retainFlag, length, stream)
        }
        case CommandType.DISCONNECT => {
          return extractDisconnect(startPos,messageType, dupFlag, qosLevel, retainFlag, length, stream)
        }
      }
    }
    null
  }

  def extractConnect(startPos:Int, messageType: Int, dupFlag:Boolean, qosLevel: Int, retainFlag:Boolean, length:Int, stream: ByteBuf) = {

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

    val rawData = null //stream.readBytes(length-12).array()

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

    val connect = Connect(CommandType(messageType),
      dupFlag,
      QosLevel(qosLevel),
      retainFlag,
      length,
      rawData,
      protocolName,
      protocolVersion,
      cleanSession,
      keepAlive,
      clientId, username, password)

    connect
  }

  def extractDisconnect(startPos:Int, messageType: Int, dupFlag:Boolean, qosLevel: Int, retainFlag:Boolean, length:Int, stream: ByteBuf) = {
    val disconnect = Disconnect(CommandType(messageType),
      dupFlag,
      QosLevel(qosLevel),
      retainFlag,
      length)

    disconnect
  }

  def writeConnack(connackCode:ConnackCode.Value):Buffer = {
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

  def extractLength(buffer: ByteBuf):Int = {
    val length1 = stream.readByte()
    if(length1 > 127)
    {
      val length2 = stream.readByte()
      if(length2 > 127)
      {
        val length3 = stream.readByte()
        if(length3 > 127)
        {
          val length4 = stream.readByte()
          (length1 << 32)+(length2 << 16)+(length3 << 8)+length4
        }
        else
        {
          (length1 << 16)+(length2 << 8)+length3
        }
      }
      else
      {
        (length1 << 8)+length2
      }
    }
    else
    {
      length1
    }
  }

  /**
   * Indicates whether or not the buffer contains enough bytes according to the
   * 'remaining length' defined in the header.
   */
  private def canReadMore(startPos: Int, remainingLength:Int, stream: ByteBuf): Boolean = {
    val currentPos = stream.readerIndex()
    ((currentPos - startPos) < remainingLength)
  }
}

/**
 * Minimal type conversion, most methods will throw an exception.
 * thus it is kept private...
 */
private class BufferAsDataInput(stream: ByteBuf) extends DataInput {

  private def notImplemented[T](): T = {
    throw new UnsupportedOperationException
  }

  def readByte(): Byte = stream.readByte()

  def readUnsignedByte(): Int = stream.readUnsignedByte()

  def readShort(): Short = stream.readShort()

  def readUnsignedShort(): Int = stream.readUnsignedShort()

  def readInt(): Int = stream.readInt()

  def readLong() = notImplemented()

  def readChar() = notImplemented()

  def readFloat() = notImplemented()

  def readDouble() = notImplemented()

  def readUTF() = notImplemented()

  def readLine() = notImplemented()

  def readBoolean() = notImplemented()

  def skipBytes(n: Int) = notImplemented()

  def readFully(b: Array[Byte], off: Int, len: Int) {
    stream.readBytes(b, off, len)
  }

  def readFully(b: Array[Byte]) {
    stream.readBytes(b)
  }
}