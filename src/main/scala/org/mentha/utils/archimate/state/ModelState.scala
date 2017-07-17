package org.mentha.utils.archimate.state

import org.mentha.utils.archimate.model._
import org.mentha.utils.archimate.model.view._

import scala.util._

object ModelState {

  import play.api.libs.json._

  type JsonObject = json.JsonObject
  val JsonObject = json.JsonObject

  type Type = String
  type ID = Identifiable.ID

  private[state] implicit class ImplicitModel(model: Model) {

    private def withPathAndName(params: JsonObject)(callback: (List[String], String) => Unit) = {
      val pathOpt = (params \ json.names.`path`).asOpt[List[String]]
      val nameOpt = (params \ json.names.`name`).asOpt[String]
      (pathOpt, nameOpt) match {
        case (Some(path), Some(name)) => callback(path, name)
        case _ =>
      }
    }

    val prepare: PartialFunction[Command, ChangeSet] = {
      case c @ Commands.AddElement(tp, _) => {
        val eMeta = nodes.mapElements.getOrElse(tp, throw new IllegalStateException(s"Unexpected element type: ${tp}"))
        ChangeSets.AddConcept(Identifiable.generateId(), c)
      }
      case c @ Commands.AddConnector(tp, rtp, _) => {
        val cMeta = nodes.mapRelationshipConnectors.getOrElse(tp, throw new IllegalStateException( s"Unexpected relationship connector type: ${tp}"))
        val rMeta = edges.mapRelations.getOrElse(rtp, throw new IllegalStateException( s"Unexpected relationship type: ${tp}"))
        ChangeSets.AddConcept(Identifiable.generateId(), c)
      }
      case c @ Commands.AddRelationship(tp, srcId, dstId, _) => {
        // TODO: move this to ModelValidator
        val rMeta = edges.mapRelations.getOrElse(tp, throw new IllegalStateException(s"Unexpected relationship type: ${tp}"))
        val src = model.concept[Concept](srcId)
        val dst = model.concept[Concept](dstId)
        require(rMeta.isLinkPossible(src.meta, dst.meta)) // TODO: implement me
        ChangeSets.AddConcept(Identifiable.generateId(), c)
      }
      case c @ Commands.AddView(_, params) => {
        // TODO: move this to ModelValidator
        withPathAndName(params) {
          case (path, name) => require(model.findView(path, name).isEmpty, s"Duplicate view name: ${name}.")
        }
         ChangeSets.AddView(Identifiable.generateId(), c)
      }
      case c @ Commands.ModView(id, params) => {
        // TODO: move this to ModelValidator
        withPathAndName(params) {
          case (path, name) => model.findView(path, name) match {
            case Some(v) => require(v.id == id, s"Duplicate view name: ${name}.")
            case None =>
          }
         }
        ChangeSets.ModView(c)
      }
      case c @ Commands.DelView(_) => {
        ChangeSets.DelView(c)
      }
    }
  }

  private[state] implicit class ImplicitConcept(concept: Concept) {
    val prepare: PartialFunction[Command, ChangeSet] = {
      case c @ Commands.ModConcept(_, _) => ChangeSets.ModConcept(c)
      case c @ Commands.DelConcept(_) => ChangeSets.DelConcept(c)
    }
  }

