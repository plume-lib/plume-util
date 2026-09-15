package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/** Test the UtilP class. */
final class UtilPTest {

  UtilPTest() {}

  // //////////////////////////////////////////////////////////////////////
  // Object
  //

  /** Test clone(). */
  @Test
  void test_clone() {
    ArrayList<String> orig = new ArrayList<>(Arrays.asList("a", "b"));
    ArrayList<String> copy = UtilP.clone(orig);
    assertEquals(orig, copy);
    assertNotSame(orig, copy);
    // The copy is shallow: the elements are shared, but the list is not.
    orig.add("c");
    assertEquals(Arrays.asList("a", "b"), copy);

    LinkedHashMap<String, String> origMap = new LinkedHashMap<>();
    origMap.put("k", "v");
    LinkedHashMap<String, String> copyMap = UtilP.clone(origMap);
    assertEquals(origMap, copyMap);
    assertNotSame(origMap, copyMap);

    assertNull(UtilP.clone((@Nullable String) null));

    // clone() is called reflectively, so it must be public.  String does not have a public
    // clone() method.
    assertThrows(Error.class, () -> UtilP.clone("a string"));
    // Arrays have a clone() method per the JLS, but core reflection does not expose it, so
    // clone() does not work on arrays.  Use `array.clone()` directly instead.
    assertThrows(Error.class, () -> UtilP.clone(new int[] {1, 2, 3}));
  }

  /** Test firstNonNull(). */
  @Test
  void test_firstNonNull() {
    assertEquals("a", UtilP.firstNonNull("a", "b"));
    assertEquals("b", UtilP.firstNonNull((@Nullable String) null, "b"));
    assertEquals("a", UtilP.firstNonNull("a", (@Nullable String) null));
    assertThrows(
        IllegalArgumentException.class,
        () -> UtilP.firstNonNull((@Nullable String) null, (@Nullable String) null));
  }

  // //////////////////////////////////////////////////////////////////////
  // Hashing
  //

  /** Test hash(). */
  @Test
  void test_hash() {
    // public static int hash(double[] a, double[] b)
    assertEquals(
        Objects.hash(Arrays.hashCode(new double[] {1.0, 2.0}), Arrays.hashCode(new double[] {3.0})),
        UtilP.hash(new double[] {1.0, 2.0}, new double[] {3.0}));
    // Equal contents hash equally.
    assertEquals(
        UtilP.hash(new double[] {1.0, 2.0}, new double[] {3.0}),
        UtilP.hash(new double[] {1.0, 2.0}, new double[] {3.0}));
    // The two arguments are not interchangeable.
    assertNotEquals(
        UtilP.hash(new double[] {1.0}, new double[] {2.0}),
        UtilP.hash(new double[] {2.0}, new double[] {1.0}));
    // Null arguments are permitted.
    assertEquals(
        UtilP.hash((double @Nullable []) null, (double @Nullable []) null),
        UtilP.hash((double @Nullable []) null, (double @Nullable []) null));
    assertNotEquals(
        UtilP.hash((double @Nullable []) null, (double @Nullable []) null),
        UtilP.hash(new double[] {1.0}, new double[] {2.0}));

    // public static int hash(long[] a, long[] b)
    assertEquals(
        Objects.hash(Arrays.hashCode(new long[] {1, 2}), Arrays.hashCode(new long[] {3})),
        UtilP.hash(new long[] {1, 2}, new long[] {3}));
    assertEquals(
        UtilP.hash(new long[] {1, 2}, new long[] {3}),
        UtilP.hash(new long[] {1, 2}, new long[] {3}));
    assertNotEquals(
        UtilP.hash(new long[] {1}, new long[] {2}), UtilP.hash(new long[] {2}, new long[] {1}));
    assertEquals(
        UtilP.hash((long @Nullable []) null, (long @Nullable []) null),
        UtilP.hash((long @Nullable []) null, (long @Nullable []) null));
  }

  // //////////////////////////////////////////////////////////////////////
  // ProcessBuilder
  //

  /** Test backticks(). */
  @DisabledOnOs(OS.WINDOWS) // the commands below are Unix commands
  @Test
  void test_backticks() {
    // public static String backticks(String... command)
    assertEquals("hello\n", UtilP.backticks("echo", "hello"));
    assertEquals("", UtilP.backticks("true"));

    // public static String backticks(List<String> command)
    assertEquals("hello\n", UtilP.backticks(List.of("echo", "hello")));

    // Standard error is redirected into the result, after standard output.
    assertEquals("out\nerr\n", UtilP.backticks("sh", "-c", "echo out; echo err >&2"));

    // The output of a failing command is returned; the exit status is not available.
    assertEquals("", UtilP.backticks("false"));

    // public static String backticks(File dir, String... command)
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    assertEquals(tmpDir.getAbsolutePath() + "\n", UtilP.backticks(tmpDir, "pwd"));

    // public static String backticks(File dir, List<String> command)
    assertEquals(tmpDir.getAbsolutePath() + "\n", UtilP.backticks(tmpDir, List.of("pwd")));
    // A null directory means the current directory.
    assertEquals(UtilP.backticks("pwd"), UtilP.backticks((@Nullable File) null, List.of("pwd")));

    // A command that cannot be started yields a diagnostic rather than an exception.
    assertTrue(
        UtilP.backticks("no-such-command-plume-util-test").startsWith("IOException: "),
        UtilP.backticks("no-such-command-plume-util-test"));
  }

