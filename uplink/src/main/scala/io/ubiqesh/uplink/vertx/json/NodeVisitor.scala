package io.ubiqesh.uplink.vertx.json

import io.ubiqesh.uplink.common.Path
import org.vertx.java.core.json.JsonObject

trait NodeVisitor {

  def visitNode(path: Path, node: JsonObject): Unit

  def visitProperty(path: Path,
                    node: JsonObject,
                    key: String,
                    value: AnyRef): Unit
}
