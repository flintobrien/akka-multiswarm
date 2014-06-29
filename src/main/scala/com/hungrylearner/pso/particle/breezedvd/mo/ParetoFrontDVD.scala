package com.hungrylearner.pso.particle.breezedvd.mo

import com.hungrylearner.pso.particle.{ParetoFront, Position, MutablePosition}
import breeze.linalg.DenseVector
import scala.collection.mutable.ArrayBuffer
import sun.reflect.generics.reflectiveObjects.NotImplementedException

object ParetoFrontDVD {
  type DVD = DenseVector[Double]
}
import ParetoFrontDVD._

/**
 * Created by flint on 6/28/14.
 */
class ParetoFrontDVD  extends ParetoFront[DVD,DVD]
{
  import PositionDVD.doCompare

  protected val _frontier = new ArrayBuffer[Position[DVD,DVD]]()

  def this( initialPosition: Position[DVD,DVD]) = {
    this()
    _frontier += initialPosition
  }

  /**
   * Store position to the Pareto frontier if it's not dominated by any positions
   * in the current Pareto frontier. Remove any current positions that are dominated
   * by the new position.
   *
   * @param mPosition Position to add if not dominated by current Pareto frontier
   * @return True if the position was added.
   */
  override def storeMutablePositionIfNonDominated( mPosition: MutablePosition[DVD,DVD]): Option[Position[DVD,DVD]] = {
    val positionFunction = () => mPosition.toPosition
    storeIfNonDominated( mPosition.fitness, positionFunction)
  }
  override def storePositionIfNonDominated( position: Position[DVD,DVD]): Option[Position[DVD,DVD]] = {
    val positionFunction = () => position
    storeIfNonDominated( position.fitness, positionFunction)
  }

  protected def storeIfNonDominated( fitness: DVD, position: () => Position[DVD,DVD]): Option[Position[DVD,DVD]] = {
    var notDominated = true
    var result: Option[Position[DVD,DVD]] = None

    // foreach frontier position (fp)
    //   if fp < position then return None because the new position is dominated by someone.
    //   if fp == position (i.e. not dominated) then keep iterating
    //   if fp > position then
    //      if not already inserted in frontier, replace fp with new position.
    //      if already inserted, remove fp
    // If get to the end and not dominated and not inserted insert new position
    //

    var length = _frontier.length
    var index = 0
    while( notDominated && index < length) {
      val fp = _frontier(index)

      doCompare( fp.fitness, fitness) match {
        case 0 =>
          // not dominated by frontier position. Keep searching.

        case compare if compare < 0 =>
          notDominated = false

        case compare if compare > 0 =>
          // new position dominates current value
          if( result.isDefined) {
            _frontier.remove(index)
            index -= 1
            length -= 1
          } else {
            val pos = position()
            _frontier.update(index, pos) // replace old with new position
            result = Some(pos)
          }

      }

      index += 1
    }

    if( notDominated && result.isEmpty) {
      val pos = position()
      _frontier += pos
      result = Some(pos)
    }

    result
  }

  /**
   *
   * @param positions
   * @return The non-dominated positions that were added.
   */
  override def storePositionsIfNonDominated( positions: Seq[Position[DVD,DVD]]): Seq[Position[DVD,DVD]] = {
    for( p <- positions;
         opt = storePositionIfNonDominated( p) if (opt.isDefined)
       ) yield opt.get
  }

  /**
   * Return the current positions in the Pareto frontier.
   */
  override def frontier: Seq[Position[DVD,DVD]] = _frontier.toSeq

  /**
   * Need a best position to update a particle, but the Pareto frontier has lots of
   * positions. Pick a reasonable position to use based on the given position.
   *
   * @param position The position used to help determine which "best position" to return.
   * @return A single best position
   */
  override def getOneBestPosition( position: MutablePosition[DVD,DVD]) = {
    // TODO: need to figure out the actual best position to return
    // TODO: what if we have no positions? The constructor allows this.
    if( frontier.length <= 0)
      null
    else
      _frontier(0)
  }

  /**
   * Return the number of positions currently in the Pareto frontier.
   */
  override def length = _frontier.length

  override def ++( other: ParetoFront[DVD,DVD]) = throw new NotImplementedException

}
