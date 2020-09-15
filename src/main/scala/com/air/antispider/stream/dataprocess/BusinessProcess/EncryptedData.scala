package com.air.antispider.stream.dataprocess.BusinessProcess

import java.util.regex.{Matcher, Pattern}

import com.air.antispider.stream.common.util.decode.MD5

object EncryptedData {
  /**
    * 对手机号加密
    * @param message
    */
def encryptedPhone(message:String): String ={
  //实例MD5
  val md5=new MD5()
  //定义临时接受数据的变量
  var encryptedData=message
  //1 获取手机号的正则表达式 网上很多这种正则
  val phonePattern: Pattern = Pattern.compile("((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[0-9])|(18[0,5-9]))\\d{8}")
  //2 使用正则匹配数据获取出有可能是手机号的数据 返回的数据放到一个相当于集合的地方
  val phones: Matcher = phonePattern.matcher(encryptedData)
  //相当于遍历集合中的数据
  while(phones.find()){//能进入整个循环的数据 一定是包含手机号的数据
    //过滤出一个可能手机号数据（不一定是手机号）
    val phone= phones.group()
    //获取11位手机号前面一位的index
    var befIndex=encryptedData.indexOf(phone)-1
    //获取11位手机号后面一位的index
    val aftIndex=encryptedData.indexOf(phone)+11
    //获取11位手机号前面一位的字符
    val befLetter=encryptedData.charAt(befIndex).toString
    //获取11位手机号后面一位的字符
    val aftLetter=encryptedData.charAt(aftIndex).toString

    //3 判断出一定是手机号的数据
    //3-1手机号码前一个位置不是数字，并且手机号码是一条数据中的最后一个数据，那么表示这个一定是手机号
    if (!befLetter.matches("^[0-9]$")){
      if(!aftLetter.matches("^[0-9]$")){
        //确定出是手机号后，将手机号加密，替换原始的数据
        encryptedData=encryptedData.replace(phone,md5.getMD5ofStr(phone))
      }
    }

  }
  encryptedData
}

  /**
    * 对身份证号进行加密
    * @param message
    */
  def encryptedID(message:String): String ={
    //实例MD5
    val md5=new MD5()
    //定义临时接受数据的变量
    var encryptedData=message
    //1 获取身份证号的正则表达式 网上很多这种正则
    val idPattern = Pattern.compile("(\\d{18})|(\\d{17}(\\d|X|x))|(\\d{15})")
    //2 使用正则匹配数据获取出有可能是身份证号的数据 返回的数据放到一个相当于集合的地方
    val ids: Matcher = idPattern.matcher(encryptedData)
    //相当于遍历集合中的数据
    while(ids.find()){
      //过滤出一个可能身份证号数据（不一定是手机号）
      val id= ids.group()
      //获取11位身份证号前面一位的index
      var befIndex=encryptedData.indexOf(id)-1
      //获取11位身份证号后面一位的index
      val aftIndex=encryptedData.indexOf(id)+18
      //获取11位身份证号前面一位的字符
      val befLetter=encryptedData.charAt(befIndex).toString
      //获取11位身份证号后面一位的字符
      val aftLetter=encryptedData.charAt(aftIndex).toString

      //3 判断出一定是身份证号的数据
      //3-1身份证号前一个位置不是数字，并且身份号码是一条数据中的最后一个数据，那么表示这个一定是手机号
      if (!befLetter.matches("^[0-9]$")){
        if(!aftLetter.matches("^[0-9]$")){
          //确定出是身份证号后，将身份证号加密，替换原始的数据
          encryptedData=encryptedData.replace(id,md5.getMD5ofStr(id))
        }
      }

    }
    encryptedData
  }
}
