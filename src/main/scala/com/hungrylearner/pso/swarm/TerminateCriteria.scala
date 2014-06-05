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


trait RegionalTerminateCriteria[F,P] extends TerminateCriteria[F,P] {
  this: RegionalId[F,P] =>
}

trait RegionalTerminateOnMaxIterations[F,P] extends RegionalTerminateCriteria[F,P] {
  this: RegionalId[F,P] =>

  override def terminateCriteriaMet( iteration: Int): Boolean = iteration >= config.context.iterations
}

