package scala.scalajs.wasi.http

package object outgoing_handler {

  // Type definitions
  type OutgoingRequest = scala.scalajs.wasi.http.types.OutgoingRequest

  type RequestOptions = scala.scalajs.wasi.http.types.RequestOptions

  type FutureIncomingResponse = scala.scalajs.wasi.http.types.FutureIncomingResponse

  type ErrorCode = scala.scalajs.wasi.http.types.ErrorCode

  // Functions
  /** This function is invoked with an outgoing HTTP Request, and it returns
   *  a resource `future-incoming-response` which represents an HTTP Response
   *  which may arrive in the future.
   *
   *  The `options` argument accepts optional parameters for the HTTP
   *  protocol's transport layer.
   *
   *  This function may return an error if the `outgoing-request` is invalid
   *  or not allowed to be made. Otherwise, protocol errors are reported
   *  through the `future-incoming-response`.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:http/outgoing-handler@0.2.0", "handle")
  def handle(request: OutgoingRequest, options: java.util.Optional[RequestOptions]): scala.scalajs.wit.Result[FutureIncomingResponse, scala.scalajs.wasi.http.types.ErrorCode] = scala.scalajs.wit.native

}
