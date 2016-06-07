package com.lucianomolinari.akkahttp_redis

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/**
  * Responsible for configuring the HTTP route to expose operations around [[Customer]].
  * <p>
  * The operations available are:
  * <ul>
  * <li>GET /api/customer/ID => Finds and returns the Customer given by ID
  * <li>POST /api/customer => Persists a new Customer.
  * <li>DELETE /api/customer/ID => Removes the Customer given by ID
  * </ul>
  *
  * @param customerRepository The repository of customers.
  */
class CustomerRest(customerRepository: CustomerRepository) extends CustomerJsonProtocol {

  val route: Route =
    logRequestResult("customer-service") {
      pathPrefix("api") {
        get {
          path("customer" / LongNumber) { id =>

            onSuccess(customerRepository.find(id)) {
              case Some(customer) => complete(customer)
              case None => complete(StatusCodes.NotFound)
            }
          }
        } ~ post {
          (path("customer") & entity(as[Customer])) { customer =>
            onSuccess(customerRepository.add(customer)) {
              case customerAdded => complete(CustomerId(customerAdded.id.get))
            }
          }
        } ~ delete {
          path("customer" / LongNumber) { id =>

            onSuccess(customerRepository.remove(id)) {
              case true => complete(StatusCodes.OK)
              case false => complete(StatusCodes.NotFound)
            }
          }
        }
      }
    }

}