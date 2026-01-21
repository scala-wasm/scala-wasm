package scala.scalajs.wasi.random

package object random {

  // Functions
  /** Return `len` cryptographically-secure random or pseudo-random bytes.
   *
   *  This function must produce data at least as cryptographically secure and
   *  fast as an adequately seeded cryptographically-secure pseudo-random
   *  number generator (CSPRNG). It must not block, from the perspective of
   *  the calling program, under any circumstances, including on the first
   *  request and on requests for numbers of bytes. The returned data must
   *  always be unpredictable.
   *
   *  This function must always return fresh data. Deterministic environments
   *  must omit this function, rather than implementing it with deterministic
   *  data.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:random/random@0.2.0", "get-random-bytes")
  def getRandomBytes(
      len: scala.scalajs.wit.unsigned.ULong): Array[scala.scalajs.wit.unsigned.UByte] = {
    scala.scalajs.wit.native
  }

  /** Return a cryptographically-secure random or pseudo-random `u64` value.
   *
   *  This function returns the same type of data as `get-random-bytes`,
   *  represented as a `u64`.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:random/random@0.2.0", "get-random-u64")
  def getRandomU64(): scala.scalajs.wit.unsigned.ULong = scala.scalajs.wit.native

}
