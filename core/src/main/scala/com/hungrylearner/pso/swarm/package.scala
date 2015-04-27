package com.hungrylearner.pso

import com.hungrylearner.pso.particle.PositionIteration

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
    val SwarmingCompleted = Value  //  TerminateCriteria met.
    val SwarmingCancelled = Value
  }

  object TerminateCriteriaStatus extends Enumeration {
    type TerminateCriteriaStatus = Val
    protected case class Val(name: String, isMet: Boolean) extends super.Val(nextId, name) {
      def isNotMet = ! isMet
    }

    val TerminateCriteriaNotMet = Val( "TerminateCriteriaNotMet", isMet=false)
    val TerminateCriteriaMetNow = Val( "TerminateCriteriaMetNow", isMet=true)  // TerminateCriteria met during this report.
    val TerminateCriteriaMetPreviously = Val( "TerminateCriteriaMetPreviously", isMet=true)
  }


  /**
   * Swarm 1 iteration. During a SwarmAround, the swarm sends this message
   * to itself to perform each iteration. This message could also be sent
   * from a parent.
   */
  case object SwarmOneIteration extends Command

  /**
   * Swarm around for 1 to N iterations. Grouping a set of iterations into a SwarmAround gives
   * multiple swarms the opportunity to communicate bestParticle every few iterations (instead of
   * every iteration).
   *
   * TODO: If we get a SwarmAround while already swarming around, we'll add it to a pending list of SwarmArounds
   *
   * @param iterations Number of iterations to run for this SwarmAround to be completed.
   */
  case class SwarmAround( iterations: Int) extends Command

  /**
   * Cancel any swarming and report SwarmingCancelled with bestParticle so far.
   */
  case object CancelSwarming extends Command


  /**
   * Tell a child swarm about an influential position. This is typically a local or global
   * best, but depends on the social strategy.
   *
   * @param newBestPositions
   * @param iteration The current iteration when message was sent.
   */
  case class InfluentialPosition[F,P]( newBestPositions: Seq[PositionIteration[F,P]], iteration: Int) extends Influence


  /**
   * State is being used by a LocalWorker. Not sure what a Supervisor's state should be since it's
   * the cumulative state of it's children. If its a synchronized supervisor, it keeps all children
   * synchronized, so it would have a more deterministic state.
   */
  object State extends Enumeration {
    type State = Value
    val UNINTIALIZED = Value
    val INTIALIZED = Value
    val SWARMING_ONE_ITERATION = Value
    val SWARMING_AROUND = Value
    val RESTING = Value
    val SWARMING_CANCELLED = Value
    val SWARMING_COMPLETED = Value
  }

}
