package net.fehmicansaglam.akkahttpclient

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import akka.stream.Materializer
import akka.util.ByteString

import scala.concurrent.Future

final case class SimpleHttpResponse(status: StatusCode, headers: Seq[HttpHeader], body: String)

sealed abstract class HttpRequestBuilder(request: HttpRequest) {

  def run(implicit system: ActorSystem, mat: Materializer, http: HttpExt): Future[SimpleHttpResponse] = {
    implicit val ec = system.dispatcher
    for {
      response <- http.singleRequest(request)
      body <- response.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    } yield SimpleHttpResponse(response.status, response.headers, body.utf8String)
  }

}

final case class HttpGetBuilder(request: HttpRequest) extends HttpRequestBuilder(request) {

  def from(uri: String): HttpGetBuilder = HttpGetBuilder(request.withUri(uri))

  def json: HttpGetBuilder =
    HttpGetBuilder(request.addHeader(Accept(MediaRange(MediaTypes.`application/json`))))

  def xml: HttpGetBuilder =
    HttpGetBuilder(request.addHeader(Accept(MediaRange(MediaTypes.`application/xml`))))

  def param(name: String, value: String): HttpGetBuilder = {
    val query = (name, value) +: request.uri.query()
    HttpGetBuilder(request.withUri(request.uri.withQuery(query)))
  }

}

final case class HttpPutPostBuilder(request: HttpRequest) extends HttpRequestBuilder(request) {

  def to(uri: String): HttpPutPostBuilder = HttpPutPostBuilder(request.withUri(uri))

  def json(body: String): HttpPutPostBuilder =
    HttpPutPostBuilder(request.withEntity(HttpEntity(ContentTypes.`application/json`, body)))

  def xml(body: String): HttpPutPostBuilder =
    HttpPutPostBuilder(request.withEntity(HttpEntity(ContentTypes.`text/xml(UTF-8)`, body)))

}

final case class HttpDeleteBuilder(request: HttpRequest) extends HttpRequestBuilder(request)

object HttpRequestBuilder {

  def get: HttpGetBuilder =
    HttpGetBuilder(HttpRequest())

  def post: HttpPutPostBuilder =
    HttpPutPostBuilder(HttpRequest(method = HttpMethods.POST))

  def put: HttpPutPostBuilder =
    HttpPutPostBuilder(HttpRequest(method = HttpMethods.PUT))

  def delete(uri: String): HttpDeleteBuilder =
    HttpDeleteBuilder(HttpRequest(method = HttpMethods.DELETE, uri = uri))

}
