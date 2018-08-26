package me.molyboha.anton.triplestore.data.model

trait Store[+T]
{
  def notions: CloseableIterator[Notion[T]]
  def relations: CloseableIterator[Relation[T]] = notions.flatMap( _.asRelation )
}
