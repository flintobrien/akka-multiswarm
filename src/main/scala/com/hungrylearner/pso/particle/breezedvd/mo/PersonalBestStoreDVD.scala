package com.hungrylearner.pso.particle.breezedvd.mo

import com.hungrylearner.pso.particle.{ParetoFrontImpl, MutablePosition, PersonalBestWriter}
import breeze.linalg.DenseVector

trait PersonalBestWriterDVD extends PersonalBestWriter[Double,DenseVector[Double]] {
  def personalBest: PositionDVD
}

trait PersonalBestStoreDVD extends PersonalBestWriterDVD {
  self: ParticleSpaceDVD =>

  protected var _paretoFront = new ParetoFrontImpl[Double,DenseVector[Double]]
//  protected var _personalBest: PositionDVD = new PositionDVD( _position.value, _position.fitness)
  _paretoFront.storePositionIfNonDominated( _position)

  override def personalBest: PositionDVD = _paretoFront.getOneBestPosition( _position).asInstanceOf[PositionDVD]

  override def storePersonalBestIfBest(position: MutablePosition[Double,DenseVector[Double]]) =
    _paretoFront.storePositionIfNonDominated( position)
}