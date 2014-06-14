package com.hungrylearner.pso.swarm

import org.specs2.mutable._
import org.specs2.mock.Mockito
import akka.actor.{ActorRef, Actor, Props}
import scala.concurrent.duration._
import akka.testkit.{TestProbe, TestActorRef}
import org.specs2.time.NoTimeConversions
import CompletedType._
import com.hungrylearner.pso.swarm.Report.{Progress,ProgressFraction,ProgressReport}
import test.AkkaTestkitSpecs2Support
import com.hungrylearner.pso.particle.EvaluatedPosition

class ReporterSpec extends Specification with NoTimeConversions with Mockito {
  sequential // forces all tests to be run sequentially

  import TerminateCriteriaStatus._

  "A TestKit" should {
    /* for every case where you would normally use "in", use
       "in new AkkaTestkitSpecs2Support" to create a new 'context'. */
    "work properly with Specs2 unit tests" in new AkkaTestkitSpecs2Support {
      within(1 second) {
        system.actorOf(Props(new Actor {
          def receive = { case x ⇒ sender ! x }
        })) ! "hallo"

        expectMsgType[String] must be equalTo "hallo"
      }
    }
  }

  /*
   * See https://gist.github.com/archie/7952671 for examples of testing parent/child interactions.
   */


  "ParentReporter Implementation" should {

    "  Send all events to parent" in new AkkaTestkitSpecs2Support {
      within(1 second) {
        class UnderTest( override val parent: ActorRef) extends ParentReporter[Int, Int]
        val parent = TestProbe()
        val underTest = new UnderTest( parent.ref)

        val report = mock[ProgressReport[Int,Int]]
        underTest.report( report)
        parent.expectMsgType[ProgressReport[Int,Int]] must be( report)
      }
    }

    val childIndex = 0
    val iteration = 1
    val evaluatedPosition = mock[EvaluatedPosition[Int, Int]]
    val progress = mock[Progress]

    "  Stack with ContinuousLocalReporting to report all events" in new AkkaTestkitSpecs2Support {
      within(1 second) {
        class UnderTest( override val parent: ActorRef)
          extends ParentReporter[Int, Int]
          with ContinuousLocalReporting[Int, Int]
        val parent = TestProbe()
        val underTest = new UnderTest( parent.ref)

        val report = mock[ProgressReport[Int,Int]]
        underTest.report( report)
        parent.expectMsgType[ProgressReport[Int,Int]] must be( report)

        underTest.reportOneIterationCompleted(childIndex, evaluatedPosition, iteration, progress)
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo(ProgressReport[Int, Int](SwarmOneIterationCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaNotMet))

        underTest.reportSwarmAroundCompleted(childIndex, evaluatedPosition, iteration, progress)
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo(ProgressReport[Int, Int](SwarmAroundCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaNotMet))

        underTest.reportSwarmingCompleted(childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaMetNow)
        parent.expectMsgType[ProgressReport[Int,Int]] must beEqualTo(ProgressReport[Int, Int](SwarmingCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaMetNow))
      }

    }
  }

  "LocalReportingStrategy Implementations" should {

    val childIndex = 0
    val iteration = 1
    val evaluatedPosition = mock[EvaluatedPosition[Int,Int]]
    val progress = mock[Progress]

    "  ContinuousLocalReporting should report all events" in {

      var result: ProgressReport[Int, Int] = null
      class UnderTest extends ContinuousLocalReporting[Int,Int] {
        override def report(progressReport: ProgressReport[Int, Int]) = result = progressReport
      }
      val underTest = new UnderTest

      underTest.reportOneIterationCompleted( childIndex, evaluatedPosition, iteration, progress)
      result must beEqualTo( ProgressReport[Int,Int](SwarmOneIterationCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaNotMet))

      underTest.reportSwarmAroundCompleted( childIndex, evaluatedPosition, iteration, progress)
      result must beEqualTo( ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaNotMet))

      underTest.reportSwarmingCompleted( childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaMetNow)
      result must beEqualTo( ProgressReport[Int,Int](SwarmingCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaMetNow))
    }

    "  PeriodicLocalReporting should report all except SwarmOneIterationCompleted" in {

      var result: ProgressReport[Int, Int] = null
      class UnderTest extends PeriodicLocalReporting[Int,Int] {
        override def report(progressReport: ProgressReport[Int, Int]) = result = progressReport
      }
      val underTest = new UnderTest

      underTest.reportOneIterationCompleted( childIndex, evaluatedPosition, iteration, progress)
      result must beNull

      underTest.reportSwarmAroundCompleted( childIndex, evaluatedPosition, iteration, progress)
      result must beEqualTo( ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaNotMet))

