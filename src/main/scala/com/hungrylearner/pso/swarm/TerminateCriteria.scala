package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.swarm.Report.{Progress, ProgressReport}
import TerminateCriteriaStatus._

trait TerminateCriteria[F,P] {
  protected var tcStatus = TerminateCriteriaNotMet
  def terminateCriteriaStatus = tcStatus
}

/**
 * Terminate criteria used by a LocalWorker.
 *
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait LocalTerminateCriteria[F,P] extends TerminateCriteria[F,P] {
  this: LocalId[F,P] =>

  protected def terminateCriteriaTest( iteration: Int): Boolean

  def terminateCriteriaMet( iteration: Int): TerminateCriteriaStatus = {
    tcStatus match {
      case TerminateCriteriaNotMet =>
        if( terminateCriteriaTest( iteration))
          tcStatus = TerminateCriteriaMetNow
      case TerminateCriteriaMetNow =>
        tcStatus = TerminateCriteriaMetPreviously
      case TerminateCriteriaMetPreviously =>
    }
    tcStatus
  }
}

trait LocalTerminateOnMaxIterations[F,P] extends LocalTerminateCriteria[F,P] {
  this: LocalId[F,P] =>

  override protected def terminateCriteriaTest( iteration: Int): Boolean =
    iteration >= config.context.iterations
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

  protected def terminateCriteriaTest( childReport: ProgressReport[F,P], regionalProgress: Progress): Boolean

  def terminateCriteriaMet( childReport: ProgressReport[F,P], regionalProgress: Progress): TerminateCriteriaStatus = {
    tcStatus match {
      case TerminateCriteriaNotMet =>
        if( terminateCriteriaTest( childReport, regionalProgress))
          tcStatus = TerminateCriteriaMetNow
      case TerminateCriteriaMetNow =>
        tcStatus = TerminateCriteriaMetPreviously
      case TerminateCriteriaMetPreviously =>
    }
    tcStatus
  }
}

trait RegionalTerminateOnOneSwarmingCompleted[F,P] extends RegionalTerminateCriteria[F,P] {
  this: RegionalId[F,P] =>

  override protected def terminateCriteriaTest( childReport: ProgressReport[F,P], regionalProgress: Progress): Boolean =
    childReport.completedType == CompletedType.SwarmingCompleted
}

trait RegionalTerminateOnAllSwarmingCompleted[F,P] extends RegionalTerminateCriteria[F,P] {
  this: RegionalId[F,P] =>

  override protected def terminateCriteriaTest( childReport: ProgressReport[F,P], regionalProgress: Progress): Boolean =
    childReport.completedType == CompletedType.SwarmingCompleted && regionalProgress.completed
}

