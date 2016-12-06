package sample

import org.scalajs.dom
import org.scalajs.dom.{Event, MouseEvent}
import rxscalajs.Observable

import scala.scalajs.js.JSApp
import scala.concurrent.duration._
import scala.scalajs.js.Dynamic.global
import scalatags.VDom.TypedTag
import scalatags.vdom.{observe, react}
import scalatags.vdom.observe._
import scalatags.vdom.raw.VNode

object Main extends JSApp {
  import scalatags.VDom.all._
  import scalatags.vdom.raw.VirtualDom
  import scalatags.events.MouseEventImplicits._

  def main(): Unit = {
    println("Started")
    val appDiv = dom.document.getElementById("app")

    val inputCheck =
      checkbox(input(`type` := "checkbox", value := "something"))()
    inputCheck.checked.subscribe(x => println("checked:" + x))
    inputCheck.value.subscribe(x => println("value:" + x))

    val inputO = observe.inputElement(input(`type` := "text", value := "something"))()
    inputO.value.subscribe(x => println("input text: " + x))
    inputO.element.subscribe(x => println("input element:" + x))
    inputO.events.subscribe(x => println("input: " + x), e => println(e))


    val printlnButtonO =
      observe.any(input(`type` := "button", value := "println"))(onclick)
    printlnButtonO.events
      .subscribe(x => println("button: " + x), e => println(e))

    // -- components that react
    val reactButton = react.at(input(`type` := "button"))(value -> inputO.value)

    val obsReactButton =
      react.at(observe.any(input(`type` := "button"))(onclick))(value -> inputO.value.map(_.length))

    obsReactButton.events.subscribe(x =>
      println("observable reactive button clicked"))

    val obsReactInput =
      react.at(observe.inputElement(input(`type` := "text"))())(value -> Observable.just("foobar"))


    //  -- static components, that do not need or force re-rendering (very rare in reality...)
    def static(dyn: TypedTag[VNode]) = div(
      inputCheck.tag,
      hr(),
      inputO.tag,
      hr(),
      printlnButtonO.tag,
      hr(),
      dyn
    )

    val initialdom = static(div())
    val el = VirtualDom.create(initialdom.render)
    appDiv.appendChild(el)

    reactButton
      .combineLatest(obsReactButton.tag, obsReactInput.tag)
      .scan((initialdom, el)) {
        case ((prevVDom, apDiv), (button1, button2, input)) =>
          val nextVDom = static(div(button1, button2, input))
          val patch = VirtualDom.diff(prevVDom.render, nextVDom.render)
          val nowDiv = VirtualDom.patch(apDiv, patch)
          (nextVDom, nowDiv)
      }
      .subscribe(_ => println("re-rendered"), e => println("error" + e))

  }
}
