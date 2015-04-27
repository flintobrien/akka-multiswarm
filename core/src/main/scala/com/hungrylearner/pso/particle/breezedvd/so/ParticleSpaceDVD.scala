package com.hungrylearner.pso.particle.breezedvd.so

import com.hungrylearner.pso.particle.{MutablePosition, ParticleSpace}
import breeze.linalg.DenseVector

object ParticleSpaceDVD {

  /**
   * initialPosition: (dimension: Int, particleIndex: Int) =>  DenseVector[Double]
   */
  case class ParticleSpaceContext( initialPosition: (Int,Int) => MutablePositionDVD,
                                        positionBounds: Array[(Double,Double)],
                                        initialHistory: (Int) => DenseVector[Double]
                                        )
}

trait ParticleSpaceDVD extends ParticleSpace[Double,DenseVector[Double]] {
  import ParticleSpaceDVD._

  override type PositionBounds = Array[(Double,Double)]
  override type History = DenseVector[Double]

  def dimension: Int
  def index: Int
  val psc: ParticleSpaceContext
  protected var _position: MutablePositionDVD = psc.initialPosition( dimension, index)
  // _personalBest needs to be a copy!  _position is mutable and will be updated

  override def position = _position
  override def positionBounds = psc.positionBounds

  protected var _history: History = psc.initialHistory( dimension)
  override def history = _history

}