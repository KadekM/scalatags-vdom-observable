package scalatags.vdom

import rxscalajs.Observable
import shapeless.Id

import scalatags.VDom.TypedTag
import scalatags.VDom.all.Attr
import scalatags.vdom.raw.VNode

object react {
  type ConnectAttribute[+T]                = (Attr, Observable[T])
  type ConnectSelfAttribute[+T, F[+ _[_]]] = (Attr, F[Observable] => Observable[T])

  // -- generalize via shapeless?
  // for static content

  def at[T](t: TypedTag[VNode])(obs: ConnectAttribute[T])(
      implicit ev: scalatags.generic.AttrValue[Builder, T]): Observable[TypedTag[VNode]] =
    Observable.just(t).merge {
      obs._2.map { v =>
        //println("setting " + v.toString + " at " + t)
        t(obs._1 := v)
      }
    }

  /*  def at2[T1, T2](t: TypedTag[VNode])(obs1: ConnectAttribute[T1], obs2: ConnectAttribute[T2])(
      implicit ev1: scalatags.generic.AttrValue[Builder, T1],
      ev2: scalatags.generic.AttrValue[Builder, T2]
  ): Observable[TypedTag[VNode]] =
    Observable.just(t).merge {
      obs1._2.combineLatestWith(obs2._2) {
        case (v1, v2) =>
          t(obs1._1 := v1, obs2._1 := v2)
      }
    }*/

  // for observable content

  def at[T, F[+ _[_]]](t: F[Id])(obs: ConnectAttribute[T])(implicit c: Activator[F, T]): F[Observable] =
    c.activate(t, obs)

  // self
  def atSelf[T, F[+ _[_]]](t: F[Id])(obs: ConnectSelfAttribute[T, F])(implicit c: Activator[F, T]): F[Observable] =
    c.activateSelf(t, obs)
}
