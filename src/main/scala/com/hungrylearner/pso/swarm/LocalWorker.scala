package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.particle.{EvaluatedPosition, Position, Particle}
import com.hungrylearner.pso.swarm.State._
import com.hungrylearner.pso.swarm.Report._


trait LocalWorker[F,P] extends Worker[F,P] {
  this: LocalId[F,P] with LocalSocialInfluence[F,P] =>

  override def onCommand(command: Command) = command match {
    case SwarmOneIteration => onOneIteration( bestPosition)
    case SwarmAround( iterations) => onSwarmAround( iterations)
    case ip: InfluentialPosition[F,P] => onInfluentialPosition( ip) // in LocalSocialInfluence
  }

  protected def onSwarmAround( iterations: Int)
  protected def onOneIteration( bestForIteration: Position[F,P])
}


/**
 * Created by flint on 6/1/14.
 */
trait LocalWorkerImpl[F,P] extends LocalWorker[F,P] {
  this: LocalId[F,P] with LocalSocialInfluence[F,P] =>

  /**
   * The initial position is iteration 0. Iteration is incremented to 1 at the start of the first iteration.
   */
  private val particles = List.tabulate[Particle[F,P]]( config.particleCount) { i => config.makeParticle( config.context, i) }
  override protected var bestPosition: Position[F,P] = particles.reduceLeft( (a, b) => a.fittest( b) ).position.toPosition
  protected var state = INTIALIZED
  protected var iteration = 0
  protected var iterationsLeftInSwarmAround = 0



  override protected def onSwarmAround( iterations: Int) = {
    // TODO: If already in SwarmAround, add this to a pending list of SwarmArounds
    Logger.debug( "LocalSwarm.onSwarmAround")
    require( bestPosition != null, "LocalSwarm.onSwarmAround: bestParticle not set. Probably because Init message not received.")

    iterationsLeftInSwarmAround = clipIterationsToConfiguredMax( iterations)

    if( iterationsLeftInSwarmAround > 0) {
      state = SWARMING_AROUND
      onOneIteration( bestPosition)
    } // TODO: else what should we do? Report invalid command?
  }

  override protected def onOneIteration( bestForIteration: Position[F,P]) = {
    if( state != SWARMING_AROUND)
      state = SWARMING_ONE_ITERATION
    iteration += 1
    particles.foreach{ p =>
      bestPosition = p.update( iteration, bestForIteration)
    }

    Logger.debug( "LocalSwarm.oneIteration end iteration={} bestParticle: {}", iteration, bestPosition.value)

    oneIterationCompleted()
  }

  def oneIterationCompleted() = {

    if( iteration < config.context.iterations) {

      if( state == SWARMING_AROUND) {
        iterationsLeftInSwarmAround -= 1
        if( iterationsLeftInSwarmAround > 0) {
          reportingStrategy.reportOneIterationCompleted( childIndex, EvaluatedPosition(bestPosition, isBest=true), iteration, ProgressOneOfOne)
          context.self ! SwarmOneIteration
        } else {
          swarmAroundCompleted()
        }
      } else {
        state = RESTING
        reportingStrategy.reportOneIterationCompleted( childIndex, EvaluatedPosition(bestPosition, isBest=true), iteration, ProgressOneOfOne)
      }

    } else {
      state = COMPLETE
      reportingStrategy.reportSwarmingCompleted( childIndex, EvaluatedPosition(bestPosition, isBest=true), iteration, ProgressOneOfOne)
    }
  }

  def swarmAroundCompleted() = {
    if( iteration < config.context.iterations) {
      state = RESTING
      reportingStrategy.reportSwarmAroundCompleted( childIndex, EvaluatedPosition(bestPosition, isBest=true), iteration, ProgressOneOfOne)
    } else {
      state = COMPLETE
      reportingStrategy.reportSwarmingCompleted(childIndex, EvaluatedPosition(bestPosition, isBest = true), iteration, ProgressOneOfOne)
    }
  }



  def clipIterationsToConfiguredMax( iterations: Int): Int = {
    math.min( config.context.iterations - iteration, iterations)
  }


}
