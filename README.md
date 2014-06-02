akka-multiswarm
===============

Scala Akka Actor based PSO (Particle Swarm Optimization) with multiple swarms

### Goals

* Single swarm or multi-swarm
* Extensible framework for building PSOs with various social strategies.
  * Each level in the swarm hierarchy can have a different social strategy.
  * Available strategies can be mixed and matched or you can create you're own.
* Use with any particle type and fitness type. A sample implementation using [Breeze](https://github.com/scalanlp/breeze) DenseVector[Double] is included.
* Use Akka actors to coordinate multiple swarms.
* Fast. It's a goal. No performance tests yet.

### Local and Regional Swarms

A single swarm is a *LocalSwarm* which runs inside a single actor and contains the particles and one best position. A *LocalSwarm* is composed of a **LocalSwarmActor** which handles messaging and a **LocalSwarmIntelligence** which does the actual work for each iteration. A LocalSwarmIntelligence can have as many particles and iterations as you configure.

A *RegionalSwarm* supervises multiple child swarms and coordinates their social interactions. It has a single best position representing the best position reported from its children. The children can be configured as RegionalSwarms or LocalSwarms. You can configure any number of RegionalSwarm hierarchies to go as deep and as wide (more children) as needed. A RegionalSwarm is composed of a **RegionalSwarmActor** which handles messaging and a **RegionalSwarmIntelligence** which does the actual work.

### Social Interaction

A swarm does not talk to its siblings directly. It can only send reports to its parent (a *RegionalSwarm*). A *RegionalSwarm* also sends reports to its parent. In addition, a *RegionalSwarm* can share information among its children via **InfluentialPosition** messages. All swarms have the option to share or not share and listen or not listen, depending on the social strategy contained in its *SwarmIntelligence*.

*Reports* go up the hierarchy. *InfluentialPositions* go down the hierarchy.

### Swarm Intelligence

A *SwarmActor* recieves messages and hands work off to the *SwarmIntelligence*. A *SwarmIntelligence* encompasses multiple aspects of the swarms behavior. Each aspect is documented below. In general, a *SwarmIntelligence* is composed by choosing a particular implementation of each aspect to get the desired overall social behavior.

**Id** — Refers to the *Id* from psychoanalysis. It contains innate information and processes used by the other parts of a SwarmIntelligence. Of these, *bestPosition* and actor *context* are most prominent.

**Worker** — Runs each iteration of the swarm. For a regional swarm the Worker is a *RegionalSupervisor* which
manages sub swarms (which themselves can be local or regional swarms). For example, an *AsyncRegionalSupervisor* receives a report from a child and immediated shares the current best position with all children. A different supervisor could be chosen which would wait for all children to report for a particular iteration, and only then; send the best position to all children.

**SocialInfluence** — determines what is done when a swarm learns of an *InfluentialPosition* from another swarm
in a multi-swarm system.

**ReportingStrategy** — Controls what gets reported to parent swarms and how often. The *ReportingStrategy* can be different for local and regional swarms. For example, *ContinuousLocalReporting* sends a report when each iteration has completed; while a *PeriodicLocalReporting* strategy reports only every 50 iterations (the number is configurable).

