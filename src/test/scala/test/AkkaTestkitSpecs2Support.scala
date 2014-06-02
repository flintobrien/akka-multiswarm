package test

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.specs2.mutable.After

/**
 * A tiny class that can be used as a Specs2 'context'.
 *
 * Usage:
 *
 *   "A TestKit" should {
 *     /* for every case where you would normally use "in", use
 *        "in new AkkaTestkitSpecs2Support" to create a new 'context'. */
 *     "work properly with Specs2 unit tests" in new AkkaTestkitSpecs2Support {
 *         within(1 second) {
 *           system.actorOf(Props(new Actor {
 *             def receive = { case x ⇒ sender ! x }
 *           })) ! "hallo"
 *
 *           expectMsgType[String] must be equalTo "hallo"
 *         }
 *       }
 *   }
 *
 * From
 * http://blog.xebia.com/2012/10/01/testing-akka-with-specs2/
 *
 */
/* A tiny class that can be used as a Specs2 'context'. */
abstract class AkkaTestkitSpecs2Support extends TestKit(ActorSystem("testSystem"))
with After
with ImplicitSender
//  with MustMatchers
{
  // make sure we shut down the actor system after all tests have run
  def after = system.shutdown()
}


