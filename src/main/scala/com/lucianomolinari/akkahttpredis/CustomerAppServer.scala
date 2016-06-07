package com.lucianomolinari.akkahttpredis

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.io.StdIn

/**
  * Main class.
  * <p>
  * Responsible for setting up application/dependencies and then starting Akka HTTP Server to listen on port 8080.
  */
object CustomerAppServer extends App {

  // Defines all implicit dependencies required by Akka HTTP. The ActorSystem instance is needed both by
  // Akka HTTP and rediscala client.
  implicit val system = ActorSystem("customer-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // Instantiates classes and their dependencies
  val customerRepository = new CustomerRepository()
  val customerRest = new CustomerRest(customerRepository)

  // Starts Akka HTTP server to listen on port 8080 and registers the route defined by CustomerRest
  val bindingFuture = Http().bindAndHandle(customerRest.route, "localhost", 8080)

  println(s"The server is ready to handle HTTP requests")

  // Listen for any enter command to stop the server/program.
  StdIn.readLine
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}
