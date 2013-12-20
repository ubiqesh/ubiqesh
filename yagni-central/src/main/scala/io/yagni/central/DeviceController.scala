package io.yagni.central

case class Device(id: String, name: String)

class DeviceController {
  def find(): List[Device] = {
    List[Device](new Device("id", "test"),new Device("id", "test"),new Device("id", "test"))
  }

  def get(id: String): Device = {
    new Device("id", "test")
  }
}