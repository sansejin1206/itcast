package com.air.antispider.stream.dataprocess.BusinessProcess

import com.air.antispider.stream.common.util.database.QueryDB

import scala.collection.mutable.ArrayBuffer

/**
  * 读取数据库的规则
  */
object AnalyzeRuleDB {
  def queryFiLterRule(): ArrayBuffer[String] ={
    var sql="select * from itcast_filter_rule";
    var str="value"
    val arr: ArrayBuffer[String] = QueryDB.queryData(sql,str)
    arr
  }
}
