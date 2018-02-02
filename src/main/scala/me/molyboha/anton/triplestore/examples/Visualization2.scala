package me.molyboha.anton.triplestore.examples

import java.awt.Frame
import java.awt.event.{WindowAdapter, WindowEvent}

import me.molyboha.anton.triplestore.data.InMemoryStore
import me.molyboha.anton.triplestore.visual.awt.{GraphView, GraphView2}

object Visualization2 extends App
{
  val store = new InMemoryStore[String]
  val subj = store.notion("Scala")
  val verb = store.notion("is")
  val obj = store.notion("Good")
  val rel = store.relation(subj, verb, obj)

  val frame = new Frame("Triplestore")
  val view = new GraphView2[String]
  view.addNode(subj, 50, 90)
  view.addNode(verb, 150, 50)
  view.addNode(obj, 250, 70)
  view.addRelation(rel, 150, 110)
  frame.add(view)
  frame.addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = frame.dispose()
  })
  frame.setVisible(true)
  frame.setSize(300, 200)
}
