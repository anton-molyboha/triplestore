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

  updateLayout()
  private def updateLayout(): Unit = {
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
    val _currentlyDrawn = computeToDraw(Set(_center), Set(_center), radius)
    layout(_currentlyDrawn, view)
    view.nodes(_center).color = Color.CYAN
  }

  view.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      for( node <- view.nodeAtPosition(e.getPoint) ) {
        center = node.notion
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
