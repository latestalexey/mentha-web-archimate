package org.mentha.utils.archimate.model

import scala.reflect.ClassTag

trait ArchimateObject {
}

trait IdentifiedArchimateObject extends ArchimateObject with Identifiable {

}

trait NamedArchimateObject extends ArchimateObject {

  private[model] var _name: String = ""
  @inline def name: String = _name

  def withName(name: String): this.type = {
    this._name = name
    this
  }

}

trait PathBasedArchimateObject extends ArchimateObject {

  private[model] var _path: List[String] = Nil
  @inline def path: List[String] = _path

  def withPath(path: List[String]): this.type = {
    this._path = path
    this
  }

}

trait PropsArchimateObject extends ArchimateObject {

  private[model] var _properties: json.JsonObject = json.JsonObject.empty
  @inline def properties: json.JsonObject = _properties

  def withProperties(properties: json.JsonObject): this.type = {
    this._properties = properties
    this
  }

  def withProperty(name: String, value: json.JsonValue): this.type = {
    this._properties =
      if (null != value) { this._properties + (name -> value) }
      else { this._properties - name }
    this
  }

  def withProperty(name: String, value: String): this.type =
    withProperty(name, json.JsonString(value))

  def withProperty(name: String, value: Boolean): this.type =
    withProperty(name, json.JsonBoolean(value))

  def withProperty(name: String, value: scala.math.BigDecimal): this.type =
    withProperty(name, json.JsonNumber(value))

  def withProperty(name: String, value: Int): this.type =
    withProperty(name, BigDecimal(value))

  def withProperty(name: String, value: Long): this.type =
    withProperty(name, BigDecimal(value))

  def withProperty(name: String, value: Double): this.type =
    withProperty(name, BigDecimal(value))


  // TODO: def extension[E](implicit tp: ClassTag[E]): Option[E] = properties
  // TODO: .value.get(tp.runtimeClass.getName)
  // TODO: .map { v => v.as[E] }

  // TODO: def withExtension[E](e: E)(implicit tp: ClassTag[E]): this.type =
  // TODO: withProperty(tp.runtimeClass.getName, json.toJson(e))

}

/** Something which could be linked withing the edges */
trait Vertex {

}

/** Links two vertexes */
trait Edge[V <: Vertex] {
  def source: V
  def target: V
}