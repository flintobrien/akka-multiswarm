package com.hungrylearner.pso.swarm

trait TerminateCriteria[F,P] {
  this: Id[F,P] =>
  def terminateCriteriaMet( iteration: Int): Boolean
}

//trait TerminateOnMaxIterations[F,P] extends TerminationCriteria[F,P] {
//  this: Id[F,P] =>
//
//  override def terminationCriteriaMet( iteration: Int): Boolean = iteration >= config.context.iterations
//}

trait LocalTerminateCriteria[F,P] extends TerminateCriteria[F,P] {
  this: LocalId[F,P] =>
}

trait LocalTerminateOnMaxIterations[F,P] extends LocalTerminateCriteria[F,P] {
  this: LocalId[F,P] =>

  override def terminateCriteriaMet( iteration: Int): Boolean = iteration >= config.context.iterations
}


/**
 * TODO: How should this be used? The current RegionalSupervisor doesn't test for TerminateCriteria,
 * it just gets reports from below. Shouldn't we test to see that all children have met the
 * criteria, then do something nice? Does RegionalSupervisor need a state to report on?
 *
 * OR maybe we don't need this at all! Maybe it's just the Progress.Completed that says when we're done.
 *
 * @tparam F
 * @tparam P
 */
trait RegionalTerminateCriteria[F,P] extends TerminateCriteria[F,P] {
  this: RegionalId[F,P] =>
}

trait RegionalTerminateOnMaxIterations[F,P] extends RegionalTerminateCriteria[F,P] {
  this: RegionalId[F,P] =>

  override def terminateCriteriaMet( iteration: Int): Boolean = iteration >= config.context.iterations
}

