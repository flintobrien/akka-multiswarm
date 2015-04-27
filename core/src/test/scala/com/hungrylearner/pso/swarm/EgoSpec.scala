package com.hungrylearner.pso.swarm

import org.specs2.mutable._
import org.specs2.mock.Mockito
import com.hungrylearner.pso.particle.{Position, PositionIteration}


/**
 * Created by flint on 6/24/14.
 */
class EgoSpec  extends Specification with BeforeAfter with Mockito {

  class PositionII( _value: Int, _fitness: Int) extends Position[Int, Int] {
    override def value = _value
    override def fitness = _fitness

    /** Result of comparing `this` with operand `that`.
      *
      * Implement this method to determine how instances of A will be sorted.
      *
      * Returns `x` where:
      *
      *   - `x < 0` when `this < that`
      *
      *   - `x == 0` when `this == that`
      *
      *   - `x > 0` when  `this > that`
      *
      */
    override def compare(that: Position[Int,Int]) = if (_fitness < that.fitness) -1 else 1
  }

  class SingleEgoImplII extends SingleEgoImpl[Int,Int]


  override def before = {
    //    println( "before --------------------------")

  }
  override def after = {
  }


  "SingleEgoImpl" should {

    val position = new PositionII( 1,1)
    val betterPosition = new PositionII( 0,0)
//    val newBestPositions = Seq( PositionIteration( position, iteration=1))
//    val newBestsEmpty = Seq[PositionIteration[Int,Int]]()

    "  Store any position if starting with null" in {
      val ego = new SingleEgoImplII

      ego.bestPosition must beNull
      ego.storePositionIfBest( position)
      ego.bestPosition must beEqualTo( position)
    }

    "  Store a better position if asked" in {
      val ego = new SingleEgoImplII

      ego.storePositionIfBest( position)
      ego.bestPosition must beEqualTo( position)

      ego.storePositionIfBest( betterPosition)
      ego.bestPosition must beEqualTo( betterPosition)
    }

    "  Not store an inferior position if asked" in {
      val ego = new SingleEgoImplII

      ego.storePositionIfBest( betterPosition)
      ego.bestPosition must beEqualTo( betterPosition)

      ego.storePositionIfBest( position)
      ego.bestPosition must beEqualTo( betterPosition)
    }

  }
}
