package net.fehmicansaglam.akkahttpclient

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.stream.Materializer
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

case class SimpleHttpResponse(status: StatusCode,
                              body: ByteString,
                              headers: Seq[HttpHeader],
                              contentType: ContentType,
                              charset: Charset) {
  def bodyAsString: String = body.decodeString(charset)
}

case class SimpleHttpRequestBuilder(request: HttpRequest) {

  def params(kvs: (String, String)*): SimpleHttpRequestBuilder = {
    val query = kvs.foldLeft(request.uri.query())((query, curr) => curr +: query)
    SimpleHttpRequestBuilder(request.withUri(request.uri.withQuery(query)))
  }

  def acceptJson: SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(request.addHeader(Accept(MediaRange(MediaTypes.`application/json`))))

  def acceptXml: SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(request.addHeader(Accept(MediaRange(MediaTypes.`application/xml`))))

  def bodyAsJson(body: String): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(request.withEntity(HttpEntity(ContentTypes.`application/json`, body)))

  def bodyAsXml(body: String): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(request.withEntity(HttpEntity(ContentTypes.`text/xml(UTF-8)`, body)))

  def bodyAsText(body: String): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(request.withEntity(HttpEntity(body)))

  def bodyAsBinary(body: Array[Byte]): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(request.withEntity(HttpEntity(body)))

  def bodyAsBinary(body: ByteString): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(request.withEntity(HttpEntity(body)))

  def bodyFromFile(contentType: ContentType, file: Path, chunkSize: Int = -1): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(request.withEntity(HttpEntity.fromPath(contentType, file, chunkSize)))

  def run(implicit system: ActorSystem,
          mat: Materializer,
          http: HttpExt,
          ec: ExecutionContext): Future[SimpleHttpResponse] = {
    for {
      response <- http.singleRequest(request)
      contentType = response.entity.contentType
      charset = contentType.charsetOption.map(_.nioCharset()).getOrElse(StandardCharsets.UTF_8)
      body <- response.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    } yield SimpleHttpResponse(response.status, body, response.headers, contentType, charset)
  }

  def runMap[T](f: SimpleHttpResponse => T)
               (implicit system: ActorSystem, mat: Materializer, http: HttpExt, ec: ExecutionContext): Future[T] = {
    run.map(f)
  }

}

object SimpleHttpRequestBuilder {

  def get(uri: String): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(HttpRequest(uri = uri))

  def head(uri: String): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(HttpRequest(method = HttpMethods.HEAD, uri = uri))

  def post(uri: String): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(HttpRequest(method = HttpMethods.POST, uri = uri))

  def put(uri: String): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(HttpRequest(method = HttpMethods.PUT, uri = uri))

  def patch(uri: String): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(HttpRequest(method = HttpMethods.PATCH, uri = uri))

  def delete(uri: String): SimpleHttpRequestBuilder =
    SimpleHttpRequestBuilder(HttpRequest(method = HttpMethods.DELETE, uri = uri))

}
