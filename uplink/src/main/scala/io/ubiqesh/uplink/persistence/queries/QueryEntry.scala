package io.ubiqesh.uplink.persistence.queries

import io.ubiqesh.uplink.common.Path
import scala.reflect.BeanProperty

/**
 * This Class associates a query (String representation of a Javascript method) with a given Path.
 * @param pathArg
 * @param query
 */
class QueryEntry(pathArg: Path, @BeanProperty var query: String) {

  var path: String = pathArg.toString

  def getPath(): Path = new Path(path)
}
