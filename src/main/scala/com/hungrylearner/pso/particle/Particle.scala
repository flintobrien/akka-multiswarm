package com.hungrylearner.pso.particle

trait Particle[F,P] extends ParticleSpace[F,P] with Kinematic[F,P] {

  /**
   * Use the one best particle from the beginning of the step.
   *
   * Pass in the bestParticle fitness too. No need to calculate it for every particle.
   *
   * Need to return the fitness for each particle. From that we keep track of the
   * best particle at the end of the step
   *
   * @param iteration
   * @param bestPosition
   * @return Update or fitness and return the new fittest particle, us or the bestParticle passing in.
   */
  def update( iteration: Int, bestPosition: Position[F,P]): Position[F,P]  = {
    updateVelocity( iteration, bestPosition)
    position.addVelocity( velocity, iteration)
    updatePersonalBest

    if( bestPosition < position)
      bestPosition
    else
      position.toPosition
  }

  def fittest( other: Particle[F,P]): Particle[F,P] =
    if( other.position < position) other else this

}