  private[state] implicit class ImplicitView(data: (Model, View)) {
    val (model, view) = data
    val prepare: PartialFunction[Command, ChangeSet] = {
      case c @ Commands.AddViewNotes(_, _) => {
        ChangeSets.AddViewObject(Identifiable.generateId(), c)
      }
      case c @ Commands.AddViewConnection(_, src, dst, _) => {
        val source = view.get[ViewObject](src)
        val target = view.get[ViewObject](dst)
        require(
          !view.objects[ViewEdge].exists { edge => (edge.source == source && edge.target == target) },
          s"Duplicate edge: ${src} -> ${dst}"
        )
        ChangeSets.AddViewObject(Identifiable.generateId(), c)
      }
      case c @ Commands.AddViewNodeConcept(_, conceptId, _) => {
        val concept = model.concept[NodeConcept](conceptId)
        require(
          !view.objects[ViewObject with ViewConcept].exists { vc => (vc.concept.id == concept) },
          s"Duplicate concept: ${conceptId}"
        )
        ChangeSets.AddViewObject(Identifiable.generateId(), c)
      }
      case c @ Commands.AddViewRelationship(_, src, dst, conceptId, _) => {
        val concept = model.concept[Relationship](conceptId)
        val source = view.get[ViewObject with ViewConcept](src)
        val target = view.get[ViewObject with ViewConcept](dst)
        require(
          !view.objects[ViewEdge].exists { edge => (edge.source == source && edge.target == target) },
          s"Duplicate edge: ${src} -> ${dst}"
        )
        require(
          !view.objects[ViewObject with ViewConcept].exists { vc => (vc.concept.id == concept) },
          s"Duplicate concept: ${conceptId}"
        )
        ChangeSets.AddViewObject(Identifiable.generateId(), c)
      }
    }
  }

  private[state] implicit class ImplicitViewObject(viewObject: ViewObject) {
    val prepare: PartialFunction[Command, ChangeSet] = {
      case c @ Commands.ModViewObject(_, _, _) => ChangeSets.ModViewObject(c)
      case c @ Commands.DelViewObject(_, _) => ChangeSets.DelViewObject(c)
    }
  }

  /** */
  private[state] sealed trait ConnectionParams {
    def src: ID
    def dst: ID
  }

  /** indicates that the request has a set of parameters to be used */
  sealed trait WithParams {
    def params: JsonObject
  }

  /** indicates that the request has the current object id */
  sealed trait ById[T] {
    def id: ID
  }

  /** indicates that the request is about object creation */
  sealed trait Add[T] extends WithParams {

  }

  /** indicates that the request is about object modification */
  sealed trait Mod[T] extends ById[T] with WithParams {

  }

  /** indicates that the request is about object deletion */
  sealed trait Del[T] extends ById[T] {

  }

  /**
    * OUTGOING MESSAGE
    * Result of the request.
    */
  sealed trait Response {
    def toJson: JsonObject
  }

  /**
    * INCOMING MESSAGE
    * An incoming request - parsed incoming message text
    */
  sealed trait Request {}

  /**
    * Request to do something (it always changes the state)
    */
  sealed trait Command extends Request {}

  /**
    * Request to look up the state (does not change the state)
    */
  sealed trait Query extends Request {}

  /**
    * 2nd phase of state changing process:
    * the command is parsed, verified and connected to the model (all the related data is stored in this wrapper)
    */
  sealed trait ChangeSet {
    def command: Command
    def commit(state: ModelState): Try[JsonObject]
  }


  /** nothing to do request (neither a command nor a query) */
  case class Noop() extends Request {}

  //
  object Queries {

    sealed trait Get[T] extends Query with ById[T] {}

    case class GetModel(id: ID = Identifiable.EMPTY_ID) extends Get[Model] {}

    case class GetConcept(id: ID) extends Get[Concept] {}

    case class GetView(id: ID) extends Get[View] {}

    case class GetViewObject(viewId: ID, id: ID) extends Get[ViewObject] {}
  }



  //
  object Commands {

    sealed trait ConceptCommand extends Command {}

    sealed trait AddConceptCommand[T] extends ConceptCommand with Add[T] {
      def tp: Type
    }

    case class AddElement(tp: Type, params: JsonObject) extends AddConceptCommand[Element] {}

    case class AddConnector(tp: Type, rel: Type, params: JsonObject) extends AddConceptCommand[RelationshipConnector] {}

    case class AddRelationship(tp: Type, src: ID, dst: ID, params: JsonObject) extends AddConceptCommand[Relationship] with ConnectionParams {}

    case class DelConcept(id: ID) extends ConceptCommand with Del[Concept] {}

    case class ModConcept(id: ID, params: JsonObject) extends ConceptCommand with Mod[Concept] {}

