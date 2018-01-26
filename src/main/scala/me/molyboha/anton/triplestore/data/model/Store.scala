package me.molyboha.anton.triplestore.data.model

trait Store
{
  def notions: Iterator[Notion]
  def relations: Iterator[Relation] = notions.flatMap( _.asRelation )
}