  // //////////////////////////////////////////////////////////////////////
  // Properties
  //

  /** Test getBooleanProperty(). */
  @Test
  void test_getBooleanProperty() {
    Properties p = new Properties();
    p.setProperty("t1", "true");
    p.setProperty("t2", "yes");
    p.setProperty("t3", "1");
    p.setProperty("f1", "false");
    p.setProperty("f2", "no");
    p.setProperty("f3", "0");
    p.setProperty("mixedCase", "TrUe");
    p.setProperty("bad", "maybe");

    for (String key : new String[] {"t1", "t2", "t3", "mixedCase"}) {
      assertTrue(UtilP.getBooleanProperty(p, key), key);
      assertTrue(UtilP.getBooleanProperty(p, key, false), key);
    }
    for (String key : new String[] {"f1", "f2", "f3"}) {
      assertFalse(UtilP.getBooleanProperty(p, key), key);
      assertFalse(UtilP.getBooleanProperty(p, key, true), key);
    }

    // An unset property yields the default, which is false for the two-argument version.
    assertFalse(UtilP.getBooleanProperty(p, "unset"));
    assertFalse(UtilP.getBooleanProperty(p, "unset", false));
    assertTrue(UtilP.getBooleanProperty(p, "unset", true));

    // A value that does not represent a boolean is an error.
    assertThrows(Error.class, () -> UtilP.getBooleanProperty(p, "bad"));
    assertThrows(Error.class, () -> UtilP.getBooleanProperty(p, "bad", true));
  }

  /** Test appendProperty(). */
  @Test
  void test_appendProperty() {
    Properties p = new Properties();

    // Appending to an unset property sets it, and returns null as the previous value.
    assertNull(UtilP.appendProperty(p, "key", "a"));
    assertEquals("a", p.getProperty("key"));

    // Appending to a set property concatenates, and returns the previous value.
    assertEquals("a", UtilP.appendProperty(p, "key", "b"));
    assertEquals("ab", p.getProperty("key"));
    assertEquals("ab", UtilP.appendProperty(p, "key", ""));
    assertEquals("ab", p.getProperty("key"));

    // Other properties are unaffected.
    p.setProperty("other", "x");
    UtilP.appendProperty(p, "key", "c");
    assertEquals("x", p.getProperty("other"));
    assertEquals("abc", p.getProperty("key"));
  }

  /** Test setDefaultMaybe(). */
  @Test
  void test_setDefaultMaybe() {
    Properties p = new Properties();

    // Setting an unset property sets it, and returns null as the previous value.
    assertNull(UtilP.setDefaultMaybe(p, "key", "a"));
    assertEquals("a", p.getProperty("key"));

    // Setting an already-set property does not change it, and returns the current value.
    assertEquals("a", UtilP.setDefaultMaybe(p, "key", "b"));
    assertEquals("a", p.getProperty("key"));

    // A property whose value is the empty string counts as set.
    p.setProperty("empty", "");
    assertEquals("", UtilP.setDefaultMaybe(p, "empty", "default"));
    assertEquals("", p.getProperty("empty"));
  }

  // //////////////////////////////////////////////////////////////////////
  // Throwable
  //

  /** Test stackTraceToString(). */
  @Test
  void test_stackTraceToString() {
    String trace = UtilP.stackTraceToString(new Throwable("my message"));
    List<String> lines = Arrays.asList(trace.split(System.lineSeparator()));
    assertEquals("java.lang.Throwable: my message", lines.get(0));
    // The remaining lines are stack frames, the first of which is this method.
    assertTrue(lines.size() > 1, trace);
    assertTrue(
        lines.get(1).trim().startsWith("at org.plumelib.util.UtilPTest.test_stackTraceToString("),
        trace);

    // A throwable with a cause prints the cause too.
    Throwable cause = new IllegalStateException("the cause");
    String traceWithCause = UtilP.stackTraceToString(new RuntimeException("outer", cause));
    assertTrue(traceWithCause.startsWith("java.lang.RuntimeException: outer"), traceWithCause);
    assertTrue(
        traceWithCause.contains("Caused by: java.lang.IllegalStateException: the cause"),
        traceWithCause);

    // The result is independent of the number of times it is computed.
    Throwable t = new Throwable("stable");
    assertEquals(UtilP.stackTraceToString(t), UtilP.stackTraceToString(t));
  }
}
