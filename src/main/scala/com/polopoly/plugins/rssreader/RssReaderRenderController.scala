/**
 *
 */
package com.polopoly.plugins.rssreader

import com.polopoly.render.CacheInfo
import com.polopoly.render.RenderRequest
import com.polopoly.siteengine.dispatcher.ControllerContext
import com.polopoly.siteengine.model.TopModel
import com.polopoly.siteengine.mvc.RenderControllerBase
import com.polopoly.model.ModelPathUtil
import scala.xml.XML
import scala.xml.Elem
import com.polopoly.model.Model
import scala.xml.NodeSeq

/**
 * @author Staffan Lundström
 *
 */
class RssReaderController extends RenderControllerBase {

  override def populateModelAfterCacheKey(request: RenderRequest, m: TopModel,
    cacheInfo: CacheInfo, context: ControllerContext) {

    val contentModel = context.getContentModel
    val url = ModelPathUtil.get(contentModel, "feedUrl/value").asInstanceOf[String]
    val strLen = ModelPathUtil.get(contentModel, "listLength/value").asInstanceOf[String]
    val listLength = toPositive { toInt(strLen, 20) } 
    val strOpenInNewWindow = ModelPathUtil.get(contentModel, "openInNewWindow/value").asInstanceOf[String]
    val openInNewWindow = toBool(strOpenInNewWindow, true)
    val target =
      if (openInNewWindow)
        "_blank"
      else
        "_self"

    val results = XML.load(url)
    val channel = results \ "channel"
    
    val teasermain = renderMain(channel, renderLi(listLength, target))

    ModelPathUtil.set(m.getLocal(), "output", teasermain)

    super.populateModelAfterCacheKey(request, m, cacheInfo, context)
  }

  private def renderMain(channel: NodeSeq, li: NodeSeq => NodeSeq) = {

    <div class="teasermain">
      <div class="rssreaderheader"><b>{ (channel \ "title").text }</b></div>
      <div class="secitems">
        <ul class="teaser">
          { li(channel \ "item") }  
        </ul>
      </div>
    </div>

  }

  private def renderLi(maxItems: Int, target: String)(items: NodeSeq): NodeSeq = {
    items take maxItems-1 map { item => 
      <li>
        <a href={ (item \ "link").text } target={ target }>{ (item \ "title").text }</a>
      </li>    
    }
  }

  private def toInt(s: String, default: Int): Int = {
    try {
      s.toInt
    } catch {
      case _: java.lang.NumberFormatException => default
    }
  }

  private def toPositive(i: Int): Int =
    if (i >= 0)
      i
    else
      0

  private def toBool(s: String, default: Boolean): Boolean = {
    try {
      s.toBoolean
    } catch {
      case _: java.lang.NumberFormatException => default
    }
  }
}