package com.hungrylearner.pso.swarm

import akka.actor._


/**
 * An actor that provides messaging and a processing thread for the SwarmIntelligence.
 */
trait SwarmActor[F,P] extends Actor with ActorLogging

/**
 * @inheritdoc
 *
 * @param swarmIntelligenceFactory Factory for creating the LocalSwarmIntelligence for this actor.
 *                                 The factory parameters are the childIndex and actor context.
 * @param childIndex The index of this child swarm.
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
class LocalSwarmActor[F,P]( swarmIntelligenceFactory: ( Int, ActorContext) => LocalSwarmIntelligence[F,P], childIndex: Int) extends SwarmActor[F,P]
{
  val intelligence: LocalSwarmIntelligence[F,P] = swarmIntelligenceFactory( childIndex, context)

  def receive = {
    // TODO: need to manage re-init, getting SwarmAround after SwarmCompleted, etc.
    case command: Command => intelligence.onCommand( command)
    case ip: InfluentialPosition[F,P] => intelligence.onInfluentialPosition( ip)
    case unknownMessage: AnyRef => log.error( "LocalSwarm.receive: Unknown message {}", unknownMessage)
  }
}

object RegionalSwarmActor {

  class ChildSwarmActorProducer[F,P]( config: RegionalSwarmConfig[F,P], childIndex: Int, context: ActorContext) extends IndirectActorProducer
  {
    override def actorClass = classOf[SwarmActor[F,P]]
    override def produce = config.childSwarmActorFactory( context, childIndex)
  }


  def makeChildren[F,P]( config: RegionalSwarmConfig[F,P], context: ActorContext): List[ActorRef] = {
    List.tabulate[ActorRef]( config.childCount) { childIndex =>
      val props = Props( classOf[ChildSwarmActorProducer[F,P]], config, childIndex, context)
      val child = context.actorOf( props, name=config.childName+childIndex)
      context.watch( child) // Watch for Terminated(child) message.
      child
    }
  }

}

/**
 * RegionalSwarm contains one or more swarms as child actors. The children can be
 * RegionalSwarm or LocalSwarm or other derivatives.
 *
 * @param swarmIntelligenceFactory Factory for creating the LocalSwarmIntelligence for this actor.
 *                                 The factory parameters are the childIndex and actor context.
 * @param childIndex The index of this child swarm.
 * @tparam F Fitness
 * @tparam P Particle backing store
 */
class RegionalSwarmActor[F,P]( swarmIntelligenceFactory: ( Int, ActorContext) => RegionalSwarmIntelligence[F,P], val childIndex: Int) extends SwarmActor[F,P]
{
  import RegionalSwarmActor._
  import Report._

  val intelligence: RegionalSwarmIntelligence[F,P] = swarmIntelligenceFactory( childIndex, context)

  /**
   * Create the child swarms as specified in config.
   */
  makeChildren( intelligence.config, context)


  def receive = {

    // From parent
    case command: Command => intelligence.onCommand( command)
    case ip: InfluentialPosition[F,P] => intelligence.onInfluentialPosition( ip)

    // From children
    case progressReport:  ProgressReport[F,P] => intelligence.onProgressReport( progressReport, sender)
    case Terminated( child) => intelligence.onTerminated( child)

    // Huh?
    case unknownMessage: AnyRef => log.error( "AbstractRegionalSwarm.receive: Unknown message {}", unknownMessage)
  }

}
