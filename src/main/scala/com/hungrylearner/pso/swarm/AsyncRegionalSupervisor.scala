package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.particle.{Position, EvaluatedPosition}
import com.hungrylearner.pso.swarm.Report._
import akka.actor.ActorRef



object AsyncRegionalSupervisor {
  import CompletedType._

  def makeProgressCounts: collection.immutable.Map[CompletedType, collection.mutable.Map[Int,Int]] =
    collection.immutable.Map[CompletedType, collection.mutable.Map[Int,Int]](
      SwarmOneIterationCompleted -> collection.mutable.Map[Int,Int](),
      SwarmAroundCompleted -> collection.mutable.Map[Int,Int](),
      SwarmingCompleted -> collection.mutable.Map[Int,Int]()
    )

}


/**
 * Supervise a region of local swarms. Listen for task completed events, report progress to parents, and update children.
 *
 * This Supervisor is "asynchronous" in that each child can progress through multiple SwarmAround cycles without waiting on
 * other siblings to catch up. There is also no time coordination with reports. When a child reports a best position, we send
 * that immediately as an InfluentialPosition to each child (even if they are in the middle of a SwarmAround).
 *
 * A RegionalSupervisor can have different strategies for what to listen to and when to report to parents and children.
 * For example, it could decide to tell children when each SwarmAroundCompleted comes in or it could wait till all
 * children report (for a specific iteration), then tell children.
 *
 */
trait AsyncRegionalSupervisor[F,P] extends RegionalSupervisor[F,P] {
  this: RegionalId[F,P] =>

  import AsyncRegionalSupervisor._
  import CompletedType._

  // Keep track of progress counts for each CommandType for each reported iteration.
  // Seems like we should own this information, but the reporter is the only one using it now (via makeUpdatedTaskReport)
  // Map of CommandType to (map of Iteration to progress counts)
  //
  protected val progressCounts: collection.immutable.Map[CompletedType, collection.mutable.Map[Int,Int]] = makeProgressCounts

  override protected def makeProgress( progressCount: Int): Progress = {

    val childrenCompleted = ProgressFraction( progressCount, config.childCount)
    // TODO: No! This is assuming children are completed.
    val descendantsCompleted = ProgressFraction( progressCount * config.childrenConfig.descendantSwarmCount, config.descendantSwarmCount)
    val completed = progressCount >= config.childCount

    Progress( childrenCompleted, descendantsCompleted, completed)
  }


  /**
   * Tell children (aka subregions) when we have a better position.
   *
   * Tell children now or later. For example, we could wait until all children report
   * for the iteration, then send the bestPosition
   *
   * @param evaluatedPosition  The position given us by the RegionalSupervisor.
   * @param iteration The current iteration
   * @param progress  This swarm's current progress for the specified iteration
   * @param originator The child that originated the report which led to the evaluatedPosition.
   *                   The evaluatedPosition may or may not be the one sent from the originator.
   *                   This can be used when the Supervisor wants to send a new InfluentialPosition
   *                   to all children except the originator.
   */
  override protected def tellChildren( evaluatedPosition: EvaluatedPosition[F,P], iteration: Int, progress: Progress, originator: ActorRef) = {
    // For now, if we get some information about a fitter position, we tell the children.
    if( evaluatedPosition.isBest)
      sendToChildren( InfluentialPosition[F,P]( evaluatedPosition, iteration), originator) // Don't send to originator.
  }


  override protected def incrementProgressCount( progressReport: ProgressReport[F,P]): Int = {
    val counters = progressCounts.get( progressReport.completedType).get
    val childrenCompletedCount = counters.getOrElse( progressReport.iteration, 0) + 1
    counters += (progressReport.iteration -> childrenCompletedCount)
    childrenCompletedCount
  }

}
