package com.hungrylearner.pso.particle.breezedvd.mo

import breeze.linalg.DenseVector
import com.hungrylearner.pso.particle.{Position, MutablePosition}
import akka.event.Logging

object PositionDVD {

  val LT = 0
  val EQ = 1
  val GT = 2
  val NON_DOMINATED = 3
  
  val nextState: Array[Array[Int]] = Array(
    //     LT             EQ  GT
    Array( LT,            LT, NON_DOMINATED),  // Current State: LT
    Array( LT,            EQ, GT),       // Current State: EQ
    Array( NON_DOMINATED, GT, GT)        // Current State: GT
  )

  /**
   * Compare two fitness values that are vectors. 
   * 
   * If every A < every B return -1 for less than.
   * 
   * If every A > every B return 1 for greater than.
   * 
   * Otherwise they do not dominate each other, so return 0 for equal.
   */
  def doCompare( aFitness: DenseVector[Double], bFitness: DenseVector[Double]): Int = {
    var state = EQ

    for( index <- 0 until aFitness.length ) {
      val aFitnessValue = aFitness( index)
      val bFitnessValue = bFitness( index)

      val c = if (aFitnessValue < bFitnessValue)
        LT
      else if (aFitnessValue > bFitnessValue)
        GT
      else
        EQ

      state = nextState(state)(c)
      if( state == NON_DOMINATED)
        return 0
    }

    state match {
      case LT => -1
      case EQ => 0
      case GT => 1
      case _ => 0
    }
  }
}

/**
 * Immutable position with fitness.
 * @param mutableValue Mutable position backing store that will be copied
 * @param _fitness The fitness value
 */
class PositionDVD( private val mutableValue: DenseVector[Double], private val _fitness: DenseVector[Double]) extends Position[DenseVector[Double],DenseVector[Double]] {
  import PositionDVD.doCompare

  // Must store a copied value since the one passed in is mutable and wil be changed out from under us.
  private val copiedValue = mutableValue.copy
  override def value = copiedValue
  override def fitness = _fitness

  /** Result of comparing `this` with operand `that`.
    *
    * Implement this method to determine how instances of A will be sorted.
    *
    * Returns `x` where:
    *
    *   - `x < 0` when `this < that`
    *
    *   - `x == 0` when `this == that`
    *
    *   - `x > 0` when  `this > that`
    *
    */
  override def compare(that: Position[DenseVector[Double], DenseVector[Double]]): Int = doCompare( _fitness, that.fitness)
}

/**
 * Created by flint on 5/16/14.
 */
abstract class MutablePositionDVD( initialPosition: DenseVector[Double], bounds: Array[(Double,Double)]) extends MutablePosition[DenseVector[Double],DenseVector[Double]]{
  import PositionDVD.doCompare

  protected var _value: DenseVector[Double] = initialPosition.copy
  protected var _fitness: DenseVector[Double] = evaluateFitness( _value, 0)

  override def value = _value
  override def fitness = _fitness

  /**
   * x(t+1) = x(t) + v(t+1)
   */
  override def addVelocity( velocity: DenseVector[Double], iteration: Int) = {
    _value += velocity
    enforceConstraints()   // TODO: enforce bounds
    _fitness = evaluateFitness( _value, iteration)
  }


  /**
   * Return a Position that is immutable (mostly). The value will be copied by Position
   * at construction time.
   *
   * The backing store for this object is a DenseVector which is not immutable.
   *
   * @return Position
   */
  override def toPosition: PositionDVD = new PositionDVD( _value, _fitness)

  
  /** Result of comparing `this` with operand `that`.
    *
    * Implement this method to determine how instances of A will be sorted.
    *
    * Returns `x` where:
    *
    *   - `x < 0` when `this < that`
    *
    *   - `x == 0` when `this == that`
    *
    *   - `x > 0` when  `this > that`
    *
    */
  override def compare(that: Position[DenseVector[Double], DenseVector[Double]]) = doCompare( _fitness, that.fitness)
}
