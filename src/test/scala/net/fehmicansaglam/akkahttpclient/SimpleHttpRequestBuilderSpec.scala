package net.fehmicansaglam.akkahttpclient

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import net.fehmicansaglam.akkahttpclient.SimpleHttpRequestBuilder._
import org.scalatest.{AsyncFlatSpec, Matchers}
import spray.json.DefaultJsonProtocol._
import spray.json._

class SimpleHttpRequestBuilderSpec extends AsyncFlatSpec with Matchers {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val http = Http()

  case class Post(id: Option[Int] = None, title: String, body: String, userId: Int)

  case class Comment(id: Int, postId: Int, name: String, email: String)

  case class Repo(id: Long, name: String)

  implicit val postFormat = jsonFormat4(Post)
  implicit val commentFormat = jsonFormat4(Comment)
  implicit val repotFormat = jsonFormat2(Repo)

  "A SimpleHttpRequestBuilder" should "get /repos" in {
    get("https://api.github.com/users/fehmicansaglam/repos")
      .run
      .map { case SimpleHttpResponse(status, body, _, _, charset) =>
        val actual = body.decodeString(charset).parseJson.convertTo[List[Repo]]
        assert(status == StatusCodes.OK)
        assert(actual.exists(_.name == "akka-http-client"))
      }
  }

  it should "get /repos as json" in {
    get("https://api.github.com/users/fehmicansaglam/repos")
      .acceptJson
      .run.map { case r@SimpleHttpResponse(status, _, _, _, _) =>
      val actual = r.bodyAsString.parseJson.convertTo[List[Repo]]
      assert(status == StatusCodes.OK)
      assert(actual.exists(_.name == "akka-http-client"))
    }
  }

  it should "get /comments?postId=2 as json" in {
    val expected = Comment(
      id = 6,
      postId = 2,
      name = "et fugit eligendi deleniti quidem qui sint nihil autem",
      email = "Presley.Mueller@myrl.com")

    get("http://jsonplaceholder.typicode.com/comments")
      .params("postId" -> "2")
      .acceptJson
      .run.map { case SimpleHttpResponse(status, body, _, _, _) =>
      val actual = body.utf8String.parseJson.convertTo[List[Comment]].head
      assert(status == StatusCodes.OK)
      assert(actual == expected)
    }
  }

  it should "post json to /posts" in {
    val data = Post(title = "foo", body = "bar", userId = 1)
    val expected = data.copy(id = Some(101))

    post("http://jsonplaceholder.typicode.com/posts")
      .bodyAsJson(data.toJson.compactPrint)
      .run.map { response =>
      val actual = response.body.utf8String.parseJson.convertTo[Post]
      assert(response.status == StatusCodes.Created)
      assert(actual == expected)
    }
  }

  it should "put json to /posts/1" in {
    val data = Post(id = Some(1), title = "foo", body = "bar", userId = 1)
    val expected = data

    put("http://jsonplaceholder.typicode.com/posts/1")
      .bodyAsJson(data.toJson.compactPrint)
      .run.map { response =>
      val actual = response.body.utf8String.parseJson.convertTo[Post]
      assert(response.status == StatusCodes.OK)
      assert(actual == expected)
    }
  }

  it should "delete /posts/1" in {
    delete("http://jsonplaceholder.typicode.com/posts/1")
      .run.map { response =>
      assert(response.status == StatusCodes.OK)
    }
  }

}
