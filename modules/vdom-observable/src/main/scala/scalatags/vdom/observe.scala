package scalatags.vdom

import scalatags.VDom.all._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLInputElement
import rxscalajs._
import shapeless.Id

import scalatags.VDom.TypedTag
import scalatags.events.AllEventsImplicits._
import scalatags.vdom.raw.VNode


object observe {
  // -- find some generalized way

  def any(t: TypedTag[VNode])(
      observe: Attr*): ObservableTag with NonReactiveTag =
    new ObservableTag with NonReactiveTag {
      override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] = observe
      override val tag: Id[TypedTag[VNode]] = applyAttrs(t)
    }

  def inputElement(t: TypedTag[VNode])(
      observe: Attr*): ObservableTag with NonReactiveTag with InputElement =
    new ObservableTag with NonReactiveTag with InputElement {
      override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] = oninput +: observe
      override val tag: Id[TypedTag[VNode]] = applyAttrs(t)
    }

  def checkbox(t: TypedTag[VNode])(
      observe: Attr*): ObservableTag with NonReactiveTag with Checkbox =
    new ObservableTag with NonReactiveTag with Checkbox {
      override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] = onchange +: observe
      override val tag: Id[TypedTag[VNode]] = applyAttrs(t)
    }
}

trait HasTag {
  type Container[+A]
  val tag: Container[TypedTag[VNode]]
}

trait NonReactiveTag extends HasTag {
  override type Container[+A] = Id[A]
}

trait ReactiveTag extends HasTag {
  override type Container[+A] = Observable[A]
}

trait ObservableTag { self: HasTag =>

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

  // internal

  val forceObserve: Seq[Attr]

  protected[this] def applyAttrs(t: TypedTag[VNode]) =
    forceObserve
      .foldLeft(t)(applyAttr)
      .apply(onvdomload := { (e: dom.Node) =>
        _element.next(e)
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
