package sample


import akka.actor._
import akka.event.{LoggingAdapter, Logging}
import breeze.linalg.{DenseVector, sum}
import breeze.numerics.abs
import com.hungrylearner.pso.particle.breezedvd.so._
import com.hungrylearner.pso.swarm._
import com.hungrylearner.pso.swarm.Report.ProgressReport
import scala.concurrent.duration._


object BasicSingleObjective {

  class MyReportingStrategy[F,P]( override val parent: ActorRef)
    extends ParentReporter[F,P]
    with PeriodicLocalReporting[F,P]

  class MySingleLocalSwarmIntelligence[F,P]( override val config: LocalSwarmConfig[F,P], override val childIndex: Int, override val context: ActorContext)
    extends LocalSwarmIntelligence[F,P]
    with SingleEgoImpl[F,P]
    with SingleLocalWorker[F,P]
    with LocalSocialInfluence[F,P]
    with LocalTerminateOnMaxIterations[F,P]
  {

    override protected val Logger: LoggingAdapter = Logging.getLogger(context.system, this)
    override val reportingStrategy: LocalReportingStrategy[F, P] = new MyReportingStrategy[F,P]( context.parent)
  }

  class MyMutablePosition( initialPosition: DenseVector[Double], bounds: Array[(Double,Double)]) extends MutablePositionDVD( initialPosition, bounds) {

    override def evaluateFitness( v: DenseVector[Double], iteration: Int): Double = {
      // Simple swarm objective. The best particle is the one wth all values closest to zero.
      sum( abs(v))
    }

    /**
     * EnforceConstraints is called by addVelocity() after adding the velocity and before calling evaluateFitness.
     * This is an opportunity to change the particles position if it's not within custom constraints. Constraints
     * can include particle boundaries or invalid combinations of values within the position vector.
     */
    override def enforceConstraints() = {
      // super.enforceConstraints()
      // TODO: enforce bounds here or in super!.
    }

    /**
     * The superclass's constructor copies the position before using it.
     * @return a deep copy.
     */
    override def copy: MyMutablePosition = new MyMutablePosition( value, bounds)
  }

  case object Initialize

  def makeLocalSwarmConfig( iterations: Int, system: ActorSystem) = {

    val positionDimension = 1     // The number of items in a position vector.
    val positionBounds = Array[(Double,Double)]( (0,100), (0,10))
    val particleSpaceContext = ParticleSpaceDVD.ParticleSpaceContext(
      initialPosition = (dim: Int, particleIndex: Int) => new MyMutablePosition( DenseVector.tabulate[Double]( dim) (i => boundedRandom( positionBounds(i))), positionBounds),
      positionBounds,
      initialHistory = (dim: Int) => DenseVector.fill[Double]( dim) (0.0)
    )

    val velocityBounds = (-5.0, 5.0)
    val randomFunction = math.random _
    val kinematicContext = KinematicParticleSpaceDVD.KinematicContext(
      initialVelocity = (dim: Int, particleIndex: Int) => DenseVector.fill[Double]( dim) (boundedRandom( velocityBounds)),
      velocityBounds = velocityBounds,
      inertiaWeight = (i: Int) => {i.toDouble / iterations.toDouble * 10.0 + 0.25},
      phiP = 0.25,
      phiG = 0.25,
      random = randomFunction,
      system = system
    )

    val particleContext = ParticleDVD.ParticleContext( positionDimension, particleSpaceContext, kinematicContext)

    // Create an "actor-in-a-box"
    //val inbox = Inbox.create(system)

    val particleCount = 3
    val simulationContext = SimulationContext( iterations, system)
    def makeParticle( swarmIndex: Int, particleIndex: Int, particleCount: Int) = new ParticleDVD(simulationContext,  particleContext, particleIndex)

    new LocalSwarmConfig[Double,DenseVector[Double]]( particleCount, makeParticle, simulationContext)
  }

  private def boundedRandom( bounds: (Double,Double)): Double = {
    val range = bounds._2 - bounds._1
    math.random * range + bounds._1
  }
}

class BasicSingleObjective( iterations: Int) extends Actor with ActorLogging {
  import BasicSingleObjective._
  import CompletedType._
  //  private val Logger = Logging.getLogger(system, this)

  var swarm: ActorRef = _
  val swarmAround = SwarmAround( iterations / 4)

  override def preStart() = {
    log.debug( "BasicSingleObjective.preStart")
    self ! Initialize
  }

  def receive = {
    case Initialize => initializePso
    case report: ProgressReport[Double,DenseVector[Double]] =>  onReport( report)
    case Terminated( child) =>
      log.info( s"LocalSwarmActor '${child.path.name}' Terminated ")
      context.system.shutdown()

    case unknownMessage: AnyRef => log.error( "RunPso.receive: Unknown message {}", unknownMessage)
  }

  def initializePso = {
    log.debug( "BasicSingleObjective.initializePso")

    if( swarm == null) {
      val swarmConfig = makeLocalSwarmConfig( iterations, context.system)

      val localSwarmIntelligenceFactory = ( childIndex: Int, context: ActorContext) =>
        new MySingleLocalSwarmIntelligence[Double,DenseVector[Double]]( swarmConfig, childIndex, context)

      val props = Props(classOf[LocalSwarmActor[Double,DenseVector[Double]]], localSwarmIntelligenceFactory, 0)
      swarm = context.actorOf(props,  "localSwarm1")
      context.watch( swarm) // watch for child Terminated
      swarm ! swarmAround
    } else {
      log.error( "BasicSingleObjective.initializePso swarm already initialized!")
    }
  }

  def onReport( report: ProgressReport[Double,DenseVector[Double]]) = {
    log.info(  s"++++++++++++++++++++++++++++++++ ProgressReport ${report.iteration} ${report.newBestPositions(0).position.value(0)}")
    if( report.completedType != SwarmingCompleted)
      swarm ! swarmAround
    else {
      // Stop the child. Since we're watching, We'll receive a Terminated when child is stopped.
      context.stop( swarm)
    }
  }
}

object Application extends App {

  val system = ActorSystem("simulation")
  private val Logger = Logging.getLogger(system, this)
  Logger.debug( "BasicSingleObjective begin")

  val iterations = 100
  val runPso = system.actorOf( Props( classOf[BasicSingleObjective], iterations),  "runPso")

}

