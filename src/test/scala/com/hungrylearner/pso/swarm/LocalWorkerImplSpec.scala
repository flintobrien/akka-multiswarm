package com.hungrylearner.pso.swarm

import org.specs2.mutable._
import org.specs2.mock.Mockito
import akka.actor._
import scala.concurrent.duration._
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef}
import org.specs2.time.NoTimeConversions
import com.hungrylearner.pso.particle._
import test.AkkaTestkitSpecs2Support
import akka.event.LoggingAdapter
import com.hungrylearner.pso.particle.PositionIteration

object LocalWorkerImplSpec extends Mockito {

  val position = mock[Position[Int,Int]]
  val mutablePosition = mock[MutablePosition[Int,Int]]
  mutablePosition.toPosition returns position

  val particle = mock[Particle[Int,Int]]
  particle.fittest( any[Particle[Int,Int]]) returns particle
  particle.position returns mutablePosition
  particle.update( anyInt, any[Position[Int,Int]]) returns Some( position)

  def makeParticle( swarmIndex: Int, particleIndex: Int, particleCount: Int) = particle

  def makeActorContext( self: TestProbe, parent: TestProbe) = {
    val context = mock[ActorContext]
    context.self returns self.ref
    context.parent returns parent.ref
    context
  }
}


class LocalWorkerImplSpec extends Specification with NoTimeConversions with Mockito {
    sequential // forces all tests to be run sequentially

  import LocalWorkerImplSpec._
  import CompletedType._
  import Report._
  import TerminateCriteriaStatus._

  class MockReportingStrategy[F,P]( override val parent: ActorRef)
    extends ParentReporter[F,P]
    with ContinuousLocalReporting[F,P]

  //  val parent = TestProbe()
  //  val mockReportingStrategy = new MockReportingStrategy( parent.ref)

  class LocalIdImplUnderTest[F,P]( override val config: LocalSwarmConfig[F,P], override val childIndex: Int, override val context: ActorContext)
    extends LocalId[F,P]
    with SingleEgoImpl[F,P]
    with LocalWorkerImpl[F,P]
    with LocalSocialInfluence[F,P]
    with LocalTerminateOnMaxIterations[F,P]
  {
    
    override val reportingStrategy: LocalReportingStrategy[F, P] = new MockReportingStrategy[F,P]( context.parent)
    override protected val Logger: LoggingAdapter = mock[LoggingAdapter]
  }

  class MockLocalSwarmConfig( override val context: SimulationContext) extends LocalSwarmConfig[Int,Int]( particleCount=1, makeParticle, context)


  "LocalSwarm" should {

    "  Swarm one iteration and report one iteration completed" in new AkkaTestkitSpecs2Support {
      within(1 second) {

        val iterations = 2
        val simulationContext = SimulationContext( iterations, system)
        val config = new MockLocalSwarmConfig( simulationContext)

        val childIndex = 0
        val self = TestProbe()
        val parent = TestProbe()
        val context = makeActorContext( self, parent)
        val underTest = new LocalIdImplUnderTest[Int,Int]( config, childIndex, context)

        underTest.onCommand( SwarmOneIteration)

        val newBestPositions = Seq( PositionIteration( position, iteration=1))
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo( ProgressReport[Int,Int](SwarmOneIterationCompleted, childIndex, newBestPositions, iteration=1, ProgressOneOfOne, TerminateCriteriaNotMet))
      }
    }

    "  Swarm one iteration and report swarming completed" in new AkkaTestkitSpecs2Support {
      within(1 second) {

        val iterations = 1
        val simulationContext = SimulationContext( iterations, system)
        val config = new MockLocalSwarmConfig( simulationContext)

        val childIndex = 0
        val self = TestProbe()
        val parent = TestProbe()
        val context = makeActorContext( self, parent)
        val underTest = new LocalIdImplUnderTest[Int,Int]( config, childIndex, context)

        underTest.onCommand( SwarmOneIteration)

        val newBestPositions = Seq( PositionIteration( position, iteration=1))
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo( ProgressReport[Int,Int](SwarmingCompleted, childIndex, newBestPositions, iteration=1, ProgressOneOfOne, TerminateCriteriaMetNow))
      }
    }


    "  Swarm around and report swarming around completed" in new AkkaTestkitSpecs2Support {
      within(1 second) {

        val iterations = 2
        val simulationContext = SimulationContext( iterations, system)
        val config = new MockLocalSwarmConfig( simulationContext)

        val childIndex = 0
        val self = TestProbe()
        val parent = TestProbe()
        val context = makeActorContext( self, parent)
        val underTest = new LocalIdImplUnderTest[Int,Int]( config, childIndex, context)

        underTest.onCommand( SwarmAround( 1))

        val newBestPositions = Seq( PositionIteration( position, iteration=1))
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo( ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, newBestPositions, iteration=1, ProgressOneOfOne, TerminateCriteriaNotMet))
      }
    }

