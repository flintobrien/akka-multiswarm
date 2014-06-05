package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.particle.Particle
import akka.actor.ActorContext

abstract class SwarmConfig[F,P]( val childCount: Int, val context: SimulationContext) {
  val descendantParticleCount: Int
  val descendantSwarmCount: Int
}

class RegionalSwarmConfig[F,P]( childCount: Int,
                             val childrenConfig: SwarmConfig[F,P],
                             val childName: String,
                             val makeChildSwarm: (ActorContext,Int)=>SwarmActor[F,P],
                             override val context: SimulationContext) extends SwarmConfig[F,P]( childCount, context) {

  override val descendantParticleCount = childCount * childrenConfig.descendantParticleCount
  override val descendantSwarmCount = childCount * childrenConfig.descendantSwarmCount
}

/**
 *
 * @param particleCount The number of particles in this swarm.
 * @param makeParticle (swarmIndex, particleIndex, particleCount) => Particle[F,P]
 *                     The swarmIndex is useful when each swarm needs to search different regions
 *                     of the overall particle space.
 * @param context SimulationContext
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
class LocalSwarmConfig[F,P]( val particleCount: Int,
                             val makeParticle: (Int, Int, Int) => Particle[F,P],
                             override val context: SimulationContext) extends SwarmConfig[F,P]( particleCount, context) {

  override val descendantParticleCount = particleCount
  override val descendantSwarmCount = 1
}
