package me.molyboha.anton.triplestore.visual.awt

import java.awt.{Component, Graphics}

import me.molyboha.anton.triplestore.data.model.{Notion, Relation}

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
  private var edges: Set[Relation] = Set()

  def addNode(notion: Notion, x: Double, y: Double): Node = {
    val res = new Node(notion, x, y)
    nodes += notion -> res
    for( relLst <- Seq(notion.subjectOf, notion.verbOf, notion.objOf) ; rel <- relLst )
    {
      if( shouldIncludeRelation(rel) )
      {
        edges += rel
      }
    }
    invalidate()
    res
  }

  def removeNode(notion: Notion): Unit = {
    nodes -= notion
    edges = edges.filterNot( (rel) => rel.subject == notion || rel.verb == notion || rel.obj == notion )
    invalidate()
  }

  private def shouldIncludeRelation(relation: Relation): Boolean = {
    nodes.contains(relation.subject) && nodes.contains(relation.verb) && nodes.contains(relation.obj)
  }

  override def paint(g: Graphics): Unit = {
    super.paint(g)
    def quadraticFit(ys: IndexedSeq[Double]): (Double) => Double = {
      val b = (ys(2) - ys(0)) / 2
      val c = ys(1)
      val a = ys(2) - b - c
      def quadratic(x: Double) = a * x * x + b * x + c
      quadratic
    }
    def arrowCoords(headStart: (Double, Double), tip: (Double, Double)): (Array[Int], Array[Int]) = {
      val direction = (tip._1 - headStart._1, tip._2 - headStart._2)
      val orth = (-direction._2, direction._1)
      (Array((headStart._1 + 0.5 * orth._1).toInt, tip._1.toInt, (headStart._1 - 0.5 * orth._1).toInt),
       Array((headStart._2 + 0.5 * orth._2).toInt, tip._2.toInt, (headStart._2 - 0.5 * orth._2).toInt))
    }
    for( rel <- edges ) {
      // Quadratic spline: fit a quadratic to x and y coordinates
      val relnodes = IndexedSeq(nodes(rel.subject), nodes(rel.verb), nodes(rel.obj))
      val xfit = quadraticFit(relnodes.map( _.x ))
      val yfit = quadraticFit(relnodes.map( _.y ))
      val npts = 10
      g.drawPolyline(
        Array.tabulate(npts)((i) => xfit(2.0 * i / (npts - 1) - 1).toInt),
        Array.tabulate(npts)((i) => yfit(2.0 * i / (npts - 1) - 1).toInt),
        npts
      )
      val arrow1 = arrowCoords((xfit(-0.6), yfit(-0.6)), (xfit(-0.5), yfit(-0.5)))
      g.setColor(java.awt.Color.WHITE)
      g.fillPolygon(arrow1._1, arrow1._2, arrow1._1.length)
      g.setColor(java.awt.Color.BLACK)
      g.drawPolygon(arrow1._1, arrow1._2, arrow1._1.length)
      val arrow2 = arrowCoords((xfit(0.4), yfit(0.4)), (xfit(0.5), yfit(0.5)))
      g.drawPolyline(arrow2._1, arrow2._2, arrow2._1.length)
    }
    for( node <- nodes.values ) {
      val str = node.notion.toString
      g.setColor(java.awt.Color.WHITE)
      g.fillOval((node.x - 10 * str.length).toInt, (node.y - 10).toInt, 20 * str.length, 20)
      g.setColor(java.awt.Color.BLACK)
      g.drawOval((node.x - 10 * str.length).toInt, (node.y - 10).toInt, 20 * str.length, 20)
      g.drawString(str, (node.x - 5 * str.length()).toInt, (node.y + 5).toInt)
    }
  }
}

object GraphView
{
}