      underTest.reportSwarmingCompleted( childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaMetNow)
      result must beEqualTo( ProgressReport[Int,Int](SwarmingCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaMetNow))
    }

    "  SwarmingCompletedLocalReporting should only report SwarmingCompleted" in {

      var result: ProgressReport[Int, Int] = null
      class UnderTest extends SwarmingCompletedLocalReporting[Int,Int] {
        override def report(progressReport: ProgressReport[Int, Int]) = result = progressReport
      }
      val underTest = new UnderTest

      underTest.reportOneIterationCompleted( childIndex, evaluatedPosition, iteration, progress)
      result must beNull

      underTest.reportSwarmAroundCompleted( childIndex, evaluatedPosition, iteration, progress)
      result must beNull

      underTest.reportSwarmingCompleted( childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaMetNow)
      result must beEqualTo( ProgressReport[Int,Int](SwarmingCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaMetNow))
    }

  }

  "RegionalReportingStrategy Implementations" should {

    val childIndex = 0
    val childIndex2 = 1
    val iteration = 1
    val evaluatedPosition = mock[EvaluatedPosition[Int,Int]]
    val evaluatedPosition2 = mock[EvaluatedPosition[Int,Int]]
    val progress = mock[Progress]
    val progress2 = mock[Progress]
    val prSwarmOneIterationCompleted = ProgressReport[Int,Int](SwarmOneIterationCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaNotMet)
    val prSwarmAroundCompleted = ProgressReport[Int,Int](SwarmAroundCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaNotMet)
    val prSwarmingCompleted = ProgressReport[Int,Int](SwarmingCompleted, childIndex, evaluatedPosition, iteration, progress, TerminateCriteriaMetNow)

    "  ContinuousRegionalReporting should report all events" in {

      var result: ProgressReport[Int, Int] = null
      class UnderTest extends ContinuousRegionalReporting[Int,Int] {
        override def report(progressReport: ProgressReport[Int, Int]) = result = progressReport
      }
      val underTest = new UnderTest


      underTest.reportForRegion( prSwarmOneIterationCompleted, childIndex2, evaluatedPosition2, progress2, TerminateCriteriaNotMet)
      result must beEqualTo( ProgressReport[Int,Int](SwarmOneIterationCompleted, childIndex2, evaluatedPosition2, iteration, progress2, TerminateCriteriaNotMet))

      underTest.reportForRegion( prSwarmAroundCompleted, childIndex2, evaluatedPosition2, progress2, TerminateCriteriaNotMet)
      result must beEqualTo( ProgressReport[Int,Int](SwarmAroundCompleted, childIndex2, evaluatedPosition2, iteration, progress2, TerminateCriteriaNotMet))

      underTest.reportForRegion( prSwarmingCompleted, childIndex2, evaluatedPosition2, progress2, TerminateCriteriaMetNow)
      result must beEqualTo( ProgressReport[Int,Int](SwarmingCompleted, childIndex2, evaluatedPosition2, iteration, progress2, TerminateCriteriaMetNow))
    }

    "  PeriodicRegionalReporting should report all events" in {

      var result: ProgressReport[Int, Int] = null
      class UnderTest extends PeriodicRegionalReporting[Int,Int] {
        override def report(progressReport: ProgressReport[Int, Int]) = result = progressReport
      }
      val underTest = new UnderTest


      underTest.reportForRegion( prSwarmOneIterationCompleted, childIndex2, evaluatedPosition2, progress2, TerminateCriteriaNotMet)
      result must beNull

      underTest.reportForRegion( prSwarmAroundCompleted, childIndex2, evaluatedPosition2, progress2, TerminateCriteriaNotMet)
      result must beEqualTo( ProgressReport[Int,Int](SwarmAroundCompleted, childIndex2, evaluatedPosition2, iteration, progress2, TerminateCriteriaNotMet))

      underTest.reportForRegion( prSwarmingCompleted, childIndex2, evaluatedPosition2, progress2, TerminateCriteriaMetNow)
      result must beEqualTo( ProgressReport[Int,Int](SwarmingCompleted, childIndex2, evaluatedPosition2, iteration, progress2, TerminateCriteriaMetNow))
    }

    "  SwarmingCompletedRegionalReporting should report all events" in {

      var result: ProgressReport[Int, Int] = null
      class UnderTest extends SwarmingCompletedRegionalReporting[Int,Int] {
        override def report(progressReport: ProgressReport[Int, Int]) = result = progressReport
      }
      val underTest = new UnderTest


      underTest.reportForRegion( prSwarmOneIterationCompleted, childIndex2, evaluatedPosition2, progress2, TerminateCriteriaNotMet)
      result must beNull

      underTest.reportForRegion( prSwarmAroundCompleted, childIndex2, evaluatedPosition2, progress2, TerminateCriteriaMetNow)
      result must beNull

      underTest.reportForRegion( prSwarmingCompleted, childIndex2, evaluatedPosition2, progress2, TerminateCriteriaMetNow)
      result must beEqualTo( ProgressReport[Int,Int](SwarmingCompleted, childIndex2, evaluatedPosition2, iteration, progress2, TerminateCriteriaMetNow))
    }


  }

}
