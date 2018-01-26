package me.molyboha.anton.triplestore.data
import me.molyboha.anton.triplestore.data.model

class InMemoryStore extends model.Factory with model.Store
{
  import InMemoryStore._

  private var data: List[Notion] = List()

  override def notion(name: String): model.Notion = {
    val res = new Notion(this, Some(name))
    data ::= res
    res
  }

  override def relation(subject: model.Notion, verb: model.Notion, obj: model.Notion, name: Option[String]): model.Relation = {
    val theSubject = subject.asInstanceOf[Notion]
    val theVerb = verb.asInstanceOf[Notion]
    val theObj = obj.asInstanceOf[Notion]
    if( theSubject.store != this ) throw new IllegalArgumentException("Notion is from a different Store")
    if( theVerb.store != this ) throw new IllegalArgumentException("Notion is from a different Store")
    if( theObj.store != this ) throw new IllegalArgumentException("Notion is from a different Store")
    val res = new Relation(this, theSubject, theVerb, theObj, name)
    data ::= res
    res
  }

  override def notions: Iterator[model.Notion] = data.iterator
}

object InMemoryStore
{
  private class Notion(val store: InMemoryStore, override val name: Option[String]) extends model.Notion
  {
    var subjectOfLst: List[Relation] = List()
    var verbOfLst: List[Relation] = List()
    var objOfLst: List[Relation] = List()

    override def subjectOf: Iterator[model.Relation] = subjectOfLst.iterator

    override def verbOf: Iterator[model.Relation] = verbOfLst.iterator

    override def objOf: Iterator[model.Relation] = objOfLst.iterator
  }

  private class Relation(store: InMemoryStore, override val subject: Notion, override val verb: Notion, override val obj: Notion, override val name: Option[String]) extends Notion(store, name) with model.Relation
  {
    subject.subjectOfLst ::= this
    verb.verbOfLst ::= this
    obj.objOfLst ::= this
  }
}
