package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.particle.{PositionIteration, Position, Particle}
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

trait AbstractLocalWorker[F,P] extends LocalWorker[F,P] {
  this: LocalId[F,P] with LocalSocialInfluence[F,P] with LocalTerminateCriteria[F,P] =>

  import TerminateCriteriaStatus._

  /**
   * The initial position is iteration 0. Iteration is incremented to 1 at the start of the first iteration.
   */
  protected val particles = List.tabulate[Particle[F,P]]( config.particleCount) { i => config.particleFactory( childIndex, i, config.particleCount) }
  protected var iteration = 0
  protected var iterationsLeftInSwarmAround = 0

  def bestPositions: Seq[PositionIteration[F,P]]

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

  def swarmAroundCompleted( terminateCriteriaStatus: TerminateCriteriaStatus): Unit = {
    // TODO: We're testing terminateCriteriaStatus.isMet twice. Doesn't seem right.
    if( terminateCriteriaStatus.isMet) {
      state = SWARMING_COMPLETED
      reportingStrategy.reportSwarmingCompleted(childIndex, Seq( PositionIteration(bestPosition, iteration)), iteration, ProgressOneOfOne, terminateCriteriaStatus)
    } else {
      state = RESTING
      reportingStrategy.reportSwarmAroundCompleted( childIndex, Seq( PositionIteration(bestPosition, iteration)), iteration, ProgressOneOfOne)
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

  /**
 * Created by flint on 6/1/14.
 */
trait LocalWorkerImpl[F,P] extends AbstractLocalWorker[F,P] {
  this: LocalId[F,P] with LocalSocialInfluence[F,P] with LocalTerminateCriteria[F,P] =>

  import TerminateCriteriaStatus._

  override protected var bestPosition: Position[F,P] = particles.reduceLeft( (a, b) => a.fittest( b) ).position.toPosition
  override def bestPositions: Seq[PositionIteration[F,P]] = Seq( PositionIteration( bestPosition, iteration))

  override protected def onOneIteration( bestForIteration: Position[F,P]): Unit = {

    if( state != SWARMING_AROUND)
      state = SWARMING_ONE_ITERATION
    iteration += 1
    particles.foreach{ p =>
      // TODO: This may find several positions that are better than bestForIteration. Why not pass in bestPosition to find the very best?
      // TODO: Can we pick a position out of the pareto frontier to pass to update or just pass the whole Pareto frontier?
      p.update( iteration, bestForIteration) match {
        case Some(newPersonalBestPosition) =>
          if( newPersonalBestPosition < bestForIteration)
            bestPosition = newPersonalBestPosition
        case None =>
      }
    }

    Logger.debug( "LocalSwarm.oneIteration end iteration={} bestParticle: {}", iteration, bestPosition.value)

    oneIterationCompleted()
  }

  def oneIterationCompleted(): Unit = {

    val terminateCriteriaStatus = terminateCriteriaMet( iteration)
    // TODO: if iterations are done and terminate criteria is not met, we need to stop, but what do we report?
    if( terminateCriteriaStatus.isMet) {

      state = SWARMING_COMPLETED
      reportingStrategy.reportSwarmingCompleted( childIndex, bestPositions, iteration, ProgressOneOfOne, terminateCriteriaStatus)

    } else {

      if( state == SWARMING_AROUND) {
        iterationsLeftInSwarmAround -= 1
        if( iterationsLeftInSwarmAround > 0) {
          reportingStrategy.reportOneIterationCompleted( childIndex, bestPositions, iteration, ProgressOneOfOne)
          context.self ! SwarmOneIteration
        } else {
          swarmAroundCompleted( terminateCriteriaStatus)
        }
      } else {
        state = RESTING
        reportingStrategy.reportOneIterationCompleted( childIndex, bestPositions, iteration, ProgressOneOfOne)
      }
    }
  }

}