    sealed trait ViewCommand extends Command {
    }

    case class AddView(viewpoint: Type, params: JsonObject) extends ViewCommand with Add[View] {}

    case class DelView(id: ID) extends ViewCommand with Del[View] {}

    case class ModView(id: ID, params: JsonObject) extends ViewCommand with Mod[View] {}

    sealed trait ViewObjectCommand extends Command {
      def viewId: ID
    }

    sealed trait AddViewObjectCommand[T] extends ViewObjectCommand with Add[T] {

    }

    case class AddViewNotes(viewId: ID, params: JsonObject) extends AddViewObjectCommand[ViewNotes] {}

    case class AddViewNodeConcept(viewId: ID, conceptId: ID, params: JsonObject) extends AddViewObjectCommand[ViewNodeConcept[_]] {}

    case class AddViewConnection(viewId: ID, src: ID, dst: ID, params: JsonObject) extends AddViewObjectCommand[ViewConnection] with ConnectionParams {}

    case class AddViewRelationship(viewId: ID, src: ID, dst: ID, conceptId: ID, params: JsonObject) extends AddViewObjectCommand[ViewRelationship[_]] with ConnectionParams {}

    case class DelViewObject(viewId: ID, id: ID) extends ViewObjectCommand with Del[ViewObject] {}

    case class ModViewObject(viewId: ID, id: ID, params: JsonObject) extends ViewObjectCommand with Mod[ViewObject] {}

    sealed trait PlaceViewObjectCommand extends ViewObjectCommand with Mod[ViewObject] {}

    case class PlaceViewNode(viewId: ID, id: ID, position: Point, size: Size, params: JsonObject) extends PlaceViewObjectCommand {}

    case class PlaceViewEdge(viewId: ID, id: ID, points: List[Point], params: JsonObject) extends PlaceViewObjectCommand {}

  }

  object ChangeSets {

    private val OP_ADD = "+"
    private val OP_DEL = "-"
    private val OP_MOD = "@"
    private val OP_SET = "="

    private def toJsonDiff(model: Model, concept: Concept, op: String): JsonObject = {
      val field = concept match {
        case _: NodeConcept => json.names.`nodes`
        case _: EdgeConcept => json.names.`edges`
      }
      Json.obj(s"${OP_MOD}${field}" -> json.toJsonPair(
        concept,
        id => s"${op}${id}"
      ))
    }

    private def toJsonDiff(model: Model, view: View, op: String): JsonObject = {
      val field = "views"
      Json.obj(s"${OP_MOD}${field}" -> json.toJsonPair(
        view,
        id => s"${op}${id}"
      ))
    }

    private def toJsonDiff(model: Model, view: View, vo: ViewObject, op: String): JsonObject = {
      val field = vo match {
        case _: ViewNode => json.names.`nodes`
        case _: ViewEdge => json.names.`edges`
      }
      Json.obj(s"${OP_MOD}views" -> Json.obj(
        s"${OP_MOD}${view.id}" -> Json.obj(
          s"${OP_MOD}${field}" -> json.toJsonPair(
            vo,
            id => s"${op}${id}"
          )
        )
      ))
    }

    sealed trait ModelChangeSet extends ChangeSet {
      def command: Command
      def commit(model: Model): Try[JsonObject]
      override def commit(state: ModelState): Try[JsonObject] = commit(state.model)
    }

    case class AddConcept(newId: ID, command: Commands.AddConceptCommand[_]) extends ModelChangeSet with Add[Concept] {
      override def params: JsonObject = command.params
      override def commit(model: Model): Try[JsonObject] = Try {
        val concept = model.add(newId) {
          command match {
            case Commands.AddElement(tp, params) => json.readElement(tp, params)
            case Commands.AddConnector(tp, rel, params) => json.readRelationshipConnector(tp, rel, params)
            case Commands.AddRelationship(tp, src, dst, params) => json.readRelationship(tp, model.concept[Concept](src), model.concept[Concept](dst), params)
          }
        }
        toJsonDiff(model, concept, OP_ADD)
      }
    }

