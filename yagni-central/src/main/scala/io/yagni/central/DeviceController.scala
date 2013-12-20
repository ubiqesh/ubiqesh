package io.yagni.central

import spray.json.DefaultJsonProtocol

case class Device(id: String, name: String)

object JsonImplicits extends DefaultJsonProtocol {
  implicit val impDevice = jsonFormat2(Device)
}

class DeviceController {
  def get(id: String): Device = {
    new Device("id", "test")
  }
}