package com.hungrylearner.pso.particle

/**
 * Created by flint on 6/18/14.
 */
trait ParetoParticleSpace[F,P] {
  type PositionBounds  // aka. Search Space
  type History

  def position: MutablePosition[F,P]
  val personalBest: ParetoFront[F,P]
  def positionBounds: PositionBounds
  def history: History


  /**
   * If the updated position is a new personal best, update personal best and return the position;
   * otherwise, return null.
   * @return A new personal best position or None
   */
  def updatePersonalBest: Option[Position[F,P]]

}
