/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalajs.testing.bridge

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

import scala.scalajs.WitUtils.toEither
import scala.scalajs.wit.unsigned.{UByte, UShort}

import scala.scalajs.wasi.cli.environment
import scala.scalajs.wasi.io.streams.{InputStream, OutputStream, StreamError}
import scala.scalajs.wasi.sockets.instance_network
import scala.scalajs.wasi.sockets.network
import scala.scalajs.wasi.sockets.tcp_create_socket

private[bridge] object WasiDirectRpcTransport {
  private val RpcHostEnv = "SCALAJS_TEST_RPC_HOST"
  private val RpcPortEnv = "SCALAJS_TEST_RPC_PORT"
  private val RpcNonceEnv = "SCALAJS_TEST_RPC_NONCE"
  private val RpcProtocolEnv = "SCALAJS_TEST_RPC_PROTOCOL"
  private val RpcProtocolVersion = "1"

  private var inStream: Option[InputStream] = None
  private var outStream: Option[OutputStream] = None
  private var connected: Boolean = false
  private val MaxBlockingWriteAndFlushBytes = 4096

  def init(): Unit =
    ensureConnected()

  def send(msg: String): Unit = {
    ensureConnected()
    writeFrame(msg)
  }

  /** Returns null when the transport is closed. */
  def pollNextMessage(): String = {
    ensureConnected()
    readFrameBytes().map(bytes => new String(toByteArray(bytes), StandardCharsets.UTF_8)).orNull
  }

  private def ensureConnected(): Unit = {
    if (!connected) {
      connectAndHandshake()
      connected = true
    }
  }

  private def connectAndHandshake(): Unit = {
    val env = environment.getEnvironment().iterator.map(kv => kv._1 -> kv._2).toMap

    val host = env.getOrElse(RpcHostEnv,
        throw new IllegalStateException(s"Missing env var $RpcHostEnv"))
    val port = env.getOrElse(RpcPortEnv,
        throw new IllegalStateException(s"Missing env var $RpcPortEnv")).toInt
    val nonce = env.getOrElse(RpcNonceEnv,
        throw new IllegalStateException(s"Missing env var $RpcNonceEnv"))
    val protocol = env.getOrElse(RpcProtocolEnv,
        throw new IllegalStateException(s"Missing env var $RpcProtocolEnv"))
    if (protocol != RpcProtocolVersion) {
      throw new IllegalStateException(
          s"Unsupported RPC protocol version: $protocol (expected $RpcProtocolVersion)")
    }

    val hostBytes = parseIpv4(host)
    val address = network.IpSocketAddress.Ipv4(
        network.Ipv4SocketAddress(
            port.toShort.asInstanceOf[UShort],
            (
              hostBytes(0).toByte.asInstanceOf[UByte],
              hostBytes(1).toByte.asInstanceOf[UByte],
              hostBytes(2).toByte.asInstanceOf[UByte],
              hostBytes(3).toByte.asInstanceOf[UByte]
            )))

    val socket = toEither(tcp_create_socket.createTcpSocket(network.IpAddressFamily.Ipv4)).fold(
      err => throw new IllegalStateException(s"createTcpSocket failed: $err"),
      identity
    )
    val net = instance_network.instanceNetwork()

    toEither(socket.startConnect(net, address)).left.foreach { err =>
      throw new IllegalStateException(s"startConnect failed: $err")
    }

    val streams = {
      var result = toEither(socket.finishConnect())
      while (result.left.toOption.contains(network.ErrorCode.WouldBlock)) {
        val pollable = socket.subscribe()
        try pollable.block()
        finally pollable.close()
        result = toEither(socket.finishConnect())
      }
      result.fold(
        err => throw new IllegalStateException(s"finishConnect failed: $err"),
        identity
      )
    }

    inStream = Some(streams._1)
    outStream = Some(streams._2)

    writeFrame(nonce)
    val echoed = readFrameBytes().map(
        bytes => new String(toByteArray(bytes), StandardCharsets.UTF_8)).getOrElse {
      throw new IllegalStateException("EOF while waiting nonce echo")
    }
    if (echoed != nonce) {
      throw new IllegalStateException("Nonce mismatch")
    }
  }

  private def parseIpv4(host: String): Array[Int] = {
    val parts = host.split("\\.")
    if (parts.length != 4) {
      throw new IllegalArgumentException(s"Only IPv4 host is supported, got: $host")
    }
    parts.map { p =>
      val v = p.toInt
      if (v < 0 || v > 255) {
        throw new IllegalArgumentException(s"Invalid IPv4 octet in $host: $p")
      }
      v
    }
  }

  private def writeFrame(msg: String): Unit = {
    val payload = msg.getBytes(StandardCharsets.UTF_8)
    val header = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(payload.length).array()
    val bytes = new Array[Byte](header.length + payload.length)
    System.arraycopy(header, 0, bytes, 0, header.length)
    System.arraycopy(payload, 0, bytes, header.length, payload.length)

    val ubytes = bytes.map(_.asInstanceOf[UByte])
    val out = outStream.getOrElse {
      throw new IllegalStateException("output stream is not connected")
    }
    var offset = 0
    while (offset < ubytes.length) {
      val len = Math.min(MaxBlockingWriteAndFlushBytes, ubytes.length - offset)
      val chunk = ubytes.slice(offset, offset + len)
      toEither(out.blockingWriteAndFlush(chunk)).left.foreach { err =>
        throw new IllegalStateException(s"write failed: $err")
      }
      offset += len
    }
  }

  private def readFrameBytes(): Option[Array[UByte]] = {
    val header = readFully(4)
    if (header.isEmpty) return None

    val len = ByteBuffer.wrap(toByteArray(header.get)).order(ByteOrder.BIG_ENDIAN).getInt
    if (len < 0) {
      throw new IllegalStateException(s"negative frame length: $len")
    }

    readFully(len).orElse {
      throw new IllegalStateException("Unexpected EOF in frame payload")
    }
  }

  private def readFully(len: Int): Option[Array[UByte]] = {
    val out = new Array[UByte](len)
    var off = 0
    while (off < len) {
      val in = inStream.getOrElse {
        throw new IllegalStateException("input stream is not connected")
      }
      val chunk = toEither(in.blockingRead((len - off).toLong)).fold(
        {
          case StreamError.Closed if off == 0 => return None
          case StreamError.Closed             => return None
          case err => throw new IllegalStateException(s"read failed: $err")
        },
        identity
      )
      if (chunk.isEmpty) {
        return None
      }

      var i = 0
      while (i < chunk.length && off < len) {
        out(off) = chunk(i)
        off += 1
        i += 1
      }
    }
    Some(out)
  }

  private def toByteArray(arr: Array[UByte]): Array[Byte] =
    arr.map(_.toByte)
}
