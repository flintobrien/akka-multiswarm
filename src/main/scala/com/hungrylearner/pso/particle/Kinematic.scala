package com.hungrylearner.pso.particle

import akka.event.Logging


/**
 * Created by flint on 5/4/14.
 */
trait Kinematic[F,P] {
  this: ParticleSpace[F,P] =>

  type VelocityBounds
  type InertiaWeight
  type Phi

  def velocity: P
  def velocityBounds: VelocityBounds
  def inertiaWeight: InertiaWeight

  /**
   * v(t+1) = w*v(t) + personalInfluence + socialInfluence
   *
   * personalInfluence = phiP * rP * (personalBest(t) - x(t))
   * socialInfluence = phiG * rG * (globalBest(t) - x(t))
   *
   * v = v.within(vMin, vMax)
   *
   *
   *   t — time
   *   v — velocity
   *   w — inertia weight
   *   rP, rG — random personal and global ~ U(0,1)
   *
   *
   * @param iteration
   * @param bestPosition
   */
  def updateVelocity( iteration: Int, bestPosition: Position[F,P]): Unit

}


