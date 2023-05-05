package vu.naya.test

import akka.actor.typed.ActorSystem
import org.slf4j.{Logger, LoggerFactory}

object Main extends App {
  // avoid slf4j noise by touching it first from single thread
  val logger = LoggerFactory.getLogger(getClass.getSimpleName.stripSuffix("$"))
  logger.debug("Starting server")

  ActorSystem(WebServer(), "WebServer")
}
