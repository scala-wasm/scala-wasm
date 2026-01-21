package scala.scalajs.wasi.sockets

package object network {

  // Type definitions
  /** Error codes.
   *
   *  In theory, every API can return any error code.
   *  In practice, API's typically only return the errors documented per API
   *  combined with a couple of errors that are always possible:
   *  - `unknown`
   *  - `access-denied`
   *  - `not-supported`
   *  - `out-of-memory`
   *  - `concurrency-conflict`
   *
   *  See each individual API for what the POSIX equivalents are. They sometimes differ per API.
   */
  @scala.scalajs.wit.annotation.WitVariant
  sealed trait ErrorCode

  object ErrorCode {
    object Unknown extends ErrorCode {
      override def toString(): String = "Unknown"
    }

    object AccessDenied extends ErrorCode {
      override def toString(): String = "AccessDenied"
    }

    object NotSupported extends ErrorCode {
      override def toString(): String = "NotSupported"
    }

    object InvalidArgument extends ErrorCode {
      override def toString(): String = "InvalidArgument"
    }

    object OutOfMemory extends ErrorCode {
      override def toString(): String = "OutOfMemory"
    }

    object Timeout extends ErrorCode {
      override def toString(): String = "Timeout"
    }

    object ConcurrencyConflict extends ErrorCode {
      override def toString(): String = "ConcurrencyConflict"
    }

    object NotInProgress extends ErrorCode {
      override def toString(): String = "NotInProgress"
    }

    object WouldBlock extends ErrorCode {
      override def toString(): String = "WouldBlock"
    }

    object InvalidState extends ErrorCode {
      override def toString(): String = "InvalidState"
    }

    object NewSocketLimit extends ErrorCode {
      override def toString(): String = "NewSocketLimit"
    }

    object AddressNotBindable extends ErrorCode {
      override def toString(): String = "AddressNotBindable"
    }

    object AddressInUse extends ErrorCode {
      override def toString(): String = "AddressInUse"
    }

    object RemoteUnreachable extends ErrorCode {
      override def toString(): String = "RemoteUnreachable"
    }

    object ConnectionRefused extends ErrorCode {
      override def toString(): String = "ConnectionRefused"
    }

    object ConnectionReset extends ErrorCode {
      override def toString(): String = "ConnectionReset"
    }

    object ConnectionAborted extends ErrorCode {
      override def toString(): String = "ConnectionAborted"
    }

    object DatagramTooLarge extends ErrorCode {
      override def toString(): String = "DatagramTooLarge"
    }

    object NameUnresolvable extends ErrorCode {
      override def toString(): String = "NameUnresolvable"
    }

    object TemporaryResolverFailure extends ErrorCode {
      override def toString(): String = "TemporaryResolverFailure"
    }

    object PermanentResolverFailure extends ErrorCode {
      override def toString(): String = "PermanentResolverFailure"
    }
  }

  @scala.scalajs.wit.annotation.WitVariant
  sealed trait IpAddressFamily

  object IpAddressFamily {
    object Ipv4 extends IpAddressFamily {
      override def toString(): String = "Ipv4"
    }

    object Ipv6 extends IpAddressFamily {
      override def toString(): String = "Ipv6"
    }
  }

  type Ipv4Address =
    scala.scalajs.wit.Tuple4[scala.scalajs.wit.unsigned.UByte, scala.scalajs.wit.unsigned.UByte,
        scala.scalajs.wit.unsigned.UByte, scala.scalajs.wit.unsigned.UByte]

  type Ipv6Address =
    scala.scalajs.wit.Tuple8[scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
        scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
        scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
        scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort]

  @scala.scalajs.wit.annotation.WitVariant
  sealed trait IpAddress

  object IpAddress {
    final class Ipv4(val value: scala.scalajs.wit.Tuple4[scala.scalajs.wit.unsigned.UByte,
            scala.scalajs.wit.unsigned.UByte, scala.scalajs.wit.unsigned.UByte,
            scala.scalajs.wit.unsigned.UByte])
        extends IpAddress {
      override def equals(other: Any): Boolean = other match {
        case that: Ipv4 => this.value == that.value
        case _          => false
      }

      override def hashCode(): Int =
        value.hashCode()

      override def toString(): String = "Ipv4(" + value + ")"
    }

    object Ipv4 {
      def apply(value: scala.scalajs.wit.Tuple4[scala.scalajs.wit.unsigned.UByte,
              scala.scalajs.wit.unsigned.UByte, scala.scalajs.wit.unsigned.UByte,
              scala.scalajs.wit.unsigned.UByte]): Ipv4 = new Ipv4(value)
    }

    final class Ipv6(val value: scala.scalajs.wit.Tuple8[scala.scalajs.wit.unsigned.UShort,
            scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
            scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
            scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
            scala.scalajs.wit.unsigned.UShort])
        extends IpAddress {
      override def equals(other: Any): Boolean = other match {
        case that: Ipv6 => this.value == that.value
        case _          => false
      }

      override def hashCode(): Int =
        value.hashCode()

      override def toString(): String = "Ipv6(" + value + ")"
    }

    object Ipv6 {
      def apply(value: scala.scalajs.wit.Tuple8[scala.scalajs.wit.unsigned.UShort,
              scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
              scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
              scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
              scala.scalajs.wit.unsigned.UShort]): Ipv6 = new Ipv6(value)
    }
  }

  @scala.scalajs.wit.annotation.WitRecord
  final class Ipv4SocketAddress(val port: scala.scalajs.wit.unsigned.UShort,
      val address: scala.scalajs.wit.Tuple4[scala.scalajs.wit.unsigned.UByte,
          scala.scalajs.wit.unsigned.UByte, scala.scalajs.wit.unsigned.UByte,
          scala.scalajs.wit.unsigned.UByte]) {
    override def equals(other: Any): Boolean = other match {
      case that: Ipv4SocketAddress => this.port == that.port && this.address == that.address
      case _                       => false
    }

    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + port.hashCode()
      result = 31 * result + address.hashCode()
      result
    }

    override def toString(): String = "Ipv4SocketAddress(" + port + ", " + address + ")"
  }

  object Ipv4SocketAddress {
    def apply(port: scala.scalajs.wit.unsigned.UShort,
        address: scala.scalajs.wit.Tuple4[scala.scalajs.wit.unsigned.UByte,
            scala.scalajs.wit.unsigned.UByte, scala.scalajs.wit.unsigned.UByte,
            scala.scalajs.wit.unsigned.UByte]): Ipv4SocketAddress = {
      new Ipv4SocketAddress(port, address)
    }
  }

  @scala.scalajs.wit.annotation.WitRecord
  final class Ipv6SocketAddress(val port: scala.scalajs.wit.unsigned.UShort,
      val flowInfo: scala.scalajs.wit.unsigned.UInt,
      val address: scala.scalajs.wit.Tuple8[scala.scalajs.wit.unsigned.UShort,
          scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
          scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
          scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
          scala.scalajs.wit.unsigned.UShort],
      val scopeId: scala.scalajs.wit.unsigned.UInt) {
    override def equals(other: Any): Boolean = other match {
      case that: Ipv6SocketAddress =>
        this.port == that.port && this.flowInfo == that.flowInfo && this.address == that.address && this.scopeId == that.scopeId
      case _ => false
    }

    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + port.hashCode()
      result = 31 * result + flowInfo.hashCode()
      result = 31 * result + address.hashCode()
      result = 31 * result + scopeId.hashCode()
      result
    }

    override def toString(): String =
      "Ipv6SocketAddress(" + port + ", " + flowInfo + ", " + address + ", " + scopeId + ")"
  }

  object Ipv6SocketAddress {
    def apply(port: scala.scalajs.wit.unsigned.UShort, flowInfo: scala.scalajs.wit.unsigned.UInt,
        address: scala.scalajs.wit.Tuple8[scala.scalajs.wit.unsigned.UShort,
            scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
            scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
            scala.scalajs.wit.unsigned.UShort, scala.scalajs.wit.unsigned.UShort,
            scala.scalajs.wit.unsigned.UShort],
        scopeId: scala.scalajs.wit.unsigned.UInt): Ipv6SocketAddress = {
      new Ipv6SocketAddress(port, flowInfo, address, scopeId)
    }
  }

  @scala.scalajs.wit.annotation.WitVariant
  sealed trait IpSocketAddress

  object IpSocketAddress {
    final class Ipv4(val value: Ipv4SocketAddress) extends IpSocketAddress {
      override def equals(other: Any): Boolean = other match {
        case that: Ipv4 => this.value == that.value
        case _          => false
      }

      override def hashCode(): Int =
        value.hashCode()

      override def toString(): String = "Ipv4(" + value + ")"
    }

    object Ipv4 {
      def apply(value: Ipv4SocketAddress): Ipv4 = new Ipv4(value)
    }

    final class Ipv6(val value: Ipv6SocketAddress) extends IpSocketAddress {
      override def equals(other: Any): Boolean = other match {
        case that: Ipv6 => this.value == that.value
        case _          => false
      }

      override def hashCode(): Int =
        value.hashCode()

      override def toString(): String = "Ipv6(" + value + ")"
    }

    object Ipv6 {
      def apply(value: Ipv6SocketAddress): Ipv6 = new Ipv6(value)
    }
  }

  // Resources
  /** An opaque resource that represents access to (a subset of) the network.
   *  This enables context-based security for networking.
   *  There is no need for this to map 1:1 to a physical network interface.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:sockets/network@0.2.0", "network")
  trait Network {
    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }

  object Network {}

}
