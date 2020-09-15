package com.air.antispider.stream.dataprocess.launch
import com.air.antispider.stream.common.util.jedis.{JedisConnectionUtil, PropertiesUtil}
import com.air.antispider.stream.common.util.log4j.LoggerLevels
import com.air.antispider.stream.dataprocess.BusinessProcess.{AnalyzeRuleDB, EncryptedData, LinkCount, URLFilter}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.{Durations, StreamingContext}
import redis.clients.jedis.JedisCluster

import scala.collection.mutable.ArrayBuffer

object DataProcessLauncher {
  //定义数据库规则的 数组
  private var filterRuleList: ArrayBuffer[String] = _;
  //定义广播变量
  private var broadcastFilterRuleList: Broadcast[ArrayBuffer[String]] = _;

  def main(args: Array[String]): Unit = {
    //添加日志级别的设置
    LoggerLevels.setStreamingLogLevels()

    //当应用被停止的时候，进行如下设置可以保证当前批次执行完之后再停止应用。
    System.setProperty("spark.streaming.stopGracefullyOnShutdown", "true")
    val conf = new SparkConf()
    conf.setMaster("local[2]")
    conf.setAppName("SparkStreamingOnKafkaDirect")
    conf.set("spark.metrics.conf.executor.source.jvm.class", "org.apache.spark.metrics.source.JvmSource")
    //程序初始话阶段 可以借此打开数据库的连接
    filterRuleList = AnalyzeRuleDB.queryFiLterRule()

    //开启集群监控功能 整个程序只会初始话一次 sparkStream对象 之后的配置 都被加载到内存中 每次都会从内存中读取
    val ssc = new StreamingContext(conf, Durations.seconds(5))

    println("---------------------------------------")
    //设置日志级别
    ssc.sparkContext.setLogLevel("Error")

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> PropertiesUtil.getStringByKey("default.brokers", "kafkaConfig.properties"),
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "MyGroupId", //

      /**
        * 当没有初始的offset，或者当前的offset不存在，如何处理数据
        * earliest ：自动重置偏移量为最小偏移量
        * latest：自动重置偏移量为最大偏移量【默认】
        * none:没有找到以前的offset,抛出异常
        */
      "auto.offset.reset" -> "latest",

      /**
        * 当设置 enable.auto.commit为false时，不会自动向kafka中保存消费者offset.需要异步的处理完数据之后手动提交
        */
      "enable.auto.commit" -> (false: java.lang.Boolean) //默认是true
    )
    //创建sparkcontext对象
    val sc: SparkContext = ssc.sparkContext

    //添加到广播变量
    broadcastFilterRuleList = sc.broadcast(filterRuleList)
    val topics: Array[String] = Array(PropertiesUtil.getStringByKey("source.nginx.topic", "kafkaConfig.properties"))
    /*调用方法这样不会显得类太乱*/
    setupSsc(ssc.sparkContext, kafkaParams, topics, ssc)

    ssc.start()
    ssc.awaitTermination()
    ssc.stop()

  }

  /**
    * 预处理数据操作
    *
    * @param sc
    * @param kafkaParams
    * @param topics
    * @param ssc
    */
  def setupSsc(sc: SparkContext, kafkaParams: Map[String, Object], topics: Array[String], ssc: StreamingContext): Unit = {
    /**
      * 程序初始话阶段 获取redis对象 需要在redis添加一个值 用这个值来判断是否重新读取mysql的规则 以便重新读取mysql
      * 这样你在添加规则的时候 把redis的ture改为 false就可以重新读取mysql规则
      */
    val jedis: JedisCluster = JedisConnectionUtil.getJedisCluster
    jedis.set("NeedUpDateFilterRule", "false")
    //监控 kafka的数据 有数据就读取过来
    val stream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent, //
      Subscribe[String, String](topics, kafkaParams)
    )

    val transStrem: DStream[String] = stream.map(record => {
      val key_value = (record.key, record.value)
      //      println("receive message key = "+key_value._1)
      //      println("receive message value = "+key_value._2)
      key_value._2
    })


    transStrem.foreachRDD(rdd => {

      /**
        * 1 链路统计功能  已完成
        */

      new LinkCount().LinkCount(rdd: RDD[String])

      /**
        * 2 数据清洗规则读取init
        */

      val boolean = jedis.get("NeedUpDateFilterRule")
      if (!boolean.isEmpty && boolean.toBoolean) {
        //重新读取规则
        filterRuleList = AnalyzeRuleDB.queryFiLterRule()
        //清空广播变量
        broadcastFilterRuleList.unpersist()
        //将新list更新到广播变量中
        broadcastFilterRuleList = sc.broadcast(filterRuleList)
        //把redis的字段设置为flase
        jedis.set("NeedUpDateFilterRule", "false")
      }

      /**
        * 2-1数据清洗逻辑代码
        */
      val filterRdd: RDD[String] = rdd.filter(message =>
        URLFilter.URLfilter(message, filterRuleList)
      )
      filterRdd.foreach(println)

      /**
        * 3 数据脱敏功能
        */

      val tuominRdd: RDD[String] = filterRdd.map(message => {
        //3-1手机号码脱敏,对每条数据进行脱敏
        var encryptedPhone: String = EncryptedData.encryptedPhone(message)

        //3-2身份证号码脱敏
        encryptedPhone = EncryptedData.encryptedID(encryptedPhone)
        encryptedPhone
      })
      tuominRdd.foreach(println)

      //4数据拆分功能

      //5数据分类功能

      //5-1 单程/ 往返
      //5-2飞行类 型!与操作类型

      //6数据的解析
      //6-1 查询类数据的解析
      //6-2预定类数据的解析

      //7历史爬虫判断I

      //8数据结构化

      //9数据推送
      //9-1查询类数据的推送
      //9-2预定类数据的推送

      //10系统监控功能(数据预处理的监控功能)

    })

  }
}
