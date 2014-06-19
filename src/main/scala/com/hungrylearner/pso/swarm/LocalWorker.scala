package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.particle.{EvaluatedPosition, Position, Particle}
import com.hungrylearner.pso.swarm.State._
import com.hungrylearner.pso.swarm.Report._


trait LocalWorker[F,P] extends Worker[F,P] {
  this: LocalId[F,P] with LocalSocialInfluence[F,P] =>

  protected var state = INTIALIZED

  override def onCommand(command: Command) = command match {

    case SwarmOneIteration =>
      if (notDone) onOneIteration( bestPosition)

    case SwarmAround( iterations) =>
      if (notDone) onSwarmAround( iterations)

    case CancelSwarming =>
      if (notDone) onCancelSwarming()

    case ip: InfluentialPosition[F,P] =>
      if( notDone) onInfluentialPosition( ip) // in LocalSocialInfluence

    case _ =>
      Logger.error( s"LocalWorker unknown command $command")
  }

  protected def notDone = state != SWARMING_CANCELLED && state != SWARMING_COMPLETED

  protected def onSwarmAround( iterations: Int): Unit
  protected def onOneIteration( bestForIteration: Position[F,P]): Unit
  protected def onCancelSwarming(): Unit
}


/**
 * Created by flint on 6/1/14.
 */
trait LocalWorkerImpl[F,P] extends LocalWorker[F,P] {
  this: LocalId[F,P] with LocalSocialInfluence[F,P] with LocalTerminateCriteria[F,P] =>

  import TerminateCriteriaStatus._

  /**
   * The initial position is iteration 0. Iteration is incremented to 1 at the start of the first iteration.
   */
  private val particles = List.tabulate[Particle[F,P]]( config.particleCount) { i => config.particleFactory( childIndex, i, config.particleCount) }
  override protected var bestPosition: Position[F,P] = particles.reduceLeft( (a, b) => a.fittest( b) ).position.toPosition
  protected var iteration = 0
  protected var iterationsLeftInSwarmAround = 0



  override protected def onSwarmAround( iterations: Int): Unit = {

    // TODO: If already in SwarmAround, add this to a pending list of SwarmArounds
    Logger.debug( "LocalSwarm.onSwarmAround")
    require( bestPosition != null, "LocalSwarm.onSwarmAround: bestParticle not set. Probably because Init message not received.")

    iterationsLeftInSwarmAround = clipIterationsToConfiguredMax( iterations)

    if( state != SWARMING_COMPLETED && iterationsLeftInSwarmAround > 0) {
      state = SWARMING_AROUND
      onOneIteration( bestPosition)
    } // TODO: else what should we do? Report invalid command?
  }

  override protected def onOneIteration( bestForIteration: Position[F,P]): Unit = {

    if( state != SWARMING_AROUND)
      state = SWARMING_ONE_ITERATION
    iteration += 1
    particles.foreach{ p =>
      // TODO: This may find several positions that are better than bestForIteration. Why not pass in bestPosition to find the very best?
      bestPosition = p.update( iteration, bestForIteration)
    }

    Logger.debug( "LocalSwarm.oneIteration end iteration={} bestParticle: {}", iteration, bestPosition.value)

    oneIterationCompleted()
  }

  def oneIterationCompleted(): Unit = {

    val terminateCriteriaStatus = terminateCriteriaMet( iteration)
    // TODO: if iterations are done and terminate criteria is not met, we need to stop, but what do we report?
    if( terminateCriteriaStatus.isMet) {

      state = SWARMING_COMPLETED
      reportingStrategy.reportSwarmingCompleted( childIndex, EvaluatedPosition(bestPosition, isBest=true), iteration, ProgressOneOfOne, terminateCriteriaStatus)

    } else {

      if( state == SWARMING_AROUND) {
        iterationsLeftInSwarmAround -= 1
        if( iterationsLeftInSwarmAround > 0) {
          reportingStrategy.reportOneIterationCompleted( childIndex, EvaluatedPosition(bestPosition, isBest=true), iteration, ProgressOneOfOne)
          context.self ! SwarmOneIteration
        } else {
          swarmAroundCompleted( terminateCriteriaStatus)
        }
      } else {
        state = RESTING
        reportingStrategy.reportOneIterationCompleted( childIndex, EvaluatedPosition(bestPosition, isBest=true), iteration, ProgressOneOfOne)
      }
    }
  }

  def swarmAroundCompleted( terminateCriteriaStatus: TerminateCriteriaStatus): Unit = {
    // TODO: We're testing terminateCriteriaStatus.isMet twice. Doesn't seem right.
    if( terminateCriteriaStatus.isMet) {
      state = SWARMING_COMPLETED
      reportingStrategy.reportSwarmingCompleted(childIndex, EvaluatedPosition(bestPosition, isBest = true), iteration, ProgressOneOfOne, terminateCriteriaStatus)
    } else {
      state = RESTING
      reportingStrategy.reportSwarmAroundCompleted( childIndex, EvaluatedPosition(bestPosition, isBest=true), iteration, ProgressOneOfOne)
    }
  }


  override protected def onCancelSwarming(): Unit = {
    state = SWARMING_CANCELLED
    Logger.info( s"LocalWorkerImpl.onCancelSwarming state = SWARMING_CANCELLED for ${context.self.path}")
  }

  def clipIterationsToConfiguredMax( iterations: Int): Int = {
    math.min( config.context.iterations - iteration, iterations)
  }


}
