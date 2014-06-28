package com.hungrylearner.pso.particle.breezedvd.mo

import com.hungrylearner.pso.particle.{Position, MutablePosition, ParticleSpace, Kinematic}
import breeze.linalg.DenseVector
import akka.actor.ActorSystem
import breeze.generic.{MappingUFunc, UFunc}
import akka.event.Logging

object KinematicParticleSpaceDVD {

  type InertiaWeightFunction = (Int) => Double

  /**
   *
   * @param initialVelocity (dimension: Int, particleIndex: Int) =>  DenseVector[Double]
   * @param velocityBounds
   * @param inertiaWeight
   * @param phiP
   * @param phiG
   * @param random
   * @param system
   */
  case class KinematicContext( initialVelocity: (Int,Int) => DenseVector[Double],
                                velocityBounds: (Double,Double),
                                inertiaWeight: InertiaWeightFunction,
                                phiP: Double,
                                phiG: Double,
                                random: () => Double,
                                system: ActorSystem)

  object maxValue extends UFunc with MappingUFunc {

    implicit object maxValueDouble extends Impl2[Double,Double,Double] { def apply( a:Double, b:Double) = math.max( a, b) }
    implicit object maxValueInPlaceDouble extends InPlaceImpl2[Double,Double] { def apply( a:Double, b:Double) = math.max( a, b) }
  }
  object minValue extends UFunc with MappingUFunc {

    implicit object minValueDouble extends Impl2[Double,Double,Double] { def apply( a:Double, b:Double) = math.min( a, b) }
    implicit object minValueInPlaceDouble extends InPlaceImpl2[Double,Double] { def apply( a:Double, b:Double) = math.min( a, b) }
  }
}

trait KinematicParticleSpaceDVD extends Kinematic[Double,DenseVector[Double]] with ParticleSpaceDVD with PersonalBestWriterDVD {
  import KinematicParticleSpaceDVD._

  private val Logger = Logging.getLogger(kc.system, this)


  type Velocity = DenseVector[Double]
  override type VelocityBounds = (Double,Double)
  override type InertiaWeight = Int => Double
  override type Phi = Double

  val kc: KinematicContext
  protected var _velocity: Velocity = kc.initialVelocity( dimension, index)

  override def velocity: Velocity = _velocity
  override def velocityBounds: VelocityBounds = kc.velocityBounds

  override def inertiaWeight = kc.inertiaWeight

  override def updateVelocity( iteration: Int, bestPosition: Position[Double,DenseVector[Double]]): Unit = {

    //    Logger.debug( "velocity 1: {}", _velocity)
    val rP: Double = kc.random()
    val rG: Double = kc.random()

    Logger.debug( f"velocity   : v:${_velocity(0)}%7.3f *= inertiaWeight($iteration):${inertiaWeight(iteration)}%7.3f)  =  ${_velocity(0)*inertiaWeight(iteration)}%7.3f    pos/fit=${position.value(0)}%7.3f / ${position.fitness}%7.3f")
    _velocity *= inertiaWeight(iteration)
    val influenceP: DenseVector[Double] = (personalBest.value - position.value) * (rP * kc.phiP)
    val influenceG: DenseVector[Double] = (bestPosition.value - position.value)  * (rG * kc.phiG)
    Logger.debug( f"influence P: (pBest=${personalBest.value(0)}%7.3f - pos=${position.value(0)}%7.3f) * (rP=$rP%7.3f * ${kc.phiP}%7.3f   =   influenceP=${influenceP(0)}%7.3f")
    Logger.debug( f"influence G: (gBest=${bestPosition.value(0)}%7.3f - pos=${position.value(0)}%7.3f) * (rG=$rG%7.3f * ${kc.phiG}%7.3f   =   influenceG=${influenceG(0)}%7.3f")
    Logger.debug( f"velocity  v: v=${_velocity(0)}%7.3f + l=${influenceP(0)}%7.3f + g=${influenceG(0)}%7.3f")
    _velocity += influenceP
    _velocity += influenceG
    _velocity = minValue( _velocity, velocityBounds._2)
    _velocity = maxValue( _velocity, velocityBounds._1)

    Logger.debug( f"velocityFin: v=${_velocity(0)}%7.3f")
  }

}


