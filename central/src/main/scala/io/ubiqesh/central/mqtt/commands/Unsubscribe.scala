package io.ubiqesh.central.mqtt.commands

class Unsubscribe(override val messageType: CommandType.Value,
                  override val DUP: Boolean,
                  override val QoS: QosLevel.Value,
                  override val retain: Boolean,
                  override val remainingLength: Long,
                  val messageId: Int,
                  val topics: List[Topic])
  extends Command(messageType, DUP, QoS,retain, remainingLength)



