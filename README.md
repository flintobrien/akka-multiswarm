akka-multiswarm
===============

Scala Akka, actor-based PSO (Particle Swarm Optimization) with multiple swarms.

See: Wikipedia [PSO](http://en.wikipedia.org/wiki/Particle_swarm_optimization) and [Multi-swarm optimization](http://en.wikipedia.org/wiki/Multi-swarm_optimization) for general information.

### Goals

* Extensible framework for building PSOs with various social strategies.
  * Each level in the swarm hierarchy can have a different social strategy.
  * Available strategies can be mixed and matched or create you're own.
* Written in Scala with Akka actors to coordinate multiple swarms.
* Single swarm or multi-swarm
* Use with any particle type and fitness type. A sample implementation using [Breeze](https://github.com/scalanlp/breeze) DenseVector[Double] is included.
* Fast. TODO: Need performance tests.

### Local and Regional Swarms

![Local and Regional Swarms](https://github.com/flintobrien/akka-multiswarm/raw/master/swarms.png)

A *LocalSwarm* is a single swarm which runs inside a single actor and contains the particles and one best position. A *LocalSwarm* is composed of a **LocalSwarmActor** which handles messaging and a **LocalSwarmIntelligence** which does the actual work for each iteration. A *LocalSwarmIntelligence* can have as many particles and iterations as you configure.

A *RegionalSwarm* supervises multiple child swarms and coordinates their social interactions. It has a single best position representing the best position reported from its children. The children can be configured as *RegionalSwarms* or *LocalSwarms*. You can configure any number of *RegionalSwarm* hierarchies to go as deep and as wide (more children) as needed. A *RegionalSwarm* is composed of a **RegionalSwarmActor** which handles messaging and a **RegionalSwarmIntelligence** which does the actual work.

### Social Interaction

A swarm does not talk to its siblings directly. It can only send reports to its parent (a *RegionalSwarm*). A *RegionalSwarm* also sends reports to its parent. In addition, a *RegionalSwarm* can share information among its children via **InfluentialPosition** messages. All swarms have the option to share or not share and listen or not listen. It  depends on the social strategy contained in its *SwarmIntelligence*.

*Reports* go up the hierarchy. *InfluentialPositions* go down the hierarchy.

### Swarm Intelligence

![Swarm Intelligence](https://github.com/flintobrien/akka-multiswarm/raw/master/swarmintelligence.png)

A **SwarmActor** recieves messages and hands work off to a **SwarmIntelligence**. A *SwarmIntelligence* encompasses multiple aspects of the swarms behavior. Each aspect is documented below. In general, a *SwarmIntelligence* is composed with a particular implementation of each aspect to get the desired overall social behavior.

#### Id
Refers to the *Id* from psychoanalysis. It contains innate information and processes used by the other parts of a SwarmIntelligence. Of these, *bestPosition* and actor *context* are most prominent.

#### Worker & Supervisor
A *Worker* updates all particles for each iteration of the swarm. For a *RegionalSwarm*, the *Worker* is a *RegionalSupervisor* which manages sub swarms (which themselves can be local or regional swarms). For example, an *AsyncRegionalSupervisor* receives a report from a child and immediated shares the current best position with all children. A different supervisor could be chosen which waits for all children to report for a particular iteration, and only then; send the best position to all children.

#### ReportingStrategy
A *ReportingStrategy* controls what gets reported to parent swarms and how often. The *ReportingStrategy* can be different for local and regional swarms. For example, *ContinuousLocalReporting* sends a report when each iteration has completed; while a *PeriodicLocalReporting* strategy reports only every 10 iterations (configurable, see SwarmAround(iterations) command below).

**Reports are important because the *RegionalSwarms* don't do anything until they receive a report from a child.** There are no social interactions between swarms when there is no report sent to a *RegionalSwarm*. If you don't want a lot of social interaction between swarms, you can throttle it in the *LocalSwarm's* *ReportingStrategy* or with a different *RegionalSupervisor* or *SocialInfluence*. If you want all reports to propogate up to a graphical display, but not be shared among siblings, choose *ContinuousLocalReporting* and a different *RegionalSupervisor* or *LocalSocialInfluence* strategy.

#### SocialInfluence
*SocialInfluence* determines what is done when a swarm learns of an *InfluentialPosition* from its parent swarm
in a multi-swarm system.

#### Terminate Criteria
Decide when to terminate all swarming. This can be based on multiple criteria including max iterations. There is a standard trait for *LocalTerminateOnMaxIterations*. *RegionalTerminateOnMaxIterations* is unused and needs more thought.

### Commands

The following commands can be sent to each swarm. By default, when a command is sent to a multiswarm, it trickles down to all LocalSwarms.

##### SwarmOneIteration
Swarm for one iteration and wait for another command. When the SwarmOneIteration has completed, a SwarmOneIterationCompleted report is generated with the current best position. The swarm's *ReportingStrategy* decides whether to let the report go up to the parent or not.

##### SwarmAround( iterations: Int)
Swarm around for a number of iterations, then wait for another command. When the SwarmAround has completed, a SwarmAroundCompleted report is generated with the current best position. The swarm's *ReportingStrategy* decides whether to let the report go up to the parent or not.

If the simulation should run for 1000 iterations, you can send 1000 SwarmOneIteration, 100 SwarmAround(10) or one SwarmAround(1000) message. The difference is how often a *SwarmAroundCompleted* report is generated and how often you want social interactions between swarms. 

##### CancelSwarming
Not implemented yet, but seems useful.
