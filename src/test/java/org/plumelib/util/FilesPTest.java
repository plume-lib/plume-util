package org.plumelib.util;

import static java.nio.charset.StandardCharsets.UTF_8;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
  }

  @Test
  void test_readCodePoint_invalidFirstByte() throws IOException {
    // 0x80 is a continuation byte, so it is not a valid first byte of a UTF-8 character.
    try (InputStream is = new ByteArrayInputStream(new byte[] {(byte) 0x80})) {
      assertThrows(IllegalArgumentException.class, () -> FilesP.readCodePoint(is));
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
  void test_isWhitespaceOnly() throws IOException {
    // A stream that does not support mark() yields null.
    try (InputStream is = new NonMarkableInputStream()) {
      assertNull(FilesP.isWhitespaceOnly(is, 10));
    }

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

    // A stream that does not contain valid UTF-8 throws IllegalArgumentException.
    try (InputStream is = new ByteArrayInputStream(new byte[] {(byte) 0x80})) {
      assertThrows(IllegalArgumentException.class, () -> FilesP.isWhitespaceOnly(is, 10));
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
}
