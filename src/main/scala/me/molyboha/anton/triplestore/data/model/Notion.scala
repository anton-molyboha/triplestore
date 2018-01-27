package me.molyboha.anton.triplestore.data.model

trait Notion[T]
{
  val data: Option[T]
  def asRelation: Option[Relation[T]] = None
  def subjectOf: Iterator[Relation[T]]
  def verbOf: Iterator[Relation[T]]
  def objOf: Iterator[Relation[T]]
  override def toString: String = data.map(_.toString).getOrElse("???")
}
