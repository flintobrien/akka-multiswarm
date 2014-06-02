package com.hungrylearner.pso.swarm

import org.specs2.mutable._
import org.specs2.mock.Mockito
import com.hungrylearner.pso.swarm.CompletedType._
import com.hungrylearner.pso.swarm.Report._
import com.hungrylearner.pso.particle.{Position, EvaluatedPosition}
import akka.event.LoggingAdapter
import akka.actor.{ActorContext, ActorRef}
import akka.testkit.TestProbe
import test.AkkaTestkitSpecs2Support

object RegionalSupervisorSpec extends Mockito {

  def makeActorContext( self: TestProbe, parent: TestProbe) = {
    val context = mock[ActorContext]
    context.self returns self.ref
    context.parent returns parent.ref
    context
  }
}

class RegionalSupervisorSpec extends Specification with Mockito {
  import RegionalSupervisorSpec._

  class PositionII( _value: Int, _fitness: Int) extends Position[Int, Int] {
    override def value = _value
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
    override def compare(that: Position[Int,Int]) = if (_fitness < that.fitness) -1 else 1
  }


  class ReportingStrategyProxy[F,P]( override val parent: ActorRef)
    extends ParentReporter[F,P]
    with ContinuousRegionalReporting[F,P]

  class RegionalSupervisorUnderTest[F,P]( override val config: RegionalSwarmConfig[F,P],
                                          override val childIndex: Int,
                                          override val context: ActorContext,
                                          override val reportingStrategy: RegionalReportingStrategy[F,P] )
    extends RegionalSupervisor[F, P] with RegionalId[F,P]
  {

    override protected var bestPosition: Position[F, P] = _

    override protected val Logger: LoggingAdapter = mock[LoggingAdapter]

    override protected def incrementProgressCount(progressReport: ProgressReport[F,P]) = 1
    override protected def makeProgress(progressCount: Int) = ProgressOneOfOne

    def proxy_tellChildren(evaluatedPosition: EvaluatedPosition[F,P], iteration: Int, progress: Progress, originator: ActorRef) = {}
    override protected def tellChildren(evaluatedPosition: EvaluatedPosition[F,P], iteration: Int, progress: Progress, originator: ActorRef) =
      proxy_tellChildren( evaluatedPosition, iteration, progress, originator)
  }


  "RegionalSupervisor" should {

    "  evaluatePosition should use isBest=true" in new AkkaTestkitSpecs2Support {

      val config = mock[RegionalSwarmConfig[Int,Int]]

      val childIndex = 0
      val position = new PositionII( 1,1)
      val evaluatedPosition = EvaluatedPosition( position, isBest=true)
      val pr = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, evaluatedPosition, iteration=1, ProgressOneOfOne)

      val selfy = TestProbe()
      val parent = TestProbe()
      val context = makeActorContext( selfy, parent)
      val reportingStrategy = new ReportingStrategyProxy[Int,Int]( parent.ref)

      val underTest = new RegionalSupervisorUnderTest[Int,Int]( config, childIndex, context, reportingStrategy)
      underTest.evaluatePosition( pr) must beEqualTo( EvaluatedPosition( position, isBest=true))
    }

    "  evaluatePosition should use isBest=false if bestPosition is null" in new AkkaTestkitSpecs2Support {

      val config = mock[RegionalSwarmConfig[Int,Int]]

      val childIndex = 0
      val position = new PositionII( 1,1)
      val evaluatedPosition = EvaluatedPosition( position, isBest=false)
      val pr = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, evaluatedPosition, iteration=1, ProgressOneOfOne)

      val selfy = TestProbe()
      val parent = TestProbe()
      val context = makeActorContext( selfy, parent)
      val reportingStrategy = new ReportingStrategyProxy[Int,Int]( parent.ref)

      val underTest = new RegionalSupervisorUnderTest[Int,Int]( config, childIndex, context, reportingStrategy)
      underTest.evaluatePosition( pr) must beEqualTo( EvaluatedPosition( position, isBest=true))
    }

    "  onProgressReport should report new best position to parent" in new AkkaTestkitSpecs2Support {

      val config = mock[RegionalSwarmConfig[Int,Int]]

      val childIndex = 0
      val iteration = 1
      val position = new PositionII( 1,1)
      val epFalse = EvaluatedPosition( position, isBest=false)
      val pr = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epFalse, iteration, ProgressOneOfOne)

      val selfy = TestProbe()
      val parent = TestProbe()
      val context = makeActorContext( selfy, parent)
      val reportingStrategy = new ReportingStrategyProxy[Int,Int]( parent.ref)

      val underTest = spy( new RegionalSupervisorUnderTest[Int,Int]( config, childIndex, context, reportingStrategy))
      val originator = TestProbe()

      // The position in the first progress report will be used because the RegionalSupervisor's bestPosition == null
      underTest.onProgressReport( pr, originator.ref)

      val epTrue = EvaluatedPosition( position, isBest=true)
      parent.expectMsg( ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epTrue, iteration, ProgressOneOfOne))
      there was one(underTest).proxy_tellChildren(epTrue, iteration, ProgressOneOfOne, originator.ref)


      // Send a position that has better fitness.
      val betterPosition = new PositionII( 0,0)
      val epBetter = EvaluatedPosition( betterPosition, isBest=true)
      val prBetter = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epBetter, iteration, ProgressOneOfOne)
      underTest.onProgressReport( prBetter, originator.ref)

      parent.expectMsg( ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epBetter, iteration, ProgressOneOfOne))
      there was one(underTest).proxy_tellChildren(epBetter, iteration, ProgressOneOfOne, originator.ref)
    }

  }
}
