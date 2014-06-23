package com.hungrylearner.pso.swarm

import akka.actor.{ActorRef, Actor}

trait SocialInfluence[F,P] {
  def onInfluentialPosition( ip: InfluentialPosition[F,P])
}

trait LocalSocialInfluence[F,P] extends SocialInfluence[F,P] {
  this: Id[F,P] =>

  override def onInfluentialPosition(ip: InfluentialPosition[F,P]) = {
    // Comes from parent
    //   - If really is best, send it down to children
    if( ip.newBestPositions.head.position < bestPosition) {
      bestPosition = ip.newBestPositions.head.position
    }
  }
}

trait RegionalSocialInfluence[F,P] extends SocialInfluence[F,P] {
  this: RegionalId[F,P] =>
  //    this: AbstractRegionalSwarm =>

  override def onInfluentialPosition( ip: InfluentialPosition[F,P]) = {
    // Comes from parent
    //   - If really is best, send it down to children
    if( ip.newBestPositions.head.position < bestPosition) {
      bestPosition = ip.newBestPositions.head.position
      sendToAllChildren( ip) // null: Send to all children. Don't exclude one.
    }
  }

}



/**
 * see stackable actor receivers at
 * http://stackoverflow.com/questions/18124643/scala-stackable-trait-pattern-with-akka-actors
 *
 * Extending Actors using PartialFunction chaining
 * http://doc.akka.io/docs/akka/snapshot/scala/actors.html
 *
 * Create a router for broadcasting
 * http://stackoverflow.com/questions/18339082/how-to-create-routers-in-akka-with-parameterized-actors
 */
trait Receiving {
  var receivers: Actor.Receive = Actor.emptyBehavior
  def receiver(next: Actor.Receive) { receivers = receivers orElse next }
  def receive = receivers // Actor.receive definition
}

/**
 * What do we tell our children and how often.
 * Collect disseminate.
 * For each event type, how do we now if we need to wait on all children, some children, just rebroadcast, etc.
 *
 * For events coming from children, we should collate it, then send it out.
 *   onEventFromChild
 *     - Iteration/SwarmAround completed - send up?
 *     - SwarmingCompleted - Send up
 *   onEventFromSibling
 *   onEventFromParent
 *
 *   beInfluenced
 *   exertInfluence children
 *
 *   ListenToSiblings
 *     - new best particle (also sent to parent). Tell myself. Tell children.
 *   ListenToParent
 *     - prospective better global particle. Tell myself and children
 *     - swarmAround for iterations. Do it.
 *     - stop
 *   ListenToChildren?
 *     - new best particle in child swarm
 *
 *
 * Social influence captures who you report with, how often, and what you say.
 *
 * Perhaps Social strategy, social structure, social influence, communication tactics, social relation?
 *
 * SwarmAroundALittle SwarmForAWhile, SwarmForASpan, stretch, stint, bit, bout, interval
 *
 * Created by flint on 5/16/14.
 */
