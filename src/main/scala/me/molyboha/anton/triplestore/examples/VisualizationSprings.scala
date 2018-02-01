package me.molyboha.anton.triplestore.examples

import java.awt.Frame
import java.awt.event.{WindowAdapter, WindowEvent}

import me.molyboha.anton.triplestore.data.InMemoryStore
import me.molyboha.anton.triplestore.visual.awt.GraphView
import me.molyboha.anton.triplestore.visual.awt.layout.SpringAutoLayout

object VisualizationSprings extends App
{
  val store = new InMemoryStore[String]
  val likes = store.notion("Likes")
  val jas = store.notion("Jas")
  val milk = store.notion("Milk")
  store.relation(jas, likes, milk)
  val george = store.notion("George")
  val leek = store.notion("Leek")
  store.relation(george, likes, leek)
  val sean = store.notion("Sean")
  val daughter = store.notion("Daughter of")
  store.relation(jas, daughter, sean)


  val frame = new Frame("Triplestore")
  val view = new GraphView[String]
  frame.add(view)
  frame.addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = frame.dispose()
  })
  frame.setVisible(true)
  frame.setSize(800, 500)

  SpringAutoLayout(store.notions.filter(_.asRelation.isEmpty).toIterable, view)
}
