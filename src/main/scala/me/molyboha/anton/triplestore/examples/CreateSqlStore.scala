package me.molyboha.anton.triplestore.examples

import me.molyboha.anton.triplestore.data.jdbc.{SqlExpandableStore, SqlStore}

/**
  * Create the necessary database tables.
  *
  * Assumes that a local mysql database "triplestore" exists but is empty,
  * and the user "triplestore" without password has the privileges to
  * create tables and manipulate data within the database.
  */
object CreateSqlStore {
  def main(args: Array[String]): Unit = {
    val sqlDrivers = java.sql.DriverManager.getDrivers
    while(sqlDrivers.hasMoreElements) {
      val driver = sqlDrivers.nextElement()
      Console.println(driver.getClass.getCanonicalName)
    }
    Console.println("===")

    val connectionString = "jdbc:mysql://localhost:3306/triplestore?user=triplestore"
    val connection = java.sql.DriverManager.getConnection(connectionString)
    SqlStore.createTables(connection)

    val store = new SqlExpandableStore(connectionString)
    val is_a = store.notion("is a")
    val verb = store.notion("verb")
    store.relation(is_a, is_a, verb)
    SqlStore.checkIntegrity(connection)

    for( node <- store.notions ) {
      Console.println( node.toString )
    }
    Console.println("---")

    for( rel <- store.relations ) {
      Console.println(rel.subject.toString + " _ " + rel.verb.toString + " _ " + rel.obj.toString)
    }
  }

}
