package com.hungrylearner.pso.particle


trait ParticleSpace[F,P] {

  type PositionBounds  // aka. Search Space
  type History

  def position: MutablePosition[F,P]
  def positionBounds: PositionBounds
  def history: History

}




