package me.molyboha.anton.triplestore.visual.awt.layout

import me.molyboha.anton.triplestore.data.model.{Notion, Relation}
import me.molyboha.anton.triplestore.visual.awt.GraphView

object SpringAutoLayout {
  def apply[T](notions: Iterable[Notion[T]], view: GraphView[T]): Unit = {
    val distanceUnit = 100
    val nodes = notions.toSet
    def isRelationVisible(relation: Relation[T]): Boolean = {
      (nodes contains relation.subject) && (nodes contains relation.verb) && (nodes contains relation.obj)
    }

    val solver = new SpringSolver[Notion[T]]
    // Each node wants to non-overlap
    for( node1 <- notions ) for( node2 <- notions ) if( node1 != node2 ) {
      // Hmm, I'm double-counting here...
      solver.addSpring(node1, node2, distanceUnit, SpringSolver.Push(), 4)
    }
    // Related nodes want to stay together
    val relations = (notions.flatMap(_.subjectOf) ++
                     notions.flatMap(_.verbOf) ++
                     notions.flatMap(_.objOf)).filter(isRelationVisible).toSet
    for( rel <- relations ) {
      solver.addSpring( rel.subject, rel.obj, 2 * distanceUnit, SpringSolver.Prefer())
      solver.addSpring( rel.subject, rel.verb, 0, SpringSolver.Prefer())
      solver.addSpring( rel.obj, rel.verb, 0, SpringSolver.Prefer())
    }

    val layout = solver.layout()
    val bbox = (SpringSolver.Vector(layout.values.map(_.x).min, layout.values.map(_.y).min),
                SpringSolver.Vector(layout.values.map(_.x).max, layout.values.map(_.y).max))
    val shift = {
      val viewCenter = SpringSolver.Vector(0.5 * view.getWidth, 0.5 * view.getHeight)
      val layoutCenter = (bbox._1 + bbox._2) / 2
      Console.println("View center: " + viewCenter)
      Console.println("Layout center: " + layoutCenter)
      viewCenter - layoutCenter
    }
    Console.println("Shift: " + shift)
    for( node <- view.nodes.keys ) view.removeNode(node)
    for( notionPos <- layout ) {
      val notion = notionPos._1
      val pos = notionPos._2
      view.addNode(notion, pos.x + shift.x, pos.y + shift.y)
    }
  }
}
