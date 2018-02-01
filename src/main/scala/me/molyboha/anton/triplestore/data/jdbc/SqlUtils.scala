package me.molyboha.anton.triplestore.data.jdbc

import java.sql.ResultSet

object SqlUtils {
  trait ResultGetter[T] {
    def get(set: ResultSet, fieldIndex: Int): T
  }
  private abstract class RowIterator[R](set: ResultSet) extends Iterator[R] {
    protected def readRow: R
    private def step(): Option[R] = {
      if( set.next() ) {
        Some(readRow)
      }
      else {
        None
      }
    }
    private var curRow: Option[R] = step()
    override def hasNext: Boolean = curRow.nonEmpty
    override def next(): R = {
      val res = curRow.get
      curRow = step()
      res
    }
  }
  implicit object ResultGetterInt extends ResultGetter[Int] {
    override def get(set: ResultSet, fieldIndex: Int): Int = set.getInt(fieldIndex)
  }
  implicit object ResultGetterString extends ResultGetter[String] {
    override def get(set: ResultSet, fieldIndex: Int): String = set.getString(fieldIndex)
  }
  class ResultGetterOption[T](getter: ResultGetter[T]) extends ResultGetter[Option[T]] {
    override def get(set: ResultSet, fieldIndex: Int): Option[T] = {
      val res = getter.get(set, fieldIndex)
      if( set.wasNull() ) None else Some(res)
    }
  }
  implicit def resultGetterNullable[T](implicit getter: ResultGetter[T]): ResultGetter[Option[T]] = {
    new ResultGetterOption[T](getter)
  }
  def iterate[R, T1](set: ResultSet, mapper: (T1) => R)(implicit getter1: ResultGetter[T1]): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(getter1.get(set, 1))
    }
  }
  def iterate[R, T1, T2](set: ResultSet, mapper: (T1, T2) => R)
                        (implicit getter1: ResultGetter[T1],
                         getter2: ResultGetter[T2]): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(getter1.get(set, 1), getter2.get(set, 2))
    }
  }
  def iterate[R, T1, T2, T3](set: ResultSet, mapper: (T1, T2, T3) => R)
                        (implicit getter1: ResultGetter[T1],
                         getter2: ResultGetter[T2],
                         getter3: ResultGetter[T3]): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(getter1.get(set, 1),
                                                 getter2.get(set, 2),
                                                 getter3.get(set, 3))
    }
  }
  def iterate[R, T1, T2, T3, T4](set: ResultSet,
                                 mapper: (T1, T2, T3, T4) => R)
                                (implicit
                                 getter1: ResultGetter[T1],
                                 getter2: ResultGetter[T2],
                                 getter3: ResultGetter[T3],
                                 getter4: ResultGetter[T4]): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(getter1.get(set, 1),
        getter2.get(set, 2),
        getter3.get(set, 3),
        getter4.get(set, 4)
      )
    }
  }
  def iterate[R, T1, T2, T3, T4, T5](set: ResultSet,
                                     mapper: (T1, T2, T3, T4, T5) => R)
                                    (implicit
                                     getter1: ResultGetter[T1],
                                     getter2: ResultGetter[T2],
                                     getter3: ResultGetter[T3],
                                     getter4: ResultGetter[T4],
                                     getter5: ResultGetter[T5]): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(getter1.get(set, 1),
        getter2.get(set, 2),
        getter3.get(set, 3),
        getter4.get(set, 4),
        getter5.get(set, 5)
      )
    }
  }
  def iterate[R, T1, T2, T3, T4, T5, T6](set: ResultSet,
                                     mapper: (T1, T2, T3, T4, T5, T6) => R)
                                    (implicit
                                     getter1: ResultGetter[T1],
                                     getter2: ResultGetter[T2],
                                     getter3: ResultGetter[T3],
                                     getter4: ResultGetter[T4],
                                     getter5: ResultGetter[T5],
                                     getter6: ResultGetter[T6]
                                    ): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(
        getter1.get(set, 1),
        getter2.get(set, 2),
        getter3.get(set, 3),
        getter4.get(set, 4),
        getter5.get(set, 5),
        getter6.get(set, 6)
      )
    }
  }
  def iterate[R, T1, T2, T3, T4, T5, T6, T7](set: ResultSet,
                                         mapper: (T1, T2, T3, T4, T5, T6, T7) => R)
                                        (implicit
                                         getter1: ResultGetter[T1],
                                         getter2: ResultGetter[T2],
                                         getter3: ResultGetter[T3],
                                         getter4: ResultGetter[T4],
                                         getter5: ResultGetter[T5],
                                         getter6: ResultGetter[T6],
                                         getter7: ResultGetter[T7]
                                        ): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(
        getter1.get(set, 1),
        getter2.get(set, 2),
        getter3.get(set, 3),
        getter4.get(set, 4),
        getter5.get(set, 5),
        getter6.get(set, 6),
        getter7.get(set, 7)
      )
    }
  }
  def iterate[R, T1, T2, T3, T4, T5, T6, T7, T8](set: ResultSet,
                                             mapper: (T1, T2, T3, T4, T5, T6, T7, T8) => R)
                                            (implicit
                                             getter1: ResultGetter[T1],
                                             getter2: ResultGetter[T2],
                                             getter3: ResultGetter[T3],
                                             getter4: ResultGetter[T4],
                                             getter5: ResultGetter[T5],
                                             getter6: ResultGetter[T6],
                                             getter7: ResultGetter[T7],
                                             getter8: ResultGetter[T8]
                                            ): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(
        getter1.get(set, 1),
        getter2.get(set, 2),
        getter3.get(set, 3),
        getter4.get(set, 4),
        getter5.get(set, 5),
        getter6.get(set, 6),
        getter7.get(set, 7),
        getter8.get(set, 8)
      )
    }
  }
  def iterate[R, T1, T2, T3, T4, T5, T6, T7, T8, T9](set: ResultSet,
                                                 mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R)
                                                (implicit
                                                 getter1: ResultGetter[T1],
                                                 getter2: ResultGetter[T2],
                                                 getter3: ResultGetter[T3],
                                                 getter4: ResultGetter[T4],
                                                 getter5: ResultGetter[T5],
                                                 getter6: ResultGetter[T6],
                                                 getter7: ResultGetter[T7],
                                                 getter8: ResultGetter[T8],
                                                 getter9: ResultGetter[T9]
                                                ): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(
        getter1.get(set, 1),
        getter2.get(set, 2),
        getter3.get(set, 3),
        getter4.get(set, 4),
        getter5.get(set, 5),
        getter6.get(set, 6),
        getter7.get(set, 7),
        getter8.get(set, 8),
        getter9.get(set, 9)
      )
    }
  }
  def iterate[R, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](set: ResultSet,
                                                     mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R)
                                                    (implicit
                                                     getter1: ResultGetter[T1],
                                                     getter2: ResultGetter[T2],
                                                     getter3: ResultGetter[T3],
                                                     getter4: ResultGetter[T4],
                                                     getter5: ResultGetter[T5],
                                                     getter6: ResultGetter[T6],
                                                     getter7: ResultGetter[T7],
                                                     getter8: ResultGetter[T8],
                                                     getter9: ResultGetter[T9],
                                                     getter10: ResultGetter[T10]
                                                    ): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(
        getter1.get(set, 1),
        getter2.get(set, 2),
        getter3.get(set, 3),
        getter4.get(set, 4),
        getter5.get(set, 5),
        getter6.get(set, 6),
        getter7.get(set, 7),
        getter8.get(set, 8),
        getter9.get(set, 9),
        getter10.get(set, 10)
      )
    }
  }
  def iterate[R, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](set: ResultSet,
                                                          mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R)
                                                         (implicit
                                                          getter1: ResultGetter[T1],
                                                          getter2: ResultGetter[T2],
                                                          getter3: ResultGetter[T3],
                                                          getter4: ResultGetter[T4],
                                                          getter5: ResultGetter[T5],
                                                          getter6: ResultGetter[T6],
                                                          getter7: ResultGetter[T7],
                                                          getter8: ResultGetter[T8],
                                                          getter9: ResultGetter[T9],
                                                          getter10: ResultGetter[T10],
                                                          getter11: ResultGetter[T11]
                                                         ): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(
        getter1.get(set, 1),
        getter2.get(set, 2),
        getter3.get(set, 3),
        getter4.get(set, 4),
        getter5.get(set, 5),
        getter6.get(set, 6),
        getter7.get(set, 7),
        getter8.get(set, 8),
        getter9.get(set, 9),
        getter10.get(set, 10),
        getter11.get(set, 11)
      )
    }
  }
  def iterate[R, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](set: ResultSet,
                                                               mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R)
                                                              (implicit
                                                               getter1: ResultGetter[T1],
                                                               getter2: ResultGetter[T2],
                                                               getter3: ResultGetter[T3],
                                                               getter4: ResultGetter[T4],
                                                               getter5: ResultGetter[T5],
                                                               getter6: ResultGetter[T6],
                                                               getter7: ResultGetter[T7],
                                                               getter8: ResultGetter[T8],
                                                               getter9: ResultGetter[T9],
                                                               getter10: ResultGetter[T10],
                                                               getter11: ResultGetter[T11],
                                                               getter12: ResultGetter[T12]
                                                              ): Iterator[R] = {
    new RowIterator[R](set) {
      override protected def readRow: R = mapper(
        getter1.get(set, 1),
        getter2.get(set, 2),
        getter3.get(set, 3),
        getter4.get(set, 4),
        getter5.get(set, 5),
        getter6.get(set, 6),
        getter7.get(set, 7),
        getter8.get(set, 8),
        getter9.get(set, 9),
        getter10.get(set, 10),
        getter11.get(set, 11),
        getter12.get(set, 12)
      )
    }
  }
}