    "  Swarm around and report all swarming completed" in new AkkaTestkitSpecs2Support {
      within(1 second) {

        val iterations = 1
        val simulationContext = SimulationContext( iterations, system)
        val config = new MockLocalSwarmConfig( simulationContext)

        val childIndex = 0
        val self = TestProbe()
        val parent = TestProbe()
        val context = makeActorContext( self, parent)
        val underTest = new LocalIdImplUnderTest[Int,Int]( config, childIndex, context)

        underTest.onCommand( SwarmAround( 1))

        val newBestPositions = Seq( PositionIteration( position, iteration=1))
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo( ProgressReport[Int,Int](SwarmingCompleted, childIndex, newBestPositions, iteration=1, ProgressOneOfOne, TerminateCriteriaMetNow))
      }
    }

    "  Swarm around for 2 iterations. Report each iteration then swarm around completed" in new AkkaTestkitSpecs2Support {
      within(1 second) {

        val iterations = 3
        val childIndex = 0
        class TestActor extends Actor {
          val simulationContext = SimulationContext( iterations, system)
          val config = new MockLocalSwarmConfig( simulationContext)
          val underTest = new LocalIdImplUnderTest[Int,Int]( config, childIndex, context)
          def receive = {
            case command: Command ⇒ underTest.onCommand( command)
          }
        }

        val parent = TestProbe()
        val testActor = TestActorRef[TestActor]( Props(new TestActor), parent.ref, "child")

        testActor ! SwarmAround( 2)

        val newBestPositions = Seq( PositionIteration( position, iteration=1))
        val newBestPositions2 = Seq( PositionIteration( position, iteration=2))
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo( ProgressReport[Int,Int](SwarmOneIterationCompleted, childIndex, newBestPositions, iteration=1, ProgressOneOfOne, TerminateCriteriaNotMet))
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo( ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, newBestPositions2, iteration=2, ProgressOneOfOne, TerminateCriteriaNotMet))
      }
    }

    "  Swarm around for 2 iterations. Report each iteration then all swarming completed" in new AkkaTestkitSpecs2Support {
      within(1 second) {

        val iterations = 2
        val childIndex = 0
        class TestActor extends Actor {
          val simulationContext = SimulationContext( iterations, system)
          val config = new MockLocalSwarmConfig( simulationContext)
          val underTest = new LocalIdImplUnderTest[Int,Int]( config, childIndex, context)
          def receive = {
            case command: Command ⇒ underTest.onCommand( command)
          }
        }

        val parent = TestProbe()
        val testActor = TestActorRef[TestActor]( Props(new TestActor), parent.ref, "child")

        testActor ! SwarmAround( 2)

        val newBestPositions = Seq( PositionIteration( position, iteration=1))
        val newBestPositions2 = Seq( PositionIteration( position, iteration=2))
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo( ProgressReport[Int,Int](SwarmOneIterationCompleted, childIndex, newBestPositions, iteration=1, ProgressOneOfOne, TerminateCriteriaNotMet))
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo( ProgressReport[Int,Int](SwarmingCompleted, childIndex, newBestPositions2, iteration=2, ProgressOneOfOne, TerminateCriteriaMetNow))
      }
    }

  }
}
