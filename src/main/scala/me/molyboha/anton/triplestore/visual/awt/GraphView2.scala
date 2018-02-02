package me.molyboha.anton.triplestore.visual.awt

import java.awt.{Color, Component, Graphics, Point}

import me.molyboha.anton.triplestore.data.model.{Notion, Relation}

// GUI has three components: visualization, layout and control
class GraphView2[T] extends GraphViewBase[T]
{
  class NotionNode(notion: Notion[T], x0: Double, y0: Double) extends NodeBase(notion, x0, y0) {
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

  class RelationNode(relation: Relation[T], x0: Double, y0: Double) extends NodeBase(relation, x0, y0) {
    require(nodes.contains(relation.subject) && nodes.contains(relation.obj))
    override val notion: Relation[T] = relation

    private case class Geometry(relationLine: Seq[Seq[java.awt.Point]], verbLine: Seq[Seq[java.awt.Point]], arrow: Seq[java.awt.Point])
    private def geometry: Geometry = {
      val subjNode = nodes(notion.subject)
      val objNode = nodes(notion.obj)
      val fx = GraphView2.quadraticFit(IndexedSeq(subjNode.x, x, objNode.x))
      val fy = GraphView2.quadraticFit(IndexedSeq(subjNode.y, y, objNode.y))
      // The curved line connecting subject to object
      val relationLine = (-1.0 to 1.0 by 0.125).
        map( (t) => new java.awt.Point(fx(t).toInt, fy(t).toInt) ).
        filterNot( (pt) => subjNode.isin(pt) || objNode.isin(pt) )
      // The arrow from the verb node to the relation node
      val verbLine = if( nodes.contains(notion.verb) ) {
        val verbNode = nodes(notion.verb)
        val dx = x - verbNode.x
        val dy = y - verbNode.y
        val lineStart = (1.0 to 0.0 by -0.05).find( (k) => !verbNode.isin(new java.awt.Point((x - k * dx).toInt, (y - k * dy).toInt))).getOrElse(0.0)
        val line = Seq(new java.awt.Point((x - lineStart * dx).toInt, (y - lineStart * dy).toInt),
                       new java.awt.Point(x.toInt, y.toInt))
        val arrowSize = 0.1
        val arrowWidth = 0.5
        val arrowLine = Seq(new java.awt.Point((x - arrowSize * dx - arrowWidth * arrowSize * dy).toInt,
                                               (y - arrowSize * dy + arrowWidth * arrowSize * dx).toInt),
                            new java.awt.Point(x.toInt, y.toInt),
                            new java.awt.Point((x - arrowSize * dx + arrowWidth * arrowSize * dy).toInt,
                                               (y - arrowSize * dy - arrowWidth * arrowSize * dx).toInt))
        Seq(line, arrowLine)
      } else Seq()
      // The triangle representing the relation node
      val arrow = {
        val arrowSize = 0.3
        val x0 = fx(-arrowSize / 3)
        val y0 = fy(-arrowSize / 3)
        val x1 = fx(2 * arrowSize / 3)
        val y1 = fy(2 * arrowSize / 3)
        val dx = x1 - x0
        val dy = y1 - y0
        Seq(new java.awt.Point((x0 - 0.5 * dy).toInt, (y0 + 0.5 * dx).toInt),
            new java.awt.Point(x1.toInt, y1.toInt),
            new java.awt.Point((x0 + 0.5 * dy).toInt, (y0 - 0.5 * dx).toInt))
      }
      Geometry(Seq(relationLine), verbLine, arrow)
    }

    override def paint(g: Graphics): Unit = {
      val geom = geometry
      for( line <- geom.relationLine ) {
        g.drawPolyline(line.map(_.x).toArray, line.map(_.y).toArray, line.length)
      }
      g.setColor(color)
      g.fillPolygon(geom.arrow.map(_.x).toArray, geom.arrow.map(_.y).toArray, geom.arrow.length)
      g.setColor(Color.BLACK)
      g.drawPolygon(geom.arrow.map(_.x).toArray, geom.arrow.map(_.y).toArray, geom.arrow.length)
      for( line <- geom.verbLine ) {
        g.drawPolyline(line.map(_.x).toArray, line.map(_.y).toArray, line.length)
      }
    }

    override def isin(pos: Point): Boolean = {
      val geom = geometry
      val edges = geom.arrow.sliding(2) ++ Seq(Seq(geom.arrow.last, geom.arrow.head))
      Console.println(pos)
      val numRightIntersections = edges.
        count( { case Seq(pt0: Point, pt1: Point) =>
          val correctYBand = (pt0.y - pos.y) * (pt1.y - pos.y) < 0
          if( correctYBand ) {
            val intersectionX = (pos.y - pt0.y) / (pt1.y - pt0.y).toDouble * pt1.x +
                                (pos.y - pt1.y) / (pt0.y - pt1.y).toDouble * pt0.x
            pos.x < intersectionX
          }
          else {
            pt0.y == pos.y && pos.x < pt0.x
          }
        } )
      (numRightIntersections & 1) != 0
    }
  }

  private var _nodes: Map[Notion[T], NotionNode] = Map()
  private var _edges: Map[Relation[T], RelationNode] = Map()
  override def nodes: Map[Notion[T], NodeBase] = _nodes ++ _edges

  def addNode(notion: Notion[T], x: Double, y: Double): NotionNode = {
    if( _nodes contains notion ) {
      val res = _nodes(notion)
      res.x = x
      res.y = y
      res
    }
    else {
      val res = new NotionNode(notion, x, y)
      _nodes += notion -> res
      repaint()
      res
    }
  }

  def removeNode(notion: Notion[T]): Unit = {
    _nodes -= notion
    _edges = _edges.filterNot( (rel) => rel._1.subject == notion || rel._1.obj == notion )
    repaint()
  }

  def addRelation(relation: Relation[T], x: Double, y: Double): RelationNode = {
    if( _edges contains relation ) {
      val res = _edges(relation)
      res.x = x
      res.y = y
      res
    }
    else {
      val res = new RelationNode(relation, x, y)
      _edges += relation -> res
      if( _nodes.contains(relation) ) {
        _nodes -= relation
      }
      repaint()
      res
    }
  }

  def removeRelation(relation: Relation[T]): Unit = {
    _edges -= relation
    repaint()
  }

  override def paint(g: Graphics): Unit = {
    super.paint(g)
    for( node <- nodes.values ) node.paint(g)
  }
}

object GraphView2 {
  def quadraticFit(ys: IndexedSeq[Double]): (Double) => Double = {
    val b = (ys(2) - ys(0)) / 2
    val c = ys(1)
    val a = ys(2) - b - c
    def quadratic(x: Double) = a * x * x + b * x + c
    quadratic
  }
}
