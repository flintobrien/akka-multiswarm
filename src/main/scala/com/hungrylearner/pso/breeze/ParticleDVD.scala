package com.hungrylearner.pso.breeze

import akka.event.Logging
import com.hungrylearner.pso.particle._
import breeze.linalg.DenseVector
import com.hungrylearner.pso.swarm.SimulationContext


object ParticleDVD {
  import ParticleSpaceDVD._
  import KinematicParticleSpaceDVD._

  case class ParticleContext( dimension: Int,
                              particleSpace: ParticleSpaceContext,
                              kinematic: KinematicContext
                            )
}
import ParticleDVD._

/**
 *
 * @param sc
 */
class ParticleDVD( sc: SimulationContext, pc:ParticleContext, particleIndex: Int) extends {
  // Early initializers for each trait's context
  val psc = pc.particleSpace
  val kc = pc.kinematic
} with Particle[Double,DenseVector[Double]] with KinematicParticleSpaceDVD {

  private val Logger = Logging.getLogger(sc.system, this)
  Logger.debug( "Particle {}", particleIndex)


  override def index = particleIndex
  override def dimension = pc.dimension



}