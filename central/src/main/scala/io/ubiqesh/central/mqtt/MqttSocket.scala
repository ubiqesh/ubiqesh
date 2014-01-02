package io.ubiqesh.central.mqtt

import org.vertx.java.core.{Handler => JHandler}
import org.vertx.java.core.buffer._
import org.vertx.scala.core.net.NetSocket
import io.netty.buffer.Unpooled
import io.ubiqesh.central.mqtt.decoder.MqttDecoder
import io.ubiqesh.central.mqtt.encoder.Encoder
import io.ubiqesh.central.mqtt.commands._
import scala.collection.mutable
import io.ubiqesh.central.authentication.AllowAllAuthenticationService

/**
 * Created by balu on 01.01.14.
 */
class MqttSocket(val client: NetSocket, val server: MqttServer) {
  val encoder = new Encoder()
  val decoder = new MqttDecoder()
  val stream = Unpooled.buffer()

  val topics = new mutable.HashSet[Topic]
  val pathMatcher = new PathMatcher
  var clientId:Option[String] = None

  client.internal.dataHandler(new JHandler[Buffer]{
    def handle(event: Buffer)
    {
      stream.writeBytes(event.getBytes)
      val command = decoder.decode(stream)
      command match {
        case None => {
          // TODO ?
        }
        case Some(_) => {
          command.get.messageType match {
            case CommandType.CONNECT => {
              val connect = command.get.asInstanceOf[Connect]
              clientId = Some(connect.clientId)
              client.internal.write(encoder.encodeConnack(ConnackCode.Accepted))
            }
            case CommandType.SUBSCRIBE => {
              val subscribe = command.get.asInstanceOf[Subscribe]

              val grantedQos = subscribe.topics.map({
                topic:Topic => topic.QOS
              }):List[QosLevel.Value]

              client.internal.write(encoder.encodeSuback(subscribe.messageId, grantedQos))

              subscribe.topics.foreach( topic => topics.add(topic))
            }
            case CommandType.UNSUBSCRIBE => {
              val unsubscribe = command.get.asInstanceOf[Unsubscribe]
              unsubscribe.topics.foreach( topic => topics.remove(topic))
              client.internal.write(encoder.encodeUnsuback(unsubscribe.messageId))
            }
            case CommandType.PINGREQ => {
              client.internal.write(encoder.encodePingresp())
            }
            case CommandType.PUBLISH => {
              val publish = command.get.asInstanceOf[Publish]
              publish.QoS match {
                case QosLevel.AtMostOnce => {
                  server.publishMessage(publish.messageId, publish.topic, publish.payload)
                }
                case QosLevel.AtLeastOnce => {
                  // This strategy basically requires persistence of the messages => How do we want to enable the persistence
                  // TODO Store message
                  // TODO Publish message to subscribers
                  // TODO Delete message
                  // TODO Send PUBACK
                }
                case QosLevel.ExactlyOnce => {
                  // This strategy basically requires persistence of the messages => How do we want to enable the persistence
                  /* TODO Variant 1:
                   * Store message
                   * Send PUBREC
                   * After receive PUBREL
                   * Publish message to subscribers
                   * Delete message
                   * Send PUBCOMP
                   */

                  /* TODO Variant 2:
                   * Store messageId
                   * Publish message to subscribers
                   * Send PUBREC
                   * After receive PUBREL
                   * Delete message ID
                   * Send PUBCOMP
                   */
                }
                case QosLevel.R3 => {
                  throw new NotImplementedError("QoS Level R3 not implemented yet")
                }
              }
            }
            case CommandType.DISCONNECT => {
              client.close()
            }
            case _ => {

            }
          }
        }
      }
    }
  })

  def publish = (messageId: Option[Int], topic:String, payload: Array[Byte]) => {
    if(matchesSubscribedTopics(topic))
    {
      client.internal.write(encoder.encodePublish(messageId, topic, payload))
    }
  }

  def matchesSubscribedTopics(topic:String): Boolean = {
    topics.foreach( subscribedTopic => {
      if(pathMatcher.matchPath(subscribedTopic.pattern, topic))
      {
        return true
      }
    })
    return false
  }
}
