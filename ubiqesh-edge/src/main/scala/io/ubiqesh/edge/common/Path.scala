package io.ubiqesh.edge.common

/**
 *
 * Representation of a Ubiqesh Resource Path. Scheme is like: /<element1>/<element2>
 *
 * @author Christoph Grotz
 *
 */
class Path(path: String) {

  private var elements: Array[String] = _

  if (!(path == Nil || path.isEmpty)) {
    this.elements = getPathElements(path)
  }

  if (elements == null) {
    this.elements = Array()
  }

  private def getPathElements(path: String): Array[String] = {
    if (path.startsWith("/")) path.substring(1).split("/") else path.split("/")
  }

  /**
   * @return the first element of the path
   */
  def getFirstElement(): String = {
    if (elements == null || elements.length == 0) {
      return null
    }
    elements(0)
  }

  /**
   * @return the last element of the path
   */
  def getLastElement(): String = {
    if (elements.length == 0) {
      return null
    }
    elements(elements.length - 1)
  }

  /**
   * Path: /element1/element2/element3/element4
   *
   * Subpath from offset 2: /element3/element4
   *
   * @param offset
   * @return returns the subpath at the offset
   */
  def getSubpath(offset: Int): Path = {
    var output = ""
    for (i <- offset until elements.length) {
      output += "/" + elements(i)
    }
    new Path(output)
  }

  /**
   * Path: /element1/element2/element3/element4
   *
   * Parent path: Path: /element1/element2/element3
   *
   * @return returns the parent path
   */
  def getParent(): Path = {
    var output = ""
    for (i <- 0 until elements.length - 1) {
      output += "/" + elements(i)
    }
    new Path(output)
  }

  /**
   * append element to path
   *
   * @param element
   * element to append
   * @return new Path with appended element
   */
  def append(element: String): Path = new Path(toString + "/" + element)

  /**
   * @return true if path consists of only one element
   */
  def isSimple(): Boolean = elements.length == 1

  /**
   * @return Path has no elements
   */
  def isEmtpy(): Boolean = elements.length <= 0 || toString == "/"

  override def toString(): String = {
    var output = ""
    for (element <- elements) {
      if (element != Nil || element.isEmpty) {
        output += "/" + element
      }
    }
    if (output.startsWith("/")) output else "/" + output
  }

  def toArray(): Array[String] = this.elements
}
