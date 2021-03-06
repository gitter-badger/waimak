package com.coxautodata.waimak.dataflow

import scala.reflect.{ClassTag, classTag}

/**
  * Maintains data flow entities (the inputs and outputs of data flow actions). Every entity has a label which must be
  * unique across the data flow.
  *
  * @param entities a map of label -> entity
  */
class DataFlowEntities(private val entities: Map[String, Option[Any]]) {

  def filterLabels(labels: List[String]): DataFlowEntities = DataFlowEntities(entities.filterKeys(labels.contains))

  def keySet: Set[String] = entities.keySet

  def get[T: ClassTag](label: String): T = {
    val entity = entities.get(label)
    entity match {
      case Some(Some(value: T)) => value
      case Some(Some(value)) => throw new DataFlowException(
        s"Label $label not of requested type ${classTag[T].runtimeClass}. Actual type: ${value.getClass.getSimpleName}"
      )
      case Some(None) => throw new DataFlowException(s"Entity $label is undefined")
      case _ => throw new DataFlowException(s"Label $label does not exist")
    }
  }

  def getOption[T: ClassTag](label: String): Option[T] = {
    val entity = entities.get(label)
    entity match {
      case Some(Some(value: T)) => Some(value)
      case Some(None) => None
      case Some(Some(value)) => throw new DataFlowException(
        s"Label $label not of requested type ${classTag[T].runtimeClass}. Actual type: ${value.getClass.getSimpleName}"
      )
      case _ => throw new DataFlowException(s"Label $label does not exist")
    }
  }

  def ++(other: DataFlowEntities): DataFlowEntities = DataFlowEntities(entities ++ other.entities)

  def +(entity: (String, Option[Any])): DataFlowEntities = DataFlowEntities(entities + entity)

  def size: Int = entities.size

  def labels: Set[String] = entities.keySet

  def canEqual(other: Any): Boolean = other.isInstanceOf[DataFlowEntities]

  def map[A](f: Any => A) = DataFlowEntities(entities.mapValues(_.map(f)))

  def filterValues(cond: Any => Boolean): DataFlowEntities = DataFlowEntities(entities.filter {
    case (_, entity) => cond(entity)
  })

  def nonEmpty: Boolean = entities.nonEmpty

  def isEmpty: Boolean = entities.isEmpty

  def getAll: Seq[Option[Any]] = entities.values.toSeq

  def contains(label: String): Boolean = entities.contains(label)

  override def equals(other: Any): Boolean = other match {
    case that: DataFlowEntities =>
      (that canEqual this) &&
        hash == that.hash &&
        entities == that.entities
    case _ => false
  }

  lazy val hash: Int = {
    val state = Seq(entities)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def hashCode(): Int = hash
}

object DataFlowEntities {

  def apply(entities: Map[String, Option[Any]]): DataFlowEntities = new DataFlowEntities(entities)

  def empty = new DataFlowEntities(Map.empty)

}
