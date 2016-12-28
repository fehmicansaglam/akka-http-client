# akka-http-client
Simple Akka HTTP Client DSL for Scala

```scala
import net.fehmicansaglam.akkahttpclient.SimpleHttpRequestBuilder._
import spray.json.DefaultJsonProtocol._
import spray.json._

case class Repo(id: Long, name: String)
implicit val repoFormat = jsonFormat2(Repo)

get("https://api.github.com/users/fehmicansaglam/repos")
  .acceptJson
  .runMap { response =>
    val actual = response.bodyAsString.parseJson.convertTo[List[Repo]]
    assert(status == StatusCodes.OK)
    assert(actual.exists(_.name == "akka-http-client"))
  }
```