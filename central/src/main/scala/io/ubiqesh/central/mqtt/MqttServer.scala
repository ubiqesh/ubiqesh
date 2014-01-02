package io.ubiqesh.central.mqtt

import org.vertx.scala.core.Vertx
import org.vertx.java.core.{Handler => JHandler}
import org.vertx.java.core.buffer._
import org.vertx.scala.core.net.NetSocket
import io.netty.buffer.{Unpooled, ByteBuf}
import java.io.{ByteArrayInputStream, DataInputStream, DataInput}
import spray.json.JsObject
import scala.util.parsing.json.JSONObject
import io.ubiqesh.central.mqtt.decoder.{MqttDecoder, Decoder}
import io.ubiqesh.central.mqtt.encoder.Encoder
import io.ubiqesh.central.mqtt.commands._
import scala.collection.mutable

/**
 * Created by Christoph Grotz on 30.12.13.
 */
class MqttServer(vertx: Vertx) {
  val sockets = new mutable.HashSet[MqttSocket]()

  def registerClient = (client: NetSocket) => {
    val socket = new MqttSocket(client, this)
    sockets.add(socket)
    client.closeHandler({
      sockets.remove(socket)
    })
  }

  def publishMessage = (messageId: Option[Int], topic:String, payload: Array[Byte]) => {
    sockets.foreach( socket => socket.publish(messageId, topic, payload))
  }
}

