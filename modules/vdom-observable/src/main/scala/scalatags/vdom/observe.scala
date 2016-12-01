package scalatags.vdom

import scalatags.VDom.all._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLInputElement
import rxscalajs._

import scalatags.VDom.TypedTag
import scalatags.vdom
import scalatags.events.AllEventsImplicits._
import scalatags.vdom.raw.VNode

object observe {

  def any(t: TypedTag[VNode])(observe: Attr*): ObservableTag =
    ObservableTag(t)(observe: _*)

  // find some generalized way

  def inputElement(t: TypedTag[VNode])(
      observe: Attr*): ObservableTag with InputElement =
    new ObservableTag with InputElement {
      override val tag: TypedTag[VNode] = (oninput +: observe)
        .foldLeft(t)(applyAttr)
        .apply(onvdomload := { (e: dom.Node) =>
          _element.next(e)
        })
    }

  def checkbox(t: TypedTag[VNode])(
    observe: Attr*): ObservableTag with Checkbox =
    new ObservableTag with Checkbox {
      override val tag: TypedTag[VNode] = (onchange +: observe)
        .foldLeft(t)(applyAttr)
        .apply(onvdomload := { (e: dom.Node) =>
          _element.next(e)
        })
    }

}

object ObservableTag {

  def apply(t: TypedTag[VNode])(observe: Attr*) = new ObservableTag {
    override lazy val tag: TypedTag[VNode] = observe
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

  protected[this] def applyAttr(t: TypedTag[VNode],
                                attr: Attr): TypedTag[VNode] =
    t(attr := { (e: dom.Event) =>
      _events.next(e)
    })
}

trait InputElement { self: ObservableTag =>

  final val value: Observable[String] = element
    .map(x => x.asInstanceOf[HTMLInputElement].value)
    .merge {
      events.collect {
        // todo: doesn't this fire for some events we don't want?
        case e: dom.Event => e.target.asInstanceOf[HTMLInputElement].value
      }
    }
    // updates on vdom cause event to fire, even if text doesnt change...
    // we could look into whole vdom hook since it has both states, hm
    // maybe it should fire (oldState, newState)
    .distinctUntilChanged
    .publishReplay(1)
    .refCount
}

trait Checkbox extends InputElement { self: ObservableTag =>

  final val checked: Observable[Boolean] =
    element.map(x => x.asInstanceOf[HTMLInputElement].checked).merge {
      events.collect {
        case e: dom.Event => e.target.asInstanceOf[HTMLInputElement].checked
      }
    }

}
