package com.hungrylearner.pso.swarm

import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import org.specs2.mock.Mockito
import test.AkkaTestkitSpecs2Support
import akka.actor._
import akka.testkit._
import scala.concurrent.duration._
import com.hungrylearner.pso.swarm.Report.ProgressReport
import com.hungrylearner.pso.particle.{Particle, MutablePosition, Position}

object SwarmActorSpec extends Mockito {
  val position = mock[Position[Int,Int]]
  val mutablePosition = mock[MutablePosition[Int,Int]]
  mutablePosition.toPosition returns position

  val particle = mock[Particle[Int,Int]]
  particle.fittest( any[Particle[Int,Int]]) returns particle
  particle.position returns mutablePosition
  particle.update( anyInt, any[Position[Int,Int]]) returns position

  def makeParticle( swarmIndex: Int, particleIndex: Int, particleCount: Int) = particle

}

/**
 * Created by flint on 6/1/14.
 */
class SwarmActorSpec extends Specification with NoTimeConversions with Mockito {
  sequential // forces all tests to be run sequentially

  import SwarmActorSpec._


  "LocalSwarmActor" should {

    val childIndex = 0

    "  Call onCommand with correct command" in new AkkaTestkitSpecs2Support {
      within(1 second) {
        val mockIntelligence = mock[LocalSwarmIntelligence[Int,Int]]
        def localSwarmIntelligenceFactory( childIndex: Int, context: ActorContext) = mockIntelligence

        val underTest = TestActorRef[LocalSwarmActor[Int,Int]]( Props(new LocalSwarmActor[Int,Int]( localSwarmIntelligenceFactory, childIndex)), "child")

        underTest ! SwarmOneIteration
        there was one(mockIntelligence).onCommand( SwarmOneIteration)
      }
    }

    "  Call onInfluentialPosition" in new AkkaTestkitSpecs2Support {
      within(1 second) {
        val mockIntelligence = mock[LocalSwarmIntelligence[Int,Int]]
        def localSwarmIntelligenceFactory( childIndex: Int, context: ActorContext) = mockIntelligence
        val underTest = TestActorRef[LocalSwarmActor[Int,Int]]( Props(new LocalSwarmActor[Int,Int]( localSwarmIntelligenceFactory, childIndex)), "child")

        val ip = mock[InfluentialPosition[Int,Int]]
        underTest ! ip
        there was one(mockIntelligence).onInfluentialPosition( ip)
      }
    }
  }

  "RegionalSwarmActor" should {

    val childIndex = 0
    val mockConfig0 = mock[RegionalSwarmConfig[Int,Int]]
    mockConfig0.childCount returns 0

    "  Call onCommand with correct command" in new AkkaTestkitSpecs2Support {
      within(1 second) {
        val mockIntelligence = mock[RegionalSwarmIntelligence[Int,Int]]
        mockIntelligence.config returns mockConfig0
        def regionalSwarmIntelligenceFactory( childIndex: Int, context: ActorContext) = mockIntelligence

        val underTest = TestActorRef[RegionalSwarmActor[Int,Int]]( Props(new RegionalSwarmActor[Int,Int]( regionalSwarmIntelligenceFactory, childIndex)), "child")

        underTest ! SwarmOneIteration
        there was one(mockIntelligence).onCommand( SwarmOneIteration)
      }
    }

    "  Call onInfluentialPosition with the position" in new AkkaTestkitSpecs2Support {
      within(1 second) {
        val mockIntelligence = mock[RegionalSwarmIntelligence[Int,Int]]
        mockIntelligence.config returns mockConfig0
        def regionalSwarmIntelligenceFactory( childIndex: Int, context: ActorContext) = mockIntelligence
        val underTest = TestActorRef[RegionalSwarmActor[Int,Int]]( Props(new RegionalSwarmActor[Int,Int]( regionalSwarmIntelligenceFactory, childIndex)), "child")

        val ip = mock[InfluentialPosition[Int,Int]]
        underTest ! ip
        there was one(mockIntelligence).onInfluentialPosition( ip)
      }
    }

    "  Call onProgressReport with the report" in new AkkaTestkitSpecs2Support {
      within(1 second) {
        val mockIntelligence = mock[RegionalSwarmIntelligence[Int,Int]]
        mockIntelligence.config returns mockConfig0
        def regionalSwarmIntelligenceFactory( childIndex: Int, context: ActorContext) = mockIntelligence
        val underTest = TestActorRef[RegionalSwarmActor[Int,Int]]( Props(new RegionalSwarmActor[Int,Int]( regionalSwarmIntelligenceFactory, childIndex)), "child")

        val pr = mock[ProgressReport[Int,Int]]
        underTest ! pr
        there was one(mockIntelligence).onProgressReport( pr, self)
      }
    }



    class MockChildSwarm( proxy: TestProbe) extends SwarmActor[Int,Int] {
      def receive = {
        case m: Message => proxy.ref forward m
      }
    }
    class MockLocalSwarmConfig( override val context: SimulationContext) extends LocalSwarmConfig[Int,Int]( particleCount=1, makeParticle, context)

    def mockConfig( iterations: Int, childCount: Int, system: ActorSystem, proxy: TestProbe) = {
      def makeChildSwarm( context: ActorContext, childIndex: Int) = new MockChildSwarm( proxy)
      val simulationContext = SimulationContext( iterations, system)
      val childrenConfig = mock[SwarmConfig[Int,Int]]
      new RegionalSwarmConfig[Int,Int]( childCount, childrenConfig, "child2", makeChildSwarm, simulationContext)
    }

    "  Make child swarms" in new AkkaTestkitSpecs2Support {
      within(1 second) {

        val iterations = 1
        val childCount = 2
        val proxy = TestProbe()
        val config = mockConfig( iterations, childCount, system, proxy)

        val mockIntelligence = mock[RegionalSwarmIntelligence[Int,Int]]
        mockIntelligence.config returns config
        def regionalSwarmIntelligenceFactory( childIndex: Int, context: ActorContext) = mockIntelligence
        val underTest = TestActorRef[RegionalSwarmActor[Int,Int]]( Props(new RegionalSwarmActor[Int,Int]( regionalSwarmIntelligenceFactory, childIndex)), "regionalSwarm")

        underTest.underlyingActor.context.children.size must equalTo( childCount)
      }
    }

  }
}
