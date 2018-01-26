package me.molyboha.anton.triplestore.data.model

trait Notion
{
  val name: Option[String]
  def asRelation: Option[Relation] = None
  def subjectOf: Iterator[Relation]
  def verbOf: Iterator[Relation]
  def objOf: Iterator[Relation]
  override def toString: String = name.getOrElse("???")
}
