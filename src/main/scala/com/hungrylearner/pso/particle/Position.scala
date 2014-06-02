package com.hungrylearner.pso.particle

trait Position[F,P] extends Ordered[Position[F,P]] {
  def value: P
  def fitness: F
}

case class EvaluatedPosition[F,P]( position: Position[F,P], isBest: Boolean)

trait MutablePosition[F,P] extends Position[F,P]{

  /**
   * Evaluate the fitness of the current position. The current iteration is available for
   * cases where the fitness should be evaluated differently as the PSO progresses.
   *
   * @param position
   * @param iteration Iteration from 0 to n. The initial position is evaluated with iteration 0.
   * @return
   */
  def evaluateFitness( position: P, iteration: Int): F

  /**
   *
   * Add the velocity to the current position, call enforceConstraints, then call evaluateFitness to
   * update fitness.
   * x(t+1) = x(t) + v(t+1)
   */
  def addVelocity( velocity: P, iteration: Int)

  /**
   * EnforceConstraints is called by addVelocity() after adding the velocity and before calling evaluateFitness.
   * This is an opportunity to change the particles position if it's not within custom constraints. Constraints
   * can include particle boundaries or invalid combinations of values within the position vector.
   */
  def enforceConstraints() = {}

  /**
   * Return a Position that is immutable (mostly). The value will be copied by Position
   * at construction time.
   *
   * If the backing store for value is mutable (ex: DenseVector or Array), it
   * is up to the caller to treat the value as immutable.
   *
   * @return Position
   */
  def toPosition: Position[F,P]

  /**
   * Copy this object. If the position or fitness are mutable (likely), it's important to
   * deep copy the position and fitness. You don't want two FitPositions referencing and updating
   * the same mutable position vector.
   *
   * The deep copy can be done on the constructor instead of the copy. Example:
   *    var _position: DenseVector[Double] = initialPosition.copy
   *
   * @return a deep copy.
   */
  def copy: MutablePosition[F,P]
}
