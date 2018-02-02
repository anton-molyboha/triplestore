package me.molyboha.anton.triplestore.visual.awt

import java.awt.{Color, Component, Graphics}

import me.molyboha.anton.triplestore.data.model.Notion

abstract class GraphViewBase[T] extends Component {
  abstract class NodeBase(val notion: Notion[T], x0: Double, y0: Double)
  {
    private var xx = x0
    private var yy = y0
    private var _color = Color.WHITE

    def x: Double = xx
    def y: Double = yy
    def color: Color = _color

    def x_=(v: Double): Unit = {
      xx = v
      GraphViewBase.this.repaint()
    }

    def y_=(v: Double): Unit = {
      yy = v
      GraphViewBase.this.repaint()
    }

    def color_=(c: Color): Unit = {
      _color = c
      GraphViewBase.this.repaint()
    }

    def paint(g: Graphics): Unit
    def isin(pos: java.awt.Point): Boolean
  }

  def nodes: Map[Notion[T], NodeBase]

  def nodeAtPosition(pos: java.awt.Point): Option[NodeBase] = {
    // If multiple nodes contain the point, return the last one (as per the iteration order)
    // since it will be the one drawn on top
    nodes.values.foldLeft(None: Option[NodeBase])( (old, node) => if( node.isin(pos) ) Some(node) else old )
  }
}
