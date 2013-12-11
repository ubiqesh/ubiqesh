package io.yagni.edge.vertx.json

import io.yagni.edge.common.Path
import org.vertx.java.core.json.JsonObject

//remove if not needed


trait NodeVisitor {

  def visitNode(path: Path, node: JsonObject): Unit

  def visitProperty(path: Path,
                    node: JsonObject,
                    key: String,
                    value: AnyRef): Unit
}