    case class ModConcept(command: Commands.ModConcept) extends ModelChangeSet with Mod[Concept] {
      override def id: ID = command.id
      override def params: JsonObject = command.params
      override def commit(model: Model): Try[JsonObject] = Try {
        val concept = model.concept[Concept](id) match {
          case el: Element => json.fillElement(el, params)
          case rc: RelationshipConnector => json.fillRelationshipConnector(rc, params)
          case rel: Relationship => json.fillRelationship(rel, params)
        }
        toJsonDiff(model, concept, OP_SET)
      }
    }

    case class DelConcept(command: Commands.DelConcept) extends ModelChangeSet with Del[Concept] {
      override def id: ID = command.id
      override def commit(model: Model): Try[JsonObject] = Try {
        val concept = model.concept[Concept](id).markAsDeleted()
        toJsonDiff(model, concept, OP_DEL)
      }
    }

    case class AddView(newId: ID, command: Commands.AddView) extends ModelChangeSet with Add[View] {
      override def params: JsonObject = command.params
      override def commit(model: Model): Try[JsonObject] = Try {
        val v = model.add(newId) { json.readView(command.viewpoint, params) }
        toJsonDiff(model, v, OP_ADD)
      }
    }

    case class ModView(command: Commands.ModView) extends ModelChangeSet with Mod[View] {
      override def id: ID = command.id
      override def params: JsonObject = command.params
      override def commit(model: Model): Try[JsonObject] = Try {
        val v = json.fillView(model.view(id), params)
        toJsonDiff(model, v, OP_SET)
      }
    }

    case class DelView(command: Commands.DelView) extends ModelChangeSet with Del[View] {
      override def id: ID = command.id
      override def commit(model: Model): Try[JsonObject] = Try {
        val v = model.view(id).markAsDeleted()
        toJsonDiff(model, v, OP_DEL)
      }
    }

    sealed trait ViewChangeSet extends ChangeSet {
      def command: Command
      def viewId: ID

      def commit(model: Model, view: View): Try[JsonObject]

      override def commit(state: ModelState): Try[JsonObject] = commit(
        state.model,
        state.model.view(viewId)
      )
    }

    case class AddViewObject(newId: ID, command: Commands.AddViewObjectCommand[_]) extends ViewChangeSet with Add[ViewObject] {
      override def viewId: ID = command.viewId
      override def params: JsonObject = command.params
      override def commit(model: Model, view: View): Try[JsonObject] = Try {
        val vo = view.add[ViewObject](newId) {
          command match {
            case Commands.AddViewNotes(_, p) => json.readViewNotes(p)
            case Commands.AddViewConnection(_, src, dst, params) => json.readViewConnection(view.get[ViewObject](src), view.get(dst), params)
            case Commands.AddViewNodeConcept(_, conceptId, params) => json.readViewNodeConcept(model.concept(conceptId), params)
            case Commands.AddViewRelationship(_, src, dst, conceptId, params) => json.readViewRelationship(view.get(src), view.get(dst), model.concept(conceptId), params)
          }
        }
        toJsonDiff(model, view, vo, OP_ADD)
      }
    }

    case class ModViewObject(command: Commands.ModViewObject) extends ViewChangeSet with Mod[ViewObject] {
      override def viewId: ID = command.viewId
      override def id: ID = command.id
      override def params: JsonObject = command.params
      override def commit(model: Model, view: View): Try[JsonObject] = Try {
        val vo = view.get[ViewObject](id) match {
          case vn: ViewNotes => json.fillViewNotes(vn, params)
          case vc: ViewConnection => json.fillViewConnection(vc, params)
          case vnc: ViewNodeConcept[_] => json.fillViewNodeConcept(vnc, params)
          case vrs: ViewRelationship[_] => json.fillViewRelationship(vrs, params)
        }
        toJsonDiff(model, view, vo, OP_SET)
      }
    }

