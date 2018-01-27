package me.molyboha.anton.triplestore.data.model

trait Factory[T]
{
  def notion(data: T): Notion[T]
  def relation(subject: Notion[T], verb: Notion[T], obj: Notion[T], data: Option[T]): Relation[T]
  def relation(subject: Notion[T], verb: Notion[T], obj: Notion[T]): Relation[T] =
    relation(subject, verb, obj, None)
  def relation(subject: Notion[T], verb: Notion[T], obj: Notion[T], data: T): Relation[T] =
    relation(subject, verb, obj, Some(data))
}
