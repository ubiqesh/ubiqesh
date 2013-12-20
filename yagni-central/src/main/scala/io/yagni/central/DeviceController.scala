package io.yagni.central

import scala.collection.Iterable
import java.util.UUID

case class Device(name: String, serial: String)
case class ResponseDevice(id:String, name: String, serial: String)

class DeviceController {
  val devices = scala.collection.mutable.Map[UUID, ResponseDevice]()
  //Map[Device](new Device("id", "test"),new Device("id", "test"),new Device("id", "test"))
  def find(): Iterable[ResponseDevice] = {
    devices.values
  }

  def get(id: UUID): ResponseDevice = {
    devices.get(id).get
  }

  def add(device: Device): String = {
    val id = UUID.randomUUID()
    devices.put(id,new ResponseDevice(id.toString,device.name, device.serial))
    id.toString
  }
}