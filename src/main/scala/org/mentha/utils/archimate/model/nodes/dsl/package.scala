package org.mentha.utils.archimate.model.nodes

import org.mentha.utils.archimate.model._
import org.mentha.utils.archimate.model.nodes.impl._

// @javax.annotation.Generated(Array("org.mentha.utils.archimate.model.generator$"))
package object dsl {

  /** @see [[http://pubs.opengroup.org/architecture/archimate3-doc/chap05.html#_Toc451757969 Derivation Rules ArchiMate® 3.0 Specification ]] */
  class derived extends scala.annotation.Annotation { }

  def stakeholder(implicit model: Model): Stakeholder = model.add(new Stakeholder)
  def driver(implicit model: Model): Driver = model.add(new Driver)
  def assessment(implicit model: Model): Assessment = model.add(new Assessment)
  def goal(implicit model: Model): Goal = model.add(new Goal)
  def outcome(implicit model: Model): Outcome = model.add(new Outcome)
  def principle(implicit model: Model): Principle = model.add(new Principle)
  def requirement(implicit model: Model): Requirement = model.add(new Requirement)
  def constraint(implicit model: Model): Constraint = model.add(new Constraint)
  def meaning(implicit model: Model): Meaning = model.add(new Meaning)
  def value(implicit model: Model): Value = model.add(new Value)
  def resource(implicit model: Model): Resource = model.add(new Resource)
  def capability(implicit model: Model): Capability = model.add(new Capability)
  def courseOfAction(implicit model: Model): CourseOfAction = model.add(new CourseOfAction)
  def businessActor(implicit model: Model): BusinessActor = model.add(new BusinessActor)
  def businessRole(implicit model: Model): BusinessRole = model.add(new BusinessRole)
  def businessCollaboration(implicit model: Model): BusinessCollaboration = model.add(new BusinessCollaboration)
  def businessInterface(implicit model: Model): BusinessInterface = model.add(new BusinessInterface)
  def businessProcess(implicit model: Model): BusinessProcess = model.add(new BusinessProcess)
  def businessFunction(implicit model: Model): BusinessFunction = model.add(new BusinessFunction)
  def businessInteraction(implicit model: Model): BusinessInteraction = model.add(new BusinessInteraction)
  def businessEvent(implicit model: Model): BusinessEvent = model.add(new BusinessEvent)
  def businessService(implicit model: Model): BusinessService = model.add(new BusinessService)
  def businessObject(implicit model: Model): BusinessObject = model.add(new BusinessObject)
  def contract(implicit model: Model): Contract = model.add(new Contract)
  def representation(implicit model: Model): Representation = model.add(new Representation)
  def product(implicit model: Model): Product = model.add(new Product)
  def applicationComponent(implicit model: Model): ApplicationComponent = model.add(new ApplicationComponent)
  def applicationCollaboration(implicit model: Model): ApplicationCollaboration = model.add(new ApplicationCollaboration)
  def applicationInterface(implicit model: Model): ApplicationInterface = model.add(new ApplicationInterface)
  def applicationFunction(implicit model: Model): ApplicationFunction = model.add(new ApplicationFunction)
  def applicationInteraction(implicit model: Model): ApplicationInteraction = model.add(new ApplicationInteraction)
  def applicationProcess(implicit model: Model): ApplicationProcess = model.add(new ApplicationProcess)
  def applicationEvent(implicit model: Model): ApplicationEvent = model.add(new ApplicationEvent)
  def applicationService(implicit model: Model): ApplicationService = model.add(new ApplicationService)
  def dataObject(implicit model: Model): DataObject = model.add(new DataObject)
  def node(implicit model: Model): Node = model.add(new Node)
  def device(implicit model: Model): Device = model.add(new Device)
  def systemSoftware(implicit model: Model): SystemSoftware = model.add(new SystemSoftware)
  def technologyCollaboration(implicit model: Model): TechnologyCollaboration = model.add(new TechnologyCollaboration)
  def technologyInterface(implicit model: Model): TechnologyInterface = model.add(new TechnologyInterface)
  def path(implicit model: Model): Path = model.add(new Path)
  def communicationNetwork(implicit model: Model): CommunicationNetwork = model.add(new CommunicationNetwork)
  def technologyFunction(implicit model: Model): TechnologyFunction = model.add(new TechnologyFunction)
  def technologyProcess(implicit model: Model): TechnologyProcess = model.add(new TechnologyProcess)
  def technologyInteraction(implicit model: Model): TechnologyInteraction = model.add(new TechnologyInteraction)
  def technologyEvent(implicit model: Model): TechnologyEvent = model.add(new TechnologyEvent)
  def technologyService(implicit model: Model): TechnologyService = model.add(new TechnologyService)
  def artifact(implicit model: Model): Artifact = model.add(new Artifact)
  def equipment(implicit model: Model): Equipment = model.add(new Equipment)
  def facility(implicit model: Model): Facility = model.add(new Facility)
  def distributionNetwork(implicit model: Model): DistributionNetwork = model.add(new DistributionNetwork)
  def material(implicit model: Model): Material = model.add(new Material)
  def workPackage(implicit model: Model): WorkPackage = model.add(new WorkPackage)
  def deliverable(implicit model: Model): Deliverable = model.add(new Deliverable)
  def implementationEvent(implicit model: Model): ImplementationEvent = model.add(new ImplementationEvent)
  def plateau(implicit model: Model): Plateau = model.add(new Plateau)
  def gap(implicit model: Model): Gap = model.add(new Gap)
  def grouping(implicit model: Model): Grouping = model.add(new Grouping)
  def location(implicit model: Model): Location = model.add(new Location)
}
