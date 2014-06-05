package com.hungrylearner.pso

import com.hungrylearner.pso.particle.EvaluatedPosition

/**
 * Created by flint on 5/23/14.
 */
package object swarm {

  trait Message
  trait Command extends Message
  trait Influence extends Message
  // also Report

  object CompletedType extends Enumeration {
    type CompletedType = Value
    val SwarmOneIterationCompleted = Value
    val SwarmAroundCompleted = Value
    val SwarmingCompleted = Value
  }


  /**
   * Swarm 1 iteration. During a SwarmAround, the swarm sends this message
   * to itself to perform each iteration. This message could also be sent
   * from a parent.
   */
  case object SwarmOneIteration extends Command

  /**
   * Swarm around for 1 to N iterations. We break the iterations into SwarmArounds so multiple
   * swarms aren't communicating bestParticle every iteration. This allows tuning
   * for computing performance on multiple cores and for search space optimizations.
   *
   * If we get a SwarmAround while already swarming around, we'll add it to a
   * pending list of SwarmArounds
   *
   * @param iterations Number of iterations to run for this step
   */
  case class SwarmAround( iterations: Int) extends Command

  /**
   * Cancel any swarming and report bestParticle so far with SwarmCompleted.
   */
  case object CancelSwarming extends Command


  /**
   * Tell a child swarm about an influential position. This is typically a local or global
   * best, but depends on the social strategy.
   *
   * @param evaluatedPosition
   * @param iteration The current iteration when message was sent.
   */
  case class InfluentialPosition[F,P]( evaluatedPosition: EvaluatedPosition[F,P], iteration: Int) extends Influence


  object State extends Enumeration {
    type State = Value
    val UNINTIALIZED = Value
    val INTIALIZED = Value
    val SWARMING_ONE_ITERATION = Value
    val SWARMING_AROUND = Value
    val RESTING = Value
    val SWARMING_COMPLETED = Value
  }

}
