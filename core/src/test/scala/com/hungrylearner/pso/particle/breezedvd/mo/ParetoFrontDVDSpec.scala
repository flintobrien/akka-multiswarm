package com.hungrylearner.pso.particle.breezedvd.mo

import org.specs2.mutable._
import org.specs2.mock.Mockito
import breeze.linalg.{DenseVector, sum}
import breeze.numerics.abs

/**
 * Created by flint on 6/29/14.
 */
class ParetoFrontDVDSpec extends Specification with BeforeAfter with Mockito {

  override def before = {
  }
  override def after = {
  }

  // Position value vector
  def v( args: Double*): DenseVector[Double] = DenseVector[Double](args.toArray)

  // Position fitness vector
  def f( args: Double*): DenseVector[Double] = DenseVector[Double](args.toArray)

  "ParetoFrontDVD" should {

    "  Store initial position" in {
      val position = new PositionDVD( v(0), f(0))
      val front = new ParetoFrontDVD( position)
      front.frontier.length must beEqualTo( 1)
      front.frontier(0) must be( position)
    }

    "  Replace position with better" in {
      val position = new PositionDVD( v(1), f(1))
      val front = new ParetoFrontDVD( position)

      val positionBetter = new PositionDVD( v(0), f(0))
      val result = front.storePositionIfNonDominated( positionBetter)
      result.isDefined must beTrue
      result must beEqualTo( Some( positionBetter))
      front.frontier.length must beEqualTo( 1)
      front.frontier(0) must be( positionBetter)
    }

    "  Not replace position with worse" in {
      val position = new PositionDVD( v(0), f(0))
      val front = new ParetoFrontDVD( position)

      val positionBetter = new PositionDVD( v(1), f(1))
      val result = front.storePositionIfNonDominated( positionBetter)
      result must beNone
      front.frontier.length must beEqualTo( 1)
      front.frontier(0) must be( position)
    }

    "  Store second position that is not dominated by first" in {
      val position = new PositionDVD( v(1,2), f(1,2))
      val front = new ParetoFrontDVD( position)

      val position2 = new PositionDVD( v(2,1), f(2,1))
      val result = front.storePositionIfNonDominated( position2)
      result must beEqualTo( Some( position2))
      front.frontier.length must beEqualTo( 2)
      front.frontier(0) must be( position)
      front.frontier(1) must be( position2)
    }

    "  Dimension 2 - Replace second position and remove third position that are dominated by new position" in {
      val position1 = new PositionDVD( v(4,1), f(4,1))
      val position2 = new PositionDVD( v(3,2), f(3,2))
      val position3 = new PositionDVD( v(2,4), f(2,4))
      val position4 = new PositionDVD( v(2,2), f(2,2)) // replaces position2 and position3

      val front = new ParetoFrontDVD( position1)

      var result = front.storePositionIfNonDominated( position2)
      result must beEqualTo( Some( position2))
      result = front.storePositionIfNonDominated( position3)
      result must beEqualTo( Some( position3))
      front.frontier.length must beEqualTo( 3)

      result = front.storePositionIfNonDominated( position4)
      result must beEqualTo( Some( position4))
      front.frontier.length must beEqualTo( 2)
      front.frontier(0) must be( position1)
      front.frontier(1) must be( position4)
    }

    "  Dimension 3 - Replace second position and remove third position that are dominated by new position" in {
      val position1 = new PositionDVD( v(1,2,3), f(1,2,3))
      val position2 = new PositionDVD( v(9,1,8), f(9,1,8))
      val position3 = new PositionDVD( v(8,1,9), f(8,1,9))
      val position4 = new PositionDVD( v(4,1,4), f(4,1,4)) // replaces position2 and position3

      val front = new ParetoFrontDVD( position1)

      var result = front.storePositionIfNonDominated( position2)
      result must beEqualTo( Some( position2))
      result = front.storePositionIfNonDominated( position3)
      result must beEqualTo( Some( position3))
      front.frontier.length must beEqualTo( 3)

      result = front.storePositionIfNonDominated( position4)
      result must beEqualTo( Some( position4))
      front.frontier.length must beEqualTo( 2)
      front.frontier(0) must be( position1)
      front.frontier(1) must be( position4)
    }

  }
}
