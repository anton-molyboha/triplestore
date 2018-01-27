package me.molyboha.anton.triplestore.data
import me.molyboha.anton.triplestore.data.model

class InMemoryStore[T] extends model.Factory[T] with model.Store[T]
{
  import InMemoryStore._

  private var data: List[Notion[T]] = List()

  override def notion(dat: T): model.Notion[T] = {
    val res = new Notion(this, Some(dat))
    data ::= res
    res
  }

  override def relation(subject: model.Notion[T], verb: model.Notion[T], obj: model.Notion[T], dat: Option[T]): model.Relation[T] = {
    val theSubject = subject.asInstanceOf[Notion[T]]
    val theVerb = verb.asInstanceOf[Notion[T]]
    val theObj = obj.asInstanceOf[Notion[T]]
    if( theSubject.store != this ) throw new IllegalArgumentException("Notion is from a different Store")
    if( theVerb.store != this ) throw new IllegalArgumentException("Notion is from a different Store")
    if( theObj.store != this ) throw new IllegalArgumentException("Notion is from a different Store")
    val res = new Relation[T](this, theSubject, theVerb, theObj, dat)
    data ::= res
    res
  }

  override def notions: Iterator[model.Notion[T]] = data.iterator
}

object InMemoryStore
{
  private class Notion[T](val store: InMemoryStore[T], override val data: Option[T]) extends model.Notion[T]
  {
    var subjectOfLst: List[Relation[T]] = List()
    var verbOfLst: List[Relation[T]] = List()
    var objOfLst: List[Relation[T]] = List()

    override def subjectOf: Iterator[model.Relation[T]] = subjectOfLst.iterator

    override def verbOf: Iterator[model.Relation[T]] = verbOfLst.iterator

    override def objOf: Iterator[model.Relation[T]] = objOfLst.iterator
  }

  private class Relation[T](store: InMemoryStore[T], override val subject: Notion[T], override val verb: Notion[T], override val obj: Notion[T], override val data: Option[T]) extends Notion[T](store, data) with model.Relation[T]
  {
    subject.subjectOfLst ::= this
    verb.verbOfLst ::= this
    obj.objOfLst ::= this
  }
}
