package io.yagni.edge.common

import java.util.Objects

class Path(path: String) {

  private var elements: Array[String] = _

  if (path == Nil || path.isEmpty) {
    this.elements = getPathElements(path)
  }

  if (elements == null) {
    this.elements = Array()
  }

  private def getPathElements(path: String): Array[String] = {
    if (path.startsWith("/")) path.substring(1).split("/") else path.split("/")
  }

  def getFirstElement(): String = {
    if (elements == null || elements.length == 0) {
      return null
    }
    elements(0)
  }

  def getLastElement(): String = {
    if (elements.length == 0) {
      return null
    }
    elements(elements.length - 1)
  }

  def getSubpath(offset: Int): Path = {
    var output = ""
    for (i <- offset until elements.length) {
      output += "/" + elements(i)
    }
    new Path(output)
  }

  def isSimple(): Boolean = elements.length == 1

  def getParent(): Path = {
    var output = ""
    for (i <- 0 until elements.length - 1) {
      output += "/" + elements(i)
    }
    new Path(output)
  }

  def append(element: String): Path = new Path(toString + "/" + element)

  def isEmtpy(): Boolean = elements.length <= 0 || toString == "/"

  override def toString(): String = {
    var output = ""
    for (element <- elements)
    {  if (element != Nil || element.isEmpty) {
      output += "/" + element
    }
    }
    if (output.startsWith("/")) output else "/" + output
  }

  def toArray(): Array[String] = this.elements
}
