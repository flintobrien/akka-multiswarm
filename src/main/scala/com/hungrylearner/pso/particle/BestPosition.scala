package com.hungrylearner.pso.particle

/**
 * Created by flint on 6/25/14.
 */
trait BestPosition[F,P] {
  def get: Position[F,P]

  /**
   * If position is better than our current position, use it and return option position.
   * If not, return None.
   * @param position The position to set if better than our current position
   * @return The position that was set or None if not better.
   */
  def setIfBest( position: MutablePosition[F,P]): Option[Position[F,P]]
}

class BestPositionImpl[F,P] extends BestPosition[F,P] {
  protected var _bestPosition: Position[F,P] = _

  override def get = _bestPosition

  override def setIfBest(position: MutablePosition[F, P]): Option[Position[F,P]] = {
    if( _bestPosition == null || position < _bestPosition) {
      _bestPosition = position.toPosition
      Some( _bestPosition)
    } else
      None
  }

}