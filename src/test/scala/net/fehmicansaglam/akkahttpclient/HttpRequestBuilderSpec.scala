package net.fehmicansaglam.akkahttpclient

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import net.fehmicansaglam.akkahttpclient.HttpRequestBuilder._
import org.scalatest.{AsyncFlatSpec, Matchers}
import spray.json.DefaultJsonProtocol._
import spray.json._

class HttpRequestBuilderSpec extends AsyncFlatSpec with Matchers {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val http = Http()

  case class Post(id: Option[Int] = None, title: String, body: String, userId: Int)

  case class Comment(id: Int, postId: Int, name: String, email: String)

  implicit val postFormat = jsonFormat4(Post)
  implicit val commentFormat = jsonFormat4(Comment)

  "An HttpRequestBuilder" should "get /posts" in {
    get.from("http://jsonplaceholder.typicode.com/posts")
      .run
      .map { response =>
        println(response)
        assert(1 == 1)
      }
  }

  it should "get /posts as json" in {
    get.json.from("http://jsonplaceholder.typicode.com/posts")
      .run
      .map { response =>
        println(response)
        assert(1 == 1)
      }
  }

  it should "get /comments?postId=2 as json" in {
    val expected = Comment(
      id = 6,
      postId = 2,
      name = "et fugit eligendi deleniti quidem qui sint nihil autem",
      email = "Presley.Mueller@myrl.com")

    get.json.from("http://jsonplaceholder.typicode.com/comments")
      .param("postId", "2")
      .run
      .map { case SimpleHttpResponse(status, _, body) =>
        val actual = body.parseJson.convertTo[List[Comment]].head
        assert(status == StatusCodes.OK)
        assert(actual == expected)
      }
  }

  it should "post json to /posts" in {
    val data = Post(title = "foo", body = "bar", userId = 1)
    val expected = data.copy(id = Some(101))

    post.json(data.toJson.compactPrint)
      .to("http://jsonplaceholder.typicode.com/posts")
      .run
      .map { case SimpleHttpResponse(status, _, body) =>
        val actual = body.parseJson.convertTo[Post]
        assert(status == StatusCodes.Created)
        assert(actual == expected)
      }
  }

  it should "delete /posts/1" in {
    delete("http://jsonplaceholder.typicode.com/posts/1")
      .run
      .map { case SimpleHttpResponse(status, _, _) =>
        assert(status == StatusCodes.OK)
      }
  }

}
