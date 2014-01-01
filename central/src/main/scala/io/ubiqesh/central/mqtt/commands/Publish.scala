package io.ubiqesh.central.mqtt.commands

/**
 * Created by balu on 01.01.14.
 */
class Publish(override val messageType: CommandType.Value,
                    override val DUP: Boolean,
                    override val QoS: QosLevel.Value,
                    override val retain: Boolean,
                    override val remainingLength: Long,
                    val messageId: Option[Int],
                    val topic: String,
                    val payload: Array[Byte]) extends Command(messageType, DUP, QoS,retain, remainingLength)
