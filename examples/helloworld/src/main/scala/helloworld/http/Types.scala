package helloworld.http

import scala.scalajs.{component => cm}
import scala.scalajs.component._
import cm.annotation._
import cm.unsigned._

import helloworld.OutputStream

object Types {

  /** Represents the ability to send an HTTP response.
    *
    * This resource is used by the {@code wasi:http/incoming-handler} interface to
    * allow a response to be sent corresponding to the request provided as the
    * other argument to {@code incomingHandler.handle}.
    */
  @cm.native
  @ComponentImport("wasi:http/types@0.2.0")
  trait ResponseOutparam extends cm.Resource {
  }
  object ResponseOutparam {
    /** Sets the value of the {@code response-outparam} to either send a response
      * or indicate an error.
      *
      * This method consumes the {@code response-outparam} to ensure that it is
      * called at most once. If it is never called, the implementation will
      * respond with an error.
      *
      * The user may provide an error to {@code response} to allow the
      * implementation to determine how to respond with an HTTP error response.
      */
    @cm.native
    def set(param: ResponseOutparam, response: cm.Result[OutgoingResponse, ErrorCode]): Unit = cm.native
  }

  /** Represents an outgoing HTTP Response. */
  @cm.native
  @ComponentImport("wasi:http/types@0.2.0")
  trait OutgoingResponse extends cm.Resource {
    /** Get the HTTP Status Code for the Response. */
    def statusCode(): StatusCode = cm.native

    /** Set the HTTP Status Code for the Response. Fails if the status-code
      * given is not a valid http status code.
      */
    def setStatusCode(statusCode: StatusCode): cm.Result[Unit, Unit] = cm.native

    /**
      * Returns the headers associated with the request.
      *
      * The returned headers resource is immutable; {@code set}, {@code append}, and
      * {@code delete} operations will fail with {@code header-error.immutable}.
      *
      * This headers resource is a child: it must be dropped before the parent
      * {@code OutgoingRequest} is dropped, or its ownership must be transferred to
      * another component, such as via {@code outgoingHandler.handle}.
      *
      * @return an immutable headers resource
      */
    def headers(): Headers = cm.native

    /** Returns the resource corresponding to the outgoing body for this response.
      *
      * The first call to this method succeeds and returns the outgoing-body resource
      * for this outgoing-response. The resource can be retrieved at most once;
      * subsequent calls will return an error.
      */
    def body(): cm.Result[OutgoingBody, Unit] = cm.native

  }
  object OutgoingResponse {
    /** Construct an `outgoing-response`, with a default `status-code` of `200`.
      * If a different `status-code` is needed, it must be set via the
      * `set-status-code` method.
      *
      * * `headers` is the HTTP Headers for the Response.
      */
    @cm.native
    def apply(headers: Headers): OutgoingResponse = cm.native
  }


  /** Represents an outgoing HTTP Request or Response's Body.
    *
    * A body has both its contents - a stream of bytes - and a (possibly
    * empty) set of trailers, indicating the full contents of the body
    * have been sent. This resource represents the contents as an
    * `output-stream` child resource, and the completion of the body (with
    * optional trailers) with a static function that consumes the
    * `outgoing-body` resource, ensuring that the user of this interface
    * may not write to the body contents after the body has been finished.
    *
    * If the user code drops this resource, as opposed to calling the static
    * method `finish`, the implementation should treat the body as incomplete,
    * indicating that an error has occurred. The implementation should propagate this
    * error to the HTTP protocol by whatever means it has available,
    * including: corrupting the body on the wire, aborting the associated
    * Request, or sending a late status code for the Response.
    */
  @cm.native
  @ComponentImport("wasi:http/types@0.2.0")
  trait OutgoingBody extends cm.Resource {
    /** Returns a stream for writing the body contents.
      *
      * The returned `output-stream` is a child resource: it must be dropped
      * before the parent `outgoing-body` resource is dropped (or finished),
      * otherwise the `outgoing-body` drop or `finish` will trap.
      *
      * Returns success on the first call: the `output-stream` resource for
      * this `outgoing-body` may be retrieved at most once. Subsequent calls
      * will return an error.
      *
      * @since 0.2.0
      */
    def write(): cm.Result[OutputStream, Unit] = cm.native
  }
  object OutgoingBody {
    /** Finalizes an outgoing body, optionally providing trailers. This must be
      * called to signal that the response is complete. If the `outgoing-body`
      * is dropped without calling `outgoing-body.finalize`, the implementation
      * should treat the body as corrupted.
      *
      * Fails if the body's `outgoing-request` or `outgoing-response` was
      * constructed with a Content-Length header, and the contents written
      * to the body (via `write`) do not match the value given in the
      * Content-Length.
      *
      * @since 0.2.0
      */
    @cm.native
    def finish(
      `this`: OutgoingBody,
      trailers: java.util.Optional[Trailers]
    ): cm.Result[Unit, ErrorCode] = cm.native
  }

