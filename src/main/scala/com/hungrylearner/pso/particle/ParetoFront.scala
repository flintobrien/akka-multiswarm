package com.hungrylearner.pso.particle

/**
 * Container of positions that are part of the Pareto Frontier.
 */
trait ParetoFront[F,P] {

  /**
   * Store position to the Pareto frontier if it's not dominated by any positions
   * in the current Pareto frontier. Remove any current positions that are dominated
   * by the new position.
   *
   * @param position Position to add if not dominated by current Pareto frontier
   * @return True if the position was added.
   */
  def storePositionIfNonDominated( position: MutablePosition[F,P]): Option[Position[F,P]]
//  override def setIfBest( position: MutablePosition[F,P]): Option[Position[F,P]]

  /**
   *
   * @param position
   * @return The non-dominated positions that were added.
   */
  def storePositionsIfNonDominated( position: Seq[Position[F,P]]): Seq[Position[F,P]]

  def ++( other: ParetoFront[F,P]): ParetoFront[F,P]

  /**
   * Need a best position to update a particle, but the Pareto frontier has lots of
   * positions. Pick a reasonable position to use based on the given position.
   *
   * @param position The position used to help determine which "best position" to return.
   * @return A single best position
   */
  def getOneBestPosition( position: MutablePosition[F,P]): Position[F,P]

  /**
   * Return the number of positions currently in the Pareto frontier.
   */
  def positionCount: Integer

  /**
   * Return the current positions in the Pareto frontier.
   */
  def frontier: Seq[Position[F,P]]  // TODO: or iterator or array?
}

class ParetoFrontImpl[F,P] extends ParetoFront[F,P]
{
  /**
   * Store position to the Pareto frontier if it's not dominated by any positions
   * in the current Pareto frontier. Remove any current positions that are dominated
   * by the new position.
   *
   * @param position Position to add if not dominated by current Pareto frontier
   * @return True if the position was added.
   */
  override def storePositionIfNonDominated(position: MutablePosition[F, P]) = {None}

  /**
   *
   * @param position
   * @return The non-dominated positions that were added.
   */
  override def storePositionsIfNonDominated(position: Seq[Position[F, P]]) = {Seq()}

  /**
   * Return the current positions in the Pareto frontier.
   */
  override def frontier = ???

  /**
   * Need a best position to update a particle, but the Pareto frontier has lots of
   * positions. Pick a reasonable position to use based on the given position.
   *
   * @param position The position used to help determine which "best position" to return.
   * @return A single best position
   */
  override def getOneBestPosition(position: MutablePosition[F, P]) = {position.toPosition}

  /**
   * Return the number of positions currently in the Pareto frontier.
   */
  override def positionCount = {0}

  override def ++(other: ParetoFront[F, P]) = {other}
}