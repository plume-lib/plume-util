package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.util.Locale;
import org.junit.jupiter.api.Test;

final class SystemPTest {

  SystemPTest() {}

  /** True if the current operating system is Windows. */
  private static final boolean isWindows =
      System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");

  @Test
  void testPathToExecutableNotFound() {
    assertNull(SystemP.pathToExecutable("this-program-surely-does-not-exist-9d3f1a"));
  }

  @Test
  void testPathToExecutableFound() {
    // "sh" is on the PATH of every Unix-like system; skip this check on Windows.
    assumeFalse(isWindows);
    String path = SystemP.pathToExecutable("sh");
    assertTrue(path != null, "sh should be found on the PATH");
    if (path != null) {
      File file = new File(path);
      assertTrue(file.isAbsolute());
      assertTrue(file.isFile());
      assertTrue(file.canExecute());
    }
  }
}
