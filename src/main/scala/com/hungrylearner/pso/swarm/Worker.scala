package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.swarm.Report.{Progress, ProgressReport}
import akka.actor.ActorRef
import com.hungrylearner.pso.particle.PositionIteration

/**
 * A Worker does the actual work during swarming. Workers work and don't supervise other
 * workers.
 *
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait Worker[F,P]

/**
 * A Supervisor supervises child workers (or child supervisors) by evaluating each ProgressReport
 * and deciding what to tell higher supervisors and children.
 *
 * TODO: Should we have a state? It might be nice to report COMPLETE.
 *
 * TODO: When do we clean up child actors?
 *
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait Supervisor[F,P] extends Worker[F,P] {

  /**
   * We received a ProgressReport from a child. We can use it to update our best position,
   * tell our parent, tell our children, or do nothing with this report. We can also decide
   * to wait for all children to catch up and complete the specified iteration before doing
   * anything.
   */
  def onProgressReport( childReport: ProgressReport[F,P], originator: ActorRef): Unit

  /**
   * A child has terminated.
   * @param child The child that terminated.
   */
  def onTerminated( child: ActorRef)

  /**
   * Decide if and when to influence children by sending them knowledge of a (potentially)
   * better position. The position sent is usually the region's current bestPosition.
   *
   * @param newBestPositions New best positions that have never been reported.
   * @param iteration The iteration of the child when the progress report was generated.
   * @param progress The current progress of our children for the reported iteration. This can be
   *                 used in the decision of when to influence children. For example, we could
   *                 wait on influencing children until all children have reported their progress
   *                 has completed for the given command and iteration.
   */
  protected def tellChildren( newBestPositions: Seq[PositionIteration[F,P]], iteration: Int, progress: Progress, originator: ActorRef)

}
