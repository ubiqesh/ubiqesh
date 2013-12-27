package io.ubiqesh.edge.persistence

import io.ubiqesh.edge.common.Path
import io.ubiqesh.edge.vertx.event.changelog.ChangeLog
import io.ubiqesh.edge.vertx.messaging.Endpoint
import io.ubiqesh.edge.persistence.queries.QueryEvaluator
import org.vertx.java.core.json.JsonObject

/**
 *
 * Interface for the state persistence.
 *
 */
trait Persistence {
  /**
   * returns the value at the given Path.
   * @param path
   * @return
   */
  def get(path: Path): AnyRef

  /**
   * gets the JsonObject at the given Path. If the value at the path is not a JsonObject, the parent JsonObject will be returned.
   * @param path
   * @return
   */
  def getJsonObject(path: Path): JsonObject

  /**
   * removes the value at the given Location.
   * As a side effect the given ChangeLog is filled with the changes happening to the data model.
   * @param log
   * @param path
   */
  def remove(log: ChangeLog, path: Path)

  /**
   * Override the data at the path with the given data.
   *
   * As a side effect the given ChangeLog is filled with the changes happening to the data model.
   * @param log
   * @param path
   * @param data
   */
  def applyNewValue(log: ChangeLog,
                    path: Path,
                    data: AnyRef)

  /**
   * Updates the data at the path with the given data. If the handed Data is a JsonObject, the JsonObjects are merged.
   *
   * As a side effect the given ChangeLog is filled with the changes happening to the data model.
   * @param log
   * @param path
   * @param data
   */
  def updateValue(log: ChangeLog,
                  path: Path,
                  data: AnyRef)

  def syncPath(path: Path, handler: Endpoint)

  def syncPropertyValue(path: Path, endpoint: Endpoint)

  def syncPathWithQuery(path: Path,
                        endpoint: Endpoint,
                        queryEvaluator: QueryEvaluator,
                        query: String): Unit
}
