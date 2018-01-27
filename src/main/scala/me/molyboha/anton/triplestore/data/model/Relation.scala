package me.molyboha.anton.triplestore.data.model

trait Relation[T] extends Notion[T]
{
  val subject: Notion[T]
  val verb: Notion[T]
  val obj: Notion[T]

  override def asRelation: Option[Relation[T]] = Some(this)
}
