package com.hungrylearner.pso.breeze

import com.hungrylearner.pso.particle.{MutablePosition, ParticleSpace}
import breeze.linalg.DenseVector

object ParticleSpaceDVD {

  /**
   * initialPosition: (dimension: Int, particleIndex: Int) =>  DenseVector[Double]
   */
  case class ParticleSpaceContext( initialPosition: (Int,Int) => MutablePosition[Double,DenseVector[Double]],
                                        positionBounds: Array[(Double,Double)],
                                        initialHistory: (Int) => DenseVector[Double]
                                        )
}

trait ParticleSpaceDVD extends ParticleSpace[Double,DenseVector[Double]] {
  import ParticleSpaceDVD._

  override type PositionBounds = Array[(Double,Double)]
  override type History = DenseVector[Double]

  type MutablePositionDVD = MutablePosition[Double,DenseVector[Double]]
  def dimension: Int
  def index: Int
  val psc: ParticleSpaceContext
  protected var _position: MutablePositionDVD = psc.initialPosition( dimension, index)
  // _personalBest needs to be a copy!  _position is mutable and will be updated
  protected var _personalBest: MutablePositionDVD = _position.copy

  override def position = _position
  override def personalBest = _personalBest
  override def positionBounds = psc.positionBounds

  protected var _history: History = psc.initialHistory( dimension)
  override def history = _history

  /**
   * If new position is personal best, update personal best.
   * @return personal best position
   */
  override def updatePersonalBest: MutablePositionDVD = {
    // If new fitness is better than recorded personal best
    if( position.fitness < personalBest.fitness) {
      _personalBest = position.copy
    }
    _personalBest
  }


}