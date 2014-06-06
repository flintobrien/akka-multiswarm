package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.swarm.Report.{Progress, ProgressReport}

trait TerminateCriteria[F,P] {
}

/**
 * Terminate criteria used by a LocalWorker.
 *
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait LocalTerminateCriteria[F,P] extends TerminateCriteria[F,P] {
  this: LocalId[F,P] =>
  def terminateCriteriaMet( iteration: Int): Boolean
}

trait LocalTerminateOnMaxIterations[F,P] extends LocalTerminateCriteria[F,P] {
  this: LocalId[F,P] =>

  override def terminateCriteriaMet( iteration: Int): Boolean = iteration >= config.context.iterations
}


/**
 * Terminate criteria used by a RegionalSupervisor.
 *
 * TODO: RegionalSupervisor is not using it yet.
 *
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait RegionalTerminateCriteria[F,P] extends TerminateCriteria[F,P] {
  this: RegionalId[F,P] =>
  def terminateCriteriaMet( childReport: ProgressReport[F,P], regionalProgress: Progress): Boolean
}

trait RegionalTerminateWhenOneChildTerminates[F,P] extends RegionalTerminateCriteria[F,P] {
  this: RegionalId[F,P] =>

  override def terminateCriteriaMet( childReport: ProgressReport[F,P], regionalProgress: Progress): Boolean =
    childReport.completedType == CompletedType.SwarmingCompleted
}

trait RegionalTerminateWhenAllChildrenTerminate[F,P] extends RegionalTerminateCriteria[F,P] {
  this: RegionalId[F,P] =>

  override def terminateCriteriaMet( childReport: ProgressReport[F,P], regionalProgress: Progress): Boolean =
    childReport.completedType == CompletedType.SwarmingCompleted && regionalProgress.completed
}

