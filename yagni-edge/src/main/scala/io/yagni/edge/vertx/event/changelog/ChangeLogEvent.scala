package io.yagni.edge.vertx.event.changelog

import io.yagni.edge.common.Path
import scala.reflect.BeanProperty

trait ChangeLogEvent

class ChildAddedLogEvent(@BeanProperty var name: String,
                         @BeanProperty var path: Path,
                         @BeanProperty var parent: Path,
                         @BeanProperty var value: AnyRef,
                         @BeanProperty var numChildren: Long) extends ChangeLogEvent {

  def getHasChildren(): Boolean = this.numChildren > 0
}

class ChildChangedLogEvent(@BeanProperty var name: String,
                           @BeanProperty var path: Path,
                           @BeanProperty var parent: Path,
                           @BeanProperty var value: AnyRef,
                           @BeanProperty var numChildren: Long) extends ChangeLogEvent {

  def getHasChildren(): Boolean = this.numChildren > 0
}

class ChildRemovedLogEvent(@BeanProperty var path: Path, @BeanProperty var name: String, @BeanProperty var value: AnyRef)
  extends ChangeLogEvent {
}

class ValueChangedLogEvent(@BeanProperty var name: String,
                           @BeanProperty var path: Path,
                           @BeanProperty var parent: Path,
                           @BeanProperty var value: AnyRef) extends ChangeLogEvent {
}