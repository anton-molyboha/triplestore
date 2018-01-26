package me.molyboha.anton.triplestore.data.model

trait Relation extends Notion
{
  val subject: Notion
  val verb: Notion
  val obj: Notion

  override def asRelation: Option[Relation] = Some(this)
}
