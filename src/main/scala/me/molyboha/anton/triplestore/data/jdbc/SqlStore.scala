package me.molyboha.anton.triplestore.data.jdbc

import me.molyboha.anton.triplestore.data.model.{CloseableIterator, Notion, Relation, Store}

class SqlStore(connectionString: String) extends Store[String] {
  //protected val connection: java.sql.Connection = java.sql.DriverManager.getConnection("jdbc:mariadb://localhost:3306/triplestore_test?user=triplestore");
  protected val connection: java.sql.Connection = java.sql.DriverManager.getConnection(connectionString)

  protected class SqlNotion(val id: Int, val name: Option[String], val asrelation: Option[Int]) extends Notion[String] {
    override val data: Option[String] = name
    override def subjectOf: CloseableIterator[Relation[String]] = {
      val statement = connection.prepareStatement(relationSql + " AND relation.subject = ?")
      statement.setInt(1, id)
      val sqlres = statement.executeQuery()
      SqlUtils.iterate(sqlres, relationMapper _)
    }
    override def verbOf: CloseableIterator[Relation[String]] = {
      val statement = connection.prepareStatement(relationSql + " AND relation.verb = ?")
      statement.setInt(1, id)
      val sqlres = statement.executeQuery()
      SqlUtils.iterate(sqlres, relationMapper _)
    }
    override def objOf: CloseableIterator[Relation[String]] = {
      val statement = connection.prepareStatement(relationSql + " AND relation.object = ?")
      statement.setInt(1, id)
      val sqlres = statement.executeQuery()
      SqlUtils.iterate(sqlres, relationMapper _)
    }
    override def asRelation: Option[Relation[String]] = asrelation.map((relid) => {
      val statement = connection.prepareStatement(relationSql + " AND relation.id = ?")
      statement.setInt(1, relid)
      val sqlres = statement.executeQuery()
      SqlUtils.iterate(sqlres, relationMapper _).next()
    })

    override def equals(other: scala.Any): Boolean = other match {
      case that: SqlNotion => that.id == id
      case _ => false
    }
    override def hashCode(): Int = id
  }
  protected object SqlNotion {
    def apply(id: Int, name: Option[String], asrelation: Option[Int]): SqlNotion = {
      new SqlNotion(id, name, asrelation)
    }
  }
  protected class SqlRelation(val relid: Int,
                              override val subject: SqlNotion,
                              override val verb: SqlNotion,
                              override val obj: SqlNotion,
                              notionId: Int,
                              notionName: Option[String])
    extends SqlNotion(notionId, notionName, Some(relid))
    with Relation[String] {
  }

  private val notionByIdStatement = connection.prepareStatement("SELECT id, name, asrelation FROM notion WHERE id = ?")
  protected def notionById(id: Int): SqlNotion = {
    notionByIdStatement.setInt(1, id)
    val resultSet = notionByIdStatement.executeQuery()
    val it = SqlUtils.iterate(resultSet, SqlNotion.apply _)
    try {
      it.next()
    } finally {
      it.close()
    }
  }

  private val relationSql = "SELECT relation.id, subj.id, subj.name, subj.asrelation, " +
                            "verb.id, verb.name, verb.asrelation, obj.id, obj.name, obj.asrelation, " +
                            "notion.id, notion.name " +
                            "FROM relation, notion AS subj, notion AS verb, notion AS obj, notion " +
                            "WHERE relation.subject = subj.id AND relation.verb = verb.id " +
                            "AND relation.object = obj.id AND notion.id = relation.asnotion "
  private def relationMapper(id: Int, subj_id: Int, subj_name: Option[String], subj_asrel: Option[Int],
                             verb_id: Int, verb_name: Option[String], verb_asrel: Option[Int],
                             obj_id: Int, obj_name: Option[String], obj_asrel: Option[Int],
                             notion_id: Int, notion_name: Option[String]): SqlRelation = {
    new SqlRelation(id,
      new SqlNotion(subj_id, subj_name, subj_asrel),
      new SqlNotion(verb_id, verb_name, verb_asrel),
      new SqlNotion(obj_id, obj_name, obj_asrel),
      notion_id, notion_name)
  }

  override def notions: CloseableIterator[Notion[String]] = {
    val selectAllNotions = connection.prepareStatement("SELECT id, name, asrelation FROM notion")
    val notionSet = selectAllNotions.executeQuery()
    selectAllNotions.closeOnCompletion()
    SqlUtils.iterate(notionSet, SqlNotion.apply _)
  }
}

object SqlStore {
  def checkIntegrity(connection: java.sql.Connection): Unit = {
    val statements = List(
      connection.prepareStatement("SELECT COUNT(relation.id) FROM notion, relation WHERE notion.id = relation.asnotion AND notion.asrelation IS NULL"),
      connection.prepareStatement("SELECT COUNT(relation.id) FROM notion, relation WHERE notion.id = relation.asnotion AND notion.asrelation <> relation.id"),
      connection.prepareStatement("SELECT COUNT(relation.id) FROM notion, relation WHERE notion.asrelation = relation.id AND relation.asnotion <> notion.id")
    )
    for( statement <- statements ) {
      val resultSet = statement.executeQuery()
      assert(resultSet.next())
      assert(resultSet.getInt(1) == 0)
      resultSet.close()
    }
  }

  def createTables(connection: java.sql.Connection): Unit = {
    val statement = connection.createStatement()
    statement.executeUpdate("CREATE TABLE notion " +
      "(id INTEGER AUTO_INCREMENT PRIMARY KEY, name VARCHAR(1024), asrelation INTEGER)")
    statement.executeUpdate("CREATE TABLE relation " +
      "(id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
      "subject INTEGER NOT NULL, " +
      "verb INTEGER NOT NULL, " +
      "object INTEGER NOT NULL, " +
      "asnotion INTEGER NOT NULL, " +
      "FOREIGN KEY (subject) REFERENCES notion (id), " +
      "FOREIGN KEY (verb) REFERENCES notion (id), " +
      "FOREIGN KEY (object) REFERENCES notion (id), " +
      "FOREIGN KEY (asnotion) REFERENCES notion (id) )")
    statement.executeUpdate("ALTER TABLE notion ADD FOREIGN KEY (asrelation) REFERENCES relation (id)")
  }
}
