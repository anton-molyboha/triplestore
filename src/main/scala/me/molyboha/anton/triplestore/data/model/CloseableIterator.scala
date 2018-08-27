package me.molyboha.anton.triplestore.data.model

import java.io.Closeable

import scala.collection.GenTraversableOnce

trait CloseableIterator[+T] extends Iterator[T] with Closeable {
  import CloseableIterator._

  protected def hasNextImpl: Boolean
  protected def nextImpl(): T
  protected def onClose(): Unit

  private var isOpen = true

  override def hasNext: Boolean = {
    if(isOpen) {
      val res = hasNextImpl
      if( !res ) {
        close()
      }
      res
    } else {
      false
    }
  }

  override def next(): T = {
    if( isOpen ) {
      nextImpl()
    } else {
      throw new IllegalStateException("Iterator has been closed")
    }
  }

  override def close(): Unit = {
    if( isOpen ) {
      isOpen = false
      onClose()
    }
  }

  private def closeBoth[B](that: CloseableIterator[B])(): Unit = {
    try{
      close()
    } finally {
      that.close()
    }
  }

  override def seq: CloseableIterator[T] = this

  //// Methods producing a value
  private def thenClose[A](value: => A): A = { try { value } finally { close() } }
  override def foreach[U](f: T => U): Unit = thenClose(super.foreach(f))
  override def find(p: T => Boolean): Option[T] = thenClose(super.find(p))
  override def exists(p: T => Boolean): Boolean = thenClose(super.exists(p))
  override def contains(elem: Any): Boolean = thenClose(super.contains(elem))
  override def forall(p: T => Boolean): Boolean = thenClose(super.forall(p))
  override def indexWhere(p: T => Boolean): Int = thenClose(super.indexWhere(p))
  override def indexWhere(p: T => Boolean, from: Int): Int = thenClose(super.indexWhere(p, from))
  override def indexOf[B >: T](elem: B): Int = thenClose(super.indexOf(elem))
  override def indexOf[B >: T](elem: B, from: Int): Int = thenClose(super.indexOf(elem, from))
  override def sameElements(that: Iterator[_]): Boolean = thenClose(super.sameElements(that))
  def sameElements(that: CloseableIterator[_]): Boolean = {
    try {
      super.sameElements(that)
    } finally {
      close()
      that.close()
    }
  }
  override def corresponds[B](that: GenTraversableOnce[B])(p: (T, B) => Boolean): Boolean = thenClose(super.corresponds(that)(p))
  def corresponds[B](that: CloseableIterator[B])(p: (T, B) => Boolean): Boolean = {
    try {
      super.corresponds(that)(p)
    } finally {
      close()
      that.close()
    }
  }

  //// Methods producing an iterator
  override protected def sliceIterator(from: Int, until: Int): CloseableIterator[T] = wrap(super.sliceIterator(from, until), close)
  override def take(n: Int): CloseableIterator[T] = sliceIterator(0, {if(n > 0) n else 0})
  override def drop(n: Int): CloseableIterator[T] = sliceIterator({if(n > 0) n else 0}, -1)
  override def slice(from: Int, until: Int): CloseableIterator[T] = sliceIterator({if(from > 0) from else 0}, {if(until > 0) until else 0})
  override def map[B](f: T => B): CloseableIterator[B] = wrap(super.map(f), close)
  override def ++[B >: T](that: => GenTraversableOnce[B]): CloseableIterator[B] = wrap(super.++(that), close)
  def ++[B >: T](that: CloseableIterator[B]): CloseableIterator[B] = wrap(super.++(that), closeBoth(that))
  override def flatMap[B](f: T => GenTraversableOnce[B]): CloseableIterator[B] = wrap(super.flatMap(f), close)
  //TODO: flatMap with CloseableIterator
  override def filter(p: T => Boolean): CloseableIterator[T] = wrap(super.filter(p), close)
  override def withFilter(p: T => Boolean): CloseableIterator[T] = wrap(super.withFilter(p), close)
  override def filterNot(p: T => Boolean): CloseableIterator[T] = wrap(super.filterNot(p), close)
  override def collect[B](pf: PartialFunction[T, B]): CloseableIterator[B] = wrap(super.collect(pf), close)
  override def scanLeft[B](z: B)(op: (B, T) => B): CloseableIterator[B] = wrap(super.scanLeft(z)(op), close)
  override def scanRight[B](z: B)(op: (T, B) => B): CloseableIterator[B] = wrap(super.scanRight(z)(op), close)
  override def takeWhile(p: T => Boolean): CloseableIterator[T] = wrap(super.takeWhile(p), close)
  override def dropWhile(p: T => Boolean): CloseableIterator[T] = wrap(super.dropWhile(p), close)
  override def zip[B](that: Iterator[B]): CloseableIterator[(T, B)] = wrap(super.zip(that), close)
  def zip[B](that: CloseableIterator[B]): CloseableIterator[(T, B)] = wrap(super.zip(that), closeBoth(that))
  override def padTo[A1 >: T](len: Int, elem: A1): CloseableIterator[A1] = wrap(super.padTo(len, elem), close)
  override def zipWithIndex: CloseableIterator[(T, Int)] = wrap(super.zipWithIndex, close)
  override def zipAll[B, A1 >: T, B1 >: B](that: Iterator[B], thisElem: A1, thatElem: B1): CloseableIterator[(A1, B1)] = {
    wrap(super.zipAll(that, thisElem, thatElem), close)
  }
  def zipAll[B, A1 >: T, B1 >: B](that: CloseableIterator[B], thisElem: A1, thatElem: B1): CloseableIterator[(A1, B1)] = {
    wrap(super.zipAll(that, thisElem, thatElem), closeBoth(that))
  }

