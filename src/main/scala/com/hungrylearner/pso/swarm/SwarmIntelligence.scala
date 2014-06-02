package com.hungrylearner.pso.swarm

/**
 * SwarmIntelligence encompasses multiple aspects of the swarms behavior. An implementation
 * can choose from different implementations of each aspect to get the desired behavior.
 *
 * The Id (from psychoanalysis) contains innate information and processes used by the other parts of a
 * SwarmIntelligence. Of these, bestPosition and actor context are most prominent.
 *
 * The Worker runs each iteration of the swarm. For a regional swarm the Worker is a Supervisor which
 * manages sub swarms (which themselves can be local or regional swarms).
 *
 * SocialInfluence determines what is done when a swarm learns of an InfluentialPosition from another swarm
 * in a multi-swarm system.
 *
 * ReportingStrategy (referenced in LocalId and RegionalId) determines which reports are reported by a swarm.
 * The implementation may choose different reporting strategies for local and regional swarms.
 *
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait SwarmIntelligence[F,P]
  extends Id[F,P]
  with Worker[F,P]
  with SocialInfluence[F,P]

/**
 * A LocalSwarmIntelligence has no children and manages a set of particles with local best.
 *
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait LocalSwarmIntelligence[F,P]
  extends SwarmIntelligence[F,P]
  with LocalId[F,P]
  with LocalWorker[F,P]
  with LocalSocialInfluence[F,P]

/**
 * A RegionalSwarmIntelligence has child swarms to supervise and presents itself as, more or less,
 * a single swarm to it's parent.
 *
 * RegionalReportingStrategy.
 *
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait RegionalSwarmIntelligence[F,P]
  extends SwarmIntelligence[F,P]
  with RegionalId[F,P]
  with RegionalSupervisor[F,P]
  with RegionalSocialInfluence[F,P]
