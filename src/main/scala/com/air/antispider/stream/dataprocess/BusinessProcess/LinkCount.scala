package com.air.antispider.stream.dataprocess.BusinessProcess

import com.air.antispider.stream.common.util.jedis.{JedisConnectionUtil, PropertiesUtil}
import org.apache.spark.rdd.RDD
import org.json4s.DefaultFormats
import org.json4s.jackson.Json
import redis.clients.jedis.JedisCluster
class LinkCount {
  def LinkCount(rdd: RDD[String]): Unit = {

    val mapRdd: RDD[(String, Int)] = rdd.map(line => {
      var ip = ""
      if (line.split("#CS#", -1).length > 9) {
        ip = line.split("#CS#", -1)(9)
      }
      (ip, 1)
    })
    val serverCount: RDD[(String, Int)] = mapRdd.reduceByKey((t1, t2) => {
      t1 + t2
   })

    val ativeCount: RDD[(String, String)] = rdd.map(line => {
      var ip = ""
      var ativeUserCount = ""
      if (line.split("#CS#").length > 11) {
        ip = line.split("#CS#")(9)
        ativeUserCount = line.split("#CS#")(line.split("#CS#").length - 1)
      }
      (ip, ativeUserCount)
    }).reduceByKey((t1, t2) => t2)
    //1 在两个数据不为 空的前提下，将两个数据转换成两个小的map
    if (!serverCount.isEmpty() && !ativeCount.isEmpty()) {
      //2封装最终要写入redis的数据(将两个小的MAP封装成一个 大的MAP)
      //serversCountMap;与前端 工程师约定好
      // activeNumMap;与前端工程师约定好
      val serverMap: collection.Map[String, Int] = serverCount.collectAsMap()
      val ativeMap: collection.Map[String, String] = ativeCount.collectAsMap()
      val sumMap = Map("serversCountMap" -> serverMap,
        "acticeNumMap" -> ativeMap
      )

      //3在配置文件中读取出数据key的前缀，+时间戳(redis中对数据的key )


      //4在配置文件中读取出数据的有效存储时间

      //5将数据写入redis
      //创建集群对象
      var key=PropertiesUtil.getStringByKey("cluster.key.monitor.linkProcess","jedisConfig.properties")+
        System.currentTimeMillis().toString;
      var time=PropertiesUtil.getStringByKey("cluster.exptime.monitor","jedisConfig.properties").toInt
       val jedis: JedisCluster = JedisConnectionUtil.getJedisCluster
      jedis.setex(key,time,Json(DefaultFormats).write(sumMap))
    }

  }
}
