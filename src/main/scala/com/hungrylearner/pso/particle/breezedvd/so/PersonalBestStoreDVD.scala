package com.hungrylearner.pso.particle.breezedvd.so

import com.hungrylearner.pso.particle.{ParetoFront, MutablePosition, PersonalBestWriter}
import breeze.linalg.DenseVector

trait PersonalBestStoreDVD extends PersonalBestWriter[Double,DenseVector[Double]] {
  def personalBest: PositionDVD
}

trait SinglePersonalBestStoreDVD extends PersonalBestStoreDVD {
  self: ParticleSpaceDVD =>

  protected var _personalBest: PositionDVD = new PositionDVD( _position.value, _position.fitness)

  override def personalBest: PositionDVD = _personalBest
  override def storePersonalBestIfBest(position: MutablePosition[Double,DenseVector[Double]]) = {
    if( _personalBest == null || position < _personalBest) {
      _personalBest = position.toPosition.asInstanceOf[PositionDVD]
      Some( _personalBest)
    } else
      None
  }
}
