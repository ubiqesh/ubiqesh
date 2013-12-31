package io.ubiqesh.central.mqtt

/**
 *
 * @param messageType Message Type
 * @param DUP This flag is set when the client or server attempts to re-deliver a
 *            PUBLISH, PUBREL, SUBSCRIBE or UNSUBSCRIBE message. This applies to
 *            messages where the value of QoS is greater than zero (0), and an
 *            acknowledgment is required. When the DUP bit is set, the variable
 *            header includes a Message ID.
 *            The recipient should treat this flag as a hint as to whether the
 *            message may have been previously received. It should not be relied
 *            on to detect duplicates.
 * @param QoS    This flag indicates the level of assurance for delivery of a PUBLISH
 *               message.
 * @param retain This flag is only used on PUBLISH messages. When a client sends a
 *               PUBLISH to a server, if the Retain flag is set (1), the server should
 *               hold on to the message after it has been delivered to the current subscribers.<br/>
 *               When a new subscription is established on a topic, the last retained message
 *               on that topic should be sent to the subscriber with the Retain flag set. If
 *               there is no retained message, nothing is sent.<br/>
 *               This is useful where publishers send messages on a "report by exception"
 *               basis, where it might be some time between messages. This allows new
 *               subscribers to instantly receive data with the retained, or Last Known Good,
 *               value. <br/>
 *               When a server sends a PUBLISH to a client as a result of a subscription that
 *               already existed when the original PUBLISH arrived, the Retain flag should not
 *               be set, regardless of the Retain flag of the original PUBLISH. This allows a
 *               client to distinguish messages that are being received because they were
 *               retained and those that are being received "live".<br/>
 *               Retained messages should be kept over restarts of the server.<br/>
 *               A server may delete a retained message if it receives a message with a
 *               zero-length payload and the Retain flag set on the same topic.<br/>
 * @param remainingLength Represents the number of bytes remaining within the current message,
 *               including data in the variable header and the payload
 * @see QosLevel
 */
trait MqttPacket {

}

/**
 *
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
 *
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
 * MQTT V3.1 Protocol Specification - section 3.2
 */
object ConnackCode extends Enumeration {
  /**
   * Connection Accepted
   */
  val Accepted = Value(0)

  /**
   * Connection Refused: unacceptable protocol version
   */
  val UnacceptableProtocolVersion = Value(1)

  /**
   * Connection Refused: identifier rejected
   */
  val IdentifierRejected = Value(2)

  /**
   * Connection Refused: server unavailable
   */
  val ServerUnavailable = Value(3)

  /**
   * Connection Refused: bad user name or password
   */
  val BadUserOrPassword = Value(4)

  /**
   * Connection Refused: not authorized
   */
  val NotAuthorized = Value(5)

  // 6 - 255
  // Reserved for future use
}