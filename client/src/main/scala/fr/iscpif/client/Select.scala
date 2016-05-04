package fr.iscpif.client

/*
 * Copyright (C) 19/04/16 // mathieu.leclaire@openmole.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) 13/01/15 // mathieu.leclaire@openmole.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import fr.iscpif.client.PopupDiv._
import fr.iscpif.scaladget.api.{BootstrapTags ⇒ bs}
import org.scalajs.dom.raw._
import fr.iscpif.scaladget.stylesheet.{all ⇒ sheet}
import sheet._
import rx._
import scalatags.JsDom.all._
import client.JsRxTags._
import scalatags.JsDom.{tags ⇒ tags}

object Select {

  ///////////////

  trait Displayable {
    def name: String
  }

  lazy val selectFilter: ModifierSeq = Seq(
    sheet.marginTop(6),
    fontSize := 14,
    sheet.paddingLeft(5),
    borderBottomRightRadius := 0,
    borderBottomLeftRadius := 0
  )

  ///


  implicit def seqToSeqOfEmptyPairs[T <: Displayable](s: Seq[T]): Seq[(T, ModifierSeq)] = s.map {
    (_, emptyMod)
  }

  implicit def seqOfTupleToSeqOfT[T <: Displayable](s: Seq[(T, _)]): Seq[T] = s.map {
    _._1
  }

  def apply[T <: Displayable](
                               //  autoID:       String,
                               contents: Seq[(T, ModifierSeq)],
                               default: Option[T],
                               key: ModifierSeq = emptyMod,
                               onclickExtra: () ⇒ Unit = () ⇒ {}
                             ) = new Select(Var(contents), default, key, onclickExtra)


}

import Select._

class Select[T <: Select.Displayable](
                                private val contents: Var[Seq[(T, ModifierSeq)]],
                                default: Option[T] = None,
                                key: ModifierSeq = emptyMod,
                                onclickExtra: () ⇒ Unit = () ⇒ {}
                              ) {

  val autoID = java.util.UUID.randomUUID.toString

  val content: Var[Option[T]] = Var(contents().size match {
    case 0 ⇒ None
    case _ ⇒ default match {
      case None ⇒ Some(contents()(0)._1)
      case _ ⇒
        val ind = contents().map {
          _._1
        }.indexOf(default.get)
        if (ind != -1) Some(contents()(ind)._1) else Some(contents()(0)._1)
    }
  })

  val hasFilter = Var(false)
  val filtered: Var[Seq[T]] = Var(contents())
  filtered() = contents().take(100)

  lazy val inputFilter: HTMLInputElement = input(selectFilter, placeholder := "Filter", oninput := { () ⇒
    filtered() = contents().filter {
      _._1.name.toUpperCase.contains(inputFilter.value.toUpperCase)
    }
  })("").render

  val glyphMap = Var(contents().toMap)

  def resetFilter = {
    filtered() = contents().take(100)
    content() = None
  }

  def setContents(cts: Seq[T], onset: () ⇒ Unit = () ⇒ {}) = {
    contents() = cts
    content() = cts.headOption
    filtered() = contents().take(100)
    glyphMap() = contents().toMap
    inputFilter.value = ""
    onset()
  }

  def emptyContents = {
    contents() = Seq()
    content() = None
  }

  def isContentsEmpty = contents().isEmpty

  lazy val selector = {

    lazy val popupButton: PopupDiv = new PopupDiv(
      span(key +++ pointer, `type` := "button")(
        Rx {
          content().map { c ⇒
            bs.glyphSpan(glyphMap()(c))
          }
        },
        Rx {
          content().map {
            _.name
          }.getOrElse(contents()(0)._1.name) + " "
        },
        span(ms("caret"))
      ).render,
      div(
        if (hasFilter())
          div(
            tags.form(inputFilter)(`type` := "submit", onsubmit := { () ⇒
              content() = filtered().headOption
             // bg.click()
              false
            })
          )
        else tags.div,
        Rx {
          tags.ul( sheet.marginLeft(0) +++ (listStyleType := "none"))(
            if (filtered().size < 100) {
              for (c ← filtered()) yield {
                scalatags.JsDom.tags.li(pointer, onclick := { () ⇒
                  println("content " + content())
                  content() = contents().filter {
                    _._1 == c
                  }.headOption.map {
                    _._1
                  }
                  onclickExtra()
                  popupButton.close
                })(c.name)
              }
            }
            else scalatags.JsDom.tags.li("To many results, filter more !")
          )
        }
      ),
      Bottom,
      whitePopupWithBorder,
      noArrow
    )

    popupButton.popup

  }

  lazy val selectorWithFilter = {
    // hasFilter() = if(contents().size > 9) true else false
    hasFilter() = true
    selector
  }
}
