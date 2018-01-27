package me.molyboha.anton.triplestore.data.model

trait Store[+T]
{
  def notions: Iterator[Notion[T]]
  def relations: Iterator[Relation[T]] = notions.flatMap( _.asRelation )
}
