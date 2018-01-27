package me.molyboha.anton.triplestore.visual.awt

import java.awt.{Component, Graphics}

import me.molyboha.anton.triplestore.data.model.Notion

// GUI has three components: visualization, layout and control
class GraphView extends Component
{
  import GraphView._
  class Node(val notion: Notion, x0: Double, y0: Double)
  {
    private var xx = x0
    private var yy = y0

    def x: Double = xx
    def y: Double = yy

    def x_=(v: Double): Unit = {
      xx = v
      GraphView.this.invalidate()
    }

    def y_=(v: Double): Unit = {
      yy = v
      GraphView.this.invalidate()
    }
  }

  private var nodes: Map[Notion, Node] = Map()

  def addNode(notion: Notion, x: Double, y: Double): Node = {
    val res = new Node(notion, x, y)
    nodes += notion -> res
    invalidate()
    res
  }

  override def paint(g: Graphics): Unit = {
    super.paint(g)
    for( node <- nodes.values ) {
      val str = node.notion.toString
      g.drawOval((node.x - 10 * str.length).toInt, (node.y - 10).toInt, 20 * str.length, 20)
      g.drawString(str, (node.x - 5 * str.length()).toInt, (node.y + 5).toInt)
    }
  }
}

object GraphView
{
}
