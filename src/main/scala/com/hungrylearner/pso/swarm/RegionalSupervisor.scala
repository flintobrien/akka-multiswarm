package com.hungrylearner.pso.swarm

import akka.actor.ActorRef
import com.hungrylearner.pso.particle.EvaluatedPosition





/**
  * The RegionalSupervisor manages reports going up the swarm hierarchy and manages
  * how/when knowledge is shared among its children.
  *
  */
trait RegionalSupervisor[F,P] extends Supervisor[F,P] {
  this: RegionalId[F,P] =>

  import com.hungrylearner.pso.swarm.Report._

  /**
  * We received a ProgressReport from a child.
  *
  * - If it has a better position, update our bestPosition
  * - Send an updated report to the RegionalReporter
  * - Decide if/when to tell our children.
  *
  */
  override def onProgressReport( progressReport: ProgressReport[F,P], originator: ActorRef): Unit = {

    // Option of waiting for all children before sending to parent or children?
    // Send to parent: always, when all children completed for iteration, when position is better?
    // Send may be different for different completed types.

    val progressCount = incrementProgressCount( progressReport)
    val progress = makeProgress( progressCount)
    if( progress.children.completedCount > progress.children.outOf)
      Logger.error( s"RegionalSupervisor.onProgressReport( type:${progressReport.completedType}, childIndex:${progressReport.childIndex}, iteration: ${progressReport.iteration}}) completed, but already heard from ${progress.children.completedCount - 1} of ${progress.children.outOf} children for this iteration")

    val evaluatedPosition = evaluatePosition( progressReport)
    updateBestPosition( evaluatedPosition)
    reportingStrategy.reportForRegion( progressReport, childIndex, evaluatedPosition, progress)
    tellChildren( evaluatedPosition, progressReport.iteration, progress, originator)
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

  protected def incrementProgressCount( progressReport: ProgressReport[F,P]): Int
  protected def makeProgress( progressCount: Int): Progress


  protected def isBetterPosition( evaluatedPosition: EvaluatedPosition[F,P]): Boolean = {
     bestPosition == null || evaluatedPosition.position < bestPosition
  }

}
