package me.molyboha.anton.triplestore.data.jdbc

import me.molyboha.anton.triplestore.data.model
import me.molyboha.anton.triplestore.data.model.{Notion, Relation}

class SqlExpandableStore(connectionString: String) extends SqlStore(connectionString) with model.Factory[String] {
  private val lastIdStatement = connection.prepareStatement("SELECT LAST_INSERT_ID()")
  private def lastId(): Int = {
    val resultSet = lastIdStatement.executeQuery()
    resultSet.next()
    val res = resultSet.getInt(1)
    resultSet.close()
    res
  }

  private val insertNotionStatement = connection.prepareStatement("INSERT INTO notion (name) VALUES (?)")
  override def notion(data: Option[String]): Notion[String] = {
    data match {
      case Some(value) => insertNotionStatement.setString (1, value)
      case None => insertNotionStatement.setNull(1, java.sql.Types.VARCHAR)
    }
    insertNotionStatement.executeUpdate()
    notionById(lastId())
  }

  private val insertRelationStatement = connection.prepareStatement(
    "INSERT INTO relation (subject, verb, object, asnotion) VALUES (?, ?, ?, ?)"
  )
  private val updateNotionAsRelationStatement = connection.prepareStatement(
    "UPDATE notion SET asrelation = ? WHERE id = ?"
  )
  override def relation(subject: Notion[String], verb: Notion[String], obj: Notion[String], data: Option[String]): Relation[String] = {
    val sqlSubject = subject.asInstanceOf[SqlNotion]
    val sqlVerb = verb.asInstanceOf[SqlNotion]
    val sqlObj = obj.asInstanceOf[SqlNotion]
    val asNotion = notion(data).asInstanceOf[SqlNotion]
    insertRelationStatement.setInt(1, sqlSubject.id)
    insertRelationStatement.setInt(2, sqlVerb.id)
    insertRelationStatement.setInt(3, sqlObj.id)
    insertRelationStatement.setInt(4, asNotion.id)
    insertRelationStatement.executeUpdate()
    val id = lastId()
    updateNotionAsRelationStatement.setInt(1, id)
    updateNotionAsRelationStatement.setInt(2, asNotion.id)
    updateNotionAsRelationStatement.executeUpdate()
    new SqlRelation(id, sqlSubject, sqlVerb, sqlObj, asNotion.id, asNotion.data)
  }
}
