package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.particle.Particle
import akka.actor.ActorContext

abstract class SwarmConfig[F,P]( val childCount: Int) {
  val descendantParticleCount: Int
  val descendantSwarmCount: Int
}

class RegionalSwarmConfig[F,P]( childCount: Int,
                             val childrenConfig: SwarmConfig[F,P],
                             val childName: String,
                             val makeChildSwarm: (ActorContext,Int)=>SwarmActor[F,P],
//                             val makeChildSwarm: (SwarmConfig[F,P],Int)=>Swarm[F,P],
//                             val childSwarmIntelligenceFactory: (ActorContext,Int) => SwarmIntelligence[F,P],
                             val context: SimulationContext) extends SwarmConfig[F,P]( childCount) {

  override val descendantParticleCount = childCount * childrenConfig.descendantParticleCount
  override val descendantSwarmCount = childCount * childrenConfig.descendantSwarmCount
}


class LocalSwarmConfig[F,P]( val particleCount: Int,
                             val makeParticle: (SimulationContext, Int) => Particle[F,P],
                             val context: SimulationContext) extends SwarmConfig[F,P]( particleCount) {

  override val descendantParticleCount = particleCount
  override val descendantSwarmCount = 1
}
