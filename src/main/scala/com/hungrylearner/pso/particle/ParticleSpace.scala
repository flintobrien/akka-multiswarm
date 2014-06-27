package com.hungrylearner.pso.particle


trait ParticleSpace[F,P] {

  type PositionBounds  // aka. Search Space
  type History
  //type PersonalBest <: BestPosition[F,P]

  def position: MutablePosition[F,P]
  val personalBest: BestPosition[F,P]
  def positionBounds: PositionBounds
  def history: History

}




