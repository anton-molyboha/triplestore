package me.molyboha.anton.triplestore.data.model

trait Notion[+T]
{
  val data: Option[T]
  def asRelation: Option[Relation[T]] = None
  def subjectOf: CloseableIterator[Relation[T]]
  def verbOf: CloseableIterator[Relation[T]]
  def objOf: CloseableIterator[Relation[T]]
  override def toString: String = data.map(_.toString).getOrElse("???")
}
