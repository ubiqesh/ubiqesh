package io.ubiqesh.central.mqtt.commands

/**
 * MQTT V3.1 Protocol Specification - section 2.1
 */
object CommandType extends Enumeration {
  /**
   * Reserved
   */
  val R0 = Value(0)

  /**
   * Client request to connect to Server
   */
  val CONNECT = Value(1)

  /**
   * Connect Acknowledgment
   */
  val CONNACK = Value(2)

  /**
   * Publish message
   */
  val PUBLISH = Value(3)

  /**
   * Publish Acknowledgment
   */
  val PUBACK = Value(4)

  /**
   * Publish Received (assured delivery part 1)
   */
  val PUBREC = Value(5)

  /**
   * Publish Release (assured delivery part 2)
   */
  val PUBREL = Value(6)

  /**
   * Publish Complete (assured delivery part 3)
   */
  val PUBCOMP = Value(7)

  /**
   * Client Subscribe request
   */
  val SUBSCRIBE = Value(8)

  /**
   * Subscribe Acknowledgment
   */
  val SUBACK = Value(9)

  /**
   * Client Unsubscribe request
   */
  val UNSUBSCRIBE = Value(10)

  /**
   * Unsubscribe Acknowledgment
   */
  val UNSUBACK = Value(11)

  /**
   * PING Request
   */
  val PINGREQ = Value(12)

  /**
   * PING Response
   */
  val PINGRESP = Value(13)

  /**
   * Client is Disconnecting
   */
  val DISCONNECT = Value(14)

  /**
   * Reserved
   */
  val R15 = Value(15)
}

/**
 * MQTT V3.1 Protocol Specification - section 2.1
 */
object QosLevel extends Enumeration {
  /**
   * At most once, Fire and Forget, <=1.
   *
   * The message is delivered according to the best efforts of the underlying TCP/IP network.
   * A response is not expected and no retry semantics are defined in the protocol.
   * The message arrives at the server either once or not at all.
   *
   * (MQTT V3.1 Protocol Specification - section 4.1)
   */
  val AtMostOnce = Value(0)

  /**
   * At least once, Acknowledged delivery, >=1.
   *
   * The receipt of a message by the server is acknowledged by a PUBACK message.
   * If there is an identified failure of either the communications link or the
   * sending device, or the acknowledgement message is not received after a specified
   * period of time, the sender resends the message with the DUP bit set in the
   * message header. The message arrives at the server at least once.
   * Both SUBSCRIBE and UNSUBSCRIBE messages use QoS level 1.
   *
   * If the client does not receive a PUBACK message (either within a time period
   * defined in the application, or if a failure is detected and the communications
   * session is restarted), the client may resend the PUBLISH message with the DUP
   * flag set.
   * When it receives a duplicate message from the client, the server republishes
   * the message to the subscribers, and sends another PUBACK message.
   *
   * (MQTT V3.1 Protocol Specification - section 4.1)
   */
  val AtLeastOnce = Value(1)

  /**
   * Exactly once, Assured delivery, =1.
   *
   * Additional protocol flows above QoS level 1 ensure that duplicate messages
   * are not delivered to the receiving application. This is the highest level
   * of delivery, for use when duplicate messages are not acceptable. There is
   * an increase in network traffic, but it is usually acceptable because of
   * the importance of the message content.
   *
   * A message with QoS level 2 has a Message ID in the message header.
   *
   * If a failure is detected, or after a defined time period, the protocol
   * flow is retried from the last unacknowledged protocol message; either
   * the PUBLISH or PUBREL.
   *
   * (MQTT V3.1 Protocol Specification - section 4.1)
   */
  val ExactlyOnce = Value(2)

  /**
   * Reserved
   */
  val R3 = Value(3)
}

/**
 * Simple tuple for holding topic information
 */
case class Topic(pattern:String, QOS:QosLevel.Value)

class PathMatcher {
  def matchPath(subscribedTopic:String, topic:String):Boolean = {
    val subscribedTopicElements = subscribedTopic.split("/")
    val topicElements = topic.split("/")
    var i = 0
    for( currentElement <- subscribedTopicElements ) {
      currentElement match {
        case "+" => {
          return (topicElements.length == i+1)
        }
        case "#" => {
          return (topicElements.length >= i+1)
        }
        case _ => {
          if(topicElements.length > i+1) {
            if(topicElements(i) != currentElement) {
              return false
            }
          }
          else if(topicElements.length == i+1) {
            return (topicElements(i) == currentElement)
          }
          else {
            return false
          }
        }
      }
      i = i+1
    }
    return false
  }
}