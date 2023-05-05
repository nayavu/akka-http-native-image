package vu.naya.test

import akka.actor.typed.*
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto.*

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class HelloRequest(name: String)

case class HelloResponse(response: String)

class WebServer(ctx: ActorContext[WebServer.Command]) extends FailFastCirceSupport {
  implicit val system: ActorSystem[_] = ctx.system

  private val config = ConfigFactory.load()
  private val host = config.getString("http.host")
  private val port = config.getInt("http.port")


  val routes: Route = {
    path("hello") {
      post {
        entity(as[HelloRequest]) { request =>
          complete(HelloResponse(s"Hello, ${request.name}"))
        }
      }
    }
  }

  private val serverBinding = Http().newServerAt(host, port).bind(routes)

  ctx.pipeToSelf(serverBinding) {
    case Success(binding) => WebServer.Started(binding)
    case Failure(ex) => WebServer.Failed(ex)
  }

  def starting(wasStopped: Boolean): Behavior[WebServer.Command] = Behaviors.receiveMessagePartial[WebServer.Command] {
    case WebServer.Failed(cause) => throw new RuntimeException("Server failed to start", cause)
    case WebServer.Started(binding) =>
      ctx.log.info("Server running at http://localhost:{}/", binding.localAddress.getPort)
      if (wasStopped) {
        ctx.self ! WebServer.Stop
      }
      running(binding)
    case WebServer.Stop =>
      starting(wasStopped = true)
  }

  def running(binding: ServerBinding): Behavior[WebServer.Command] = Behaviors.receiveMessagePartial[WebServer.Command] {
    case WebServer.Stop =>
      ctx.log.info("Stopping server http://localhost:{}/", binding.localAddress.getPort)
      Behaviors.stopped
  }.receiveSignal {
    case (_, PreRestart) =>
      ctx.log.info("Restarting the server")
      binding.terminate(5.seconds)
      Behaviors.same

    case (_, PostStop) =>
      ctx.log.info("Server stopped")
      system.terminate()
      Behaviors.same
  }
}

object WebServer {
  sealed trait Command

  private final case class Failed(cause: Throwable) extends Command

  private final case class Started(binding: ServerBinding) extends Command

  private case object Stop extends Command

  def apply(): Behavior[Command] = Behaviors.setup[Command] { ctx =>
    new WebServer(ctx).starting(false)
  }
}
