package me.molyboha.anton.triplestore.visual.awt

import java.awt.{Color, Component, Graphics}

import me.molyboha.anton.triplestore.data.model.{Notion, Relation}

// GUI has three components: visualization, layout and control
class GraphView[T] extends GraphViewBase[T]
{
  import GraphView._

  class Node(notion: Notion[T], x0: Double, y0: Double) extends NodeBase(notion, x0, y0) {
    val halfWidth: Int = 10 * notion.toString.length
    val halfHeight: Int = 10

    override def paint(g: Graphics): Unit = {
      val str = notion.toString
      g.setColor(color)
      g.fillOval((x - halfWidth).toInt, (y - halfHeight).toInt, 2 * halfWidth, 2 * halfHeight)
      g.setColor(java.awt.Color.BLACK)
      g.drawOval((x - halfWidth).toInt, (y - halfHeight).toInt, 2 * halfWidth, 2 * halfHeight)
      g.drawString(str, (x - halfWidth / 2).toInt, (y + halfHeight / 2).toInt)
    }


    override def isin(pos: java.awt.Point): Boolean = {
      val dx = (pos.x - x) / halfWidth
      val dy = (pos.y - y) / halfHeight
      dx * dx + dy * dy <= 1
    }
  }

  private var _nodes: Map[Notion[T], Node] = Map()
  private var edges: Set[Relation[T]] = Set()
  override def nodes: Map[Notion[T], Node] = _nodes

  def addNode(notion: Notion[T], x: Double, y: Double): Node = {
    if( _nodes contains notion ) {
      val res = _nodes(notion)
      res.x = x
      res.y = y
      res
    }
    else {
      val res = new Node(notion, x, y)
      _nodes += notion -> res
      for (relLst <- Seq(notion.subjectOf, notion.verbOf, notion.objOf); rel <- relLst) {
        if (shouldIncludeRelation(rel)) {
          edges += rel
        }
      }
      repaint()
      res
    }
  }

  def removeNode(notion: Notion[T]): Unit = {
    _nodes -= notion
    edges = edges.filterNot( (rel) => rel.subject == notion || rel.verb == notion || rel.obj == notion )
    repaint()
  }

  private def shouldIncludeRelation(relation: Relation[T]): Boolean = {
    _nodes.contains(relation.subject) && _nodes.contains(relation.verb) && _nodes.contains(relation.obj)
  }

  override def paint(g: Graphics): Unit = {
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
      val relnodes = IndexedSeq(_nodes(rel.subject), _nodes(rel.verb), _nodes(rel.obj))
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
    for( node <- _nodes.values ) node.paint(g)
  }
}

object GraphView
{
}
