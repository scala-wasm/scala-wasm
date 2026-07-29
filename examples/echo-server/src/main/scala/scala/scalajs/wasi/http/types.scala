package scala.scalajs.wasi.http

import scala.scalajs.wasi.io.poll.Pollable
import scala.scalajs.wasi.io.streams.{InputStream, OutputStream}
import scala.scalajs.wit.Result
import scala.scalajs.wit.Tuple2
import scala.scalajs.wit.annotation.{
  WitImport,
  WitResourceConstructor,
  WitResourceDrop,
  WitResourceImport,
  WitResourceMethod,
  WitResourceStaticMethod,
  WitRecord,
  WitVariant
}
import scala.scalajs.wit.native
import scala.scalajs.wit.unsigned.{UByte, UInt, ULong, UShort}

package object types {

  type Duration = ULong
  type InputStream = scala.scalajs.wasi.io.streams.InputStream
  type OutputStream = scala.scalajs.wasi.io.streams.OutputStream
  type IoError = scala.scalajs.wasi.io.error.Error
  type Pollable = scala.scalajs.wasi.io.poll.Pollable

  @WitVariant
  sealed trait Method

  object Method {
    object Get extends Method { override def toString(): String = "Get" }
    object Head extends Method { override def toString(): String = "Head" }
    object Post extends Method { override def toString(): String = "Post" }
    object Put extends Method { override def toString(): String = "Put" }
    object Delete extends Method { override def toString(): String = "Delete" }
    object Connect extends Method { override def toString(): String = "Connect" }
    object Options extends Method { override def toString(): String = "Options" }
    object Trace extends Method { override def toString(): String = "Trace" }
    object Patch extends Method { override def toString(): String = "Patch" }

    final class Other(val value: String) extends Method {
      override def equals(other: Any): Boolean = other match {
        case that: Other => this.value == that.value
        case _           => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "Other(" + value + ")"
    }

    object Other {
      def apply(value: String): Other = new Other(value)
      def unapply(arg: Other): Some[String] = Some(arg.value)
    }
  }

  @WitVariant
  sealed trait Scheme

  object Scheme {
    object Http extends Scheme { override def toString(): String = "Http" }
    object Https extends Scheme { override def toString(): String = "Https" }

    final class Other(val value: String) extends Scheme {
      override def equals(other: Any): Boolean = other match {
        case that: Other => this.value == that.value
        case _           => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "Other(" + value + ")"
    }

    object Other {
      def apply(value: String): Other = new Other(value)
      def unapply(arg: Other): Some[String] = Some(arg.value)
    }
  }

  @WitRecord
  final class DnsErrorPayload(val rcode: java.util.Optional[String],
      val infoCode: java.util.Optional[UShort]) {
    override def equals(other: Any): Boolean = other match {
      case that: DnsErrorPayload => this.rcode == that.rcode && this.infoCode == that.infoCode
      case _                     => false
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
    def apply(rcode: java.util.Optional[String],
        infoCode: java.util.Optional[UShort]): DnsErrorPayload = new DnsErrorPayload(rcode, infoCode)

    def unapply(
        arg: DnsErrorPayload): Some[(java.util.Optional[String], java.util.Optional[UShort])] = {
      Some((arg.rcode, arg.infoCode))
    }
  }

  @WitRecord
  final class TlsAlertReceivedPayload(val alertId: java.util.Optional[UByte],
      val alertMessage: java.util.Optional[String]) {
    override def equals(other: Any): Boolean = other match {
      case that: TlsAlertReceivedPayload =>
        this.alertId == that.alertId && this.alertMessage == that.alertMessage
      case _ => false
    }

    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + alertId.hashCode()
      result = 31 * result + alertMessage.hashCode()
      result
    }

    override def toString(): String =
      "TlsAlertReceivedPayload(" + alertId + ", " + alertMessage + ")"
  }

  object TlsAlertReceivedPayload {
    def apply(alertId: java.util.Optional[UByte],
        alertMessage: java.util.Optional[String]): TlsAlertReceivedPayload = {
      new TlsAlertReceivedPayload(alertId, alertMessage)
    }

    def unapply(arg: TlsAlertReceivedPayload): Some[(java.util.Optional[UByte],
        java.util.Optional[String])] = Some((arg.alertId, arg.alertMessage))
  }

  @WitRecord
  final class FieldSizePayload(val fieldName: java.util.Optional[String],
      val fieldSize: java.util.Optional[UInt]) {
    override def equals(other: Any): Boolean = other match {
      case that: FieldSizePayload =>
        this.fieldName == that.fieldName && this.fieldSize == that.fieldSize
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
    def apply(fieldName: java.util.Optional[String],
        fieldSize: java.util.Optional[UInt]): FieldSizePayload = {
      new FieldSizePayload(fieldName, fieldSize)
    }

    def unapply(
        arg: FieldSizePayload): Some[(java.util.Optional[String], java.util.Optional[UInt])] = {
      Some((arg.fieldName, arg.fieldSize))
    }
  }

  @WitVariant
  sealed trait ErrorCode

  object ErrorCode {
    object DnsTimeout extends ErrorCode { override def toString(): String = "DnsTimeout" }

    final class DnsError(val value: DnsErrorPayload) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: DnsError => this.value == that.value
        case _              => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "DnsError(" + value + ")"
    }

    object DnsError {
      def apply(value: DnsErrorPayload): DnsError = new DnsError(value)
      def unapply(arg: DnsError): Some[DnsErrorPayload] = Some(arg.value)
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

    object TlsProtocolError extends ErrorCode { override def toString(): String = "TlsProtocolError" }

    object TlsCertificateError extends ErrorCode {
      override def toString(): String = "TlsCertificateError"
    }

    final class TlsAlertReceived(val value: TlsAlertReceivedPayload) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: TlsAlertReceived => this.value == that.value
        case _                      => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "TlsAlertReceived(" + value + ")"
    }

    object TlsAlertReceived {
      def apply(value: TlsAlertReceivedPayload): TlsAlertReceived = new TlsAlertReceived(value)
      def unapply(arg: TlsAlertReceived): Some[TlsAlertReceivedPayload] = Some(arg.value)
    }

    object HttpRequestDenied extends ErrorCode {
      override def toString(): String = "HttpRequestDenied"
    }

    object HttpRequestLengthRequired extends ErrorCode {
      override def toString(): String = "HttpRequestLengthRequired"
    }

    final class HttpRequestBodySize(val value: java.util.Optional[ULong]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpRequestBodySize => this.value == that.value
        case _                         => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpRequestBodySize(" + value + ")"
    }

    object HttpRequestBodySize {
      def apply(value: java.util.Optional[ULong]): HttpRequestBodySize =
        new HttpRequestBodySize(value)

      def unapply(arg: HttpRequestBodySize): Some[java.util.Optional[ULong]] = Some(arg.value)
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

    final class HttpRequestHeaderSectionSize(val value: java.util.Optional[UInt])
        extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpRequestHeaderSectionSize => this.value == that.value
        case _                                  => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpRequestHeaderSectionSize(" + value + ")"
    }

    object HttpRequestHeaderSectionSize {
      def apply(value: java.util.Optional[UInt]): HttpRequestHeaderSectionSize =
        new HttpRequestHeaderSectionSize(value)

      def unapply(
          arg: HttpRequestHeaderSectionSize): Some[java.util.Optional[UInt]] = Some(arg.value)
    }

    final class HttpRequestHeaderSize(val value: java.util.Optional[FieldSizePayload])
        extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpRequestHeaderSize => this.value == that.value
        case _                           => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpRequestHeaderSize(" + value + ")"
    }

    object HttpRequestHeaderSize {
      def apply(value: java.util.Optional[FieldSizePayload]): HttpRequestHeaderSize =
        new HttpRequestHeaderSize(value)

      def unapply(
          arg: HttpRequestHeaderSize): Some[java.util.Optional[FieldSizePayload]] = Some(arg.value)
    }

    final class HttpRequestTrailerSectionSize(val value: java.util.Optional[UInt]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpRequestTrailerSectionSize => this.value == that.value
        case _                                   => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpRequestTrailerSectionSize(" + value + ")"
    }

    object HttpRequestTrailerSectionSize {
      def apply(value: java.util.Optional[UInt]): HttpRequestTrailerSectionSize =
        new HttpRequestTrailerSectionSize(value)

      def unapply(
          arg: HttpRequestTrailerSectionSize): Some[java.util.Optional[UInt]] = Some(arg.value)
    }

    final class HttpRequestTrailerSize(val value: FieldSizePayload) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpRequestTrailerSize => this.value == that.value
        case _                            => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpRequestTrailerSize(" + value + ")"
    }

    object HttpRequestTrailerSize {
      def apply(value: FieldSizePayload): HttpRequestTrailerSize = new HttpRequestTrailerSize(value)
      def unapply(arg: HttpRequestTrailerSize): Some[FieldSizePayload] = Some(arg.value)
    }

    object HttpResponseIncomplete extends ErrorCode {
      override def toString(): String = "HttpResponseIncomplete"
    }

    final class HttpResponseHeaderSectionSize(val value: java.util.Optional[UInt]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseHeaderSectionSize => this.value == that.value
        case _                                   => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpResponseHeaderSectionSize(" + value + ")"
    }

    object HttpResponseHeaderSectionSize {
      def apply(value: java.util.Optional[UInt]): HttpResponseHeaderSectionSize =
        new HttpResponseHeaderSectionSize(value)

      def unapply(
          arg: HttpResponseHeaderSectionSize): Some[java.util.Optional[UInt]] = Some(arg.value)
    }

    final class HttpResponseHeaderSize(val value: FieldSizePayload) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseHeaderSize => this.value == that.value
        case _                            => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpResponseHeaderSize(" + value + ")"
    }

    object HttpResponseHeaderSize {
      def apply(value: FieldSizePayload): HttpResponseHeaderSize = new HttpResponseHeaderSize(value)
      def unapply(arg: HttpResponseHeaderSize): Some[FieldSizePayload] = Some(arg.value)
    }

    final class HttpResponseBodySize(val value: java.util.Optional[ULong]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseBodySize => this.value == that.value
        case _                          => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpResponseBodySize(" + value + ")"
    }

    object HttpResponseBodySize {
      def apply(value: java.util.Optional[ULong]): HttpResponseBodySize =
        new HttpResponseBodySize(value)

      def unapply(arg: HttpResponseBodySize): Some[java.util.Optional[ULong]] = Some(arg.value)
    }

    final class HttpResponseTrailerSectionSize(val value: java.util.Optional[UInt])
        extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseTrailerSectionSize => this.value == that.value
        case _                                    => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpResponseTrailerSectionSize(" + value + ")"
    }

    object HttpResponseTrailerSectionSize {
      def apply(value: java.util.Optional[UInt]): HttpResponseTrailerSectionSize =
        new HttpResponseTrailerSectionSize(value)

      def unapply(
          arg: HttpResponseTrailerSectionSize): Some[java.util.Optional[UInt]] = Some(arg.value)
    }

    final class HttpResponseTrailerSize(val value: FieldSizePayload) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseTrailerSize => this.value == that.value
        case _                             => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpResponseTrailerSize(" + value + ")"
    }

    object HttpResponseTrailerSize {
      def apply(value: FieldSizePayload): HttpResponseTrailerSize =
        new HttpResponseTrailerSize(value)

      def unapply(arg: HttpResponseTrailerSize): Some[FieldSizePayload] = Some(arg.value)
    }

    final class HttpResponseTransferCoding(val value: java.util.Optional[String])
        extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseTransferCoding => this.value == that.value
        case _                                => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpResponseTransferCoding(" + value + ")"
    }

    object HttpResponseTransferCoding {
      def apply(value: java.util.Optional[String]): HttpResponseTransferCoding =
        new HttpResponseTransferCoding(value)

      def unapply(
          arg: HttpResponseTransferCoding): Some[java.util.Optional[String]] = Some(arg.value)
    }

    final class HttpResponseContentCoding(val value: java.util.Optional[String]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: HttpResponseContentCoding => this.value == that.value
        case _                               => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "HttpResponseContentCoding(" + value + ")"
    }

    object HttpResponseContentCoding {
      def apply(value: java.util.Optional[String]): HttpResponseContentCoding =
        new HttpResponseContentCoding(value)

      def unapply(arg: HttpResponseContentCoding): Some[java.util.Optional[String]] = Some(arg.value)
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

    object LoopDetected extends ErrorCode { override def toString(): String = "LoopDetected" }

    object ConfigurationError extends ErrorCode {
      override def toString(): String = "ConfigurationError"
    }

    final class InternalError(val value: java.util.Optional[String]) extends ErrorCode {
      override def equals(other: Any): Boolean = other match {
        case that: InternalError => this.value == that.value
        case _                   => false
      }

      override def hashCode(): Int = value.hashCode()
      override def toString(): String = "InternalError(" + value + ")"
    }

    object InternalError {
      def apply(value: java.util.Optional[String]): InternalError = new InternalError(value)
      def unapply(arg: InternalError): Some[java.util.Optional[String]] = Some(arg.value)
    }
  }

  @WitVariant
  sealed trait HeaderError

  object HeaderError {
    object InvalidSyntax extends HeaderError { override def toString(): String = "InvalidSyntax" }
    object Forbidden extends HeaderError { override def toString(): String = "Forbidden" }
    object Immutable extends HeaderError { override def toString(): String = "Immutable" }
  }

  type FieldKey = String
  type FieldValue = Array[UByte]
  type Headers = Fields
  type Trailers = Fields
  type StatusCode = UShort

  @WitResourceImport("wasi:http/types@0.2.0", "fields")
  final class Fields private () extends Object {
    @WitResourceMethod("get")
    def get(name: String): Array[Array[UByte]] = native

    @WitResourceMethod("has")
    def has(name: String): Boolean = native

    @WitResourceMethod("set")
    def set(name: String, value: Array[Array[UByte]]): Result[Unit, HeaderError] = native

    @WitResourceMethod("delete")
    def delete(name: String): Result[Unit, HeaderError] = native

    @WitResourceMethod("append")
    def append(name: String, value: Array[UByte]): Result[Unit, HeaderError] = native

    @WitResourceMethod("entries")
    def entries(): Array[Tuple2[String, Array[UByte]]] = native

    @WitResourceMethod("clone")
    def clone_(): Fields = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object Fields {
    @WitResourceConstructor
    def apply(): Fields = native

    @WitResourceStaticMethod("from-list")
    def fromList(entries: Array[Tuple2[String, Array[UByte]]]): Result[Fields, HeaderError] = native
  }

  @WitResourceImport("wasi:http/types@0.2.0", "incoming-request")
  final class IncomingRequest private () extends Object {
    @WitResourceMethod("method")
    def method(): Method = native

    @WitResourceMethod("path-with-query")
    def pathWithQuery(): java.util.Optional[String] = native

    @WitResourceMethod("scheme")
    def scheme(): java.util.Optional[Scheme] = native

    @WitResourceMethod("authority")
    def authority(): java.util.Optional[String] = native

    @WitResourceMethod("headers")
    def headers(): Headers = native

    @WitResourceMethod("consume")
    def consume(): Result[IncomingBody, Unit] = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object IncomingRequest {}

  @WitResourceImport("wasi:http/types@0.2.0", "outgoing-request")
  final class OutgoingRequest private () extends Object {
    @WitResourceMethod("body")
    def body(): Result[OutgoingBody, Unit] = native

    @WitResourceMethod("method")
    def method(): Method = native

    @WitResourceMethod("set-method")
    def setMethod(method: Method): Result[Unit, Unit] = native

    @WitResourceMethod("path-with-query")
    def pathWithQuery(): java.util.Optional[String] = native

    @WitResourceMethod("set-path-with-query")
    def setPathWithQuery(pathWithQuery: java.util.Optional[String]): Result[Unit, Unit] = native

    @WitResourceMethod("scheme")
    def scheme(): java.util.Optional[Scheme] = native

    @WitResourceMethod("set-scheme")
    def setScheme(scheme: java.util.Optional[Scheme]): Result[Unit, Unit] = native

    @WitResourceMethod("authority")
    def authority(): java.util.Optional[String] = native

    @WitResourceMethod("set-authority")
    def setAuthority(authority: java.util.Optional[String]): Result[Unit, Unit] = native

    @WitResourceMethod("headers")
    def headers(): Headers = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object OutgoingRequest {
    @WitResourceConstructor
    def apply(headers: Headers): OutgoingRequest = native
  }

  @WitResourceImport("wasi:http/types@0.2.0", "request-options")
  final class RequestOptions private () extends Object {
    @WitResourceMethod("connect-timeout")
    def connectTimeout(): java.util.Optional[ULong] = native

    @WitResourceMethod("set-connect-timeout")
    def setConnectTimeout(duration: java.util.Optional[ULong]): Result[Unit, Unit] = native

    @WitResourceMethod("first-byte-timeout")
    def firstByteTimeout(): java.util.Optional[ULong] = native

    @WitResourceMethod("set-first-byte-timeout")
    def setFirstByteTimeout(duration: java.util.Optional[ULong]): Result[Unit, Unit] = native

    @WitResourceMethod("between-bytes-timeout")
    def betweenBytesTimeout(): java.util.Optional[ULong] = native

    @WitResourceMethod("set-between-bytes-timeout")
    def setBetweenBytesTimeout(duration: java.util.Optional[ULong]): Result[Unit, Unit] = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object RequestOptions {
    @WitResourceConstructor
    def apply(): RequestOptions = native
  }

  @WitResourceImport("wasi:http/types@0.2.0", "response-outparam")
  final class ResponseOutparam private () extends Object {
    @WitResourceDrop
    def close(): Unit = native
  }

  object ResponseOutparam {
    @WitResourceStaticMethod("set")
    def set(param: ResponseOutparam, response: Result[OutgoingResponse, ErrorCode]): Unit = native
  }

  @WitResourceImport("wasi:http/types@0.2.0", "incoming-response")
  final class IncomingResponse private () extends Object {
    @WitResourceMethod("status")
    def status(): UShort = native

    @WitResourceMethod("headers")
    def headers(): Headers = native

    @WitResourceMethod("consume")
    def consume(): Result[IncomingBody, Unit] = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object IncomingResponse {}

  @WitResourceImport("wasi:http/types@0.2.0", "incoming-body")
  final class IncomingBody private () extends Object {
    @WitResourceMethod("stream")
    def stream(): Result[InputStream, Unit] = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object IncomingBody {
    @WitResourceStaticMethod("finish")
    def finish(`this`: IncomingBody): FutureTrailers = native
  }

  @WitResourceImport("wasi:http/types@0.2.0", "future-trailers")
  final class FutureTrailers private () extends Object {
    @WitResourceMethod("subscribe")
    def subscribe(): Pollable = native

    @WitResourceMethod("get")
    def get(): java.util.Optional[Result[Result[java.util.Optional[Trailers], ErrorCode], Unit]] =
      native

    @WitResourceDrop
    def close(): Unit = native
  }

  object FutureTrailers {}

  @WitResourceImport("wasi:http/types@0.2.0", "outgoing-response")
  final class OutgoingResponse private () extends Object {
    @WitResourceMethod("status-code")
    def statusCode(): UShort = native

    @WitResourceMethod("set-status-code")
    def setStatusCode(statusCode: UShort): Result[Unit, Unit] = native

    @WitResourceMethod("headers")
    def headers(): Headers = native

    @WitResourceMethod("body")
    def body(): Result[OutgoingBody, Unit] = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object OutgoingResponse {
    @WitResourceConstructor
    def apply(headers: Headers): OutgoingResponse = native
  }

  @WitResourceImport("wasi:http/types@0.2.0", "outgoing-body")
  final class OutgoingBody private () extends Object {
    @WitResourceMethod("write")
    def write(): Result[OutputStream, Unit] = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object OutgoingBody {
    @WitResourceStaticMethod("finish")
    def finish(`this`: OutgoingBody, trailers: java.util.Optional[Trailers]): Result[Unit,
        ErrorCode] = native
  }

  @WitResourceImport("wasi:http/types@0.2.0", "future-incoming-response")
  final class FutureIncomingResponse private () extends Object {
    @WitResourceMethod("subscribe")
    def subscribe(): Pollable = native

    @WitResourceMethod("get")
    def get(): java.util.Optional[Result[Result[IncomingResponse, ErrorCode], Unit]] = native

    @WitResourceDrop
    def close(): Unit = native
  }

  object FutureIncomingResponse {}

  @WitImport("wasi:http/types@0.2.0", "http-error-code")
  def httpErrorCode(err: IoError): java.util.Optional[ErrorCode] = native

}
