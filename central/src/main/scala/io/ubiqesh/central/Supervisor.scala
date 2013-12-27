package io.ubiqesh.central

import akka.actor.{UntypedActor, ActorLogging}
import org.vertx.scala.core.http.HttpServerRequest

class Supervisor extends UntypedActor with ActorLogging {

  def onReceive(message: Any) = {
    if (message.isInstanceOf[HttpServerRequest]) {
      val req = message.asInstanceOf[HttpServerRequest]
      val serverName = req.absoluteURI().getScheme +"://"+req.absoluteURI().getHost+":"+req.absoluteURI().getPort
      if(req.uri().endsWith("/v1/garden_temperature"))
      {
        req.response()
          .putHeader("Content-Type","application/json")
          .end("""{
             "name": "garden_temperature2",
             "description": "Temperature of test setup in garden.",
             "current": """"+serverName+"""/v1/garden_temperature/current",
             "type": {
                 "name": """"+serverName+"""/v1/types/number_with_unit",
                 "unit": {
                     "system": "SI",
                     "name": "K"
                 }
             },
             "refresh": {
                 "timestamp": "2013-12-19T21:59,578+05:30",
                 "name": """"+serverName+"""/v1/refresh_strategies/interval",
                 "interval": {
                     "period": "PT5S",
                     "likely_update": "2013-12-19T22:04,578+05:30"
                 }
             }
         }""")
      }
      else if(req.uri().endsWith("/v1/garden_temperature/current")) {
        req.response().putHeader("Content-Type","application/json").end("""{
              "timestamp": "2013-12-19T21:59,578+05:30",
              "value": 270.8
          }""")
      }
      else if(req.uri().endsWith("/v1/garden_temperature/subscribe")) {
        req.response().end()
      }
      else if(req.uri().endsWith("/v1/rainfall")) {
        req.response()
          .putHeader("Content-Type","application/json")
          .end("""{
                "name": "rainfall",
                "description": "Aggregated data example.",
                "current": """"+serverName+"""/v1/rainfall/current",
                "type": {
                    "name": """"+serverName+"""/v1/types/number_with_unit",
                    "unit": {
                        "system": "SI",
                        "name": "l"
                    }
                },
                "refresh": {
                    "timestamp": "2013-12-19T21:59,578+05:30",
                    "aggregation": {
                        "method": """"+serverName+"""/v1/aggregations/summarize",
                        "period": "PT-1H"
                    },
                    "name": """"+serverName+"""/v1/refresh_strategies/interval",
                    "interval": {
                        "period": "PT1H",
                        "likely_update": "2013-12-19T22:04,578+05:30"
                    }
                }
            }""")
      }
      else if(req.uri().endsWith("/v1/rainfall/current")) {
        req.response()
          .putHeader("Content-Type","application/json")
          .end("""{
                      "timestamp": "2013-12-19T21:59,578+05:30",
                      "aggregation": {
                          "method": """"+serverName+"""/v1/aggregations/summarize",
                          "period": "PT-1H"
                      },
                      "value": 2.4
                  }""")
      }
      else
      {
        req.response()
          .putHeader("Content-Type","application/json")
          .end("""{
                  "id":""
                  "description": "Weather Station Example",
                  "sensors": [
                  {
                    "name": "garden_temperature",
                    "description": "Temperature of test setup in garden.",
                    "uri": """"+serverName+"""/v1/garden_temperature"
                  },
                  {
                    "name": "rainfall",
                    "description": "Aggregated data example.",
                    "uri": """"+serverName+"""/v1/rainfall"
                  }
                  ]
                }""");
      }
    }
  }
}