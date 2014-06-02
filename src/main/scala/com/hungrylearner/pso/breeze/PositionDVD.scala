package com.hungrylearner.pso.breeze

import breeze.linalg.DenseVector
import com.hungrylearner.pso.particle.{Position, MutablePosition}
import akka.event.Logging

/**
 * Immutable position with fitness.
 * @param mutableValue Mutable position backing store that will be copied
 * @param _fitness The fitness value
 */
class PositionDVD( private val mutableValue: DenseVector[Double], private val _fitness: Double) extends Position[Double, DenseVector[Double]] {
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
  override def compare(that: Position[Double, DenseVector[Double]]) = if (_fitness < that.fitness) -1 else 1
}


/**
 * Created by flint on 5/16/14.
 */
abstract class MutablePositionDVD( initialPosition: DenseVector[Double], bounds: Array[(Double,Double)]) extends MutablePosition[Double, DenseVector[Double]]{

  protected var _value: DenseVector[Double] = initialPosition.copy
  protected var _fitness: Double = evaluateFitness( _value, 0)

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
  override def toPosition: Position[Double,DenseVector[Double]] = new PositionDVD( _value, _fitness)

  
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
  override def compare(that: Position[Double, DenseVector[Double]]) =
    if( _fitness < that.fitness) -1 else 1
}
