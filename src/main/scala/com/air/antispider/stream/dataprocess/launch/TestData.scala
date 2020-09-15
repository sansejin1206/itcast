package com.air.antispider.stream.dataprocess.launch

object TestData {
  def main(args: Array[String]): Unit = {
    var str="abc15560101785ac";
    var str2="15560101785"
   val i: Int = str.indexOf(str2)+11
    println(i)

  }
}
