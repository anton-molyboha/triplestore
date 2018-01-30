package me.molyboha.anton.triplestore.visual.awt.layout

import scala.util.Random

class SpringSolver[T] {
  import SpringSolver._
  protected case class Spring(node1: Int, node2: Int, length: Double, direction: SpringDirection, strength: Double = 1)
  {
    def force(node: Vector, otherNode: Vector): Vector = {
      val dist = (otherNode - node).norm
      if( direction.applicable(length, dist) ) {
        (otherNode - node) * (dist - length) / dist * strength
      }
      else {
        Vector.zero
      }
    }
    def potential(node: Vector, otherNode: Vector): Double = {
      val dist = (otherNode - node).norm
      if( direction.applicable(length, dist) ) {
        (dist - length) * (dist - length) * strength / 2
      }
      else {
        0.0
      }
    }
  }

  private var _nodes: List[T] = List()
  private var _nodesMap: Map[T, Int] = Map()
  protected def nodeIndex(node: T): Int = {
    if(!_nodesMap.contains(node)) {
      _nodesMap += node -> _nodes.length
      _nodes ::= node
    }
    _nodesMap(node)
  }
  protected def nodes: Seq[T] = _nodes.reverse

  protected var _springs: List[Spring] = List()
  def addSpring(node1: T, node2: T, length: Double, direction: SpringDirection, strength: Double = 1): Unit =
  {
    _springs ::= Spring(nodeIndex(node1), nodeIndex(node2), length, direction, strength)
  }

  def layout(): Map[T, Vector] = {
    val numnodes = nodes.length
    val lengthUnit = _springs.iterator.map(_.length).sum / _springs.length
    val strengthUnit = _springs.iterator.map(_.strength).sum / _springs.length
    val forceUnit = strengthUnit * lengthUnit
    val eps = 1e-2

    def forces(pos: Array[Vector]): Array[Vector] = {
      val res = Array.fill(numnodes)(Vector.zero)
      for( spring <- _springs ) {
        val force = spring.force(pos(spring.node1), pos(spring.node2))
        res(spring.node1) += force
        res(spring.node2) -= force
      }
      res
    }
    def potential(pos: Array[Vector]): Double = {
      _springs.map( (spring) => spring.potential(pos(spring.node1), pos(spring.node2)) ).sum
    }
    def relax(pos: Array[Vector], maxIter: Int = 1000): Unit = {
      if( maxIter > 0 ) {
        val g = forces(pos)
        if (g.iterator.map(_.norm > eps * forceUnit).reduceLeft(_ || _)) {
          for (i <- 0 until numnodes) {
            pos(i) += g(i) * 0.01 / strengthUnit
          }
          relax(pos, maxIter - 1) // Tail recursion
        }
      }
    }
    def randomPlacement(): Array[Vector] = {
      val scale = lengthUnit * math.sqrt(numnodes)
      Array.tabulate(numnodes)( (_) => Vector(Random.nextDouble() * scale, Random.nextDouble() * scale) )
    }
    def attempt(): Array[Vector] = {
      val res = randomPlacement()
      relax(res)
      res
    }
    def choose(cand1: Array[Vector], cand2: Array[Vector]) = {
      if( potential(cand1) < potential(cand2) ) cand1 else cand2
    }

    val res = (0 until 40).foldLeft(attempt())( (best, i) => choose(best, attempt()) )
    // Debugging info
//    {
//      val g = forces(res)
//      for( nodeInd <- nodes.iterator.zipWithIndex ) {
//        Console.println(nodeInd._1.toString + ": " + g(nodeInd._2))
//      }
//    }
    // Result
    nodes.iterator.zipWithIndex.map( (nodeInd) => nodeInd._1 -> res(nodeInd._2) ).toMap
  }
}

object SpringSolver {
  case class Vector(x: Double, y: Double) {
    def +(other: Vector) = Vector(x + other.x, y + other.y)
    def -(other: Vector) = Vector(x - other.x, y - other.y)
    def *(k: Double) = Vector(x * k, y * k)
    def /(k: Double) = Vector(x / k, y / k)
    def norm: Double = math.sqrt(x * x + y * y)
    override def toString: String = "(" + x + ", " + y +")"
  }
  object Vector {
    val zero = Vector(0, 0)
    //def *(k: Double, v: Vector) = Vector(v.x * k, v.y * k)
    class VectorScalarMultiplication(val k: Double) {
      def *(v: Vector) = Vector(v.x * k, v.y * k)
    }
    implicit def scalarMultiplication(k: Double): VectorScalarMultiplication = new VectorScalarMultiplication(k)
  }

  trait SpringDirection
  {
    def applicable(springLength: Double, distance: Double): Boolean
  }
  case class Push() extends SpringDirection {
    override def applicable(springLength: Double, distance: Double): Boolean = distance < springLength
  }
  case class Pull() extends SpringDirection {
    override def applicable(springLength: Double, distance: Double): Boolean = distance > springLength
  }
  case class Prefer() extends SpringDirection {
    override def applicable(springLength: Double, distance: Double): Boolean = true
  }
}