  type StatusCode = UShort
  type Trailers = Fields;

  sealed trait ErrorCode extends cm.Variant
  object ErrorCode {
    final case object DNSTimeout extends ErrorCode { val _index = 0; type T = Unit; val value = () }
    final class DNSError(val value: DNSErrorPayload) extends ErrorCode { val _index = 1; type T = DNSErrorPayload }
    final case object DestinationNotFound extends ErrorCode { val _index = 2; type T = Unit; val value = () }
    final case object DestinationUnavailable extends ErrorCode { val _index = 3; type T = Unit; val value = () }
    final case object DestinationIPProhibited extends ErrorCode { val _index = 4; type T = Unit; val value = () }
    final case object DestinationIPUnroutable extends ErrorCode { val _index = 5; type T = Unit; val value = () }
    final case object ConnectionRefused extends ErrorCode { val _index = 6; type T = Unit; val value = () }
    final case object ConnectionTerminated extends ErrorCode { val _index = 7; type T = Unit; val value = () }
    final case object ConnectionTimeout extends ErrorCode { val _index = 8; type T = Unit; val value = () }
    final case object ConnectionReadTimeout extends ErrorCode { val _index = 9; type T = Unit; val value = () }
    final case object ConnectionWriteTimeout extends ErrorCode { val _index = 10; type T = Unit; val value = () }
    final case object ConnectionLimitReached extends ErrorCode { val _index = 11; type T = Unit; val value = () }
    final case object TLSProtocolError extends ErrorCode { val _index = 12; type T = Unit; val value = () }
    final case object TLSCertificateError extends ErrorCode { val _index = 13; type T = Unit; val value = () }
    final class TLSAlertReceived(val value: TLSAlertReceivedPayload) extends ErrorCode { val _index = 14; type T = TLSAlertReceivedPayload }
    final case object HTTPRequestDenied extends ErrorCode { val _index = 15; type T = Unit; val value = () }
    final case object HTTPRequestLengthRequired extends ErrorCode { val _index = 16; type T = Unit; val value = () }
    final class HTTPRequestBodySize(val value: java.util.Optional[Long]) extends ErrorCode { val _index = 17; type T = java.util.Optional[Long] }
    final case object HTTPRequestMethodInvalid extends ErrorCode { val _index = 18; type T = Unit; val value = () }
    final case object HTTPRequestURIInvalid extends ErrorCode { val _index = 19; type T = Unit; val value = () }
    final case object HTTPRequestURITooLong extends ErrorCode { val _index = 20; type T = Unit; val value = () }
    final class HTTPRequestHeaderSectionSize(val value: java.util.Optional[Int]) extends ErrorCode { val _index = 21; type T = java.util.Optional[Int] }
    final class HTTPRequestHeaderSize(val value: java.util.Optional[FieldSizePayload]) extends ErrorCode { val _index = 22; type T = java.util.Optional[FieldSizePayload] }
    final class HTTPRequestTrailerSectionSize(val value: java.util.Optional[Int]) extends ErrorCode { val _index = 23; type T = java.util.Optional[Int] }
    final class HTTPRequestTrailerSize(val value: FieldSizePayload) extends ErrorCode { val _index = 24; type T = FieldSizePayload }
    final case object HTTPResponseIncomplete extends ErrorCode { val _index = 25; type T = Unit; val value = () }
    final class HTTPResponseHeaderSectionSize(val value: java.util.Optional[Int]) extends ErrorCode { val _index = 26; type T = java.util.Optional[Int] }
  }

  // record DNS-error-payload {
  //   rcode: option<string>,
  //   info-code: option<u16>
  // }
  @ComponentRecord
  final case class DNSErrorPayload(
    rcode: java.util.Optional[String],
    infoCode: java.util.Optional[UShort]
  )

