package io.yagni.edge.persistence

import io.yagni.edge.common.Path
import io.yagni.edge.vertx.event.changelog.ChangeLog
import io.yagni.edge.vertx.json.Node
import io.yagni.edge.vertx.messaging.Endpoint
import io.yagni.edge.persistence.queries.QueryEvaluator
import org.vertx.java.core.json.JsonObject

//remove if not needed


trait Persistence {

  def get(path: Path): AnyRef

  def getNode(path: Path): JsonObject

  def remove(log: ChangeLog, path: Path): Unit

  def applyNewValue(log: ChangeLog,
                    path: Path,
                    payload: AnyRef): Unit

  def updateValue(log: ChangeLog,
                  path: Path,
                  payload: AnyRef): Unit

  def syncPath(path: Path, handler: Endpoint): Unit

  def syncPropertyValue(path: Path, yagniEventHandler: Endpoint): Unit

  def syncPathWithQuery(path: Path,
                        handler: Endpoint,
                        queryEvaluator: QueryEvaluator,
                        query: String): Unit
}
