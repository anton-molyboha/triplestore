package me.molyboha.anton.triplestore.examples

import me.molyboha.anton.triplestore.data.jdbc.SqlStore

object DumpSqlStore {
  def main(args: Array[String]): Unit = {
    val sqlDrivers = java.sql.DriverManager.getDrivers
    while(sqlDrivers.hasMoreElements) {
      val driver = sqlDrivers.nextElement()
      Console.println(driver.getClass.getCanonicalName)
    }
    Console.println("===")

    val store = new SqlStore("jdbc:mysql://localhost:3306/triplestore_test?user=triplestore")
    for( node <- store.notions ) {
      Console.println( node.toString )
    }
    Console.println("---")

    for( rel <- store.relations ) {
      Console.println(rel.subject.toString + " _ " + rel.verb.toString + " _ " + rel.obj.toString)
    }
  }

}
