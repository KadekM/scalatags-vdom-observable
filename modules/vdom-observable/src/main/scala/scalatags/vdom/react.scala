package scalatags.vdom

import rxscalajs.Observable

import scalatags.VDom.TypedTag
import scalatags.VDom.all.Attr
import scalatags.vdom.raw.VNode

object react {
  type ConnectAttribute[+T] = (Attr, Observable[T])
  type ConnectSelfAttribute[+T, A] = (Attr, A => Observable[T])

  // -- generalize via shapeless?
  // for static content

  def at[T](t: TypedTag[VNode])(obs: ConnectAttribute[T])(
    implicit ev: scalatags.generic.AttrValue[Builder, T])
  : Observable[TypedTag[VNode]] =
    Observable.just(t).merge {
      obs._2.map { v =>
        t(obs._1 := v)
      }
    }

  def at2[T1, T2](t: TypedTag[VNode])(obs1: ConnectAttribute[T1],
                                         obs2: ConnectAttribute[T2])(
                      implicit ev1: scalatags.generic.AttrValue[Builder, T1],
                      ev2: scalatags.generic.AttrValue[Builder, T2]
                    ): Observable[TypedTag[VNode]] =
    Observable.just(t).merge {
      obs1._2.combineLatestWith(obs2._2) {
        case (v1, v2) =>
          t(obs1._1 := v1, obs2._1 := v2)
      }
    }

  // for observable content

  def at[T](t: ObservableTag with NonReactiveTag)(obs: ConnectAttribute[T])(
    implicit ev: scalatags.generic.AttrValue[Builder, T])
  : ObservableTag with ReactiveTag = new ObservableTag with ReactiveTag {

    override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] =
      t.forceObserve

    override val tag: Observable[TypedTag[VNode]] =
      Observable.just(t.tag).merge {
        obs._2.map { v =>
          applyAttrs(t.tag(obs._1 := v))
        }
      }
  }

  def at2[T1, T2](t: ObservableTag with NonReactiveTag)(
    obs1: ConnectAttribute[T1],
    obs2: ConnectAttribute[T2])(
                      implicit ev1: scalatags.generic.AttrValue[Builder, T1],
                      ev2: scalatags.generic.AttrValue[Builder, T2])
  : ObservableTag with ReactiveTag = new ObservableTag with ReactiveTag {

    override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] =
      t.forceObserve

    override val tag: Observable[TypedTag[VNode]] =
      Observable.just(t.tag).merge {
        obs1._2.combineLatestWith(obs2._2) {
          case (v1, v2) =>
            applyAttrs(t.tag(obs1._1 := v1, obs2._1 := v2))
        }
      }
  }

  // self
/*
  def atSelf[T, A <: ObservableTag](t: A with NonReactiveTag)(obs: ConnectSelfAttribute[T, A])(
    implicit ev: scalatags.generic.AttrValue[Builder, T])
  : A with ReactiveTag = new ObservableTag with ReactiveTag {

    override val forceObserve: Seq[_root_.scalatags.VDom.all.Attr] =
      t.forceObserve

    override val tag: Observable[TypedTag[VNode]] =
      Observable.just(t.tag).merge {
        obs._2.map { v =>
          applyAttrs(t.tag(obs._1 := v))
        }
      }
  }*/
}
