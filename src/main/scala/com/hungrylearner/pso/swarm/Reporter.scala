package com.hungrylearner.pso.swarm

import Report._
import TerminateCriteriaStatus._
import com.hungrylearner.pso.particle.PositionIteration

/**
 * ReportingStrategy determines which reports are reported by a swarm. The implementation may choose different
 * reporting strategies for local and regional swarms.
 *
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait ReportingStrategy[F,P] {
  def report( progressReport: ProgressReport[F,P])
}

trait LocalReportingStrategy[F,P] extends ReportingStrategy[F,P]{
  def reportOneIterationCompleted( childIndex: Int, newBestPositions: Seq[PositionIteration[F,P]], iteration: Int, progress: Progress): Unit
  def reportSwarmAroundCompleted( childIndex: Int, newBestPositions: Seq[PositionIteration[F,P]], iteration: Int, progress: Progress): Unit
  def reportSwarmingCompleted( childIndex: Int, newBestPositions: Seq[PositionIteration[F,P]], iteration: Int, progress: Progress, terminateCriteriaStatus: TerminateCriteriaStatus): Unit
}

trait RegionalReportingStrategy[F,P] extends ReportingStrategy[F,P] {

  def reportForRegion( progressReport: ProgressReport[F,P], childIndex: Int, newBestPositions: Seq[PositionIteration[F,P]], progress: Progress, terminateCriteriaStatus: TerminateCriteriaStatus)
}