    case class DelViewObject(command: Commands.DelViewObject) extends ViewChangeSet with Del[ViewObject] {
      override def viewId: ID = command.viewId
      override def id: ID = command.id
      override def commit(model: Model, view: View): Try[JsonObject] = Try {
        val vo = view.get[ViewObject](id).markAsDeleted()
        toJsonDiff(model, view, vo, OP_DEL)
      }
    }

    case class PlaceViewObject(command: Commands.PlaceViewObjectCommand) extends ViewChangeSet with Mod[ViewObject] {
      override def viewId: ID = command.viewId
      override def id: ID = command.id
      override def params: JsonObject = command.params
      override def commit(model: Model, view: View): Try[JsonObject] = Try {
        val vo = command match {
          case Commands.PlaceViewNode(_, id, position, size, _) => view.get[ViewNode](id).withPosition(position).withSize(size)
          case Commands.PlaceViewEdge(_, id, points, _) => view.get[ViewEdge](id).withPoints(points)
        }
        toJsonDiff(model, view, vo, OP_SET)
      }
    }

  }

  object Responses {

    /**
      * RESPONSE: ChangeSet is commit, the difference is provided.
      * @param modelId the model
      * @param changeSet the changeSet built from the incoming command
      * @param diffJson the model diff JSON
      */
    case class ModelChangeSet(modelId: String, changeSet: ModelState.ChangeSet, diffJson: ModelState.JsonObject) extends ModelState.Response {
      override def toJson: JsonObject = Json.obj(
        "_tp" -> "commit",
        "commit" -> diffJson
      )
    }

    /**
      * RESPONSE: Requested object json
      * @param modelId the model
      * @param request the original request
      * @param objectJson requested object json
      */
    case class ModelObjectJson(modelId: String, request: ModelState.Query, objectJson: ModelState.JsonObject) extends ModelState.Response {
      override def toJson: JsonObject = Json.obj(
        "_tp" -> "object",
        "object" -> objectJson
      )
    }

    /**
      * RESPONSE: There was an error
      * @param modelId the model
      * @param request original request, either parsed request or the original incoming message
      * @param error the error has been thrown
      */
    case class ModelStateError(modelId: String, request: Either[ModelState.Request, String], error: Throwable) extends ModelState.Response {
      override def toJson: JsonObject = Json.obj(
        "_tp" -> "error",
        "error" -> error.getMessage
      )
    }

    /**
      * RESPONSE: The client does nothing for a long time, we are still here...
      * @param modelId the model
      */
    case class ModelStateNoop(modelId: String) extends ModelState.Response {
      override def toJson: JsonObject = Json.obj(
        "_tp" -> "noop",
        "noop" -> JsonObject.empty
      )
    }


  }

