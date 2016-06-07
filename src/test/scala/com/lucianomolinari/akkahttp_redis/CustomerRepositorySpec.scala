package com.lucianomolinari.akkahttp_redis


import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import redis.RedisClient

import scala.concurrent.Await
import scala.concurrent.duration._

class CustomerRepositorySpec extends TestKit(ActorSystem()) with WordSpecLike with Matchers with ScalaFutures
  with BeforeAndAfter {

  // Overrides default timeout used by ScalaFutures operations, like whenReady
  override implicit def patienceConfig = PatienceConfig(timeout = Span(1, Second))

  // ActorSystem is automatically provided by TestKit
  val customerRepository = new CustomerRepository()

  // DB is cleaned before each test case
  before {
    val redis = RedisClient()
    Await.ready(redis.flushdb(), 1 second)
  }

  "The customer repository" should {

    "return the customer when he exists" in {
      addCustomer(new Customer("John", 30), 1)
      whenReady(customerRepository.find(1)) {
        customer => customer shouldBe Some(Customer(Some(1), "John", 30))
      }
    }

    "return None when the customer doesn't exist" in {
      whenReady(customerRepository.find(1)) {
        customer => customer shouldBe None
      }
    }

    "return false when the customer to be removed exists" in {
      addCustomer(new Customer("John", 30), 1)
      whenReady(customerRepository.remove(1)) {
        res => res shouldBe true
      }
      whenReady(customerRepository.find(1)) {
        customer => customer shouldBe None
      }
    }

    "return false when the customer to be removed doesn't exist" in {
      whenReady(customerRepository.remove(1)) {
        res => res shouldBe false
      }
    }

    "continue to increase the id value even if other customers are removed" in {
      addCustomer(new Customer("John", 30), 1)
      addCustomer(new Customer("Mary", 25), 2)
      whenReady(customerRepository.remove(2)) {
        res => res shouldBe true
      }
      addCustomer(new Customer("Carl", 25), 3)
    }

  }

  def addCustomer(customer: Customer, expectedId: Long) = {
    whenReady(customerRepository.add(customer)) {
      customerAdded => customerAdded shouldBe customer.copy(id = Some(expectedId))
    }
  }

}
