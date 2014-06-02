package com.hungrylearner.pso.swarm

import com.hungrylearner.pso.particle.Position
import akka.actor.{ActorContext, ActorRef}
import akka.event.LoggingAdapter

/**
 * The Id (from psychoanalysis) contains innate information and processes used by the other parts of a
 * SwarmIntelligence. Of these, bestPosition and actor context are most prominent.
 *
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait Id[F,P] {
//  this: ReportingStrategy[F,P] =>

  val childIndex: Int

  /** The current known best position known by the swarm */
  protected var bestPosition: Position[F,P]

  /** The actor context used for accessing our parent and possible children */
  protected val context: ActorContext

  /** General logging */
  protected val Logger: LoggingAdapter

  /**
   * Process a swarm command
   */
  def onCommand( command: Command)

}

/**
 * LocalId has no children.
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait LocalId[F,P] extends Id[F,P] {

  val config: LocalSwarmConfig[F,P]
  val reportingStrategy: LocalReportingStrategy[F,P]

}

/**
 * RegionalId has children.
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
trait RegionalId[F,P] extends Id[F,P] {
  this: RegionalSupervisor[F,P] =>

  val config: RegionalSwarmConfig[F,P]
  val reportingStrategy: RegionalReportingStrategy[F,P]

  override def onCommand( command: Command) = sendToAllChildren( command)

  // TODO: Create route for all children.
  protected def sendToAllChildren( message: Message) = for( child <- context.children) { child ! message}

  protected def sendToChildren( message: Message, exceptThisOne: ActorRef = null) = {
    if( exceptThisOne == null)
      sendToAllChildren( message)
    else
      for( child <- context.children if child != exceptThisOne) child ! message
  }

}
