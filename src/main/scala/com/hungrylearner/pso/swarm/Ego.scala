package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.particle.{PositionIteration, Position}

/**
 * Created by flint on 6/22/14.
 */
trait Ego[F,P] {
  def storePositionIfBest( position: Position[F,P]): Boolean
  def storePositionsIfBest( positions: Seq[PositionIteration[F,P]]): Seq[PositionIteration[F,P]]
}

trait SingleEgo[F,P] extends Ego[F,P] {
  def bestPosition: Position[F,P]
}

trait MultiEgo[F,P] extends Ego[F,P] {
  def bestPositions: Seq[PositionIteration[F,P]]
//  def storePositionsIfNonDominated( positions: Seq[Position[F,P]]): Seq[Position[F,P]]
}


trait SingleEgoImpl[F,P] extends SingleEgo[F,P] {
  protected var _bestPosition: Position[F,P] = _

  override def bestPosition = _bestPosition

  override def storePositionIfBest(position: Position[F, P]): Boolean = {
    if( _bestPosition == null || position < _bestPosition) {
      _bestPosition = position
      true
    } else
      false
  }

  override def storePositionsIfBest( positions: Seq[PositionIteration[F, P]]) =
    positions.filter( pi => storePositionIfBest( pi.position))

}