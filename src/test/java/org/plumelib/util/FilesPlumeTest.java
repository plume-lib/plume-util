package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

final class FilesPlumeTest {

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
      assertTrue(FilesPlume.canCreateAndWrite(new File("TestPlume.java")));

      // This test fails if run by the superuser (who can overwrite
      // any file).
      if (!System.getProperty("user.name").equals("root")) {
        File readOnly = new File("temp");
        readOnly.createNewFile();
        readOnly.setReadOnly();
        assertTrue(!FilesPlume.canCreateAndWrite(readOnly));
        readOnly.delete();
      }

      assertTrue(FilesPlume.canCreateAndWrite(new File("temp")));
      assertTrue(!FilesPlume.canCreateAndWrite(new File("temp/temp")));
    } catch (IOException e) {
      e.printStackTrace();
      fail("failure while testing FilesPlume.canCreateAndWrite(): " + e.toString());
    }
  }

  // public static void streamCopy(java.io.InputStream from, java.io.OutputStream to)

}
