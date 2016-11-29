package scalatags.vdom

import scalatags.VDom.all._
import org.scalajs.dom
import rxscalajs._

import scalatags.VDom.TypedTag
import scalatags.vdom
import scalatags.events.AllEventsImplicits._
import scalatags.vdom.raw.VNode

object observe {

  def obs(t: TypedTag[VNode])(es: Attr*): ObservableTag = ObservableTag(t)(es: _*)

  def obsTextInput(t: TypedTag[VNode])(
      es: Attr*): ObservableTag with ObservableTextInput =
    new ObservableTag with ObservableTextInput {
      override val tag: TypedTag[VNode] = ???
    }

}

object ObservableTag {

  def apply(t: TypedTag[VNode])(es: Attr*) = new ObservableTag {
    override lazy val tag: TypedTag[VNode] = es
      .foldLeft(t)(applyAttr)
      .apply(onvdomload := { (e: dom.Node) =>
        _element.next(e)
      })
  }
}

trait ObservableTag {

  val tag: TypedTag[VNode]

  // events

  protected[this] final val _events: Subject[dom.raw.Event] =
    Subject[dom.raw.Event]()

  final val events: Observable[dom.raw.Event] = _events

  protected[this] final val _element: Subject[dom.raw.Node] =
    Subject[dom.raw.Node]()

  final val element: Observable[dom.raw.Node] = _element

  // internal api

  protected[this] def feedGenericEvent(e: dom.Event): Unit = _events.next(e)

  protected[this] lazy val obsMap: Map[Attr, dom.Event => Unit] = Map(
    onchange -> feedGenericEvent,
    onload -> feedGenericEvent,
    oninput -> feedGenericEvent,
    onclick -> feedGenericEvent
  )

  protected[this] def applyAttr(t: TypedTag[VNode],
                                attr: Attr): TypedTag[VNode] =
    obsMap.get(attr) match {
      case None => t
      case Some(f) => t(attr := f)
    }
}

trait ObservableTextInput { self: ObservableTag =>

  protected[this] final val _text: Subject[String] =
    Subject[String]() // element.asInstanceOf.value.publishReplay ...etc

  final val text: Observable[dom.raw.Event] = _events

}
