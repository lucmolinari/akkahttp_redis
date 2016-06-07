package com.lucianomolinari.akkahttpredis

import akka.actor.ActorSystem
import akka.util.ByteString
import redis.ByteStringSerializer._
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

/**
  * Class responsible for managing [[Customer]] instances in a Redis Database.
  * <p>
  * It's important to notice that all operations performed in this class are non-blocking and, thus,
  * all of the methods wrap their responses in [[Future]] objects. This is because rediscala supports
  * non-blocking operations.
  *
  * @param actorSystem Implicit [[ActorSystem]]. Required by rediscala client.
  */
class CustomerRepository(implicit actorSystem: ActorSystem) {

  /**
    * Client holding a connection towards Redis Database server.
    */
  val redis = RedisClient()

  /**
    * Responsible for persisting a [[Customer]]. It first finds out the next ID to be used, sets it in the
    * customer and then persists him in the DB.
    *
    * @param customer The [[Customer]] to be persisted.
    * @return A [[Future]] with the [[Customer]] persisted, including his ID.
    */
  def add(customer: Customer): Future[Customer] = {
    getNextId flatMap { id =>
      val customerWithId = customer.copy(id = Some(id))
      val futureAdded = redis.hmset(getCustomerKey(id), Map("id" -> id.toString, "name" -> customerWithId.name,
        "age" -> customerWithId.age.toString))
      futureAdded map { res => customerWithId }
    }
  }

  /**
    * @param id The ID of the [[Customer]] to be found.
    * @return A [[Future]] with an [[Option]] holding the [[Customer]] found or None.
    */
  def find(id: Long): Future[Option[Customer]] = {
    redis.hgetall(getCustomerKey(id)) map { keysAndValues =>
      if (keysAndValues.isEmpty) None else Some(mapToCustomer(keysAndValues))
    }
  }

  /**
    * Removes the [[Customer]] given by the id.
    *
    * @param id The ID of the [[Customer]] to be removed.
    * @return A [[Future]] wrapping a Boolean value indicating whether the customer was removed or not.
    */
  def remove(id: Long): Future[Boolean] = {
    redis.del(getCustomerKey(id)) map { rowsDeleted =>
      rowsDeleted == 1
    }
  }

  private def getNextId(): Future[Long] = {
    redis.incr("next_customer_id")
  }

  private def mapToCustomer(keysAndValues: Map[String, ByteString]): Customer = {
    Customer(Some(keysAndValues("id").utf8String.toLong), keysAndValues("name").utf8String,
      keysAndValues("age").utf8String.toInt)
  }

  private def getCustomerKey(id: Long) = s"customer:$id"

}
