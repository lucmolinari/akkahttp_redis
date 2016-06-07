package com.lucianomolinari.akkahttpredis

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Represents the domain model of a Customer.
  * <p>
  * A Customer only has an id once he's persisted, so his id field is of type Option.
  */
case class Customer(id: Option[Long], name: String, age: Int) {

  def this(name: String, age: Int) = this(None, name, age)

}

case class CustomerId(id: Long)

/**
  * Wraps all the required json formatting stuff, to allow a Customer/CustomerId to be automatically
  * marshalled/unmarshalled from an HTTP Request and to an HTTP response.
  */
trait CustomerJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val customerFormat = jsonFormat3(Customer)
  implicit val customerIdFormat = jsonFormat1(CustomerId)
}
