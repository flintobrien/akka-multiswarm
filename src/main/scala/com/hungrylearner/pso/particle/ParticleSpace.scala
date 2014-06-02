package com.hungrylearner.pso.particle


trait ParticleSpace[F,P] {

//  type Position
  type PositionBounds  // aka. Search Space
  type History

  def position: MutablePosition[F,P]
  def personalBest: MutablePosition[F,P]
  def positionBounds: PositionBounds
  def history: History


  /**
   * If new position is personal best, update personal best.
   * @return personal best position
   */
  def updatePersonalBest: MutablePosition[F,P]

}