  /** it translates given incomming message text to a request */
  def parseMessage(value: String): ModelState.Request = {
    import play.api.libs.json._
    val (cmd, js) = Json.parse(value) match {
      case o : JsonObject if o.value.size == 0 => ("noop", o)
      case o : JsonObject if o.value.size == 1 => o.fields.head match { case (cmd, jsv) => (cmd, jsv.as[JsonObject]) }
      case _ => throw new IllegalStateException(s"Unexpected command structure: ${value}.")
    }

    cmd match {
      case "get-model" => Queries.GetModel(id = (js \ "id").asOpt[ID].getOrElse(Identifiable.EMPTY_ID))

      case "get-concept" => Queries.GetConcept(id = (js \ "id").as[ID])
      case "get-view" => Queries.GetView(id = (js \ "id").as[ID])
      case "get-view-object" => Queries.GetViewObject(viewId = (js \ "viewId").as[ID], id = (js \ "id").as[ID])

      case "add-element" => Commands.AddElement(tp = (js \ json.names.`tp`).as[Type], params = js)
      case "add-connector" => Commands.AddConnector(tp = (js \ json.names.`tp`).as[Type], rel = (js \ json.names.`rel`).as[Type], params = js)
      case "add-relationship" => Commands.AddRelationship(tp = (js \ json.names.`tp`).as[Type], src = (js \ json.names.`src`).as[ID], dst = (js \ json.names.`dst`).as[ID], params = js)
      case "add-view" => Commands.AddView(viewpoint = (js \ json.names.`viewpoint`).as[Type], params = js)
      case "add-view-notes" => Commands.AddViewNotes(viewId = (js \ "viewId").as[ID], params = js)
      case "add-view-connection" => Commands.AddViewConnection(viewId = (js \ "viewId").as[ID], src = (js \ json.names.`src`).as[ID], dst = (js \ json.names.`dst`).as[ID], params = js)
      case "add-view-node-concept" => Commands.AddViewNodeConcept(viewId = (js \ "viewId").as[ID], conceptId = (js \ "concept").as[ID], params = js)
      case "add-view-relationship" => Commands.AddViewRelationship(viewId = (js \ "viewId").as[ID], src = (js \ json.names.`src`).as[ID], dst = (js \ json.names.`dst`).as[ID], conceptId = (js \ "concept").as[ID], params = js)

      case "del-concept" => Commands.DelConcept(id = (js \ "id").as[ID])
      case "del-view" => Commands.DelView(id = (js \ "id").as[ID])
      case "del-view-object" => Commands.DelViewObject(viewId = (js \ "viewId").as[ID], id = (js \ "id").as[ID])

      case "mod-concept" => Commands.ModConcept(id = (js \ "id").as[ID], params = js)
      case "mod-view" => Commands.ModView(id = (js \ "id").as[ID], params = js)
      case "mod-view-object" => Commands.ModViewObject(viewId = (js \ "viewId").as[ID], id = (js \ "id").as[ID], params = js)

      case "noop" => Noop()
      case _ => throw new IllegalStateException(s"Unexpected command: ${cmd}.")
    }
  }

  /** deserialize from string */
  private[state] def fromJson(json: String): ModelState = new ModelState(
    org.mentha.utils.archimate.model.json.fromJsonString(json)
  )

  /** serialize to string */
  private[state] def toJson(state: ModelState): String = {
    org.mentha.utils.archimate.model.json.toJsonString(state.model)
  }

}

class ModelState(private[state] val model: Model = new Model) {

  import ModelState._

  def prepare(command: Command): ChangeSet = command match {
    case Commands.AddElement(_, _) => model.prepare(command)
    case Commands.AddConnector(_, _, _) => model.prepare(command)
    case Commands.AddRelationship(_, _, _, _) => model.prepare(command)
    case Commands.AddView(_, _) => model.prepare(command)
    case Commands.AddViewNotes(viewId, _) => (model, view(viewId)).prepare(command)
    case Commands.AddViewConnection(viewId, _,_, _) => (model, view(viewId)).prepare(command)
    case Commands.AddViewNodeConcept(viewId, _,_) => (model, view(viewId)).prepare(command)
    case Commands.AddViewRelationship(viewId, _,_, _, _) => (model, view(viewId)).prepare(command)
    case c: Commands.ConceptCommand with ById[_] => concept(c.id).prepare(command)
    case c: Commands.ViewCommand with ById[_] => (model, view(c.id)).prepare(command)
    case c: Commands.ViewObjectCommand with ById[_] => viewObject(c.viewId, c.id).prepare(command)
  }

  def query(query: ModelState.Query): ModelState.JsonObject = query match {
    case Queries.GetModel(_) => json.toJsonPair(model)
    case Queries.GetConcept(id) => json.toJsonPair(concept(id))
    case Queries.GetView(id) => json.toJsonPair(view(id))
    case Queries.GetViewObject(viewId, id) => json.toJsonPair(viewObject(viewId, id))
  }

  private def concept(id: ID): Concept = model.concept[Concept](id)
  private def view(viewId: ID): View = model.view(viewId)
  private def viewObject(viewId: ID, id: ID): ViewObject = view(viewId).get[ViewObject](id)

  def commit(changeSet: ChangeSet): JsonObject = {
    changeSet.commit(this).get
  }

}
