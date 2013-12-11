package io.yagni.edge.persistence.queries

import io.yagni.edge.common.Path
import scala.reflect.BeanProperty

//remove if not needed


class QueryEntry(pathArg: Path, @BeanProperty var query: String) {

  var path: String = pathArg.toString

  def getPath(): Path = new Path(path)
}
