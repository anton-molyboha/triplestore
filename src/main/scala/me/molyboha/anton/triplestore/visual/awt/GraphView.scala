package me.molyboha.anton.triplestore.visual.awt

import java.awt.{Color, Component, Graphics}

import me.molyboha.anton.triplestore.data.model.{Notion, Relation}

// GUI has three components: visualization, layout and control
class GraphView[T] extends Component
{
  import GraphView._
  class Node(val notion: Notion[T], x0: Double, y0: Double)
  {
    private var xx = x0
    private var yy = y0
    private var _color = Color.WHITE

    def x: Double = xx
    def y: Double = yy
    def color: Color = _color

    def x_=(v: Double): Unit = {
      xx = v
      GraphView.this.repaint()
    }

    def y_=(v: Double): Unit = {
      yy = v
      GraphView.this.repaint()
    }

    def color_=(c: Color): Unit = {
      _color = c
      GraphView.this.repaint()
    }

    val halfWidth: Int = 10 * notion.toString.length
    val halfHeight: Int = 10
  }

  private var _nodes: Map[Notion[T], Node] = Map()
  private var edges: Set[Relation[T]] = Set()
  def nodes: Map[Notion[T], Node] = _nodes

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

  def nodeAtPosition(pos: java.awt.Point): Option[Node] = {
    def isin(node: Node): Boolean = {
      val dx = (pos.x - node.x) / node.halfWidth
      val dy = (pos.y - node.y) / node.halfHeight
      dx * dx + dy * dy <= 1
    }
    // If multiple nodes contain the point, return the last one (as per the iteration order)
    // since it will be the one drawn on top
    _nodes.values.foldLeft(None: Option[Node])( (old, node) => if( isin(node) ) Some(node) else old )
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
    for( node <- _nodes.values ) {
      val str = node.notion.toString
      g.setColor(node.color)
      g.fillOval((node.x - node.halfWidth).toInt, (node.y - node.halfHeight).toInt, 2 * node.halfWidth, 2 * node.halfHeight)
      g.setColor(java.awt.Color.BLACK)
      g.drawOval((node.x - node.halfWidth).toInt, (node.y - node.halfHeight).toInt, 2 * node.halfWidth, 2 * node.halfHeight)
      g.drawString(str, (node.x - node.halfWidth / 2).toInt, (node.y + node.halfHeight / 2).toInt)
    }
  }
}

object GraphView
{
}