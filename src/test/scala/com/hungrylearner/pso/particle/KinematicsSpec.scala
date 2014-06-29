package com.hungrylearner.pso.particle

import org.specs2.mutable._
import org.mockito.Mockito._
import org.specs2.mock.Mockito
import breeze.linalg.{DenseVector, sum}
import breeze.numerics.abs
import com.hungrylearner.pso.particle.breezedvd.so.MutablePositionDVD

/**
 * Created by flint on 5/14/14.
 */
class KinematicsSpec  extends Specification with BeforeAfter with Mockito  {

  class MutablePositionExample( initialPosition: DenseVector[Double], bounds: Array[(Double,Double)]) extends MutablePositionDVD( initialPosition, bounds) {
    override def evaluateFitness( v: DenseVector[Double], iteration: Int): Double = { sum(abs(v)) }
    // The constructor copies the position before using it.
    override def copy: MutablePositionExample = new MutablePositionExample( value, bounds)
  }


  abstract class Particle_Class extends Particle[Double,DenseVector[Double]] {
  }

  override def before = {
//    println( "before --------------------------")

  }
  override def after = {
  }

  type ParticleDVD = Particle[Double,DenseVector[Double]]
  val positionWith1 = makeFitPosition( 1.0)
  val positionWith2 = makeFitPosition( 2.0)
  val positionBounds = Array[(Double,Double)]( (0.0,10.0))
  def makeFitPosition( p: Double) = new MutablePositionExample( DenseVector[Double](p), positionBounds)

  "ParticleLike" should {

//    "return other as the fittest particle" in {
//      //      println( "test 1 ------------------------------")
//      val particle = mock[Particle_Class]
//      val particleBest = mock[Particle_Class]
//      when(particle.fittest(any[ParticleDVD])).thenCallRealMethod
//      particle.position returns positionWith2
//      particleBest.position returns positionWith1
//
//      val result = particle.fittest(particleBest)
//      result must be(particleBest)
//    }
//
//    "return this as the fittest particle" in {
//      //      println( "test 1 ------------------------------")
//      val particle = mock[Particle_Class]
//      val particleBest = mock[Particle_Class]
//      when(particle.fittest(any[ParticleDVD])).thenCallRealMethod
//      particle.position returns positionWith1
//      particleBest.position returns positionWith2
//
//      val result = particle.fittest(particleBest)
//      result must be(particle)
//    }

  }

}
