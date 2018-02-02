package me.molyboha.anton.triplestore.visual.awt

import java.awt.{BorderLayout, Color, Panel}
import java.awt.event.{ComponentAdapter, ComponentEvent, MouseAdapter, MouseEvent}

import me.molyboha.anton.triplestore.data.model.Notion

class CentralView[T](startingNotion: Notion[T], val radius: Int = 2, layout: (Iterable[Notion[T]], GraphView[T]) => Unit) extends Panel(new BorderLayout) {
  private val view = new GraphView[T]
  add(view)
  view.setVisible(true)

  private var _center: Notion[T] = startingNotion
  def center: Notion[T] = _center
  def center_= (notion: Notion[T]): Unit = {
    if( notion != _center ) {
      _center = notion
      updateLayout()
    }
  }

  private var _pinned: Set[Notion[T]] = Set()
  def pinned: Set[Notion[T]] = _pinned
  def pin(node: Notion[T]): Unit = {
    if( ! _pinned.contains(node) ) {
      _pinned += node
      if( view.nodes.contains(node) ) {
        view.nodes(node).color = nodeColor(node)
      }
      else updateLayout()
    }
  }
  def unpin(node: Notion[T]): Unit = {
    if( _pinned.contains(node) ) {
      _pinned -= node
      if( node == center ) view.nodes(node).color = nodeColor(node)
      else updateLayout()
    }
  }

  private val defaultColor = new Color(0.9.toFloat, 0.9.toFloat, 0.9.toFloat)
  private val pinnedColor = new Color(1.0.toFloat, 1.0.toFloat, 1.0.toFloat)
  private val centralColor = new Color(0.toFloat, 0.9.toFloat, 0.9.toFloat)
  private val pinnedCentralColor = new Color(0.toFloat, 1.0.toFloat, 1.0.toFloat)
  private def nodeColor(node: Notion[T]): Color = {
    if( _pinned.contains(node) ) {
      if( node == _center ) pinnedCentralColor
      else pinnedColor
    }
    else {
      if( node == _center ) centralColor
      else defaultColor
    }
  }

  updateLayout()
  def updateLayout(): Unit = {
    for( node <- view.nodes.keys ) view.removeNode(node)
    def computeToDraw(startSet: Set[Notion[T]], expandSet: Set[Notion[T]], radius: Int): Set[Notion[T]] = {
      if( radius > 0 ) {
        val expansion = expandSet.flatMap((node) =>
          node.subjectOf.map(_.verb) ++ node.subjectOf.map(_.obj) ++
            node.objOf.map(_.subject) ++ node.objOf.map(_.verb) ++
            node.verbOf.map(_.subject) ++ node.verbOf.map(_.obj))
        computeToDraw(startSet ++ expansion, expansion.filterNot(startSet), radius - 1)
      }
      else startSet
    }
    val _currentlyDrawn = computeToDraw(_pinned + _center, _pinned + _center, radius)
    layout(_currentlyDrawn, view)
    for( node <- view.nodes.values ) node.color = nodeColor(node.notion)
  }

  view.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      for( node <- view.nodeAtPosition(e.getPoint) ) {
        if( node.notion == center ) {
          if( pinned.contains(node.notion) ) unpin(node.notion)
          else pin(node.notion)
        }
        else center = node.notion
      }
    }
  })

  addComponentListener(new ComponentAdapter {
    override def componentResized(e: ComponentEvent): Unit = {
      val curCenterX = view.nodes.values.map( _.x ).sum / view.nodes.size.toDouble
      val curCenterY = view.nodes.values.map( _.y ).sum / view.nodes.size.toDouble
      val shiftX = 0.5 * getWidth - curCenterX
      val shiftY = 0.5 * getHeight - curCenterY
      for( node <- view.nodes.values ) {
        node.x += shiftX
        node.y += shiftY
      }
    }
  })
}
