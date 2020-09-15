package com.air.antispider.stream.dataprocess.BusinessProcess

import scala.collection.mutable.ArrayBuffer

object URLFilter {
  def URLfilter(message: String, filterRuleList: ArrayBuffer[String]): Boolean = {
    var save = true
    var urls = if (message.split("#CS#").length > 2) message.split("#CS#")(1) else ""
    for (str <- filterRuleList) {
      if (urls.matches(str)) {
        save = false
      }
    }
    save
  }


}
