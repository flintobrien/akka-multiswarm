package com.hungrylearner.pso.particle.breezedvd

import org.specs2.mutable._
import org.mockito.Mockito._
import org.specs2.mock.Mockito
import akka.actor.ActorSystem
import breeze.linalg.{DenseVector, sum}
import breeze.numerics.abs

/**
 * Created by flint on 5/14/14.
 */
class KinematicParticleSpaceSpec extends Specification with BeforeAfter with Mockito  {

  class MutablePositionExample( initialPosition: DenseVector[Double], bounds: Array[(Double,Double)]) extends MutablePositionDVD( initialPosition, bounds) {
    override def evaluateFitness( v: DenseVector[Double], iteration: Int): Double = { sum(abs(v)) }
    // The constructor copies the position before using it.
    override def copy: MutablePositionExample = new MutablePositionExample( value, bounds)
  }


  val _system = ActorSystem("KinematicParticleSpaceSpec")

  val _dimension = 1
  val _phiP = 0.25
  val _phiG = 0.25
  var system: ActorSystem = _
  val particleSpaceContext = makeParticleSpaceContext

  val p0t0 = 5.0
  val positionBounds = Array[(Double,Double)]( (0.0,10.0))
  def makeMutablePosition( p: Double) = new MutablePositionExample( DenseVector[Double](p), positionBounds)
  def makePosition( p: Double) = new PositionDVD( DenseVector[Double](p), Math.abs(p))

  def makeParticleSpaceContext = {
    val initialPosition = (dim: Int, particleIndex: Int) => makeMutablePosition(p0t0)
    val initialHistory = (dim: Int) => DenseVector.fill[Double]( dim) (0.0)
    ParticleSpaceDVD.ParticleSpaceContext( initialPosition, positionBounds, initialHistory)
  }


  abstract class AbstractKinematicParticleSpaceDVD extends KinematicParticleSpaceDVD {
    override def index = 1
    override def dimension = _dimension
  }

  /**
   * Produce random number given an Iterator. When the list is exhausted,
   * the last value is repeated forever.
   * @param iter
   */
  class RandomIterator( iter: Iterator[Double]) {
    var last = 0.0
    def random = () => {
      if( iter.hasNext)
        last = iter.next
      last
    }
  }

  override def before = {
  }
  override def after = {
  }


  "Kinematics" should {

    "update velocity when position == personalBest" in {
      import  KinematicParticleSpaceDVD._

      val v0t0 = 2.0
      val velocityBounds = (-5.0, 5.0)
      val initialVelocity = (dim: Int, particleIndex: Int) => DenseVector[Double](v0t0)
      val wt0 = 1.0
      val inertiaWeight = (i: Int) => {wt0}
      val randomP = 0.4
      val randomG = 0.6
      val randomIterator = new RandomIterator( Iterator(randomP, randomG))
      val kinematicsContext = KinematicContext( initialVelocity, velocityBounds, inertiaWeight, _phiP, _phiG, randomIterator.random, _system)

      // personalBest == position
      //
      val personalBest1 = p0t0
      class KP extends {
        val kc = kinematicsContext
        val psc = particleSpaceContext
      } with AbstractKinematicParticleSpaceDVD {
        personalBest.setIfBest( makeMutablePosition(personalBest1))
      }
      val kp1 = new KP()
      kp1.velocity(0) must beEqualTo(2.0)

      val bp0t0 = p0t0
      //val bestParticle = mock[KP]
      //bestParticle.position returns makeFitPosition(bp0t0)
      val bestPosition = makePosition(bp0t0)

      kp1.updateVelocity( 1, bestPosition)
      val influenceP = (personalBest1 - p0t0) * randomP * _phiP
      val influenceG = (bp0t0 - p0t0) * randomG * _phiG
      val v1t2 = v0t0 * wt0 + influenceP + influenceG
      kp1.velocity(0) must beCloseTo( v1t2, 0.01)
    }

    "update velocity when position != personalBest" in {
      import  KinematicParticleSpaceDVD._

      val v0t0 = 2.0
      val velocityBounds = (-5.0, 5.0)
      val initialVelocity = (dim: Int, particleIndex: Int) => DenseVector[Double](v0t0)
      val wt0 = 1.0
      val inertiaWeight = (i: Int) => {wt0}
      val randomP = 0.4
      val randomG = 0.6
      val randomIterator = new RandomIterator( Iterator(randomP, randomG))
      val kinematicsContext = KinematicContext( initialVelocity, velocityBounds, inertiaWeight, _phiP, _phiG, randomIterator.random, _system)


      // personalBest != position
      //
      val personalBest1 = 4.0
      class KP extends {
        val kc = kinematicsContext
        val psc = particleSpaceContext
      } with AbstractKinematicParticleSpaceDVD {
        personalBest.setIfBest( makeMutablePosition(personalBest1))
      }
      val kp1 = new KP()
      kp1.velocity(0) must beEqualTo(2.0)

      val bp0t0 = 2.5
      val bestPosition = makePosition(bp0t0)

      kp1.updateVelocity( 1, bestPosition)
      val influenceP = (personalBest1 - p0t0) * randomP * _phiP
      val influenceG = (bp0t0 - p0t0) * randomG * _phiG
      val v1t2 = v0t0 * wt0 + influenceP + influenceG
      kp1.velocity(0) must beCloseTo( v1t2, 0.01)
    }

  }

}
