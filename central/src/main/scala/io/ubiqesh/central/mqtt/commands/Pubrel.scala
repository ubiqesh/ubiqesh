package io.ubiqesh.central.mqtt.commands

/**
 * Created by balu on 01.01.14.
 */
class Pubrel (override val messageType: CommandType.Value,
              override val DUP: Boolean,
              override val QoS: QosLevel.Value,
              override val retain: Boolean,
              override val remainingLength: Long) extends Command (messageType, DUP, QoS,retain, remainingLength)
