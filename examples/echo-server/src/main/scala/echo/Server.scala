/* Scala.js example code
 * Public domain
 * @author  Sébastien Doeraene
 */

package echo

import scala.scalajs.wit
import scala.scalajs.wit.annotation._

import scala.scalajs.WitUtils._
import scala.collection.mutable

import scala.scalajs.wasi.http.types._

object Server {
  @WitExport(WitScope("wasi", "http", "incoming-handler", "0.2.0"), "handle")
  def handle(@WitName("request") request: IncomingRequest,
      @WitName("response-out") outParam: ResponseOutparam): Unit = {
    val inputBody = readRequestBody(request)

    val headers: Headers = Fields()
    val resp = OutgoingResponse(headers)
    val body: OutgoingBody =
      toEither(resp.body()).getOrElse(throw new Error("failed to obtain outgoing response"))

    ResponseOutparam.set(outParam, new wit.Ok(resp))

    writeResponseBody(body, inputBody)
  }

  private def readRequestBody(request: IncomingRequest): Array[Byte] = {
    (for {
      body <- toEither(request.consume())
      inputStream <- toEither(body.stream())
    } yield {
      var eof = false

      val in = mutable.ArrayBuffer.empty[Byte]
      while (!eof) {
        toEither(inputStream.blockingRead(1024L)) match {
          case Right(bytes) =>
            if (bytes.length == 0)
              eof = true
            else
              in ++= bytes
          case Left(_) =>
            eof = true
        }
      }
      in.toArray
    }).getOrElse(throw new Error("failed to obtain request body"))
  }

  private def writeResponseBody(body: OutgoingBody, bytes: Array[Byte]): Unit = {
    val out = toEither(body.write()).getOrElse(throw new Error("failed to get outgoing stream"))
    out.blockingWriteAndFlush(bytes)

    out.close()

    toEither(OutgoingBody.finish(body, java.util.Optional.empty[Trailers]())).getOrElse(
        throw new Error("failed to finish outgoing body"))
  }
}
