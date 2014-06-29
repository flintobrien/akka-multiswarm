package com.hungrylearner.pso.particle.breezedvd.mo

import org.specs2.mutable._
import org.specs2.mock.Mockito
import breeze.linalg.{DenseVector, sum}
import breeze.numerics.abs

/**
 * Created by flint on 6/29/14.
 */
class PositionDVDSpec extends Specification with BeforeAfter with Mockito {

  override def before = {
  }
  override def after = {
  }

  "object PositionDVD" should {
    import PositionDVD._

    "  doCompare returns 0 when fitness are equal" in {
      doCompare(
        DenseVector[Double](0),
        DenseVector[Double](0)) must beEqualTo( 0)

      doCompare(
        DenseVector[Double](1,2),
        DenseVector[Double](1,2)) must beEqualTo( 0)
    }

    "  doCompare returns -1 when fitness are less than" in {
      doCompare(
        DenseVector[Double](0),
        DenseVector[Double](1)) must beEqualTo( -1)

      doCompare(
        DenseVector[Double](1,2),
        DenseVector[Double](3,4)) must beEqualTo( -1)
    }

    "  doCompare returns 1 when fitness are greater than" in {
      doCompare(
        DenseVector[Double](1),
        DenseVector[Double](0)) must beEqualTo( 1)

      doCompare(
        DenseVector[Double](3,4),
        DenseVector[Double](1,2)) must beEqualTo( 1)
    }

    "  doCompare returns 0 when fitness are not dominated in at least on dimension" in {
      doCompare(
        DenseVector[Double](1,2),
        DenseVector[Double](2,1)) must beEqualTo( 0)
      doCompare(
        DenseVector[Double](2,1),
        DenseVector[Double](1,2)) must beEqualTo( 0)
      doCompare(
        DenseVector[Double](1,1,2),
        DenseVector[Double](1,2,1)) must beEqualTo( 0)
      doCompare(
        DenseVector[Double](1,2,1),
        DenseVector[Double](1,1,2)) must beEqualTo( 0)
    }

  }
}
