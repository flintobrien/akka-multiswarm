package com.hungrylearner.pso.swarm

import akka.actor.ActorRef
import com.hungrylearner.pso.particle.PositionIteration
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
  this: RegionalId[F,P] with Ego[F,P] with RegionalTerminateCriteria[F,P] =>

  import RegionalSupervisor._
  import TerminateCriteriaStatus._
  import CompletedType._

  // Keep track of progress counts for each CommandType for each reported iteration.
  // The count is the count of descendants. We'll receive progress reports form children, but that doesn't mean
  // the child's children have completed that command/iteration.
  //
  protected val childProgressCounters = new ProgressCounters[F,P]
  protected val descendantProgressCounters = new ProgressCounters[F,P]

  /**
   * We received a ProgressReport from a child. Update our best position, report to our parent, and update our
   * children.
   */
  override def onProgressReport( childReport: ProgressReport[F,P], originator: ActorRef): Unit = {

    val regionalProgress = calculateRegionalProgress( childReport)
    val newBestPositions = storePositionsIfBest( childReport.newBestPositions)

    val terminateCriteriaStatus = terminateCriteriaMet( childReport, regionalProgress)
    if( terminateCriteriaStatus == TerminateCriteriaMetNow) {

      // We're not going to stop our child actors now. If some of the children are not completed,
      // send a CancelSwarming. Our parent needs to deal with stopping the whole actor tree later.
      //
      if( childrenHaveNotCompleted( childReport.completedType, regionalProgress))
        sendToChildren( CancelSwarming, originator)
    }

    // Report the position our child gave us. evaluatedPosition specifies whether it is our best or not.
    reportingStrategy.reportForRegion( childReport, childIndex, newBestPositions, regionalProgress, terminateCriteriaStatus)

    // If terminate criteria is not met, tell the children (except for the originator child who sent the position).
    // The implementer of tellChildren decides when and whether the children should be told. This decision can be based on
    // evaluatedPosition.isBest or other criteria.
    //
    if( terminateCriteriaStatus.isNotMet)
      tellChildren( newBestPositions, childReport.iteration, regionalProgress, originator)
  }

  protected def childrenHaveNotCompleted( completedType: CompletedType, regionalProgress: Progress) =
    ! (completedType == SwarmingCompleted && regionalProgress.completed)

  /**
   * A child has terminated.
   * @param child The child that terminated.
   */
  override def onTerminated(child: ActorRef) = {
    // If terminating children, let our parent know when they have all terminated.
    Logger.info( s"RegionalSwarmActor Terminated( child='${child.path.name}')")
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

}
