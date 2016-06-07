package com.lucianomolinari.akkahttpredis

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import redis.RedisClient

import scala.concurrent.Await
import scala.concurrent.duration._

class CustomerRestSpec extends WordSpec with Matchers with ScalatestRouteTest with BeforeAndAfter
  with CustomerJsonProtocol {

  val customerRest = new CustomerRest(new CustomerRepository())

  before {
    val redis = RedisClient()
    Await.ready(redis.flushdb(), 1 second)
  }

  "The Customer Rest service" should {

    "return the customer when he exists" in {
      addCustomer(new Customer("John", 30), 1)
      Get("/api/customer/1") ~> customerRest.route ~> check {
        responseAs[Customer] shouldBe Customer(Some(1), "John", 30)
      }
    }

    "return NotFound when the customer doesn't exist" in {
      Get("/api/customer/1") ~> customerRest.route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "remove a customer when he exists" in {
      addCustomer(new Customer("John", 30), 1)
      Delete("/api/customer/1") ~> customerRest.route ~> check {
        status shouldBe StatusCodes.OK
      }
      Get("/api/customer/1") ~> customerRest.route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return NotFound when the customer to be removed doesn't exist" in {
      Delete("/api/customer/1") ~> customerRest.route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    def addCustomer(customer: Customer, expectedId: Long) = {
      Post("/api/customer", customer) ~> customerRest.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[CustomerId] shouldBe CustomerId(expectedId)
      }
    }

  }

}
