package org.plumelib.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
