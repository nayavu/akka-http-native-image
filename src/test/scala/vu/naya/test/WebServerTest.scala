package vu.naya.test

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.util.ByteString
import org.scalatest.matchers.should.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class WebServerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with BeforeAndAfterEach {
  var webServer: ActorRef[WebServer.Command] = _

  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    webServer = testKit.spawn(WebServer())
  }

  override def afterEach(): Unit = {
    testKit.stop(webServer)
    super.afterEach()
  }

  private def await[T](future: Future[T]): T = Await.result(future, 10.seconds)

  private def readBody(response: HttpResponse) = await(response.entity.dataBytes.runFold(ByteString(""))(_ ++ _)).utf8String

  "WebServer" must {
    "respond on hello" in {
      val request = HttpRequest(
        HttpMethods.POST,
        Uri("http://localhost:9000/hello"),
        entity = HttpEntity.Strict(ContentTypes.`application/json`, ByteString("""{"name":"Anubis"}"""))
      )
      val response = await(Http().singleRequest(request))
      response.status shouldBe StatusCodes.OK

      val body = readBody(response)
      body shouldBe """{"response":"Hello, Anubis"}"""
    }
  }
}