  // record TLS-alert-received-payload {
  //   alert-id: option<u8>,
  //   alert-message: option<string>
  // }
  @ComponentRecord
  final case class TLSAlertReceivedPayload(
    alertId: java.util.Optional[UByte],
    alertMessage: java.util.Optional[String]
  )

  // record field-size-payload {
  //   field-name: option<string>,
  //   field-size: option<u32>
  // }
  @ComponentRecord
  final case class FieldSizePayload(
    fieldName: java.util.Optional[String],
    fieldSize: java.util.Optional[UInt]
  )


  /** Represents an incoming HTTP Request. */
  @cm.native
  @ComponentImport("wasi:http/types@0.2.0")
  trait IncomingRequest extends cm.Resource {
    /** Returns the method of the incoming request. */
    def method(): Method = cm.native

    // path-with-query: func() -> option<string>;
    def pathWithQuery(): java.util.Optional[String] = cm.native

    // scheme: func() -> option<scheme>;
    def scheme(): java.util.Optional[Scheme] = cm.native

    // authority: func() -> option<string>;
    def authority(): java.util.Optional[String] = cm.native

    /// Get the `headers` associated with the request.
    ///
    /// The returned `headers` resource is immutable: `set`, `append`, and
    /// `delete` operations will fail with `header-error.immutable`.
    ///
    /// The `headers` returned are a child resource: it must be dropped before
    /// the parent `incoming-request` is dropped. Dropping this
    /// `incoming-request` before all children are dropped will trap.
    /// @since(version = 0.2.0)
    /// headers: func() -> headers;
    def headers(): Headers = cm.native

    /** Gives the `incoming-body` associated with this request. Will only
      * return success at most once, and subsequent calls will return error.
      * {{{consume: func() -> result<incoming-body>;}}}
      */
    def consume(): cm.Result[IncomingBody, Unit] = cm.native
  }

  sealed trait Method extends cm.Variant
  object Method {
    final case object Get extends Method { val value = (); type T = Unit; val _index = 0 }
    final case object Head extends Method { val value = (); type T = Unit; val _index = 1 }
    final case object Post extends Method { val value = (); type T = Unit; val _index = 2 }
    final case object Put extends Method { val value = (); type T = Unit; val _index = 3 }
    final case object Delete extends Method { val value = (); type T = Unit; val _index = 4 }
    final case object Connect extends Method { val value = (); type T = Unit; val _index = 5 }
    final case object Options extends Method { val value = (); type T = Unit; val _index = 6 }
    final case object Trace extends Method { val value = (); type T = Unit; val _index = 7 }
    final case object Patch extends Method { val value = (); type T = Unit; val _index = 8 }
    final class Other(val value: String) extends Method {
      type T = String
      val _index = 9
    }
  }

  sealed trait Scheme extends cm.Variant
  object Scheme {
    final case object HTTP extends Scheme { val value = (); type T = Unit; val _index = 0 }
    final case object HTTPS extends Scheme { val value = (); type T = Unit; val _index = 1 }
    final class Other(val value: String) extends Method {
      type T = String
      val _index = 2
    }
  }

  // type headers = fields;
  type Headers = Fields


  /// This following block defines the `fields` resource which corresponds to
  /// HTTP standard Fields. Fields are a common representation used for both
  /// Headers and Trailers.
  ///
  /// A `fields` may be mutable or immutable. A `fields` created using the
  /// constructor, `from-list`, or `clone` will be mutable, but a `fields`
  /// resource given by other means (including, but not limited to,
  /// `incoming-request.headers`, `outgoing-request.headers`) might be
  /// immutable. In an immutable fields, the `set`, `append`, and `delete`
  /// operations will fail with `header-error.immutable`.
  @cm.native
  @ComponentImport("wasi:http/types@0.2.0")
  trait Fields extends cm.Resource {
    /** get: func(name: field-name) -> list<field-value>; */
    def get(name: FieldName): Array[FieldValue] = cm.native

    def has(name: FieldName): Boolean = cm.native

