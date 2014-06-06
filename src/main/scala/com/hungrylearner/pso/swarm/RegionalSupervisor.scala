package com.hungrylearner.pso.swarm

import akka.actor.ActorRef
import com.hungrylearner.pso.particle.EvaluatedPosition
import com.hungrylearner.pso.swarm.Report._


object RegionalSupervisor {
  import CompletedType._

  def makeProgressCounts: collection.immutable.Map[CompletedType, collection.mutable.Map[Int,Int]] =
    collection.immutable.Map[CompletedType, collection.mutable.Map[Int,Int]](
      SwarmOneIterationCompleted -> collection.mutable.Map[Int,Int](),
      SwarmAroundCompleted -> collection.mutable.Map[Int,Int](),
      SwarmingCompleted -> collection.mutable.Map[Int,Int]()
    )

  /**
   * Keep track of progress counts for each CommandType for each reported iteration.
   *
   * @tparam F Fitness
   * @tparam P Particle backing store
   */
  class ProgressCounters[F,P] {

    // Map of CompletedType to (map of Iteration to descendant progress counts)
    val counters: collection.immutable.Map[CompletedType, collection.mutable.Map[Int,Int]] = makeProgressCounts

    /**
     * return the current progress count for the CompletedType/iteration
     * @param progressReport The progress report from one of our children.
     * @return The current count
     */
    def progressCount( progressReport: ProgressReport[F,P]): Int = {
      val completedTypeCounters = counters.get( progressReport.completedType).get
      completedTypeCounters.getOrElse( progressReport.iteration, 0)
    }

    /**
     * Increment the progress count for the CompletedType/iteration. Return the new count.
     * @param progressReport The progress report from one of our children.
     * @return The incremented count
     */
    def incrementProgressCount( progressReport: ProgressReport[F,P]): Int = {
      val completedTypeCounters = counters.get( progressReport.completedType).get
      val completedCount = completedTypeCounters.getOrElse( progressReport.iteration, 0) + 1
      completedTypeCounters += (progressReport.iteration -> completedCount)
      completedCount
    }

  }
}


/**
  * The RegionalSupervisor manages reports going up the swarm hierarchy and manages
  * how/when knowledge is shared among its children.
  *
  */
trait RegionalSupervisor[F,P] extends Supervisor[F,P] {
  this: RegionalId[F,P] with RegionalTerminateCriteria[F,P] =>

  import RegionalSupervisor._

  // Keep track of progress counts for each CommandType for each reported iteration.
  // The count is the count of descendants. We'll receive progress reports form children, but that doesn't mean
  // the child's children have completed that command/iteration.
  //
  protected val childProgressCounters = new ProgressCounters[F,P]
  protected val descendantProgressCounters = new ProgressCounters[F,P]

  /**
  * We received a ProgressReport from a child.
  *
  * - If it has a better position, update our bestPosition
  * - Send an updated report to the RegionalReporter
  * - Decide if/when to tell our children.
  *
  */
  override def onProgressReport( childReport: ProgressReport[F,P], originator: ActorRef): Unit = {

    val regionalProgress = calculateRegionalProgress( childReport)
    val evaluatedPosition = evaluatePosition( childReport)
    updateBestPosition( evaluatedPosition)

    if( terminateCriteriaMet( childReport, regionalProgress)) {
      // TODO: Set our state and terminate all children.
      // How does this change the regional report we send out?
    }
    // TODO: We're sending the child's position. Shouldn't we be sending our best?
    reportingStrategy.reportForRegion( childReport, childIndex, evaluatedPosition, regionalProgress)

    // TODO: We're sending the child's position. Shouldn't we telling children about our best
    // or not telling if they already have it?
    tellChildren( evaluatedPosition, childReport.iteration, regionalProgress, originator)
  }

  def updateBestPosition( evaluatedPosition: EvaluatedPosition[F,P]) =
    if( evaluatedPosition.isBest || bestPosition == null)
      bestPosition = evaluatedPosition.position



  /**
  * Decide whether the progress report's position is better than our bestPosition.
  *
  * @param progressReport Progress report from child
  * @return Our evaluation of the reported position compared to our bestPosition
  */
  def evaluatePosition( progressReport: ProgressReport[F,P]): EvaluatedPosition[F,P] = {
    val isRegionalBest = isBetterPosition( progressReport.evaluatedPosition)
    EvaluatedPosition( progressReport.evaluatedPosition.position, isRegionalBest)
  }

  protected def calculateRegionalProgress( progressReport: ProgressReport[F,P]): Progress = {
    val descendantCompletedCount = descendantProgressCounters.incrementProgressCount( progressReport)
    val descendantProgress = ProgressFraction( descendantCompletedCount, config.descendantSwarmCount)

    val childCompletedCount = if( progressReport.progress.completed)
      childProgressCounters.incrementProgressCount( progressReport)
    else
      childProgressCounters.progressCount( progressReport)
    val childProgress = ProgressFraction( childCompletedCount, config.childCount)

    val completed = childCompletedCount >= config.childCount

    if( descendantCompletedCount > config.descendantSwarmCount)
      Logger.error( s"RegionalSupervisor.onProgressReport:  childIndex:${progressReport.childIndex}, type:${progressReport.completedType}, iteration:${progressReport.iteration} descendantCompletedCount:${descendantCompletedCount} is greater than descendantSwarmCount:${config.descendantSwarmCount} for this iteration")

    Progress( childProgress, descendantProgress, completed)
  }


  protected def isBetterPosition( evaluatedPosition: EvaluatedPosition[F,P]): Boolean = {
     bestPosition == null || evaluatedPosition.position < bestPosition
  }

}
