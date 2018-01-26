package me.molyboha.anton.triplestore.data.model

trait Factory
{
  def notion(name: String): Notion
  def relation(subject: Notion, verb: Notion, obj: Notion, name: Option[String]): Relation
  def relation(subject: Notion, verb: Notion, obj: Notion): Relation = relation(subject, verb, obj, None)
  def relation(subject: Notion, verb: Notion, obj: Notion, name: String): Relation = relation(subject, verb, obj, Some(name))
}
