package helloworld

import scala.scalajs.js
import scala.scalajs.{component => cm}
import scala.scalajs.component._
import cm.annotation._
import cm.unsigned._

import http.Types._

/** This interface defines a handler of incoming HTTP Requests. It should
  * be exported by components which can respond to HTTP Requests.
  */
@ComponentExport("wasi:http/incoming-handler@0.2.0")
object IncomingHandler extends cm.Interface {
  /** /// This function is invoked with an incoming HTTP Request, and a resource
    * /// `response-outparam` which provides the capability to reply with an HTTP
    * /// Response. The response is sent by calling the `response-outparam.set`
    * /// method, which allows execution to continue after the response has been
    * /// sent. This enables both streaming to the response body, and performing other
    * /// work.
    * ///
    * /// The implementor of this function must write a response to the
    * /// `response-outparam` before returning, or else the caller will respond
    * /// with an error on its behalf.
    * {{{
    * @since(version = 0.2.0)
    * handle: func(
    *   request: incoming-request,
    *   response-out: response-outparam
    * );
    * }}}
    */
  def handle(request: IncomingRequest, responseOut: ResponseOutparam): Unit = {
    val headers = Fields()
    val resp = OutgoingResponse(headers)
    val body = resp.body() match {
      case Err(_) => throw new AssertionError("err")

      // get value: j.l.Object
      // cast to OutgoingValue
      // but there's no asInstance for body
      // How does JSNativeClass handle?
      case Ok(body) => body
    }

    ResponseOutparam.set(responseOut, cm.Ok(resp))

    val cm.Ok(out) = body.write()
    val cm.Ok(_) = out.blockingWriteAndFlush("Hello, wasi:http/proxy world!".getBytes())

    out.close()

    OutgoingBody.finish(body, java.util.Optional.empty())
  }
}

