package me.molyboha.anton.triplestore.visual.awt.layout

import me.molyboha.anton.triplestore.data.model.{Notion, Relation}
import me.molyboha.anton.triplestore.visual.awt.GraphView2

object SpringAutoLayout2 {
  def apply[T](notions: Iterable[Notion[T]], view: GraphView2[T]): Unit = {
    val distanceUnit = 100
    val notionsSet = notions.toSet
    val edges = notionsSet.flatMap( _.subjectOf.filter( (rel) => notionsSet.contains(rel.obj) ))
    val nodes = notionsSet ++ edges

    val solver = new SpringSolver[Notion[T]]
    // Each node wants to non-overlap
    for( node1 <- nodes ) for( node2 <- nodes ) if( node1 != node2 ) {
      // Hmm, I'm double-counting here...
      solver.addSpring(node1, node2, distanceUnit, SpringSolver.Push(), 4)
    }
    // Related nodes want to stay together
    for( rel <- edges ) {
      solver.addSpring( rel.subject, rel.obj, 2 * distanceUnit, SpringSolver.Prefer())
      solver.addSpring( rel.subject, rel, 0, SpringSolver.Prefer())
      solver.addSpring( rel.obj, rel, 0, SpringSolver.Prefer())
      if( nodes.contains(rel.verb) ) {
        solver.addSpring( rel.verb, rel, distanceUnit, SpringSolver.Prefer())
      }
    }

    val layout = solver.layout()
    val bbox = (SpringSolver.Vector(layout.values.map(_.x).min, layout.values.map(_.y).min),
                SpringSolver.Vector(layout.values.map(_.x).max, layout.values.map(_.y).max))
    val shift = {
      val viewCenter = SpringSolver.Vector(0.5 * view.getWidth, 0.5 * view.getHeight)
      val layoutCenter = (bbox._1 + bbox._2) / 2
      viewCenter - layoutCenter
    }
    for( notion <- nodes ) {
      val pos = layout(notion)
      view.addNode(notion, pos.x + shift.x, pos.y + shift.y)
    }
    for( edge <- edges ) {
      val pos = layout(edge)
      view.addRelation(edge, pos.x + shift.x, pos.y + shift.y)
    }
  }
}
