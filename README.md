# akka-http-client
Simple Akka HTTP Client DSL for Scala

## Quick Start

### Setting up dependencies

If you want to be on the bleeding edge using snapshots, latest snapshot release is **10.0_0-SNAPSHOT**. Add the following repository and dependency:
```scala
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "net.fehmicansaglam" %% "akka-http-client" % "10.0_0-SNAPSHOT"
)
```

## Examples

```scala
import net.fehmicansaglam.akkahttpclient.SimpleHttpRequestBuilder._
import spray.json.DefaultJsonProtocol._
import spray.json._

case class Repo(id: Long, name: String)
case class Post(id: Option[Int] = None, title: String, body: String, userId: Int)
implicit val repoFormat = jsonFormat2(Repo)
implicit val postFormat = jsonFormat4(Post)

get("https://api.github.com/users/fehmicansaglam/repos")
  .acceptJson
  .run.map { response =>
    val actual = response.bodyAsString.parseJson.convertTo[List[Repo]]
    assert(status == StatusCodes.OK)
    assert(actual.exists(_.name == "akka-http-client"))
  }
  
val data = Post(title = "foo", body = "bar", userId = 1)
val expected = data.copy(id = Some(101))

post("http://jsonplaceholder.typicode.com/posts")
  .bodyAsJson(data.toJson.compactPrint)
  .run.map { response =>
    val actual = response.body.utf8String.parseJson.convertTo[Post]
    assert(response.status == StatusCodes.Created)
    assert(actual == expected)
  }  
```

## Retry strategies

```scala
delete("http://jsonplaceholder.typicode.com/posts/1")
  .retryBackoff(max = 4)
  
```

## Contributions
akka-http-client needs your help. Good ways to contribute include:

* Raising bugs and feature requests
* Fixing bugs
* Adding to the documentation
