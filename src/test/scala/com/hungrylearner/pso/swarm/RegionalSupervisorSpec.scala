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
  import TerminateCriteriaStatus._

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
    extends RegionalSupervisor[F, P] with RegionalId[F,P] with RegionalTerminateOnAllSwarmingCompleted[F,P]
  {

    override protected var bestPosition: Position[F, P] = _

    override protected val Logger: LoggingAdapter = mock[LoggingAdapter]

    def proxy_makeProgress(progressReport: ProgressReport[F,P]) = calculateRegionalProgress( progressReport)

    def proxy_tellChildren(evaluatedPosition: EvaluatedPosition[F,P], iteration: Int, progress: Progress, originator: ActorRef) = {}
    override protected def tellChildren(evaluatedPosition: EvaluatedPosition[F,P], iteration: Int, progress: Progress, originator: ActorRef) =
      proxy_tellChildren( evaluatedPosition, iteration, progress, originator)
  }


  "ProgressCounters" should {
    import RegionalSupervisor._

    val ep = mock[EvaluatedPosition[Int,Int]]

    "  Increment counts and return 0 for unseen CompletedType or iteration." in {
      val counters = new ProgressCounters[Int,Int]
      val pr1 = ProgressReport[Int,Int](SwarmOneIterationCompleted, 1, ep, iteration=1, ProgressOneOfOne, TerminateCriteriaNotMet)
      val pr2 = ProgressReport[Int,Int](SwarmAroundCompleted, 1, ep, iteration=1, ProgressOneOfOne, TerminateCriteriaMetNow)
      val pr3 = ProgressReport[Int,Int](SwarmAroundCompleted, 2, ep, iteration=2, ProgressOneOfOne, TerminateCriteriaMetNow)

      counters.progressCount( pr1) must beEqualTo( 0)
      counters.incrementProgressCount( pr1) must beEqualTo( 1)
      counters.progressCount( pr2) must beEqualTo( 0)
      counters.progressCount( pr3) must beEqualTo( 0)
      counters.incrementProgressCount( pr2) must beEqualTo( 1)
      counters.incrementProgressCount( pr3) must beEqualTo( 1)
    }
  }

  "RegionalSupervisor" should {

    "  evaluatePosition should use isBest=true" in new AkkaTestkitSpecs2Support {

      val config = mock[RegionalSwarmConfig[Int,Int]]

      val childIndex = 0
      val position = new PositionII( 1,1)
      val evaluatedPosition = EvaluatedPosition( position, isBest=true)
      val pr = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, evaluatedPosition, iteration=1, ProgressOneOfOne, TerminateCriteriaMetNow)

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
      val pr = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, evaluatedPosition, iteration=1, ProgressOneOfOne, TerminateCriteriaMetNow)

      val selfy = TestProbe()
      val parent = TestProbe()
      val context = makeActorContext( selfy, parent)
      val reportingStrategy = new ReportingStrategyProxy[Int,Int]( parent.ref)

      val underTest = new RegionalSupervisorUnderTest[Int,Int]( config, childIndex, context, reportingStrategy)
      underTest.evaluatePosition( pr) must beEqualTo( EvaluatedPosition( position, isBest=true))
    }

    "  onProgressReport should report new best position to parent" in new AkkaTestkitSpecs2Support {

      val config = mock[RegionalSwarmConfig[Int,Int]]
      config.childCount returns 1
      config.descendantSwarmCount returns 1

      val childIndex = 0
      val iteration = 1
      val position = new PositionII( 1,1)
      val epFalse = EvaluatedPosition( position, isBest=false)
      val pr = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epFalse, iteration, ProgressOneOfOne, TerminateCriteriaMetNow)

      val selfy = TestProbe()
      val parent = TestProbe()
      val context = makeActorContext( selfy, parent)
      val reportingStrategy = new ReportingStrategyProxy[Int,Int]( parent.ref)

      val underTest = spy( new RegionalSupervisorUnderTest[Int,Int]( config, childIndex, context, reportingStrategy))
      val originator = TestProbe()

      // The position in the first progress report will be used because the RegionalSupervisor's bestPosition == null
      underTest.onProgressReport( pr, originator.ref)

      val epTrue = EvaluatedPosition( position, isBest=true)
      parent.expectMsg( ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epTrue, iteration, ProgressOneOfOne, TerminateCriteriaNotMet))
      there was one(underTest).proxy_tellChildren(epTrue, iteration, ProgressOneOfOne, originator.ref)


      // Send a position that has better fitness.
      val betterPosition = new PositionII( 0,0)
      val epBetter = EvaluatedPosition( betterPosition, isBest=true)
      val prBetter = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epBetter, iteration, ProgressOneOfOne, TerminateCriteriaMetNow)
      underTest.onProgressReport( prBetter, originator.ref)

      val progressTwoOfOne = Progress( ProgressFraction(2,1), ProgressFraction(2,1), completed=true)
      parent.expectMsg( ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epBetter, iteration, progressTwoOfOne, TerminateCriteriaNotMet))
      there was one(underTest).proxy_tellChildren(epBetter, iteration, progressTwoOfOne, originator.ref)
    }

    "  makeProgress should keep count of child progress." in new AkkaTestkitSpecs2Support {

      val config = mock[RegionalSwarmConfig[Int,Int]]
      config.childCount returns 2
      config.descendantSwarmCount returns 8 // Generated reports assume we have 2 children with 4 descendants each

      val childIndex = 0
      val position = new PositionII( 1,1)
      val epFalse = EvaluatedPosition( position, isBest=false)

      val selfy = TestProbe()
      val parent = TestProbe()
      val context = makeActorContext( selfy, parent)
      val reportingStrategy = new ReportingStrategyProxy[Int,Int]( parent.ref)

      val underTest = new RegionalSupervisorUnderTest[Int,Int]( config, childIndex, context, reportingStrategy)

      val child0of2desc1of4 = Progress( ProgressFraction(0,2), ProgressFraction(1,4), completed=false)
      val child1a = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epFalse, iteration=1, child0of2desc1of4, TerminateCriteriaNotMet)
      underTest.proxy_makeProgress( child1a) must beEqualTo( Progress( ProgressFraction(0,2), ProgressFraction(1,8), completed=false))

      val child1of2desc2of4 = Progress( ProgressFraction(1,2), ProgressFraction(2,4), completed=false)
      val child1b = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epFalse, iteration=1, child1of2desc2of4, TerminateCriteriaNotMet)
      underTest.proxy_makeProgress( child1b) must beEqualTo( Progress( ProgressFraction(0,2), ProgressFraction(2,8), completed=false))

      val child1of2desc3of4 = Progress( ProgressFraction(1,2), ProgressFraction(3,4), completed=false)
      val child1c = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epFalse, iteration=1, child1of2desc3of4, TerminateCriteriaNotMet)
      underTest.proxy_makeProgress( child1c) must beEqualTo( Progress( ProgressFraction(0,2), ProgressFraction(3,8), completed=false))

      // Should not affect the counts we're testing because the CompletedType is different
      val child1Other = ProgressReport[Int,Int](SwarmOneIterationCompleted, childIndex, epFalse, iteration=1, child0of2desc1of4, TerminateCriteriaNotMet)
      underTest.proxy_makeProgress( child1Other) must beEqualTo( Progress( ProgressFraction(0,2), ProgressFraction(1,8), completed=false))

      val child2of2desc4of4 = Progress( ProgressFraction(2,2), ProgressFraction(4,4), completed=true)
      val child1d = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, epFalse, iteration=1, child2of2desc4of4, TerminateCriteriaMetNow)
      underTest.proxy_makeProgress( child1d) must beEqualTo( Progress( ProgressFraction(1,2), ProgressFraction(4,8), completed=false))

      // Use child1 messages as if they were from child2
      underTest.proxy_makeProgress( child1a) must beEqualTo( Progress( ProgressFraction(1,2), ProgressFraction(5,8), completed=false))
      underTest.proxy_makeProgress( child1b) must beEqualTo( Progress( ProgressFraction(1,2), ProgressFraction(6,8), completed=false))
      underTest.proxy_makeProgress( child1c) must beEqualTo( Progress( ProgressFraction(1,2), ProgressFraction(7,8), completed=false))
      // finally, we reprot completed=true!
      underTest.proxy_makeProgress( child1d) must beEqualTo( Progress( ProgressFraction(2,2), ProgressFraction(8,8), completed=true))
    }

  }
}
