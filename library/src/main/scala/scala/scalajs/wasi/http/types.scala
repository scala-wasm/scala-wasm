package scala.scalajs.wasi.http

package object types {

  // Type definitions
  type Duration = scala.scalajs.wit.unsigned.ULong

  type InputStream = scala.scalajs.wasi.io.streams.InputStream

  type OutputStream = scala.scalajs.wasi.io.streams.OutputStream

  type IoError = scala.scalajs.wasi.io.error.Error

  type Pollable = scala.scalajs.wasi.io.poll.Pollable

  /** This type corresponds to HTTP standard Methods.
   */
  @scala.scalajs.wit.annotation.WitVariant
  sealed trait Method
  object Method {
    object Get extends Method {
      override def toString(): String = "Get"
    }
    object Head extends Method {
      override def toString(): String = "Head"
    }
    object Post extends Method {
      override def toString(): String = "Post"
    }
    object Put extends Method {
      override def toString(): String = "Put"
    }
    object Delete extends Method {
      override def toString(): String = "Delete"
    }
    object Connect extends Method {
      override def toString(): String = "Connect"
    }
    object Options extends Method {
      override def toString(): String = "Options"
    }
    object Trace extends Method {
      override def toString(): String = "Trace"
    }
    object Patch extends Method {
      override def toString(): String = "Patch"
    }
    final class Other(val value: String) extends Method {
      override def equals(other: Any): Boolean = other match {
        case that: Other => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "Other(" + value + ")"
    }
    object Other {
      def apply(value: String): Other = new Other(value)
    }
  }

  /** This type corresponds to HTTP standard Related Schemes.
   */
  @scala.scalajs.wit.annotation.WitVariant
  sealed trait Scheme
  object Scheme {
    object Http extends Scheme {
      override def toString(): String = "Http"
    }
    object Https extends Scheme {
      override def toString(): String = "Https"
    }
    final class Other(val value: String) extends Scheme {
      override def equals(other: Any): Boolean = other match {
        case that: Other => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "Other(" + value + ")"
    }
    object Other {
      def apply(value: String): Other = new Other(value)
    }
  }

  /** Defines the case payload type for `DNS-error` above:
   */
  @scala.scalajs.wit.annotation.WitRecord
  final class DnsErrorPayload(val rcode: java.util.Optional[String], val infoCode: java.util.Optional[scala.scalajs.wit.unsigned.UShort]) {
    override def equals(other: Any): Boolean = other match {
      case that: DnsErrorPayload => this.rcode == that.rcode && this.infoCode == that.infoCode
      case _ => false
    }
    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + rcode.hashCode()
      result = 31 * result + infoCode.hashCode()
      result
    }
    override def toString(): String = "DnsErrorPayload(" + rcode + ", " + infoCode + ")"
  }
  object DnsErrorPayload {
    def apply(rcode: java.util.Optional[String], infoCode: java.util.Optional[scala.scalajs.wit.unsigned.UShort]): DnsErrorPayload = new DnsErrorPayload(rcode, infoCode)
  }

  /** Defines the case payload type for `TLS-alert-received` above:
   */
  @scala.scalajs.wit.annotation.WitRecord
  final class TlsAlertReceivedPayload(val alertId: java.util.Optional[scala.scalajs.wit.unsigned.UByte], val alertMessage: java.util.Optional[String]) {
    override def equals(other: Any): Boolean = other match {
      case that: TlsAlertReceivedPayload => this.alertId == that.alertId && this.alertMessage == that.alertMessage
      case _ => false
    }
    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + alertId.hashCode()
      result = 31 * result + alertMessage.hashCode()
      result
    }
    override def toString(): String = "TlsAlertReceivedPayload(" + alertId + ", " + alertMessage + ")"
  }
  object TlsAlertReceivedPayload {
    def apply(alertId: java.util.Optional[scala.scalajs.wit.unsigned.UByte], alertMessage: java.util.Optional[String]): TlsAlertReceivedPayload = new TlsAlertReceivedPayload(alertId, alertMessage)
  }

  /** Defines the case payload type for `HTTP-response-{header,trailer}-size` above:
   */
  @scala.scalajs.wit.annotation.WitRecord
  final class FieldSizePayload(val fieldName: java.util.Optional[String], val fieldSize: java.util.Optional[scala.scalajs.wit.unsigned.UInt]) {
    override def equals(other: Any): Boolean = other match {
      case that: FieldSizePayload => this.fieldName == that.fieldName && this.fieldSize == that.fieldSize
      case _ => false
    }
    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + fieldName.hashCode()
      result = 31 * result + fieldSize.hashCode()
      result
    }
    override def toString(): String = "FieldSizePayload(" + fieldName + ", " + fieldSize + ")"
  }
  object FieldSizePayload {
    def apply(fieldName: java.util.Optional[String], fieldSize: java.util.Optional[scala.scalajs.wit.unsigned.UInt]): FieldSizePayload = new FieldSizePayload(fieldName, fieldSize)
  }

  /** These cases are inspired by the IANA HTTP Proxy Error Types:
   *  https://www.iana.org/assignments/http-proxy-status/http-proxy-status.xhtml#table-http-proxy-error-types
   */
  @scala.scalajs.wit.annotation.WitVariant
  sealed trait ErrorCode
  object ErrorCode {
    object DnsTimeout extends ErrorCode {
      override def toString(): String = "DnsTimeout"
    }
    final class DnsError(val value: DnsErrorPayload) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: DnsError => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "DnsError(" + value + ")"
    }
    object DnsError {
      def apply(value: DnsErrorPayload): DnsError = new DnsError(value)
    }
    object DestinationNotFound extends ErrorCode {
      override def toString(): String = "DestinationNotFound"
    }
    object DestinationUnavailable extends ErrorCode {
      override def toString(): String = "DestinationUnavailable"
    }
    object DestinationIpProhibited extends ErrorCode {
      override def toString(): String = "DestinationIpProhibited"
    }
    object DestinationIpUnroutable extends ErrorCode {
      override def toString(): String = "DestinationIpUnroutable"
    }
    object ConnectionRefused extends ErrorCode {
      override def toString(): String = "ConnectionRefused"
    }
    object ConnectionTerminated extends ErrorCode {
      override def toString(): String = "ConnectionTerminated"
    }
    object ConnectionTimeout extends ErrorCode {
      override def toString(): String = "ConnectionTimeout"
    }
    object ConnectionReadTimeout extends ErrorCode {
      override def toString(): String = "ConnectionReadTimeout"
    }
    object ConnectionWriteTimeout extends ErrorCode {
      override def toString(): String = "ConnectionWriteTimeout"
    }
    object ConnectionLimitReached extends ErrorCode {
      override def toString(): String = "ConnectionLimitReached"
    }
    object TlsProtocolError extends ErrorCode {
      override def toString(): String = "TlsProtocolError"
    }
    object TlsCertificateError extends ErrorCode {
      override def toString(): String = "TlsCertificateError"
    }
    final class TlsAlertReceived(val value: TlsAlertReceivedPayload) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: TlsAlertReceived => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "TlsAlertReceived(" + value + ")"
    }
    object TlsAlertReceived {
      def apply(value: TlsAlertReceivedPayload): TlsAlertReceived = new TlsAlertReceived(value)
    }
    object HttpRequestDenied extends ErrorCode {
      override def toString(): String = "HttpRequestDenied"
    }
    object HttpRequestLengthRequired extends ErrorCode {
      override def toString(): String = "HttpRequestLengthRequired"
    }
    final class HttpRequestBodySize(val value: java.util.Optional[scala.scalajs.wit.unsigned.ULong]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpRequestBodySize => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpRequestBodySize(" + value + ")"
    }
    object HttpRequestBodySize {
      def apply(value: java.util.Optional[scala.scalajs.wit.unsigned.ULong]): HttpRequestBodySize = new HttpRequestBodySize(value)
    }
    object HttpRequestMethodInvalid extends ErrorCode {
      override def toString(): String = "HttpRequestMethodInvalid"
    }
    object HttpRequestUriInvalid extends ErrorCode {
      override def toString(): String = "HttpRequestUriInvalid"
    }
    object HttpRequestUriTooLong extends ErrorCode {
      override def toString(): String = "HttpRequestUriTooLong"
    }
    final class HttpRequestHeaderSectionSize(val value: java.util.Optional[scala.scalajs.wit.unsigned.UInt]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpRequestHeaderSectionSize => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpRequestHeaderSectionSize(" + value + ")"
    }
    object HttpRequestHeaderSectionSize {
      def apply(value: java.util.Optional[scala.scalajs.wit.unsigned.UInt]): HttpRequestHeaderSectionSize = new HttpRequestHeaderSectionSize(value)
    }
    final class HttpRequestHeaderSize(val value: java.util.Optional[FieldSizePayload]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpRequestHeaderSize => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpRequestHeaderSize(" + value + ")"
    }
    object HttpRequestHeaderSize {
      def apply(value: java.util.Optional[FieldSizePayload]): HttpRequestHeaderSize = new HttpRequestHeaderSize(value)
    }
    final class HttpRequestTrailerSectionSize(val value: java.util.Optional[scala.scalajs.wit.unsigned.UInt]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpRequestTrailerSectionSize => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpRequestTrailerSectionSize(" + value + ")"
    }
    object HttpRequestTrailerSectionSize {
      def apply(value: java.util.Optional[scala.scalajs.wit.unsigned.UInt]): HttpRequestTrailerSectionSize = new HttpRequestTrailerSectionSize(value)
    }
    final class HttpRequestTrailerSize(val value: FieldSizePayload) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpRequestTrailerSize => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpRequestTrailerSize(" + value + ")"
    }
    object HttpRequestTrailerSize {
      def apply(value: FieldSizePayload): HttpRequestTrailerSize = new HttpRequestTrailerSize(value)
    }
    object HttpResponseIncomplete extends ErrorCode {
      override def toString(): String = "HttpResponseIncomplete"
    }
    final class HttpResponseHeaderSectionSize(val value: java.util.Optional[scala.scalajs.wit.unsigned.UInt]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseHeaderSectionSize => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpResponseHeaderSectionSize(" + value + ")"
    }
    object HttpResponseHeaderSectionSize {
      def apply(value: java.util.Optional[scala.scalajs.wit.unsigned.UInt]): HttpResponseHeaderSectionSize = new HttpResponseHeaderSectionSize(value)
    }
    final class HttpResponseHeaderSize(val value: FieldSizePayload) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseHeaderSize => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpResponseHeaderSize(" + value + ")"
    }
    object HttpResponseHeaderSize {
      def apply(value: FieldSizePayload): HttpResponseHeaderSize = new HttpResponseHeaderSize(value)
    }
    final class HttpResponseBodySize(val value: java.util.Optional[scala.scalajs.wit.unsigned.ULong]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseBodySize => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpResponseBodySize(" + value + ")"
    }
    object HttpResponseBodySize {
      def apply(value: java.util.Optional[scala.scalajs.wit.unsigned.ULong]): HttpResponseBodySize = new HttpResponseBodySize(value)
    }
    final class HttpResponseTrailerSectionSize(val value: java.util.Optional[scala.scalajs.wit.unsigned.UInt]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseTrailerSectionSize => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpResponseTrailerSectionSize(" + value + ")"
    }
    object HttpResponseTrailerSectionSize {
      def apply(value: java.util.Optional[scala.scalajs.wit.unsigned.UInt]): HttpResponseTrailerSectionSize = new HttpResponseTrailerSectionSize(value)
    }
    final class HttpResponseTrailerSize(val value: FieldSizePayload) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseTrailerSize => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpResponseTrailerSize(" + value + ")"
    }
    object HttpResponseTrailerSize {
      def apply(value: FieldSizePayload): HttpResponseTrailerSize = new HttpResponseTrailerSize(value)
    }
    final class HttpResponseTransferCoding(val value: java.util.Optional[String]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseTransferCoding => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpResponseTransferCoding(" + value + ")"
    }
    object HttpResponseTransferCoding {
      def apply(value: java.util.Optional[String]): HttpResponseTransferCoding = new HttpResponseTransferCoding(value)
    }
    final class HttpResponseContentCoding(val value: java.util.Optional[String]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseContentCoding => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "HttpResponseContentCoding(" + value + ")"
    }
    object HttpResponseContentCoding {
      def apply(value: java.util.Optional[String]): HttpResponseContentCoding = new HttpResponseContentCoding(value)
    }
    object HttpResponseTimeout extends ErrorCode {
      override def toString(): String = "HttpResponseTimeout"
    }
    object HttpUpgradeFailed extends ErrorCode {
      override def toString(): String = "HttpUpgradeFailed"
    }
    object HttpProtocolError extends ErrorCode {
      override def toString(): String = "HttpProtocolError"
    }
    object LoopDetected extends ErrorCode {
      override def toString(): String = "LoopDetected"
    }
    object ConfigurationError extends ErrorCode {
      override def toString(): String = "ConfigurationError"
    }
    final class InternalError(val value: java.util.Optional[String]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: InternalError => this.value == that.value
        case _ => false
      }
      override def hashCode(): Int = {
        value.hashCode()
      }
      override def toString(): String = "InternalError(" + value + ")"
    }
    object InternalError {
      def apply(value: java.util.Optional[String]): InternalError = new InternalError(value)
    }
  }

  /** This type enumerates the different kinds of errors that may occur when
   *  setting or appending to a `fields` resource.
   */
  @scala.scalajs.wit.annotation.WitVariant
  sealed trait HeaderError
  object HeaderError {
    object InvalidSyntax extends HeaderError {
      override def toString(): String = "InvalidSyntax"
    }
    object Forbidden extends HeaderError {
      override def toString(): String = "Forbidden"
    }
    object Immutable extends HeaderError {
      override def toString(): String = "Immutable"
    }
  }

  type FieldKey = String

  type FieldValue = Array[scala.scalajs.wit.unsigned.UByte]

  type Headers = Fields

  type Trailers = Fields

  type StatusCode = scala.scalajs.wit.unsigned.UShort

  // Resources
  /** This following block defines the `fields` resource which corresponds to
   *  HTTP standard Fields. Fields are a common representation used for both
   *  Headers and Trailers.
   *
   *  A `fields` may be mutable or immutable. A `fields` created using the
   *  constructor, `from-list`, or `clone` will be mutable, but a `fields`
   *  resource given by other means (including, but not limited to,
   *  `incoming-request.headers`, `outgoing-request.headers`) might be be
   *  immutable. In an immutable fields, the `set`, `append`, and `delete`
   *  operations will fail with `header-error.immutable`.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "fields")
  trait Fields {
    /** Get all of the values corresponding to a key. If the key is not present
     *  in this `fields`, an empty list is returned. However, if the key is
     *  present but empty, this is represented by a list with one or more
     *  empty field-values present.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("get")
    def get(name: String): Array[Array[scala.scalajs.wit.unsigned.UByte]] = scala.scalajs.wit.native
    /** Returns `true` when the key is present in this `fields`. If the key is
     *  syntactically invalid, `false` is returned.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("has")
    def has(name: String): Boolean = scala.scalajs.wit.native
    /** Set all of the values for a key. Clears any existing values for that
     *  key, if they have been set.
     *
     *  Fails with `header-error.immutable` if the `fields` are immutable.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set")
    def set(name: String, value: Array[Array[scala.scalajs.wit.unsigned.UByte]]): scala.scalajs.wit.Result[Unit, HeaderError] = scala.scalajs.wit.native
    /** Delete all values for a key. Does nothing if no values for the key
     *  exist.
     *
     *  Fails with `header-error.immutable` if the `fields` are immutable.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("delete")
    def delete(name: String): scala.scalajs.wit.Result[Unit, HeaderError] = scala.scalajs.wit.native
    /** Append a value for a key. Does not change or delete any existing
     *  values for that key.
     *
     *  Fails with `header-error.immutable` if the `fields` are immutable.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("append")
    def append(name: String, value: Array[scala.scalajs.wit.unsigned.UByte]): scala.scalajs.wit.Result[Unit, HeaderError] = scala.scalajs.wit.native
    /** Retrieve the full set of keys and values in the Fields. Like the
     *  constructor, the list represents each key-value pair.
     *
     *  The outer list represents each key-value pair in the Fields. Keys
     *  which have multiple values are represented by multiple entries in this
     *  list with the same key.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("entries")
    def entries(): Array[scala.scalajs.wit.Tuple2[String, Array[scala.scalajs.wit.unsigned.UByte]]] = scala.scalajs.wit.native
    /** Make a deep copy of the Fields. Equivelant in behavior to calling the
     *  `fields` constructor on the return value of `entries`. The resulting
     *  `fields` is mutable.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("clone")
    def clone_(): Fields = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object Fields {
    /** Construct an empty HTTP Fields.
     *
     *  The resulting `fields` is mutable.
     */
    @scala.scalajs.wit.annotation.WitResourceConstructor
    def apply(): Fields = scala.scalajs.wit.native
    /** Construct an HTTP Fields.
     *
     *  The resulting `fields` is mutable.
     *
     *  The list represents each key-value pair in the Fields. Keys
     *  which have multiple values are represented by multiple entries in this
     *  list with the same key.
     *
     *  The tuple is a pair of the field key, represented as a string, and
     *  Value, represented as a list of bytes. In a valid Fields, all keys
     *  and values are valid UTF-8 strings. However, values are not always
     *  well-formed, so they are represented as a raw list of bytes.
     *
     *  An error result will be returned if any header or value was
     *  syntactically invalid, or if a header was forbidden.
     */
    @scala.scalajs.wit.annotation.WitResourceStaticMethod("from-list")
    def fromList(entries: Array[scala.scalajs.wit.Tuple2[String, Array[scala.scalajs.wit.unsigned.UByte]]]): scala.scalajs.wit.Result[Fields, HeaderError] = scala.scalajs.wit.native
  }

  /** Represents an incoming HTTP Request.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "incoming-request")
  trait IncomingRequest {
    /** Returns the method of the incoming request.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("method")
    def method(): Method = scala.scalajs.wit.native
    /** Returns the path with query parameters from the request, as a string.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("path-with-query")
    def pathWithQuery(): java.util.Optional[String] = scala.scalajs.wit.native
    /** Returns the protocol scheme from the request.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("scheme")
    def scheme(): java.util.Optional[Scheme] = scala.scalajs.wit.native
    /** Returns the authority from the request, if it was present.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("authority")
    def authority(): java.util.Optional[String] = scala.scalajs.wit.native
    /** Get the `headers` associated with the request.
     *
     *  The returned `headers` resource is immutable: `set`, `append`, and
     *  `delete` operations will fail with `header-error.immutable`.
     *
     *  The `headers` returned are a child resource: it must be dropped before
     *  the parent `incoming-request` is dropped. Dropping this
     *  `incoming-request` before all children are dropped will trap.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("headers")
    def headers(): Headers = scala.scalajs.wit.native
    /** Gives the `incoming-body` associated with this request. Will only
     *  return success at most once, and subsequent calls will return error.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("consume")
    def consume(): scala.scalajs.wit.Result[IncomingBody, Unit] = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object IncomingRequest {
  }

  /** Represents an outgoing HTTP Request.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "outgoing-request")
  trait OutgoingRequest {
    /** Returns the resource corresponding to the outgoing Body for this
     *  Request.
     *
     *  Returns success on the first call: the `outgoing-body` resource for
     *  this `outgoing-request` can be retrieved at most once. Subsequent
     *  calls will return error.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("body")
    def body(): scala.scalajs.wit.Result[OutgoingBody, Unit] = scala.scalajs.wit.native
    /** Get the Method for the Request.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("method")
    def method(): Method = scala.scalajs.wit.native
    /** Set the Method for the Request. Fails if the string present in a
     *  `method.other` argument is not a syntactically valid method.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-method")
    def setMethod(method: Method): scala.scalajs.wit.Result[Unit, Unit] = scala.scalajs.wit.native
    /** Get the combination of the HTTP Path and Query for the Request.
     *  When `none`, this represents an empty Path and empty Query.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("path-with-query")
    def pathWithQuery(): java.util.Optional[String] = scala.scalajs.wit.native
    /** Set the combination of the HTTP Path and Query for the Request.
     *  When `none`, this represents an empty Path and empty Query. Fails is the
     *  string given is not a syntactically valid path and query uri component.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-path-with-query")
    def setPathWithQuery(pathWithQuery: java.util.Optional[String]): scala.scalajs.wit.Result[Unit, Unit] = scala.scalajs.wit.native
    /** Get the HTTP Related Scheme for the Request. When `none`, the
     *  implementation may choose an appropriate default scheme.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("scheme")
    def scheme(): java.util.Optional[Scheme] = scala.scalajs.wit.native
    /** Set the HTTP Related Scheme for the Request. When `none`, the
     *  implementation may choose an appropriate default scheme. Fails if the
     *  string given is not a syntactically valid uri scheme.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-scheme")
    def setScheme(scheme: java.util.Optional[Scheme]): scala.scalajs.wit.Result[Unit, Unit] = scala.scalajs.wit.native
    /** Get the HTTP Authority for the Request. A value of `none` may be used
     *  with Related Schemes which do not require an Authority. The HTTP and
     *  HTTPS schemes always require an authority.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("authority")
    def authority(): java.util.Optional[String] = scala.scalajs.wit.native
    /** Set the HTTP Authority for the Request. A value of `none` may be used
     *  with Related Schemes which do not require an Authority. The HTTP and
     *  HTTPS schemes always require an authority. Fails if the string given is
     *  not a syntactically valid uri authority.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-authority")
    def setAuthority(authority: java.util.Optional[String]): scala.scalajs.wit.Result[Unit, Unit] = scala.scalajs.wit.native
    /** Get the headers associated with the Request.
     *
     *  The returned `headers` resource is immutable: `set`, `append`, and
     *  `delete` operations will fail with `header-error.immutable`.
     *
     *  This headers resource is a child: it must be dropped before the parent
     *  `outgoing-request` is dropped, or its ownership is transfered to
     *  another component by e.g. `outgoing-handler.handle`.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("headers")
    def headers(): Headers = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object OutgoingRequest {
    /** Construct a new `outgoing-request` with a default `method` of `GET`, and
     *  `none` values for `path-with-query`, `scheme`, and `authority`.
     *
     *  * `headers` is the HTTP Headers for the Request.
     *
     *  It is possible to construct, or manipulate with the accessor functions
     *  below, an `outgoing-request` with an invalid combination of `scheme`
     *  and `authority`, or `headers` which are not permitted to be sent.
     *  It is the obligation of the `outgoing-handler.handle` implementation
     *  to reject invalid constructions of `outgoing-request`.
     */
    @scala.scalajs.wit.annotation.WitResourceConstructor
    def apply(headers: Headers): OutgoingRequest = scala.scalajs.wit.native
  }

  /** Parameters for making an HTTP Request. Each of these parameters is
   *  currently an optional timeout applicable to the transport layer of the
   *  HTTP protocol.
   *
   *  These timeouts are separate from any the user may use to bound a
   *  blocking call to `wasi:io/poll.poll`.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "request-options")
  trait RequestOptions {
    /** The timeout for the initial connect to the HTTP Server.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("connect-timeout")
    def connectTimeout(): java.util.Optional[scala.scalajs.wit.unsigned.ULong] = scala.scalajs.wit.native
    /** Set the timeout for the initial connect to the HTTP Server. An error
     *  return value indicates that this timeout is not supported.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-connect-timeout")
    def setConnectTimeout(duration: java.util.Optional[scala.scalajs.wit.unsigned.ULong]): scala.scalajs.wit.Result[Unit, Unit] = scala.scalajs.wit.native
    /** The timeout for receiving the first byte of the Response body.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("first-byte-timeout")
    def firstByteTimeout(): java.util.Optional[scala.scalajs.wit.unsigned.ULong] = scala.scalajs.wit.native
    /** Set the timeout for receiving the first byte of the Response body. An
     *  error return value indicates that this timeout is not supported.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-first-byte-timeout")
    def setFirstByteTimeout(duration: java.util.Optional[scala.scalajs.wit.unsigned.ULong]): scala.scalajs.wit.Result[Unit, Unit] = scala.scalajs.wit.native
    /** The timeout for receiving subsequent chunks of bytes in the Response
     *  body stream.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("between-bytes-timeout")
    def betweenBytesTimeout(): java.util.Optional[scala.scalajs.wit.unsigned.ULong] = scala.scalajs.wit.native
    /** Set the timeout for receiving subsequent chunks of bytes in the Response
     *  body stream. An error return value indicates that this timeout is not
     *  supported.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-between-bytes-timeout")
    def setBetweenBytesTimeout(duration: java.util.Optional[scala.scalajs.wit.unsigned.ULong]): scala.scalajs.wit.Result[Unit, Unit] = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object RequestOptions {
    /** Construct a default `request-options` value.
     */
    @scala.scalajs.wit.annotation.WitResourceConstructor
    def apply(): RequestOptions = scala.scalajs.wit.native
  }

  /** Represents the ability to send an HTTP Response.
   *
   *  This resource is used by the `wasi:http/incoming-handler` interface to
   *  allow a Response to be sent corresponding to the Request provided as the
   *  other argument to `incoming-handler.handle`.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "response-outparam")
  trait ResponseOutparam {
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object ResponseOutparam {
    /** Set the value of the `response-outparam` to either send a response,
     *  or indicate an error.
     *
     *  This method consumes the `response-outparam` to ensure that it is
     *  called at most once. If it is never called, the implementation
     *  will respond with an error.
     *
     *  The user may provide an `error` to `response` to allow the
     *  implementation determine how to respond with an HTTP error response.
     */
    @scala.scalajs.wit.annotation.WitResourceStaticMethod("set")
    def set(param: ResponseOutparam, response: scala.scalajs.wit.Result[OutgoingResponse, ErrorCode]): Unit = scala.scalajs.wit.native
  }

  /** Represents an incoming HTTP Response.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "incoming-response")
  trait IncomingResponse {
    /** Returns the status code from the incoming response.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("status")
    def status(): scala.scalajs.wit.unsigned.UShort = scala.scalajs.wit.native
    /** Returns the headers from the incoming response.
     *
     *  The returned `headers` resource is immutable: `set`, `append`, and
     *  `delete` operations will fail with `header-error.immutable`.
     *
     *  This headers resource is a child: it must be dropped before the parent
     *  `incoming-response` is dropped.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("headers")
    def headers(): Headers = scala.scalajs.wit.native
    /** Returns the incoming body. May be called at most once. Returns error
     *  if called additional times.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("consume")
    def consume(): scala.scalajs.wit.Result[IncomingBody, Unit] = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object IncomingResponse {
  }

  /** Represents an incoming HTTP Request or Response's Body.
   *
   *  A body has both its contents - a stream of bytes - and a (possibly
   *  empty) set of trailers, indicating that the full contents of the
   *  body have been received. This resource represents the contents as
   *  an `input-stream` and the delivery of trailers as a `future-trailers`,
   *  and ensures that the user of this interface may only be consuming either
   *  the body contents or waiting on trailers at any given time.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "incoming-body")
  trait IncomingBody {
    /** Returns the contents of the body, as a stream of bytes.
     *
     *  Returns success on first call: the stream representing the contents
     *  can be retrieved at most once. Subsequent calls will return error.
     *
     *  The returned `input-stream` resource is a child: it must be dropped
     *  before the parent `incoming-body` is dropped, or consumed by
     *  `incoming-body.finish`.
     *
     *  This invariant ensures that the implementation can determine whether
     *  the user is consuming the contents of the body, waiting on the
     *  `future-trailers` to be ready, or neither. This allows for network
     *  backpressure is to be applied when the user is consuming the body,
     *  and for that backpressure to not inhibit delivery of the trailers if
     *  the user does not read the entire body.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("stream")
    def stream(): scala.scalajs.wit.Result[InputStream, Unit] = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object IncomingBody {
    /** Takes ownership of `incoming-body`, and returns a `future-trailers`.
     *  This function will trap if the `input-stream` child is still alive.
     */
    @scala.scalajs.wit.annotation.WitResourceStaticMethod("finish")
    def finish(`this`: IncomingBody): FutureTrailers = scala.scalajs.wit.native
  }

  /** Represents a future which may eventaully return trailers, or an error.
   *
   *  In the case that the incoming HTTP Request or Response did not have any
   *  trailers, this future will resolve to the empty set of trailers once the
   *  complete Request or Response body has been received.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "future-trailers")
  trait FutureTrailers {
    /** Returns a pollable which becomes ready when either the trailers have
     *  been received, or an error has occured. When this pollable is ready,
     *  the `get` method will return `some`.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("subscribe")
    def subscribe(): Pollable = scala.scalajs.wit.native
    /** Returns the contents of the trailers, or an error which occured,
     *  once the future is ready.
     *
     *  The outer `option` represents future readiness. Users can wait on this
     *  `option` to become `some` using the `subscribe` method.
     *
     *  The outer `result` is used to retrieve the trailers or error at most
     *  once. It will be success on the first call in which the outer option
     *  is `some`, and error on subsequent calls.
     *
     *  The inner `result` represents that either the HTTP Request or Response
     *  body, as well as any trailers, were received successfully, or that an
     *  error occured receiving them. The optional `trailers` indicates whether
     *  or not trailers were present in the body.
     *
     *  When some `trailers` are returned by this method, the `trailers`
     *  resource is immutable, and a child. Use of the `set`, `append`, or
     *  `delete` methods will return an error, and the resource must be
     *  dropped before the parent `future-trailers` is dropped.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("get")
    def get(): java.util.Optional[scala.scalajs.wit.Result[scala.scalajs.wit.Result[java.util.Optional[Trailers], ErrorCode], Unit]] = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object FutureTrailers {
  }

  /** Represents an outgoing HTTP Response.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "outgoing-response")
  trait OutgoingResponse {
    /** Get the HTTP Status Code for the Response.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("status-code")
    def statusCode(): scala.scalajs.wit.unsigned.UShort = scala.scalajs.wit.native
    /** Set the HTTP Status Code for the Response. Fails if the status-code
     *  given is not a valid http status code.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-status-code")
    def setStatusCode(statusCode: scala.scalajs.wit.unsigned.UShort): scala.scalajs.wit.Result[Unit, Unit] = scala.scalajs.wit.native
    /** Get the headers associated with the Request.
     *
     *  The returned `headers` resource is immutable: `set`, `append`, and
     *  `delete` operations will fail with `header-error.immutable`.
     *
     *  This headers resource is a child: it must be dropped before the parent
     *  `outgoing-request` is dropped, or its ownership is transfered to
     *  another component by e.g. `outgoing-handler.handle`.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("headers")
    def headers(): Headers = scala.scalajs.wit.native
    /** Returns the resource corresponding to the outgoing Body for this Response.
     *
     *  Returns success on the first call: the `outgoing-body` resource for
     *  this `outgoing-response` can be retrieved at most once. Subsequent
     *  calls will return error.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("body")
    def body(): scala.scalajs.wit.Result[OutgoingBody, Unit] = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object OutgoingResponse {
    /** Construct an `outgoing-response`, with a default `status-code` of `200`.
     *  If a different `status-code` is needed, it must be set via the
     *  `set-status-code` method.
     *
     *  * `headers` is the HTTP Headers for the Response.
     */
    @scala.scalajs.wit.annotation.WitResourceConstructor
    def apply(headers: Headers): OutgoingResponse = scala.scalajs.wit.native
  }

  /** Represents an outgoing HTTP Request or Response's Body.
   *
   *  A body has both its contents - a stream of bytes - and a (possibly
   *  empty) set of trailers, inducating the full contents of the body
   *  have been sent. This resource represents the contents as an
   *  `output-stream` child resource, and the completion of the body (with
   *  optional trailers) with a static function that consumes the
   *  `outgoing-body` resource, and ensures that the user of this interface
   *  may not write to the body contents after the body has been finished.
   *
   *  If the user code drops this resource, as opposed to calling the static
   *  method `finish`, the implementation should treat the body as incomplete,
   *  and that an error has occured. The implementation should propogate this
   *  error to the HTTP protocol by whatever means it has available,
   *  including: corrupting the body on the wire, aborting the associated
   *  Request, or sending a late status code for the Response.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "outgoing-body")
  trait OutgoingBody {
    /** Returns a stream for writing the body contents.
     *
     *  The returned `output-stream` is a child resource: it must be dropped
     *  before the parent `outgoing-body` resource is dropped (or finished),
     *  otherwise the `outgoing-body` drop or `finish` will trap.
     *
     *  Returns success on the first call: the `output-stream` resource for
     *  this `outgoing-body` may be retrieved at most once. Subsequent calls
     *  will return error.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("write")
    def write(): scala.scalajs.wit.Result[OutputStream, Unit] = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object OutgoingBody {
    /** Finalize an outgoing body, optionally providing trailers. This must be
     *  called to signal that the response is complete. If the `outgoing-body`
     *  is dropped without calling `outgoing-body.finalize`, the implementation
     *  should treat the body as corrupted.
     *
     *  Fails if the body's `outgoing-request` or `outgoing-response` was
     *  constructed with a Content-Length header, and the contents written
     *  to the body (via `write`) does not match the value given in the
     *  Content-Length.
     */
    @scala.scalajs.wit.annotation.WitResourceStaticMethod("finish")
    def finish(`this`: OutgoingBody, trailers: java.util.Optional[Trailers]): scala.scalajs.wit.Result[Unit, ErrorCode] = scala.scalajs.wit.native
  }

  /** Represents a future which may eventaully return an incoming HTTP
   *  Response, or an error.
   *
   *  This resource is returned by the `wasi:http/outgoing-handler` interface to
   *  provide the HTTP Response corresponding to the sent Request.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:http/types@0.2.0", "future-incoming-response")
  trait FutureIncomingResponse {
    /** Returns a pollable which becomes ready when either the Response has
     *  been received, or an error has occured. When this pollable is ready,
     *  the `get` method will return `some`.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("subscribe")
    def subscribe(): Pollable = scala.scalajs.wit.native
    /** Returns the incoming HTTP Response, or an error, once one is ready.
     *
     *  The outer `option` represents future readiness. Users can wait on this
     *  `option` to become `some` using the `subscribe` method.
     *
     *  The outer `result` is used to retrieve the response or error at most
     *  once. It will be success on the first call in which the outer option
     *  is `some`, and error on subsequent calls.
     *
     *  The inner `result` represents that either the incoming HTTP Response
     *  status and headers have recieved successfully, or that an error
     *  occured. Errors may also occur while consuming the response body,
     *  but those will be reported by the `incoming-body` and its
     *  `output-stream` child.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("get")
    def get(): java.util.Optional[scala.scalajs.wit.Result[scala.scalajs.wit.Result[IncomingResponse, ErrorCode], Unit]] = scala.scalajs.wit.native
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }
  object FutureIncomingResponse {
  }

  // Functions
  /** Attempts to extract a http-related `error` from the wasi:io `error`
   *  provided.
   *
   *  Stream operations which return
   *  `wasi:io/stream/stream-error::last-operation-failed` have a payload of
   *  type `wasi:io/error/error` with more information about the operation
   *  that failed. This payload can be passed through to this function to see
   *  if there's http-related information about the error to return.
   *
   *  Note that this function is fallible because not all io-errors are
   *  http-related errors.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:http/types@0.2.0", "http-error-code")
  def httpErrorCode(err: IoError): java.util.Optional[ErrorCode] = scala.scalajs.wit.native

}