  override def buffered: CloseableIterator[T] with BufferedIterator[T] = new CloseableIterator[T] with BufferedIterator[T] {
    private val it = super.buffered
    override protected def hasNextImpl: Boolean = it.hasNext
    override protected def nextImpl(): T = it.next()
    override protected def onClose(): Unit = close()
    override def head: T = it.head
  }

  override def patch[B >: T](from: Int, patchElems: Iterator[B], replaced: Int): CloseableIterator[B] = {
    wrap(super.patch(from, patchElems, replaced), close)
  }
  def patch[B >: T](from: Int, patchElems: CloseableIterator[B], replaced: Int): CloseableIterator[B] = {
    wrap(super.patch(from, patchElems, replaced), closeBoth(patchElems))
  }

  override def toIterator: Iterator[T] = toTraversable.toIterator

  private class CountingCloser(count: Int) extends (() => Unit) {
    private var refCount = count
    override def apply(): Unit = {
      refCount -= 1
      if( refCount <= 0 ) close()
    }
  }
  private def countingCloser(count: Int) = new CountingCloser(count)

  override def duplicate: (CloseableIterator[T], CloseableIterator[T]) = {
    val (res1, res2) = super.duplicate
    val closer = countingCloser(2)
    (wrap(res1, closer), wrap(res2, closer))
  }

  override def partition(p: T => Boolean): (CloseableIterator[T], CloseableIterator[T]) = {
    val (res1, res2) = super.partition(p)
    val closer = countingCloser(2)
    (wrap(res1, closer), wrap(res2, closer))
  }

  override def span(p: T => Boolean): (CloseableIterator[T], CloseableIterator[T]) = {
    val (res1, res2) = super.span(p)
    val closer = countingCloser(2)
    (wrap(res1, closer), wrap(res2, closer))
  }

  override def grouped[B >: T](size: Int): CloseableGroupedIterator[B] = new CloseableGroupedIterator[B](this, size, size)
  override def sliding[B >: T](size: Int, step: Int): GroupedIterator[B] = new CloseableGroupedIterator[B](this, size, step)

  class CloseableGroupedIterator[T2 >: T](self: CloseableIterator[T], size: Int, step: Int)
  extends GroupedIterator[T2](self, size, step) with CloseableIterator[List[T2]] {
    override protected def hasNextImpl: Boolean = super.hasNext
    override protected def nextImpl(): List[T2] = super.next()
    override protected def onClose(): Unit = self.close()
  }
}

object CloseableIterator {
  def wrap[T](it: Iterator[T], closeOp: () => Unit): CloseableIterator[T] = new CloseableIterator[T] {
    override protected def hasNextImpl: Boolean = it.hasNext
    override protected def nextImpl(): T = it.next()
    override protected def onClose(): Unit = closeOp()
  }

  implicit def fromIterator[T](it: Iterator[T]): CloseableIterator[T] = new CloseableIterator[T] {
    override protected def hasNextImpl: Boolean = it.hasNext
    override protected def nextImpl(): T = it.next()
    override protected def onClose(): Unit = {}
  }
}
