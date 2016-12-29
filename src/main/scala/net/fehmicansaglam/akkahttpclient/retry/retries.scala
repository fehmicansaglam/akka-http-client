package net.fehmicansaglam.akkahttpclient.retry

import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class Success[-T](val predicate: T => Boolean)

object Success {
  implicit def successfulEither[A, B]: Success[Either[A, B]] =
    Success(_.isRight)

  implicit def successfulOption[A]: Success[Option[A]] =
    Success(_.isDefined)

  implicit def successfulTry[A]: Success[Try[A]] =
    Success(_.isSuccess)

  def apply[T](predicate: T => Boolean) = new Success(predicate)
}

object SleepFuture {
  def apply[T](delay: FiniteDuration)(todo: => T)
              (implicit system: ActorSystem, ec: ExecutionContext): Future[T] = {
    val promise = scala.concurrent.Promise[T]()
    system.scheduler.scheduleOnce(delay) {
      promise.complete(Try(todo))
      ()
    }
    promise.future
  }
}

trait CountingRetry {
  /** Applies the given function and will retry up to `max` times,
    * until a successful result is produced.
    */
  protected def retry[T](max: Int,
                         promise: () => Future[T],
                         success: Success[T],
                         orElse: Int => Future[T])
                        (implicit ec: ExecutionContext) = {
    val fut = promise()
    fut.flatMap { res =>
      if (max < 1 || success.predicate(res)) fut
      else orElse(max - 1)
    }
  }
}

/** Retry immediately after failure */
object Directly extends CountingRetry {
  def apply[T](max: Int = 5)
              (promise: () => Future[T])
              (implicit success: Success[T], ec: ExecutionContext): Future[T] = {
    retry(max, promise, success, Directly(_)(promise))
  }
}

/** Retry with a pause between attempts */
object Pause extends CountingRetry {
  def apply[T](max: Int = 5, delay: FiniteDuration = 500.milliseconds)
              (promise: () => Future[T])
              (implicit success: Success[T], system: ActorSystem, ec: ExecutionContext): Future[T] = {
    retry(max,
      promise,
      success,
      max => SleepFuture(delay) {
        Pause(max, delay)(promise)
      }.flatMap(identity))
  }
}

/** Retry with exponential backoff */
object Backoff extends CountingRetry {
  def apply[T](max: Int = 5, delay: FiniteDuration = 500.milliseconds, base: Int = 2)
              (promise: () => Future[T])
              (implicit success: Success[T], system: ActorSystem, ec: ExecutionContext): Future[T] = {
    retry(max,
      promise,
      success,
      count => SleepFuture(delay) {
        Backoff(count, Duration(delay.length * base, delay.unit), base)(promise)
      }.flatMap(identity))
  }
}
