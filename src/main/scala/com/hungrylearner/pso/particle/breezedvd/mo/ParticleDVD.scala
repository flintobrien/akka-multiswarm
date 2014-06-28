package com.hungrylearner.pso.particle.breezedvd.mo

import com.hungrylearner.pso.swarm.SimulationContext
import com.hungrylearner.pso.particle.Particle
import breeze.linalg.DenseVector
import akka.event.Logging

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
 * Created by flint on 6/27/14.
 */
class ParticleDVD ( sc: SimulationContext, pc:ParticleContext, particleIndex: Int) extends {
  // Early initializers for each trait's context
  val psc = pc.particleSpace
  val kc = pc.kinematic
} with Particle[Double,DenseVector[Double]] with KinematicParticleSpaceDVD with MoPersonalBestStoreDVD {

  private val Logger = Logging.getLogger(sc.system, this)
  Logger.debug( "Particle {}", particleIndex)


  override def index = particleIndex
  override def dimension = pc.dimension



}
