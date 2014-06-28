package com.hungrylearner.pso.particle

trait PersonalBestWriter[F,P] {
  def storePersonalBestIfBest( position: MutablePosition[F,P]): Option[Position[F,P]]
}

