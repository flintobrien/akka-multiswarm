package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.particle.PositionIteration

object Report {
  import CompletedType._
  import TerminateCriteriaStatus._

  case class ProgressFraction( completedCount: Int, outOf: Int)
  case class Progress( children: ProgressFraction, descendantSwarms: ProgressFraction, completed: Boolean)
  val ProgressOneOfOne = Progress( ProgressFraction(1,1), ProgressFraction(1,1), completed=true)


  /**
   * A task completed message sent from child. The task that completed is one of
   * ONE_ITERATION, SWARM_AROUND, or SWARMING.
   *
   * @param completedType Task of type ONE_ITERATION, SWARM_AROUND, or SWARMING has completed.
   * @param childIndex Index if the child that sent this report
   * @param newBestPositions New best positions that have not been previously reported.
   * @param iteration The iteration when the report was generated. This is an absolute number across all
   *                  reports.
   * @param progress Progress statistics
   * @tparam F Fitness type
   * @tparam P Position value type
   */
  case class ProgressReport[F,P]( completedType: CompletedType,
                                  childIndex: Int,
                                  newBestPositions: Seq[PositionIteration[F,P]],
                                  iteration: Int,
                                  progress: Progress,
                                  terminateCriteriaStatus: TerminateCriteriaStatus
                                  ) extends Message

}
