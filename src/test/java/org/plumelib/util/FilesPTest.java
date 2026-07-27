package org.plumelib.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class FilesPTest {

  FilesPTest() {}

  // public static BufferedReader bufferedFileReader(String filename)
  // public static LineNumberReader lineNumberFileReader(String filename)
  // public static BufferedWriter bufferedFileWriter(String filename) throws IOException
  // public static Class classForName(String className)

  // public static void addToClasspath(String dir)
  // public static final class WildcardFilter implements FilenameFilter
  //   public WildcardFilter(String filename)
  //   public boolean accept(File dir, String name)
  // public static boolean canCreateAndWrite(File file)
  // public static void writeObject(Object o, File file) throws IOException
  // public static Object readObject(File file)
  // public static File createTempDir(String prefix, String suffix)

  // public Object incrementHashMap(HashMap hm, Object key, int count)

  @Test
  void test_canCreateAndWrite() {

    try {
      assertTrue(FilesP.canCreateAndWrite(new File("TestCanCreateAndWrite.java")));

      // This test fails if run by the superuser (who can overwrite
      // any file).
      if (!System.getProperty("user.name").equals("root")) {
        File readOnly = new File("temp");
        readOnly.createNewFile();
        readOnly.setReadOnly();
        assertFalse(FilesP.canCreateAndWrite(readOnly));
        readOnly.delete();
      }

      assertTrue(FilesP.canCreateAndWrite(new File("temp")));
      assertFalse(FilesP.canCreateAndWrite(new File("temp/temp")));
    } catch (IOException e) {
      e.printStackTrace();
      fail("failure while testing FilesP.canCreateAndWrite(): " + e.toString());
    }
  }

  // public static void streamCopy(java.io.InputStream from, java.io.OutputStream to)

  @Test
  void test_newBufferedFileWriter_truncates(@TempDir Path tempDir) throws IOException {
    Path file = tempDir.resolve("truncate.txt");
    Files.writeString(file, "a long line that will be overwritten\n", UTF_8);

    // Opening for writing (not appending) truncates the file, rather than leaving the trailing
    // bytes of the longer previous contents.
    try (BufferedWriter bw = FilesP.newBufferedFileWriter(file.toString(), false)) {
      bw.write("short\n");
    }
    assertEquals("short\n", Files.readString(file, UTF_8));

    // Opening for appending retains the previous contents.
    try (BufferedWriter bw = FilesP.newBufferedFileWriter(file.toString(), true)) {
      bw.write("more\n");
    }
    assertEquals("short\nmore\n", Files.readString(file, UTF_8));
  }

  @Test
  void test_newBufferedFileWriter_appendCreates(@TempDir Path tempDir) throws IOException {
    // Appending to a nonexistent file creates it.
    Path file = tempDir.resolve("created-by-append.txt");
    assertFalse(Files.exists(file));
    try (BufferedWriter bw = FilesP.newBufferedFileWriter(file.toString(), true)) {
      bw.write("hello\n");
    }
    assertEquals("hello\n", Files.readString(file, UTF_8));
  }

  @Test
  void test_newBufferedFileWriter_writeCreates(@TempDir Path tempDir) throws IOException {
    // Opening a nonexistent file for writing (not appending) creates it.  This exercises the CREATE
    // half of the options that are used when not appending.
    Path file = tempDir.resolve("created-by-write.txt");
    assertFalse(Files.exists(file));
    try (BufferedWriter bw = FilesP.newBufferedFileWriter(file.toString(), false)) {
      bw.write("hello\n");
    }
    assertEquals("hello\n", Files.readString(file, UTF_8));
  }

  @Test
  void test_newFileOutputStream_appendCreates(@TempDir Path tempDir) throws IOException {
    // Appending to a nonexistent file creates it, rather than throwing NoSuchFileException.
    Path file = tempDir.resolve("created-by-append-stream.txt");
    assertFalse(Files.exists(file));
    try (OutputStream os = FilesP.newFileOutputStream(file, true)) {
      os.write("hello\n".getBytes(UTF_8));
    }
    assertEquals("hello\n", Files.readString(file, UTF_8));

    // A second append adds to the file.
    try (OutputStream os = FilesP.newFileOutputStream(file, true)) {
      os.write("again\n".getBytes(UTF_8));
    }
    assertEquals("hello\nagain\n", Files.readString(file, UTF_8));

    // Opening without appending truncates.
    try (OutputStream os = FilesP.newFileOutputStream(file, false)) {
      os.write("x\n".getBytes(UTF_8));
    }
    assertEquals("x\n", Files.readString(file, UTF_8));
  }

  @Test
  void test_newFileOutputStream_gzAppendCreates(@TempDir Path tempDir) throws IOException {
    // Appending to a nonexistent ".gz" file creates it.  A ".gz" file goes through a different code
    // path than an uncompressed file, so it needs its own test.
    Path file = tempDir.resolve("created-by-append.txt.gz");
    assertFalse(Files.exists(file));
    try (OutputStream os = FilesP.newFileOutputStream(file, true)) {
      os.write("hello\n".getBytes(UTF_8));
    }
    assertEquals("hello", readLine(file));
  }

  @Test
  void test_newFileWriter_truncates(@TempDir Path tempDir) throws IOException {
    Path file = tempDir.resolve("writer-truncate.txt");
    Files.writeString(file, "a long line that will be overwritten\n", UTF_8);

    try (Writer w = FilesP.newFileWriter(file)) {
      w.write("short\n");
    }
    assertEquals("short\n", Files.readString(file, UTF_8));
  }

  @Test
  void test_newBufferedFileWriter_gz(@TempDir Path tempDir) throws IOException {
    // A ".gz" file goes through a different code path than an uncompressed file.
    Path file = tempDir.resolve("compressed.txt.gz");
    try (BufferedWriter bw = FilesP.newBufferedFileWriter(file.toString(), false)) {
      bw.write("a long line that will be overwritten\n");
    }
    assertEquals("a long line that will be overwritten", readLine(file));

    // Opening for writing (not appending) truncates the file, so the shorter contents are not
    // followed by the tail of a longer previous gzip stream.
    try (BufferedWriter bw = FilesP.newBufferedFileWriter(file.toString(), false)) {
      bw.write("short\n");
    }
    assertEquals("short", readLine(file));
  }

  @Test
  void test_newBufferedFileWriter_gzAppend(@TempDir Path tempDir) throws IOException {
    // Appending to a ".gz" file starts a second gzip stream rather than extending the first one.
    Path file = tempDir.resolve("appended.txt.gz");
    try (BufferedWriter bw = FilesP.newBufferedFileWriter(file.toString(), false)) {
      bw.write("first\n");
    }
    try (BufferedWriter bw = FilesP.newBufferedFileWriter(file.toString(), true)) {
      bw.write("second\n");
    }
    // Java reads all the concatenated gzip streams, so both lines are visible.
    assertEquals(Arrays.asList("first", "second"), readLines(file));
  }

  @Test
  void test_newBufferedFileOutputStream(@TempDir Path tempDir) throws IOException {
    Path file = tempDir.resolve("buffered-stream.txt");

    // Appending to a nonexistent file creates it.
    assertFalse(Files.exists(file));
    try (OutputStream os = FilesP.newBufferedFileOutputStream(file.toString(), true)) {
      os.write("hello\n".getBytes(UTF_8));
    }
    assertEquals("hello\n", Files.readString(file, UTF_8));

    // A second append adds to the file.
    try (OutputStream os = FilesP.newBufferedFileOutputStream(file.toString(), true)) {
      os.write("again\n".getBytes(UTF_8));
    }
    assertEquals("hello\nagain\n", Files.readString(file, UTF_8));

    // Opening without appending truncates.
    try (OutputStream os = FilesP.newBufferedFileOutputStream(file.toString(), false)) {
      os.write("x\n".getBytes(UTF_8));
    }
    assertEquals("x\n", Files.readString(file, UTF_8));
  }

  @Test
  void test_newBufferedFileOutputStream_gz(@TempDir Path tempDir) throws IOException {
    // A ".gz" file goes through a different code path than an uncompressed file.
    Path file = tempDir.resolve("buffered-stream.txt.gz");
    try (OutputStream os = FilesP.newBufferedFileOutputStream(file.toString(), false)) {
      os.write("a long line that will be overwritten\n".getBytes(UTF_8));
    }
    assertEquals("a long line that will be overwritten", readLine(file));

    try (OutputStream os = FilesP.newBufferedFileOutputStream(file.toString(), false)) {
      os.write("short\n".getBytes(UTF_8));
    }
    assertEquals("short", readLine(file));
  }

  @Test
  void test_newFileWriter_charset(@TempDir Path tempDir) throws IOException {
    // A non-null charset is used instead of the UTF-8 default.
    Path file = tempDir.resolve("latin1.txt");
    try (Writer w = FilesP.newFileWriter(file, StandardCharsets.ISO_8859_1)) {
      w.write("é\n");
    }
    // In ISO-8859-1, "é" is a single byte; in UTF-8 it would be two bytes.
    assertArrayEquals(new byte[] {(byte) 0xe9, (byte) '\n'}, Files.readAllBytes(file));
  }

  /**
   * Returns the first line of the given possibly-compressed file.
   *
   * @param file the possibly-compressed file to read
   * @return the first line of the file, or null if the file is empty
   * @throws IOException if there is trouble reading the file
   */
  private static @Nullable String readLine(Path file) throws IOException {
    try (BufferedReader br = FilesP.newBufferedFileReader(file.toString())) {
      return br.readLine();
    }
  }

  /**
   * Returns all the lines of the given possibly-compressed file.
   *
   * @param file the possibly-compressed file to read
   * @return all the lines of the file
   * @throws IOException if there is trouble reading the file
   */
  private static List<String> readLines(Path file) throws IOException {
    try (BufferedReader br = FilesP.newBufferedFileReader(file.toString())) {
      List<String> result = new ArrayList<>();
      String line = br.readLine();
      while (line != null) {
        result.add(line);
        line = br.readLine();
      }
      return result;
    }
  }

  @Test
  void test_readCodePoint() throws IOException {
    // One-byte, two-byte, three-byte, and four-byte UTF-8 characters.
    String s = "aé€😀";
    try (InputStream is = new ByteArrayInputStream(s.getBytes(UTF_8))) {
      assertEquals('a', FilesP.readCodePoint(is));
      assertEquals(0x00e9, FilesP.readCodePoint(is)); // e with acute accent
      assertEquals(0x20ac, FilesP.readCodePoint(is)); // euro sign
      assertEquals(0x1f600, FilesP.readCodePoint(is)); // grinning face
      assertEquals(-1, FilesP.readCodePoint(is)); // end of file
      assertEquals(-1, FilesP.readCodePoint(is)); // still end of file
    }
  }

  @Test
  void test_readCodePoint_truncated() throws IOException {
    // The euro sign is 3 bytes in UTF-8; supply only its first 2 bytes.
    byte[] euro = "€".getBytes(UTF_8);
    assertEquals(3, euro.length);
    try (InputStream is = new ByteArrayInputStream(Arrays.copyOf(euro, 2))) {
      // Decoding a truncated character yields the replacement character rather than reading past
      // the end of the stream or fabricating bytes.
      assertEquals(0xfffd, FilesP.readCodePoint(is));
      assertEquals(-1, FilesP.readCodePoint(is));
    }

    // The grinning face is 4 bytes in UTF-8; supply only its first 2 bytes.
    byte[] grinningFace = "😀".getBytes(UTF_8);
    assertEquals(4, grinningFace.length);
    try (InputStream is = new ByteArrayInputStream(Arrays.copyOf(grinningFace, 2))) {
      assertEquals(0xfffd, FilesP.readCodePoint(is));
      assertEquals(-1, FilesP.readCodePoint(is));
    }
  }

  @Test
  void test_readCodePoint_invalidFirstByte() throws IOException {
    // 0x80 is a continuation byte, so it is not a valid first byte of a UTF-8 character.
    try (InputStream is = new ByteArrayInputStream(new byte[] {(byte) 0x80})) {
      IllegalArgumentException e =
          assertThrows(IllegalArgumentException.class, () -> FilesP.readCodePoint(is));
      // The message identifies the offending byte.
      String message = String.valueOf(e.getMessage());
      assertTrue(message.contains("0x80"), message);
    }
    // 0xf8 starts the bit pattern 11111xxx, which cannot start a UTF-8 character either.
    try (InputStream is = new ByteArrayInputStream(new byte[] {(byte) 0xf8})) {
      IllegalArgumentException e =
          assertThrows(IllegalArgumentException.class, () -> FilesP.readCodePoint(is));
      String message = String.valueOf(e.getMessage());
      assertTrue(message.contains("0xf8"), message);
    }
  }

  @Test
  void test_readCodePoint_malformedButAccepted() throws IOException {
    // 0xc0 has the bit pattern 110xxxxx, so getByteCount accepts it, but 0xc0 0x80 is an overlong
    // encoding.  Decoding it yields the replacement character rather than throwing.
    try (InputStream is = new ByteArrayInputStream(new byte[] {(byte) 0xc0, (byte) 0x80})) {
      assertEquals(0xfffd, FilesP.readCodePoint(is));
      assertEquals(-1, FilesP.readCodePoint(is));
    }
  }

  @Test
  void test_readCodePoint_invalidContinuationByte() throws IOException {
    // 0xe2 is the first byte of the euro sign, which is 3 bytes in UTF-8.  Follow it by two bytes
    // that are not continuation bytes.
    byte[] bytes = {(byte) 0xe2, (byte) 'a', (byte) 'b'};
    try (InputStream is = new ByteArrayInputStream(bytes)) {
      // Reading stops at the first byte that is not a continuation byte, so decoding yields the
      // replacement character.
      assertEquals(0xfffd, FilesP.readCodePoint(is));
      // The offending 'a' was consumed and cannot be pushed back, but 'b' was not consumed.
      assertEquals('b', FilesP.readCodePoint(is));
      assertEquals(-1, FilesP.readCodePoint(is));
    }
  }

  @Test
  void test_readCodePoint_invalidLaterContinuationByte() throws IOException {
    // 0xf0 is the first byte of a 4-byte UTF-8 character.  Follow it by one valid continuation byte
    // and then a byte that is not a continuation byte, so reading stops after 2 of the 4 bytes.
    // This differs from test_readCodePoint_invalidContinuationByte, where reading stops after 1.
    byte[] bytes = {(byte) 0xf0, (byte) 0x9f, (byte) 'a', (byte) 'b'};
    try (InputStream is = new ByteArrayInputStream(bytes)) {
      assertEquals(0xfffd, FilesP.readCodePoint(is));
      // The offending 'a' was consumed and cannot be pushed back, but 'b' was not consumed.
      assertEquals('b', FilesP.readCodePoint(is));
      assertEquals(-1, FilesP.readCodePoint(is));
    }
  }

  @Test
  void test_isWhitespaceOnly_resetFails() throws IOException {
    // When reset() fails, isWhitespaceOnly returns its result rather than propagating the
    // IOException, and the stream is left positioned after the bytes that were consumed.
    try (InputStream is = new ResetFailingInputStream("  x  ".getBytes(UTF_8))) {
      assertEquals(false, FilesP.isWhitespaceOnly(is, 10));
      // The two spaces and the "x" were consumed, so reading continues after them.
      assertEquals(' ', FilesP.readCodePoint(is));
    }
  }

  @Test
  void test_isWhitespaceOnly() throws IOException {
    // A stream that does not support mark() yields null.
    try (InputStream is = new NonMarkableInputStream()) {
      assertNull(FilesP.isWhitespaceOnly(is, 10));
    }

    // These use assertEquals rather than assertTrue/assertFalse because isWhitespaceOnly returns a
    // @Nullable Boolean, and assertTrue would throw NullPointerException on a null result instead
    // of failing.
    assertEquals(true, isWhitespaceOnly("   \t\n  ", 10));
    assertEquals(false, isWhitespaceOnly("   x   ", 10));
    // A stream shorter than readLimit is whitespace-only if all of it is whitespace.
    assertEquals(true, isWhitespaceOnly(" ", 10));
    assertEquals(true, isWhitespaceOnly("", 10));
    // Only the first readLimit code points are examined.
    assertEquals(true, isWhitespaceOnly("   x", 3));
    assertEquals(false, isWhitespaceOnly("   x", 4));
    // readLimit counts code points, not bytes: the 3-byte euro sign is one code point.
    assertEquals(false, isWhitespaceOnly(" €", 2));
    assertEquals(true, isWhitespaceOnly(" €", 1));

    // isWhitespaceOnly resets the stream, so the caller can re-read it.
    try (InputStream is = new ByteArrayInputStream("  hello".getBytes(UTF_8))) {
      assertEquals(false, FilesP.isWhitespaceOnly(is, 10));
      assertEquals(' ', FilesP.readCodePoint(is));
    }

    // A stream whose bytes cannot start a UTF-8 character throws IllegalArgumentException.
    try (InputStream is = new ByteArrayInputStream(new byte[] {(byte) 0x80})) {
      assertThrows(IllegalArgumentException.class, () -> FilesP.isWhitespaceOnly(is, 10));
    }

    // The stream is reset even when isWhitespaceOnly throws an exception.
    try (InputStream is = new ByteArrayInputStream(new byte[] {(byte) ' ', (byte) 0x80})) {
      assertThrows(IllegalArgumentException.class, () -> FilesP.isWhitespaceOnly(is, 10));
      assertEquals(' ', FilesP.readCodePoint(is));
    }
  }

  /**
   * Returns the result of {@link FilesP#isWhitespaceOnly} on a mark-supporting input stream over
   * the UTF-8 encoding of the given string.
   *
   * @param contents the contents of the stream
   * @param readLimit how many code points to look ahead in the stream
   * @return whether the first {@code readLimit} code points of {@code contents} are whitespace
   * @throws IOException if there is trouble reading the stream
   */
  private static @Nullable Boolean isWhitespaceOnly(String contents, @Positive int readLimit)
      throws IOException {
    try (InputStream is = new ByteArrayInputStream(contents.getBytes(UTF_8))) {
      return FilesP.isWhitespaceOnly(is, readLimit);
    }
  }

  /** An input stream, containing no data, that does not support {@code mark()}. */
  private static final class NonMarkableInputStream extends InputStream {
    /** Creates a NonMarkableInputStream. */
    NonMarkableInputStream() {}

    @Override
    public @GTENegativeOne int read() {
      return -1;
    }

    @Override
    public boolean markSupported() {
      return false;
    }
  }

  /**
   * An input stream that supports {@code mark()} but whose {@code reset()} always fails. It
   * inherits {@code mark()} and {@code markSupported()} from the underlying {@code
   * ByteArrayInputStream}, which supports both.
   */
  private static final class ResetFailingInputStream extends FilterInputStream {

    /**
     * Creates a ResetFailingInputStream.
     *
     * @param bytes the bytes that the input stream yields
     */
    ResetFailingInputStream(byte[] bytes) {
      super(new ByteArrayInputStream(bytes));
    }

    @Override
    public synchronized void reset() throws IOException {
      throw new IOException("reset() always fails");
    }
  }
}
