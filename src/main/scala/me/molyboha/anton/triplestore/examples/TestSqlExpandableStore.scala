package me.molyboha.anton.triplestore.examples

import me.molyboha.anton.triplestore.data.jdbc.{SqlExpandableStore, SqlStore}

object TestSqlExpandableStore {
  def main(args: Array[String]): Unit = {
    val sqlDrivers = java.sql.DriverManager.getDrivers
    while(sqlDrivers.hasMoreElements) {
      val driver = sqlDrivers.nextElement()
      Console.println(driver.getClass.getCanonicalName)
    }
    Console.println("===")

    val store = new SqlExpandableStore("jdbc:mysql://localhost:3306/triplestore_test?user=triplestore")
    // Insert
    val test = store.notion("Test")
    val is_a = store.notions.find( _.data.contains("is a") ).get
    store.relation(test, is_a, test)

    // Print
    for( node <- store.notions ) {
      Console.println( node.toString )
    }
    Console.println("---")

    for( rel <- store.relations ) {
      Console.println(rel.subject.toString + " _ " + rel.verb.toString + " _ " + rel.obj.toString)
    }
  }

}
