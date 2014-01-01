package io.ubiqesh.central.mqtt.decoder

import io.netty.buffer.ByteBuf
import java.io.DataInput
import io.ubiqesh.central.mqtt.commands._

trait Decoder {
  def decode(startPos:Int, messageType: Int, dupFlag:Boolean, qosLevel: Int, retainFlag:Boolean, length:Int, stream: ByteBuf): Option[Command]

  /**
   * Indicates whether or not the buffer contains enough bytes according to the
   * 'remaining length' defined in the header.
   */
  def canReadMore(startPos: Int, remainingLength:Int, stream: ByteBuf): Boolean = {
    val currentPos = stream.readerIndex()
    ((currentPos - startPos) < remainingLength)
  }
}

/**
 * Created by balu on 31.12.13.
 */
class MqttDecoder {
  val connectDecoder = new ConnectDecoder
  val disconnectDecoder = new DisconnectDecoder
  val subscribeDecoder = new SubscribeDecoder
  val unsubscribeDecoder = new UnsubscribeDecoder
  val publishDecoder = new PublishDecoder
  val pingreqDecoder = new PingreqDecoder
  val pubrelDecoder = new PubrelDecoder

  def decode(stream: ByteBuf): Option[Command] = {
    if(stream.readableBytes() < 2) {
      // TODO noComplete Header
    }
    else {
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
          return connectDecoder.decode(startPos,messageType, dupFlag, qosLevel, retainFlag, length, stream)
        }
        case CommandType.DISCONNECT => {
          return disconnectDecoder.decode(startPos,messageType, dupFlag, qosLevel, retainFlag, length, stream)
        }
        case CommandType.PUBLISH => {
          return publishDecoder.decode(startPos,messageType, dupFlag, qosLevel, retainFlag, length, stream)
        }
        case CommandType.SUBSCRIBE => {
          return subscribeDecoder.decode(startPos,messageType, dupFlag, qosLevel, retainFlag, length, stream)
        }
        case CommandType.UNSUBSCRIBE => {
          return unsubscribeDecoder.decode(startPos,messageType, dupFlag, qosLevel, retainFlag, length, stream)
        }
        case CommandType.PINGREQ => {
          return pingreqDecoder.decode(startPos,messageType, dupFlag, qosLevel, retainFlag, length, stream)
        }
        case CommandType.PUBREL => {
          return pubrelDecoder.decode(startPos,messageType, dupFlag, qosLevel, retainFlag, length, stream)
        }
        case _ => {

        }
      }
    }
    None
  }

  def extractLength(stream: ByteBuf):Int = {
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