    /** Set all of the values for a name. Clears any existing values for that
     * name, if they have been set.
     * Fails with `header-error.immutable` if the `fields` are immutable.
     * Fails with `header-error.invalid-syntax` if the `field-name` or any of
     * the `field-value`s are syntactically invalid.
     * {{{
     *     set: func(name: field-name, value: list<field-value>) -> result<_, header-error>;
     * }}}
     */
    def set(name: FieldName, value: Array[FieldValue]): cm.Result[Unit, HeaderError] = cm.native

    /** Delete all values for a name. Does nothing if no values for the name
      * exist.
      *
      * Fails with `header-error.immutable` if the `fields` are immutable.
      *
      * Fails with `header-error.invalid-syntax` if the `field-name` is
      * syntactically invalid.
      *
      * {{{delete: func(name: field-name) -> result<_, header-error>;}}}
      */
    def delete(name: FieldName): cm.Result[Unit, HeaderError] = cm.native


    /** Retrieve the full set of names and values in the Fields. Like the
      * constructor, the list represents each name-value pair.
      *
      * The outer list represents each name-value pair in the Fields. Names
      * which have multiple values are represented by multiple entries in this
      * list with the same name.
      *
      * The names and values are always returned in the original casing and in
      * the order in which they will be serialized for transport.
      *
      * {{{entries: func() -> list<tuple<field-name,field-value>>;}}}
      */
    def entries(): Array[(FieldName, FieldValue)] = cm.native

    /** Make a deep copy of the Fields. Equivalent in behavior to calling the
      * `fields` constructor on the return value of `entries`. The resulting
      * `fields` is mutable.
      * {{{clone: func() -> fields;}}}
      */
    override def clone(): Fields = cm.native
  }
  object Fields {
    @cm.native
    def apply(): Fields = cm.native

    // from-list: static func(
    //   entries: list<tuple<field-name,field-value>>
    // ) -> result<fields, header-error>;
    @cm.native
    def fromList(entries: Array[(FieldName, FieldValue)]): cm.Result[Fields, HeaderError] = cm.native
  }

  // type field-name = field-key;
  type FieldName = FieldKey
  type FieldKey = String

  type FieldValue = Array[UByte]

  /** This type enumerates the different kinds of errors that may occur when
    * setting or appending to a `fields` resource.
    */
  sealed trait HeaderError extends cm.Variant
  object HeaderError {
    /** This error indicates that a `field-name` or `field-value` was
      * syntactically invalid when used with an operation that sets headers in a
      * `fields`.
      */
    final case object InvalidSyntax extends HeaderError { val value = (); type T = Unit; val _index = 0 }
    final case object Forbidden extends HeaderError { val value = (); type T = Unit; val _index = 1 }
    final case object Immutable extends HeaderError { val value = (); type T = Unit; val _index = 2 }
  }

  // TODO
  /** Represents an incoming HTTP Request or Response's Body.
    *
    * A body has both its contents - a stream of bytes - and a (possibly
    * empty) set of trailers, indicating that the full contents of the
    * body have been received. This resource represents the contents as
    * an {@code InputStream} and the delivery of trailers as a {@code Future<Trailers>},
    * and ensures that the user of this interface may only be consuming either
    * the body contents or waiting on trailers at any given time.
    */
  @cm.native
  @ComponentImport("wasi:http/types@0.2.0")
  trait IncomingBody extends cm.Resource {
    /** Returns the contents of the body as a stream of bytes.
      *
      * The first call to this method succeeds and returns a stream representing the contents.
      * The stream can only be retrieved once; subsequent calls will return an error.
      *
      * The returned InputStream is a child resource: it must be closed before the
      * parent IncomingBody is dropped, or it must be fully consumed by
      * IncomingBody.finish().
      *
      * This invariant ensures that the implementation can determine whether the user is
      * consuming the body, waiting for the future trailers to be ready, or neither. This allows
      * network backpressure to be applied when the user is consuming the body, while ensuring
      * that backpressure does not inhibit the delivery of trailers if the user does not read
      * the entire body.
      *
      * @return an InputStream representing the body contents
      * @throws IOException if the stream has already been retrieved
      */
    // def stream(): cm.Result[InputStream, Unit] = cm.native

  }
  object IncomingBody {
    /** Takes ownership of `incoming-body`, and returns a `future-trailers`.
      * This function will trap if the `input-stream` child is still alive.
      */
    // def finish(`this`: IncomingBody): FutureTrailers = cm.native
  }

}