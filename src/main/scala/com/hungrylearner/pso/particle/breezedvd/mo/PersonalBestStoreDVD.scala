package com.hungrylearner.pso.particle.breezedvd.mo

import com.hungrylearner.pso.particle.{MutablePosition, PersonalBestWriter}
import breeze.linalg.DenseVector

trait PersonalBestWriterDVD extends PersonalBestWriter[DenseVector[Double],DenseVector[Double]] {
  def personalBest: PositionDVD
}

trait PersonalBestStoreDVD extends PersonalBestWriterDVD {
  self: ParticleSpaceDVD =>

  protected var _paretoFront = new ParetoFrontDVD
  _paretoFront.storeMutablePositionIfNonDominated( _position)

  override def personalBest: PositionDVD = _paretoFront.getOneBestPosition( _position).asInstanceOf[PositionDVD]

  override def storePersonalBestIfBest(position: MutablePosition[DenseVector[Double],DenseVector[Double]]) =
    _paretoFront.storeMutablePositionIfNonDominated( position)
}
