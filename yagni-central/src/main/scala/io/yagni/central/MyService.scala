package io.yagni.central

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import scala.concurrent._
import ExecutionContext.Implicits.global


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {
  val deviceController = new DeviceController
  val myRoute = {
    path("api") {
      get {
        respondWithMediaType(`application/json`) {
          complete {
            Future[Device] {
              deviceController.get(null)
            }
          }
        }
      } ~
        path("1") {
          path("devices") {
            get {
              respondWithMediaType(`application/json`) {
                complete {
                  Future[Device] {
                    deviceController.get(null)
                  }
                }
              }
            }
          }
        } ~
        path("devices" / JavaUUID) {
          id => {
            get {
              respondWithMediaType(`application/json`) {
                complete {
                  Future[Device] {
                    deviceController.get(id.toString)
                  }
                }
              }
            }
          }
        }
      }
    }
}