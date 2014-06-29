package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.particle.{PositionIteration, Position, Particle}
import com.hungrylearner.pso.swarm.State._
import com.hungrylearner.pso.swarm.Report._


trait LocalWorker[F,P] extends Worker[F,P] {
  this: LocalId[F,P] with LocalSocialInfluence[F,P] =>

  protected var state = INTIALIZED

  override def onCommand(command: Command) = command match {

    case SwarmOneIteration =>
      if (notDone) onOneIteration

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
  protected def onOneIteration: Unit
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

  def bestPositionsForReport: Seq[PositionIteration[F,P]]

  override protected def onSwarmAround( iterations: Int): Unit = {

    // TODO: If already in SwarmAround, add this to a pending list of SwarmArounds
    Logger.debug( "LocalSwarm.onSwarmAround")

    iterationsLeftInSwarmAround = clipIterationsToConfiguredMax( iterations)

    if( state != SWARMING_COMPLETED && iterationsLeftInSwarmAround > 0) {
      state = SWARMING_AROUND
      onOneIteration
    } // TODO: else what should we do? Report invalid command?
  }

  def oneIterationCompleted(): Unit = {

    val terminateCriteriaStatus = terminateCriteriaMet( iteration)
    // TODO: if iterations are done and terminate criteria is not met, we need to stop, but what do we report?
    if( terminateCriteriaStatus.isMet) {

      state = SWARMING_COMPLETED
      reportingStrategy.reportSwarmingCompleted( childIndex, bestPositionsForReport, iteration, ProgressOneOfOne, terminateCriteriaStatus)

    } else {

      if( state == SWARMING_AROUND) {
        iterationsLeftInSwarmAround -= 1
        if( iterationsLeftInSwarmAround > 0) {
          reportingStrategy.reportOneIterationCompleted( childIndex, bestPositionsForReport, iteration, ProgressOneOfOne)
          context.self ! SwarmOneIteration
        } else {
          swarmAroundCompleted( terminateCriteriaStatus)
        }
      } else {
        state = RESTING
        reportingStrategy.reportOneIterationCompleted( childIndex, bestPositionsForReport, iteration, ProgressOneOfOne)
      }
    }
  }

  def swarmAroundCompleted( terminateCriteriaStatus: TerminateCriteriaStatus): Unit = {
    // TODO: We're testing terminateCriteriaStatus.isMet twice. Doesn't seem right.
    if( terminateCriteriaStatus.isMet) {
      state = SWARMING_COMPLETED
      reportingStrategy.reportSwarmingCompleted(childIndex, bestPositionsForReport, iteration, ProgressOneOfOne, terminateCriteriaStatus)
    } else {
      state = RESTING
      reportingStrategy.reportSwarmAroundCompleted( childIndex, bestPositionsForReport, iteration, ProgressOneOfOne)
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
trait SingleLocalWorker[F,P] extends AbstractLocalWorker[F,P] {
  this: LocalId[F,P] with SingleEgo[F,P] with LocalSocialInfluence[F,P] with LocalTerminateCriteria[F,P] =>

  particles.foreach( particle => storeMutablePositionIfBest( particle.position))
//  storePositionIfBest( particles.reduceLeft( (a, b) => a.fittest( b) ).position.toPosition)

  override def bestPositionsForReport: Seq[PositionIteration[F,P]] = Seq( PositionIteration( bestPosition, iteration))

  override protected def onOneIteration: Unit = {

    if( state != SWARMING_AROUND)
      state = SWARMING_ONE_ITERATION
    iteration += 1
    particles.foreach{ p =>
      // TODO: Can we pick a position out of the pareto frontier to pass to update or just pass the whole Pareto frontier?
      p.update( iteration, bestPosition) match {
        case Some(newPersonalBestPosition) =>
          storePositionIfBest( newPersonalBestPosition)
        case None =>
      }
    }

    Logger.debug( s"LocalSwarm.oneIteration end iteration=$iteration bestPosition: ${bestPosition.value}")

    oneIterationCompleted()
  }

}
