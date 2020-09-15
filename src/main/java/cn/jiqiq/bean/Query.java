package cn.jiqiq.bean;

import java.util.LinkedHashMap;
import java.util.List;

public class Query {
   private String id;
   private String key;
   private String tableName;
   private String className;
   private List<LinkedHashMap<String, Object>> column;
   private List<Column> columnList;

   public String getId() {
      return id;
   }
   public void setId(String id) {
      this.id = id;
   }
   //这里省略部分getter与setter方法
   public List<LinkedHashMap<String, Object>> getColumn() {
      return column;
   }
   public void setColumn(List<LinkedHashMap<String, Object>> column) {
      this.column = column;
   }
   public List<Column> getColumnList() {
      return columnList;
   }
   public void setColumnList(List<Column> columnList) {
      this.columnList = columnList;
   }

   @Override
   public String toString() {
      return "Query{" +
              "id='" + id + '\'' +
              ", key='" + key + '\'' +
              ", tableName='" + tableName + '\'' +
              ", className='" + className + '\'' +
              ", column=" + column +
              ", columnList=" + columnList +
              '}';
   }
}
