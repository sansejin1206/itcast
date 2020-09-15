package cn.jiqiq.bean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FastJsonTest {
   public static void main(String[] args) {
// Object parse(String text); //
// JSONObject parseObject(String text)； // 把JSON文本parse成JSONObject
// <T> T parseObject(String text, Class<T> clazz); // 把JSON文本parse为JavaBean
// JSONArray parseArray(String text); // 把JSON文本parse成JSONArray
// <T> List<T> parseArray(String text, Class<T> clazz); //把JSON文本parse成JavaBean集合
// String toJSONString(Object object); // 将JavaBean序列化为JSON文本
// String toJSONString(Object object, boolean prettyFormat); // 将JavaBean序列化为带格式的JSON文本
// Object toJSON(Object javaObject); //将JavaBean转换为JSONObject或者JSONArray。

//-----------------------------JSON转对象----------------------------------------------------
      /**
       *
       * parse从语法上分析
       * json字符串转List<Object>对象
       * 字符串：[{"password":"123123","username":"zhangsan"},{"password":"321321","username":"lisi"}]
       */
      String jsonStr2 = "[{'name':'zhangsan','age':18}]";
      List<Student> stu = JSON.parseArray(jsonStr2, Student.class);
      JSONArray stu2 = JSON.parseArray(jsonStr2);
//      System.out.println(stu2);

      /**
       * 把JSON文本parse为JSONObject或者JSONArray
       */
      String str = "{lisan:18}";
      Object parse = JSON.parse(str);
//      System.out.println(parse);
      /**
       * parseObject
       * 把JSON文本parse为JavaBean
       */
      String str2 = "{'name':'lisan','age':18}";
      Student student = JSON.parseObject(str2, Student.class);
//      System.out.println(student.getName());

      /**
       * JSON.toJavaObject()
       * 把json转为java对像
       */
      Student student1 = new Student("lisan", 18);
      //转为json字符串 把json字符串转为json
      JSONObject parse2 = (JSONObject) JSON.toJSON(student1);
      Student student2 = JSON.toJavaObject(parse2, Student.class);
//      System.out.println(student2);
      /**
       * 把json转为java对象f\m
       */
      String str4="{'name':'lisan','age':18}";
      JSONObject jsonObject = JSON.parseObject(str4);
      Student userDemo = JSON.toJavaObject(jsonObject, Student.class);
//      System.out.println(userDemo);
//-----------------------------对象转JSON--------------------------------------------

   /**
    * toJSON
    * <h1>把对象转json字符串</h1>
    */
      Student student3 = new Student("lisan", 18);
      JSONObject parse3 = (JSONObject) JSON.toJSON(student3);
//      System.out.println(parse3);

      /**
       * 创建json对象
       * 把map转为json字符串
       */
      HashMap<String, Object> map = new HashMap<>();
      map.put("lisan",18);
      JSONObject js=new JSONObject(map);

   }
}
