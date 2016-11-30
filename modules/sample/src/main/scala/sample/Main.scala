package sample

import org.scalajs.dom
import org.scalajs.dom.{Event, MouseEvent}

import scala.scalajs.js.JSApp
import scala.scalajs.js.Dynamic.global
import scalatags.vdom.observe._

object Main extends JSApp {
  import scalatags.VDom.all._
  import scalatags.vdom.raw.VirtualDom
  import scalatags.events.MouseEventImplicits._

  def main(): Unit = {
    println("Started")
    val appDiv = dom.document.getElementById("app")

    val inputCheck = checkbox(input(`type` := "checkbox", value := "something"))()
    inputCheck.checked.subscribe(x => println("checked:" + x))
    inputCheck.value.subscribe(x => println("value:" + x))

    val inputO = inputElement(input(`type` := "text", value := "something"))()
    inputO.value.subscribe(x => println("input text: " + x))
    inputO.element.subscribe(x => println("input element:" + x))
    inputO.events.subscribe(x => println("input: " + x), e => println(e))

    val printlnButtonO = any(input(`type` := "button", value := "println"))(onclick)
    printlnButtonO.events.subscribe(x => println("button: " + x), e => println(e))

    val vdom = div(
      inputCheck.tag
      ,inputO.tag
      ,printlnButtonO.tag
    )


    val vnode = vdom.render

    val el = VirtualDom.create(vnode)
    appDiv.appendChild(el)

  }
}



