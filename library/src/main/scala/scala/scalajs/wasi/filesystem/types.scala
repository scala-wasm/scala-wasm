package scala.scalajs.wasi.filesystem

package object types {

  // Type definitions
  type InputStream = scala.scalajs.wasi.io.streams.InputStream

  type OutputStream = scala.scalajs.wasi.io.streams.OutputStream

  type Error = scala.scalajs.wasi.io.error.Error

  type Datetime = scala.scalajs.wasi.clocks.wall_clock.Datetime

  type Filesize = scala.scalajs.wit.unsigned.ULong

  /** The type of a filesystem object referenced by a descriptor.
   *
   *  Note: This was called `filetype` in earlier versions of WASI.
   */
  @scala.scalajs.wit.annotation.WitVariant
  sealed trait DescriptorType

  object DescriptorType {
    object Unknown extends DescriptorType {
      override def toString(): String = "Unknown"
    }

    object BlockDevice extends DescriptorType {
      override def toString(): String = "BlockDevice"
    }

    object CharacterDevice extends DescriptorType {
      override def toString(): String = "CharacterDevice"
    }

    object Directory extends DescriptorType {
      override def toString(): String = "Directory"
    }

    object Fifo extends DescriptorType {
      override def toString(): String = "Fifo"
    }

    object SymbolicLink extends DescriptorType {
      override def toString(): String = "SymbolicLink"
    }

    object RegularFile extends DescriptorType {
      override def toString(): String = "RegularFile"
    }

    object Socket extends DescriptorType {
      override def toString(): String = "Socket"
    }
  }

  /** Descriptor flags.
   *
   *  Note: This was called `fdflags` in earlier versions of WASI.
   */
  @scala.scalajs.wit.annotation.WitFlags(6)
  final class DescriptorFlags(val value: Int) {
    def |(other: DescriptorFlags): DescriptorFlags = new DescriptorFlags(value | other.value)
    def &(other: DescriptorFlags): DescriptorFlags = new DescriptorFlags(value & other.value)
    def ^(other: DescriptorFlags): DescriptorFlags = new DescriptorFlags(value ^ other.value)
    def unary_~ : DescriptorFlags = new DescriptorFlags(~value)
    def contains(other: DescriptorFlags): Boolean = (value & other.value) == other.value

    override def equals(other: Any): Boolean = other match {
      case that: DescriptorFlags => this.value == that.value
      case _                     => false
    }

    override def hashCode(): Int =
      value.hashCode()

    override def toString(): String = "DescriptorFlags(" + value + ")"
  }

  object DescriptorFlags {
    def apply(value: Int): DescriptorFlags = new DescriptorFlags(value)
    val read = new DescriptorFlags(1 << 0)
    val write = new DescriptorFlags(1 << 1)
    val fileIntegritySync = new DescriptorFlags(1 << 2)
    val dataIntegritySync = new DescriptorFlags(1 << 3)
    val requestedWriteSync = new DescriptorFlags(1 << 4)
    val mutateDirectory = new DescriptorFlags(1 << 5)
  }

  /** Flags determining the method of how paths are resolved. */
  @scala.scalajs.wit.annotation.WitFlags(1)
  final class PathFlags(val value: Int) {
    def |(other: PathFlags): PathFlags = new PathFlags(value | other.value)
    def &(other: PathFlags): PathFlags = new PathFlags(value & other.value)
    def ^(other: PathFlags): PathFlags = new PathFlags(value ^ other.value)
    def unary_~ : PathFlags = new PathFlags(~value)
    def contains(other: PathFlags): Boolean = (value & other.value) == other.value

    override def equals(other: Any): Boolean = other match {
      case that: PathFlags => this.value == that.value
      case _               => false
    }

    override def hashCode(): Int =
      value.hashCode()

    override def toString(): String = "PathFlags(" + value + ")"
  }

  object PathFlags {
    def apply(value: Int): PathFlags = new PathFlags(value)
    val symlinkFollow = new PathFlags(1 << 0)
  }

  /** Open flags used by `open-at`. */
  @scala.scalajs.wit.annotation.WitFlags(4)
  final class OpenFlags(val value: Int) {
    def |(other: OpenFlags): OpenFlags = new OpenFlags(value | other.value)
    def &(other: OpenFlags): OpenFlags = new OpenFlags(value & other.value)
    def ^(other: OpenFlags): OpenFlags = new OpenFlags(value ^ other.value)
    def unary_~ : OpenFlags = new OpenFlags(~value)
    def contains(other: OpenFlags): Boolean = (value & other.value) == other.value

    override def equals(other: Any): Boolean = other match {
      case that: OpenFlags => this.value == that.value
      case _               => false
    }

    override def hashCode(): Int =
      value.hashCode()

    override def toString(): String = "OpenFlags(" + value + ")"
  }

  object OpenFlags {
    def apply(value: Int): OpenFlags = new OpenFlags(value)
    val create = new OpenFlags(1 << 0)
    val directory = new OpenFlags(1 << 1)
    val exclusive = new OpenFlags(1 << 2)
    val truncate = new OpenFlags(1 << 3)
  }

  type LinkCount = scala.scalajs.wit.unsigned.ULong

  /** File attributes.
   *
   *  Note: This was called `filestat` in earlier versions of WASI.
   */
  @scala.scalajs.wit.annotation.WitRecord
  final class DescriptorStat(val `type`: DescriptorType,
      val linkCount: scala.scalajs.wit.unsigned.ULong, val size: scala.scalajs.wit.unsigned.ULong,
      val dataAccessTimestamp: java.util.Optional[scala.scalajs.wasi.clocks.wall_clock.Datetime],
      val dataModificationTimestamp: java.util.Optional[
          scala.scalajs.wasi.clocks.wall_clock.Datetime],
      val statusChangeTimestamp: java.util.Optional[
          scala.scalajs.wasi.clocks.wall_clock.Datetime]) {
    override def equals(other: Any): Boolean = other match {
      case that: DescriptorStat =>
        this.`type` == that.`type` && this.linkCount == that.linkCount && this.size == that.size && this.dataAccessTimestamp == that.dataAccessTimestamp && this.dataModificationTimestamp == that.dataModificationTimestamp && this.statusChangeTimestamp == that.statusChangeTimestamp
      case _ => false
    }

    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + `type`.hashCode()
      result = 31 * result + linkCount.hashCode()
      result = 31 * result + size.hashCode()
      result = 31 * result + dataAccessTimestamp.hashCode()
      result = 31 * result + dataModificationTimestamp.hashCode()
      result = 31 * result + statusChangeTimestamp.hashCode()
      result
    }

    override def toString(): String =
      "DescriptorStat(" + `type` + ", " + linkCount + ", " + size + ", " + dataAccessTimestamp + ", " + dataModificationTimestamp + ", " + statusChangeTimestamp + ")"
  }

  object DescriptorStat {
    def apply(`type`: DescriptorType, linkCount: scala.scalajs.wit.unsigned.ULong,
        size: scala.scalajs.wit.unsigned.ULong,
        dataAccessTimestamp: java.util.Optional[scala.scalajs.wasi.clocks.wall_clock.Datetime],
        dataModificationTimestamp: java.util.Optional[scala.scalajs.wasi.clocks.wall_clock.Datetime],
        statusChangeTimestamp: java.util.Optional[
            scala.scalajs.wasi.clocks.wall_clock.Datetime]): DescriptorStat = {
      new DescriptorStat(`type`, linkCount, size, dataAccessTimestamp, dataModificationTimestamp,
          statusChangeTimestamp)
    }
  }

  /** When setting a timestamp, this gives the value to set it to. */
  @scala.scalajs.wit.annotation.WitVariant
  sealed trait NewTimestamp

  object NewTimestamp {
    object NoChange extends NewTimestamp {
      override def toString(): String = "NoChange"
    }

    object Now extends NewTimestamp {
      override def toString(): String = "Now"
    }

    final class Timestamp(val value: scala.scalajs.wasi.clocks.wall_clock.Datetime)
        extends NewTimestamp {
      override def equals(other: Any): Boolean = other match {
        case that: Timestamp => this.value == that.value
        case _               => false
      }

      override def hashCode(): Int =
        value.hashCode()

      override def toString(): String = "Timestamp(" + value + ")"
    }

    object Timestamp {
      def apply(value: scala.scalajs.wasi.clocks.wall_clock.Datetime): Timestamp =
        new Timestamp(value)
    }
  }

  /** A directory entry. */
  @scala.scalajs.wit.annotation.WitRecord
  final class DirectoryEntry(val `type`: DescriptorType, val name: String) {
    override def equals(other: Any): Boolean = other match {
      case that: DirectoryEntry => this.`type` == that.`type` && this.name == that.name
      case _                    => false
    }

    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + `type`.hashCode()
      result = 31 * result + name.hashCode()
      result
    }

    override def toString(): String = "DirectoryEntry(" + `type` + ", " + name + ")"
  }

  object DirectoryEntry {
    def apply(`type`: DescriptorType, name: String): DirectoryEntry =
      new DirectoryEntry(`type`, name)
  }

  /** Error codes returned by functions, similar to `errno` in POSIX.
   *  Not all of these error codes are returned by the functions provided by this
   *  API; some are used in higher-level library layers, and others are provided
   *  merely for alignment with POSIX.
   */
  @scala.scalajs.wit.annotation.WitVariant
  sealed trait ErrorCode

  object ErrorCode {
    object Access extends ErrorCode {
      override def toString(): String = "Access"
    }

    object WouldBlock extends ErrorCode {
      override def toString(): String = "WouldBlock"
    }

    object Already extends ErrorCode {
      override def toString(): String = "Already"
    }

    object BadDescriptor extends ErrorCode {
      override def toString(): String = "BadDescriptor"
    }

    object Busy extends ErrorCode {
      override def toString(): String = "Busy"
    }

    object Deadlock extends ErrorCode {
      override def toString(): String = "Deadlock"
    }

    object Quota extends ErrorCode {
      override def toString(): String = "Quota"
    }

    object Exist extends ErrorCode {
      override def toString(): String = "Exist"
    }

    object FileTooLarge extends ErrorCode {
      override def toString(): String = "FileTooLarge"
    }

    object IllegalByteSequence extends ErrorCode {
      override def toString(): String = "IllegalByteSequence"
    }

    object InProgress extends ErrorCode {
      override def toString(): String = "InProgress"
    }

    object Interrupted extends ErrorCode {
      override def toString(): String = "Interrupted"
    }

    object Invalid extends ErrorCode {
      override def toString(): String = "Invalid"
    }

    object Io extends ErrorCode {
      override def toString(): String = "Io"
    }

    object IsDirectory extends ErrorCode {
      override def toString(): String = "IsDirectory"
    }

    object Loop extends ErrorCode {
      override def toString(): String = "Loop"
    }

    object TooManyLinks extends ErrorCode {
      override def toString(): String = "TooManyLinks"
    }

    object MessageSize extends ErrorCode {
      override def toString(): String = "MessageSize"
    }

    object NameTooLong extends ErrorCode {
      override def toString(): String = "NameTooLong"
    }

    object NoDevice extends ErrorCode {
      override def toString(): String = "NoDevice"
    }

    object NoEntry extends ErrorCode {
      override def toString(): String = "NoEntry"
    }

    object NoLock extends ErrorCode {
      override def toString(): String = "NoLock"
    }

    object InsufficientMemory extends ErrorCode {
      override def toString(): String = "InsufficientMemory"
    }

    object InsufficientSpace extends ErrorCode {
      override def toString(): String = "InsufficientSpace"
    }

    object NotDirectory extends ErrorCode {
      override def toString(): String = "NotDirectory"
    }

    object NotEmpty extends ErrorCode {
      override def toString(): String = "NotEmpty"
    }

    object NotRecoverable extends ErrorCode {
      override def toString(): String = "NotRecoverable"
    }

    object Unsupported extends ErrorCode {
      override def toString(): String = "Unsupported"
    }

    object NoTty extends ErrorCode {
      override def toString(): String = "NoTty"
    }

    object NoSuchDevice extends ErrorCode {
      override def toString(): String = "NoSuchDevice"
    }

    object Overflow extends ErrorCode {
      override def toString(): String = "Overflow"
    }

    object NotPermitted extends ErrorCode {
      override def toString(): String = "NotPermitted"
    }

    object Pipe extends ErrorCode {
      override def toString(): String = "Pipe"
    }

    object ReadOnly extends ErrorCode {
      override def toString(): String = "ReadOnly"
    }

    object InvalidSeek extends ErrorCode {
      override def toString(): String = "InvalidSeek"
    }

    object TextFileBusy extends ErrorCode {
      override def toString(): String = "TextFileBusy"
    }

    object CrossDevice extends ErrorCode {
      override def toString(): String = "CrossDevice"
    }
  }

  /** File or memory access pattern advisory information. */
  @scala.scalajs.wit.annotation.WitVariant
  sealed trait Advice

  object Advice {
    object Normal extends Advice {
      override def toString(): String = "Normal"
    }

    object Sequential extends Advice {
      override def toString(): String = "Sequential"
    }

    object Random extends Advice {
      override def toString(): String = "Random"
    }

    object WillNeed extends Advice {
      override def toString(): String = "WillNeed"
    }

    object DontNeed extends Advice {
      override def toString(): String = "DontNeed"
    }

    object NoReuse extends Advice {
      override def toString(): String = "NoReuse"
    }
  }

  /** A 128-bit hash value, split into parts because wasm doesn't have a
   *  128-bit integer type.
   */
  @scala.scalajs.wit.annotation.WitRecord
  final class MetadataHashValue(val lower: scala.scalajs.wit.unsigned.ULong,
      val upper: scala.scalajs.wit.unsigned.ULong) {
    override def equals(other: Any): Boolean = other match {
      case that: MetadataHashValue => this.lower == that.lower && this.upper == that.upper
      case _                       => false
    }

    override def hashCode(): Int = {
      var result = 1
      result = 31 * result + lower.hashCode()
      result = 31 * result + upper.hashCode()
      result
    }

    override def toString(): String = "MetadataHashValue(" + lower + ", " + upper + ")"
  }

  object MetadataHashValue {
    def apply(lower: scala.scalajs.wit.unsigned.ULong,
        upper: scala.scalajs.wit.unsigned.ULong): MetadataHashValue = {
      new MetadataHashValue(lower, upper)
    }
  }

  // Resources
  /** A descriptor is a reference to a filesystem object, which may be a file,
   *  directory, named pipe, special file, or other object on which filesystem
   *  calls may be made.
   */
  @scala.scalajs.wit.annotation.WitResourceImport("wasi:filesystem/types@0.2.0", "descriptor")
  trait Descriptor {

    /** Return a stream for reading from a file, if available.
     *
     *  May fail with an error-code describing why the file cannot be read.
     *
     *  Multiple read, write, and append streams may be active on the same open
     *  file and they do not interfere with each other.
     *
     *  Note: This allows using `read-stream`, which is similar to `read` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("read-via-stream")
    def readViaStream(
        offset: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[InputStream,
        ErrorCode] = scala.scalajs.wit.native

    /** Return a stream for writing to a file, if available.
     *
     *  May fail with an error-code describing why the file cannot be written.
     *
     *  Note: This allows using `write-stream`, which is similar to `write` in
     *  POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("write-via-stream")
    def writeViaStream(
        offset: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[OutputStream,
        ErrorCode] = scala.scalajs.wit.native

    /** Return a stream for appending to a file, if available.
     *
     *  May fail with an error-code describing why the file cannot be appended.
     *
     *  Note: This allows using `write-stream`, which is similar to `write` with
     *  `O_APPEND` in in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("append-via-stream")
    def appendViaStream(): scala.scalajs.wit.Result[OutputStream, ErrorCode] =
      scala.scalajs.wit.native

    /** Provide file advisory information on a descriptor.
     *
     *  This is similar to `posix_fadvise` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("advise")
    def advise(offset: scala.scalajs.wit.unsigned.ULong, length: scala.scalajs.wit.unsigned.ULong,
        advice: Advice): scala.scalajs.wit.Result[Unit, ErrorCode] = scala.scalajs.wit.native

    /** Synchronize the data of a file to disk.
     *
     *  This function succeeds with no effect if the file descriptor is not
     *  opened for writing.
     *
     *  Note: This is similar to `fdatasync` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("sync-data")
    def syncData(): scala.scalajs.wit.Result[Unit, ErrorCode] = scala.scalajs.wit.native

    /** Get flags associated with a descriptor.
     *
     *  Note: This returns similar flags to `fcntl(fd, F_GETFL)` in POSIX.
     *
     *  Note: This returns the value that was the `fs_flags` value returned
     *  from `fdstat_get` in earlier versions of WASI.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("get-flags")
    def getFlags(): scala.scalajs.wit.Result[DescriptorFlags, ErrorCode] = scala.scalajs.wit.native

    /** Get the dynamic type of a descriptor.
     *
     *  Note: This returns the same value as the `type` field of the `fd-stat`
     *  returned by `stat`, `stat-at` and similar.
     *
     *  Note: This returns similar flags to the `st_mode & S_IFMT` value provided
     *  by `fstat` in POSIX.
     *
     *  Note: This returns the value that was the `fs_filetype` value returned
     *  from `fdstat_get` in earlier versions of WASI.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("get-type")
    def getType(): scala.scalajs.wit.Result[DescriptorType, ErrorCode] = scala.scalajs.wit.native

    /** Adjust the size of an open file. If this increases the file's size, the
     *  extra bytes are filled with zeros.
     *
     *  Note: This was called `fd_filestat_set_size` in earlier versions of WASI.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-size")
    def setSize(size: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[Unit, ErrorCode] =
      scala.scalajs.wit.native

    /** Adjust the timestamps of an open file or directory.
     *
     *  Note: This is similar to `futimens` in POSIX.
     *
     *  Note: This was called `fd_filestat_set_times` in earlier versions of WASI.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-times")
    def setTimes(dataAccessTimestamp: NewTimestamp,
        dataModificationTimestamp: NewTimestamp): scala.scalajs.wit.Result[Unit, ErrorCode] = {
      scala.scalajs.wit.native
    }

    /** Read from a descriptor, without using and updating the descriptor's offset.
     *
     *  This function returns a list of bytes containing the data that was
     *  read, along with a bool which, when true, indicates that the end of the
     *  file was reached. The returned list will contain up to `length` bytes; it
     *  may return fewer than requested, if the end of the file is reached or
     *  if the I/O operation is interrupted.
     *
     *  In the future, this may change to return a `stream<u8, error-code>`.
     *
     *  Note: This is similar to `pread` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("read")
    def read(length: scala.scalajs.wit.unsigned.ULong,
        offset: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[
        scala.scalajs.wit.Tuple2[Array[scala.scalajs.wit.unsigned.UByte], Boolean], ErrorCode] = {
      scala.scalajs.wit.native
    }

    /** Write to a descriptor, without using and updating the descriptor's offset.
     *
     *  It is valid to write past the end of a file; the file is extended to the
     *  extent of the write, with bytes between the previous end and the start of
     *  the write set to zero.
     *
     *  In the future, this may change to take a `stream<u8, error-code>`.
     *
     *  Note: This is similar to `pwrite` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("write")
    def write(buffer: Array[scala.scalajs.wit.unsigned.UByte],
        offset: scala.scalajs.wit.unsigned.ULong): scala.scalajs.wit.Result[
        scala.scalajs.wit.unsigned.ULong, ErrorCode] = scala.scalajs.wit.native

    /** Read directory entries from a directory.
     *
     *  On filesystems where directories contain entries referring to themselves
     *  and their parents, often named `.` and `..` respectively, these entries
     *  are omitted.
     *
     *  This always returns a new stream which starts at the beginning of the
     *  directory. Multiple streams may be active on the same directory, and they
     *  do not interfere with each other.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("read-directory")
    def readDirectory(): scala.scalajs.wit.Result[DirectoryEntryStream, ErrorCode] =
      scala.scalajs.wit.native

    /** Synchronize the data and metadata of a file to disk.
     *
     *  This function succeeds with no effect if the file descriptor is not
     *  opened for writing.
     *
     *  Note: This is similar to `fsync` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("sync")
    def sync(): scala.scalajs.wit.Result[Unit, ErrorCode] = scala.scalajs.wit.native

    /** Create a directory.
     *
     *  Note: This is similar to `mkdirat` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("create-directory-at")
    def createDirectoryAt(path: String): scala.scalajs.wit.Result[Unit, ErrorCode] =
      scala.scalajs.wit.native

    /** Return the attributes of an open file or directory.
     *
     *  Note: This is similar to `fstat` in POSIX, except that it does not return
     *  device and inode information. For testing whether two descriptors refer to
     *  the same underlying filesystem object, use `is-same-object`. To obtain
     *  additional data that can be used do determine whether a file has been
     *  modified, use `metadata-hash`.
     *
     *  Note: This was called `fd_filestat_get` in earlier versions of WASI.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("stat")
    def stat(): scala.scalajs.wit.Result[DescriptorStat, ErrorCode] = scala.scalajs.wit.native

    /** Return the attributes of a file or directory.
     *
     *  Note: This is similar to `fstatat` in POSIX, except that it does not
     *  return device and inode information. See the `stat` description for a
     *  discussion of alternatives.
     *
     *  Note: This was called `path_filestat_get` in earlier versions of WASI.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("stat-at")
    def statAt(pathFlags: PathFlags, path: String): scala.scalajs.wit.Result[DescriptorStat,
        ErrorCode] = scala.scalajs.wit.native

    /** Adjust the timestamps of a file or directory.
     *
     *  Note: This is similar to `utimensat` in POSIX.
     *
     *  Note: This was called `path_filestat_set_times` in earlier versions of
     *  WASI.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("set-times-at")
    def setTimesAt(pathFlags: PathFlags, path: String, dataAccessTimestamp: NewTimestamp,
        dataModificationTimestamp: NewTimestamp): scala.scalajs.wit.Result[Unit, ErrorCode] = {
      scala.scalajs.wit.native
    }

    /** Create a hard link.
     *
     *  Note: This is similar to `linkat` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("link-at")
    def linkAt(oldPathFlags: PathFlags, oldPath: String, newDescriptor: Descriptor,
        newPath: String): scala.scalajs.wit.Result[Unit, ErrorCode] = scala.scalajs.wit.native

    /** Open a file or directory.
     *
     *  The returned descriptor is not guaranteed to be the lowest-numbered
     *  descriptor not currently open/ it is randomized to prevent applications
     *  from depending on making assumptions about indexes, since this is
     *  error-prone in multi-threaded contexts. The returned descriptor is
     *  guaranteed to be less than 2**31.
     *
     *  If `flags` contains `descriptor-flags::mutate-directory`, and the base
     *  descriptor doesn't have `descriptor-flags::mutate-directory` set,
     *  `open-at` fails with `error-code::read-only`.
     *
     *  If `flags` contains `write` or `mutate-directory`, or `open-flags`
     *  contains `truncate` or `create`, and the base descriptor doesn't have
     *  `descriptor-flags::mutate-directory` set, `open-at` fails with
     *  `error-code::read-only`.
     *
     *  Note: This is similar to `openat` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("open-at")
    def openAt(pathFlags: PathFlags, path: String, openFlags: OpenFlags,
        flags: DescriptorFlags): scala.scalajs.wit.Result[Descriptor, ErrorCode] = {
      scala.scalajs.wit.native
    }

    /** Read the contents of a symbolic link.
     *
     *  If the contents contain an absolute or rooted path in the underlying
     *  filesystem, this function fails with `error-code::not-permitted`.
     *
     *  Note: This is similar to `readlinkat` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("readlink-at")
    def readlinkAt(path: String): scala.scalajs.wit.Result[String, ErrorCode] =
      scala.scalajs.wit.native

    /** Remove a directory.
     *
     *  Return `error-code::not-empty` if the directory is not empty.
     *
     *  Note: This is similar to `unlinkat(fd, path, AT_REMOVEDIR)` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("remove-directory-at")
    def removeDirectoryAt(path: String): scala.scalajs.wit.Result[Unit, ErrorCode] =
      scala.scalajs.wit.native

    /** Rename a filesystem object.
     *
     *  Note: This is similar to `renameat` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("rename-at")
    def renameAt(oldPath: String, newDescriptor: Descriptor,
        newPath: String): scala.scalajs.wit.Result[Unit, ErrorCode] = scala.scalajs.wit.native

    /** Create a symbolic link (also known as a "symlink").
     *
     *  If `old-path` starts with `/`, the function fails with
     *  `error-code::not-permitted`.
     *
     *  Note: This is similar to `symlinkat` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("symlink-at")
    def symlinkAt(oldPath: String, newPath: String): scala.scalajs.wit.Result[Unit, ErrorCode] =
      scala.scalajs.wit.native

    /** Unlink a filesystem object that is not a directory.
     *
     *  Return `error-code::is-directory` if the path refers to a directory.
     *  Note: This is similar to `unlinkat(fd, path, 0)` in POSIX.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("unlink-file-at")
    def unlinkFileAt(path: String): scala.scalajs.wit.Result[Unit, ErrorCode] =
      scala.scalajs.wit.native

    /** Test whether two descriptors refer to the same filesystem object.
     *
     *  In POSIX, this corresponds to testing whether the two descriptors have the
     *  same device (`st_dev`) and inode (`st_ino` or `d_ino`) numbers.
     *  wasi-filesystem does not expose device and inode numbers, so this function
     *  may be used instead.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("is-same-object")
    def isSameObject(other: Descriptor): Boolean = scala.scalajs.wit.native

    /** Return a hash of the metadata associated with a filesystem object referred
     *  to by a descriptor.
     *
     *  This returns a hash of the last-modification timestamp and file size, and
     *  may also include the inode number, device number, birth timestamp, and
     *  other metadata fields that may change when the file is modified or
     *  replaced. It may also include a secret value chosen by the
     *  implementation and not otherwise exposed.
     *
     *  Implementations are encourated to provide the following properties:
     *
     *  - If the file is not modified or replaced, the computed hash value should
     *  usually not change.
     *  - If the object is modified or replaced, the computed hash value should
     *  usually change.
     *  - The inputs to the hash should not be easily computable from the
     *  computed hash.
     *
     *  However, none of these is required.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("metadata-hash")
    def metadataHash(): scala.scalajs.wit.Result[MetadataHashValue, ErrorCode] =
      scala.scalajs.wit.native

    /** Return a hash of the metadata associated with a filesystem object referred
     *  to by a directory descriptor and a relative path.
     *
     *  This performs the same hash computation as `metadata-hash`.
     */
    @scala.scalajs.wit.annotation.WitResourceMethod("metadata-hash-at")
    def metadataHashAt(pathFlags: PathFlags, path: String): scala.scalajs.wit.Result[
        MetadataHashValue, ErrorCode] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }

  object Descriptor {}

  /** A stream of directory entries. */
  @scala.scalajs.wit.annotation.WitResourceImport(
      "wasi:filesystem/types@0.2.0", "directory-entry-stream")
  trait DirectoryEntryStream {

    /** Read a single directory entry from a `directory-entry-stream`. */
    @scala.scalajs.wit.annotation.WitResourceMethod("read-directory-entry")
    def readDirectoryEntry(): scala.scalajs.wit.Result[java.util.Optional[DirectoryEntry],
        ErrorCode] = scala.scalajs.wit.native

    @scala.scalajs.wit.annotation.WitResourceDrop
    def close(): Unit = scala.scalajs.wit.native
  }

  object DirectoryEntryStream {}

  // Functions
  /** Attempts to extract a filesystem-related `error-code` from the stream
   *  `error` provided.
   *
   *  Stream operations which return `stream-error::last-operation-failed`
   *  have a payload with more information about the operation that failed.
   *  This payload can be passed through to this function to see if there's
   *  filesystem-related information about the error to return.
   *
   *  Note that this function is fallible because not all stream-related
   *  errors are filesystem-related errors.
   */
  @scala.scalajs.wit.annotation.WitImport("wasi:filesystem/types@0.2.0", "filesystem-error-code")
  def filesystemErrorCode(err: Error): java.util.Optional[ErrorCode] = scala.scalajs.wit.native

}
