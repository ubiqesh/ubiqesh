package io.ubiqesh.central.rest

import akka.actor.{UntypedActor, ActorLogging}
import org.vertx.scala.core.http.HttpServerRequest

class DevicesRestEndpoint(prefix: String) extends UntypedActor with ActorLogging {

  def onReceive(message: Any) = {
    if (message.isInstanceOf[HttpServerRequest]) {
      val req = message.asInstanceOf[HttpServerRequest]
      val serverName = req.absoluteURI().getScheme +"://"+req.absoluteURI().getHost+":"+req.absoluteURI().getPort+prefix

      val deviceId = req.params().get("deviceId")
      val sensorId = req.params().get("sensorId")
      if(deviceId != null && sensorId == null)
      {
        req.response()
          .putHeader("Content-Type","application/json")
          .end("""{
                  "id":""""+serverName+"""/devices/device1",
                  "description": "Weather Station Example",
                  "sensors": [
                  {
                    "name": "garden_temperature",
                    "description": "Temperature of test setup in garden.",
                    "uri": """"+serverName+"""/devices/device1/sensors/garden_temperature"
                  },
                  {
                    "name": "rainfall",
                    "description": "Aggregated data example.",
                    "uri": """"+serverName+"""/devices/device1/sensors/rainfall"
                  }
                  ]
                }""")
      }
      else if(deviceId != null && sensorId != null)
      {
        if(req.uri().endsWith("/current")) {
          if(sensorId == "rainfall")
          {
            req.response()
              .putHeader("Content-Type","application/json")
              .end("""{
                      "timestamp": "2013-12-19T21:59,578+05:30",
                      "aggregation": {
                          "method": """"+serverName+"""/aggregations/summarize",
                          "period": "PT-1H"
                      },
                      "value": 2.4
                  }""")
          }
          else
          {
            req.response().putHeader("Content-Type","application/json").end("""{
                "timestamp": "2013-12-19T21:59,578+05:30",
                "value": 270.8
            }""")
          }
        }
        else
        {
          if(sensorId == "rainfall")
          {
            req.response()
              .putHeader("Content-Type","application/json")
              .end("""{
                  "name": "rainfall",
                  "description": "Aggregated data example.",
                  "current": """"+serverName+"""/devices/device1/sensors/rainfall/current",
                  "type": {
                      "name": """"+serverName+"""/semantics/types/number_with_unit",
                      "unit": {
                          "system": "SI",
                          "name": "l"
                      }
                  },
                  "refresh": {
                      "timestamp": "2013-12-19T21:59,578+05:30",
                      "aggregation": {
                          "method": """"+serverName+"""/semantics/aggregations/summarize",
                          "period": "PT-1H"
                      },
                      "name": """"+serverName+"""/semantics/refresh_strategies/interval",
                      "interval": {
                          "period": "PT1H",
                          "likely_update": "2013-12-19T22:04,578+05:30"
                      }
                  }
              }""")
          }
          else {
            req.response()
              .putHeader("Content-Type","application/json")
              .end("""{
               "name": "garden_temperature2",
               "description": "Temperature of test setup in garden.",
               "current": """"+serverName+"""/devices/device1/sensors/garden_temperature/current",
               "type": {
                   "name": """"+serverName+"""/semantics/types/number_with_unit",
                   "unit": {
                       "system": "SI",
                       "name": "K"
                   }
               },
               "refresh": {
                   "timestamp": "2013-12-19T21:59,578+05:30",
                   "name": """"+serverName+"""/semantics/refresh_strategies/interval",
                   "interval": {
                       "period": "PT5S",
                       "likely_update": "2013-12-19T22:04,578+05:30"
                   }
               }
           }""")
          }
        }
      }
      else
      {
        req.response()
          .putHeader("Content-Type","application/json")
          .end("""{
                  "id":""""+serverName+"""/devices/device1",
                  "description": "Weather Station Example",
                  "sensors": [
                  {
                    "name": "garden_temperature",
                    "description": "Temperature of test setup in garden.",
                    "uri": """"+serverName+"""/devices/device1/sensors/garden_temperature"
                  },
                  {
                    "name": "rainfall",
                    "description": "Aggregated data example.",
                    "uri": """"+serverName+"""/devices/device1/sensors/rainfall"
                  }
                  ]
                }""")
      }
    }
  }
}