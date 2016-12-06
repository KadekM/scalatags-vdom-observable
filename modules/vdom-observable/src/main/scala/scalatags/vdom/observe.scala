package scalatags.vdom

import scalatags.VDom.all._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLInputElement
import rxscalajs._
import shapeless.Id

import scalatags.VDom.TypedTag
import scalatags.events.AllEventsImplicits._
import scalatags.vdom.raw.VNode
import scalatags.vdom.react.{ConnectAttribute, ConnectSelfAttribute}
import scala.scalajs.js.Dynamic.global

object observe {
  // -- find some generalized way

  def any(t: TypedTag[VNode])(observe: Attr*): ObservableTag[Id] =
    new ObservableTag[Id] {
      override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] = observe
      override val tag: Id[TypedTag[VNode]]                          = applyAttrs(t)
    }

  def inputElement(t: TypedTag[VNode])(observe: Attr*): InputElement[Id] =
    new InputElement[Id] {
      override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] = oninput +: observe
      override val tag: Id[TypedTag[VNode]]                          = applyAttrs(t)
    }

  def checkbox(t: TypedTag[VNode])(observe: Attr*): Checkbox[Id] =
    new Checkbox[Id] {
      override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] = onchange +: observe
      override val tag: Id[TypedTag[VNode]]                          = applyAttrs(t)
    }
}

trait ObservableTag[Container[+ _]] {

  val tag: Container[TypedTag[VNode]]

  // events

  protected[this] final val _events: Subject[dom.raw.Event] =
    Subject[dom.raw.Event]()

  final val events: Observable[dom.raw.Event] = _events

  protected[this] final val _element: Subject[dom.raw.Node] =
    Subject[dom.raw.Node]()

  final val element: Observable[dom.raw.Node] = _element

  protected[this] def applyAttr(t: TypedTag[VNode], attr: Attr): TypedTag[VNode] =
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

trait InputElement[Container[+ _]] extends ObservableTag[Container] {

  final val value: Observable[String] = element
    .map(x => x.asInstanceOf[HTMLInputElement].value)
    .merge {
      events.collect {
        // todo: doesn't this fire for some events we don't want?
        case e: dom.Event =>
          e.target.asInstanceOf[HTMLInputElement].value
      }
    }
    // updates on vdom cause event to fire, even if text doesnt change...
    // we could look into whole vdom hook since it has both states, hm
    // maybe it should fire (oldState, newState)
    .distinctUntilChanged
    .publishReplay(1)
    .refCount

}

trait Checkbox[Container[+ _]] extends InputElement[Container] {

  final val checked: Observable[Boolean] =
    element.map(x => x.asInstanceOf[HTMLInputElement].checked).merge {
      events.collect {
        case e: dom.Event => e.target.asInstanceOf[HTMLInputElement].checked
      }
    }

}

sealed trait Activator[F[+ _[_]], T] {
  def activate(x: F[Id], att: ConnectAttribute[T]): F[Observable]
  def activateSelf(x: F[Id], att: ConnectSelfAttribute[T, F]): F[Observable]
}

// todo generalize activation

object InputElement {
  implicit def activator[T](
      implicit ev: scalatags.generic.AttrValue[Builder, T]): Activator[InputElement, T] =
    new Activator[InputElement, T] {
      override def activate(x: InputElement[Id],
                            att: (_root_.scalatags.VDom.all.Attr, Observable[T])): InputElement[Observable] =
        new InputElement[Observable] {

          override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] =
            x.forceObserve

          override val tag: Observable[TypedTag[VNode]] =
            Observable.just(x.tag).merge {
              att._2.map { v =>
                val t = applyAttrs(x.tag(att._1 := v))
                println(t)
                t
              }
            }
        }

      override def activateSelf(
          x: InputElement[Id],
          att: (_root_.scalatags.VDom.all.Attr, (InputElement[Observable]) => Observable[T]))
        : InputElement[Observable] = {

        new InputElement[Observable] {
          override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] =
            x.forceObserve

          override val tag: Observable[TypedTag[VNode]] =
            Observable.just(x.tag).merge {
              att._2(this).map { v =>
                applyAttrs(x.tag(att._1 := v))
              }
            }
        }
      }
    }
}

object ObservableTag {
  implicit def activator[T](
      implicit ev: scalatags.generic.AttrValue[Builder, T]): Activator[ObservableTag, T] =
    new Activator[ObservableTag, T] {
      override def activate(
          x: ObservableTag[Id],
          att: (_root_.scalatags.VDom.all.Attr, Observable[T])): ObservableTag[Observable] =
        new ObservableTag[Observable] {
          override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] =
            x.forceObserve

          override val tag: Observable[TypedTag[VNode]] =
            Observable.just(x.tag).merge {
              att._2.map { v =>
                applyAttrs(x.tag(att._1 := v))
              }
            }
        }

      override def activateSelf(
          x: ObservableTag[Id],
          att: (_root_.scalatags.VDom.all.Attr, (ObservableTag[Observable]) => Observable[T]))
        : ObservableTag[Observable] =
        new ObservableTag[Observable] {
          override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] =
            x.forceObserve

          override val tag: Observable[TypedTag[VNode]] =
            Observable.just(x.tag).merge {
              att._2(this).map { v =>
                applyAttrs(x.tag(att._1 := v))
              }
            }
        }

    }
}

object Checkbox {}
