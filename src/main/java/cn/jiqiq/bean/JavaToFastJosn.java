package cn.jiqiq.bean;
import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

public class JavaToFastJosn {
   /**
    * java对象转 json字符串
    */

   public static void main(String[] args) {

      //简单java类转json字符串
      User user = new User("dmego", "123456");
      String UserJson = JSON.toJSONString(user);
//      System.out.println("简单java类转json字符串:"+UserJson);

      //List<Object>转json字符串
      User user1 = new User("zhangsan", "123123");
      User user2 = new User("lisi", "321321");
      List<User> users = new ArrayList<User>();
      users.add(user1);
      users.add(user2);
      String ListUserJson = JSON.toJSONString(users);
//      System.out.println("List<Object>转json字符串:"+ListUserJson);

      //复杂java类转json字符串
      UserGroup userGroup = new UserGroup("userGroup", users);
      String userGroupJson = JSON.toJSONString(userGroup);
      System.out.println("复杂java类转json字符串:"+userGroupJson);

   }

}
