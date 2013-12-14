package io.yagni.edge.persistence.queries

import org.slf4j.LoggerFactory
import io.yagni.edge.common.Path
import io.yagni.edge.persistence.queries.scripting.SandboxedScriptingEnvironment
import scala.collection.mutable
import org.vertx.java.core.json.JsonObject

import scala.collection.JavaConversions._

class QueryEvaluator {

  private val LOGGER = LoggerFactory.getLogger(classOf[QueryEvaluator])

  var attached_queries: mutable.MultiMap[String, String] = new mutable.HashMap[String, mutable.Set[String]] with mutable.MultiMap[String, String]

  var nodesForQuery: mutable.MultiMap[String, String] = new mutable.HashMap[String, mutable.Set[String]] with mutable.MultiMap[String, String]

  var scriptingEnvironment: SandboxedScriptingEnvironment = new SandboxedScriptingEnvironment()

  def appliesToQuery(path: Path, value: AnyRef): Boolean = {
    for (queryStrs <- attached_queries.get(path.toString)) {
      for (queryStr: String <- queryStrs) {
        return evaluateQueryOnValue(value, queryStr)
      }
    }
    false
  }

  def evaluateQueryOnValue(value: AnyRef, queryStr: String): Boolean = {
    try {
      var parsedValue: AnyRef = null
      parsedValue = if (value.isInstanceOf[JsonObject]) scriptingEnvironment.eval("JSON.parse('" + value.toString + "');") else value
      scriptingEnvironment.eval("var query = " + queryStr + ";")
      val result = scriptingEnvironment.invokeFunction("query", parsedValue).asInstanceOf[java.lang.Boolean]
      return result.booleanValue()
    } catch {
      case e: Exception => LOGGER.error("Error (" + e.getMessage + ") on Query (" + queryStr +
        ")", e)
    }
    false
  }

  def addQuery(path: Path, query: String) {
    attached_queries.addBinding(path.toString, query)
  }

  def removeQuery(path: Path, query: String) {
    attached_queries.removeBinding(path.toString, query)
  }

  def hasQuery(path: Path): Boolean = {
    attached_queries.containsKey(path.toString)
  }

  def queryContainsNode(queryPath: Path, query: String, nodePath: Path): Boolean = {
    nodesForQuery.entryExists(new QueryEntry(queryPath, query).toString, _ == nodePath.toString)
  }

  def addNodeToQuery(path: Path, query: String, pathToNode: Path) = {
    nodesForQuery.addBinding(new QueryEntry(path, query).toString, pathToNode.toString)
  }

  def removeNodeFromQuery(path: Path, query: String, pathToNode: Path) = {
    nodesForQuery.removeBinding(new QueryEntry(path, query).toString, pathToNode.toString)
  }

  def getQueries() = attached_queries
}
