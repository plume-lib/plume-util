// This class should be kept in sync with org.checkerframework.checker.regex.util.RegexUtilTest in
// the Checker Framework project.

package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.regex.qual.Regex;
import org.junit.jupiter.api.Test;

public final class RegexUtilTest {

  @Test
  public void test_isRegex_and_asRegex() {

    String s1 = "colo(u?)r";
    String s2 = "(brown|beige)";
    String s3 = "colou?r";
    String s4 = "1) first point";

    assertTrue(RegexUtil.isRegex(s1));
    RegexUtil.asRegex(s1);
    assertTrue(RegexUtil.isRegex(s1, 0));
    RegexUtil.asRegex(s1, 0);
    assertTrue(RegexUtil.isRegex(s1, 1));
    RegexUtil.asRegex(s1, 1);
    assertFalse(RegexUtil.isRegex(s1, 2));
    assertThrows(Error.class, () -> RegexUtil.asRegex(s1, 2));

    assertTrue(RegexUtil.isRegex(s2));
    RegexUtil.asRegex(s2);
    assertTrue(RegexUtil.isRegex(s2, 0));
    RegexUtil.asRegex(s2, 0);
    assertTrue(RegexUtil.isRegex(s2, 1));
    RegexUtil.asRegex(s2, 1);
    assertFalse(RegexUtil.isRegex(s2, 2));
    assertThrows(Error.class, () -> RegexUtil.asRegex(s2, 2));

    assertTrue(RegexUtil.isRegex(s3));
    RegexUtil.asRegex(s3);
    assertTrue(RegexUtil.isRegex(s3, 0));
    RegexUtil.asRegex(s3, 0);
    assertFalse(RegexUtil.isRegex(s3, 1));
    assertThrows(Error.class, () -> RegexUtil.asRegex(s3, 1));
    assertFalse(RegexUtil.isRegex(s3, 2));
    assertThrows(Error.class, () -> RegexUtil.asRegex(s3, 2));

    assertFalse(RegexUtil.isRegex(s4));
    assertThrows(Error.class, () -> RegexUtil.asRegex(s4));
    assertFalse(RegexUtil.isRegex(s4, 0));
    assertThrows(Error.class, () -> RegexUtil.asRegex(s4, 0));
    assertFalse(RegexUtil.isRegex(s4, 1));
    assertThrows(Error.class, () -> RegexUtil.asRegex(s4, 1));
    assertFalse(RegexUtil.isRegex(s4, 2));
    assertThrows(Error.class, () -> RegexUtil.asRegex(s4, 2));
  }

  List<String> s1 = Arrays.asList(new String[] {"a", "b", "c"});
  List<String> s2 = Arrays.asList(new String[] {"a", "b", "c", "d"});
  List<String> s3 = Arrays.asList(new String[] {"aa", "bb", "cc"});
  List<String> s4 = Arrays.asList(new String[] {"a", "aa", "b", "bb", "c"});
  List<String> s5 = Arrays.asList(new String[] {"d", "ee", "fff"});
  List<String> s6 = Arrays.asList(new String[] {"a", "d", "ee", "fff"});

  List<@Regex String> r1 = Arrays.asList(new @Regex String[] {});
  List<@Regex String> r2 = Arrays.asList(new @Regex String[] {"a", "b", "c"});
  List<@Regex String> r3 = Arrays.asList(new @Regex String[] {"a+", "b+", "c"});
  List<@Regex String> r4 = Arrays.asList(new @Regex String[] {"a+", "b+", "c+"});
  List<@Regex String> r5 = Arrays.asList(new @Regex String[] {".*"});

  List<@Regex String> r6 = Arrays.asList(new @Regex String[] {"a?b", "a*"});
  List<@Regex String> r7 = Arrays.asList(new @Regex String[] {"a?b+", "a*"});

  List<String> empty = Collections.emptyList();
  List<String> onlyA = Arrays.asList(new String[] {"a"});
  List<String> onlyAA = Arrays.asList(new String[] {"aa"});
  List<String> onlyC = Arrays.asList(new String[] {"c"});
  List<String> onlyCC = Arrays.asList(new String[] {"cc"});
  List<String> onlyD = Arrays.asList(new String[] {"d"});
  List<String> aaab = Arrays.asList(new String[] {"a", "aa", "b"});
  List<String> ab = Arrays.asList(new String[] {"a", "b"});
  List<String> aabb = Arrays.asList(new String[] {"aa", "bb"});
  List<String> aacc = Arrays.asList(new String[] {"aa", "cc"});
  List<String> bbc = Arrays.asList(new String[] {"bb", "c"});
  List<String> bbcc = Arrays.asList(new String[] {"bb", "cc"});
  List<String> cc = Arrays.asList(new String[] {"cc"});
  List<String> cd = Arrays.asList(new String[] {"c", "d"});
  List<String> eefff = Arrays.asList(new String[] {"ee", "fff"});

  @Test
  public void test_anyMatches() {
    assertEquals(RegexUtil.anyMatches(s1, r1), empty);
    assertEquals(RegexUtil.anyMatches(s2, r1), empty);
    assertEquals(RegexUtil.anyMatches(s3, r1), empty);
    assertEquals(RegexUtil.anyMatches(s4, r1), empty);
    assertEquals(RegexUtil.anyMatches(s5, r1), empty);
    assertEquals(RegexUtil.anyMatches(s6, r1), empty);

    assertEquals(RegexUtil.anyMatches(s1, r2), s1);
    assertEquals(RegexUtil.anyMatches(s2, r2), s1);
    assertEquals(RegexUtil.anyMatches(s3, r2), empty);
    assertEquals(RegexUtil.anyMatches(s4, r2), s1);
    assertEquals(RegexUtil.anyMatches(s5, r2), empty);
    assertEquals(RegexUtil.anyMatches(s6, r2), onlyA);

    assertEquals(RegexUtil.anyMatches(s1, r3), s1);
    assertEquals(RegexUtil.anyMatches(s2, r3), s1);
    assertEquals(RegexUtil.anyMatches(s3, r3), aabb);
    assertEquals(RegexUtil.anyMatches(s4, r3), s4);
    assertEquals(RegexUtil.anyMatches(s5, r3), empty);
    assertEquals(RegexUtil.anyMatches(s6, r3), onlyA);

    assertEquals(RegexUtil.anyMatches(s1, r4), s1);
    assertEquals(RegexUtil.anyMatches(s2, r4), s1);
    assertEquals(RegexUtil.anyMatches(s3, r4), s3);
    assertEquals(RegexUtil.anyMatches(s4, r4), s4);
    assertEquals(RegexUtil.anyMatches(s5, r4), empty);
    assertEquals(RegexUtil.anyMatches(s6, r4), onlyA);

    assertEquals(RegexUtil.anyMatches(s1, r5), s1);
    assertEquals(RegexUtil.anyMatches(s2, r5), s2);
    assertEquals(RegexUtil.anyMatches(s3, r5), s3);
    assertEquals(RegexUtil.anyMatches(s4, r5), s4);
    assertEquals(RegexUtil.anyMatches(s5, r5), s5);
    assertEquals(RegexUtil.anyMatches(s6, r5), s6);

    assertFalse(RegexUtil.allMatch(s1, r1));
    assertFalse(RegexUtil.allMatch(s2, r1));
    assertFalse(RegexUtil.allMatch(s3, r1));
    assertFalse(RegexUtil.allMatch(s4, r1));
    assertFalse(RegexUtil.allMatch(s5, r1));
    assertFalse(RegexUtil.allMatch(s6, r1));

    assertTrue(RegexUtil.allMatch(s1, r2));
    assertFalse(RegexUtil.allMatch(s2, r2));
    assertFalse(RegexUtil.allMatch(s3, r2));
    assertFalse(RegexUtil.allMatch(s4, r2));
    assertFalse(RegexUtil.allMatch(s5, r2));
    assertFalse(RegexUtil.allMatch(s6, r2));

    assertTrue(RegexUtil.allMatch(s1, r3));
    assertFalse(RegexUtil.allMatch(s2, r3));
    assertFalse(RegexUtil.allMatch(s3, r3));
    assertTrue(RegexUtil.allMatch(s4, r3));
    assertFalse(RegexUtil.allMatch(s5, r3));
    assertFalse(RegexUtil.allMatch(s6, r3));

    assertTrue(RegexUtil.allMatch(s1, r4));
    assertFalse(RegexUtil.allMatch(s2, r4));
    assertTrue(RegexUtil.allMatch(s3, r4));
    assertTrue(RegexUtil.allMatch(s4, r4));
    assertFalse(RegexUtil.allMatch(s5, r4));
    assertFalse(RegexUtil.allMatch(s6, r4));

    assertTrue(RegexUtil.allMatch(s1, r5));
    assertTrue(RegexUtil.allMatch(s2, r5));
    assertTrue(RegexUtil.allMatch(s3, r5));
    assertTrue(RegexUtil.allMatch(s4, r5));
    assertTrue(RegexUtil.allMatch(s5, r5));
    assertTrue(RegexUtil.allMatch(s6, r5));
  }

  @Test
  public void test_noneMatches() {
    assertEquals(RegexUtil.noneMatches(s1, r1), s1);
    assertEquals(RegexUtil.noneMatches(s2, r1), s2);
    assertEquals(RegexUtil.noneMatches(s3, r1), s3);
    assertEquals(RegexUtil.noneMatches(s4, r1), s4);
    assertEquals(RegexUtil.noneMatches(s5, r1), s5);
    assertEquals(RegexUtil.noneMatches(s6, r1), s6);

    assertEquals(RegexUtil.noneMatches(s1, r2), empty);
    assertEquals(RegexUtil.noneMatches(s2, r2), onlyD);
    assertEquals(RegexUtil.noneMatches(s3, r2), s3);
    assertEquals(RegexUtil.noneMatches(s4, r2), aabb);
    assertEquals(RegexUtil.noneMatches(s5, r2), s5);
    assertEquals(RegexUtil.noneMatches(s6, r2), s5);

    assertEquals(RegexUtil.noneMatches(s1, r3), empty);
    assertEquals(RegexUtil.noneMatches(s2, r3), onlyD);
    assertEquals(RegexUtil.noneMatches(s3, r3), cc);
    assertEquals(RegexUtil.noneMatches(s4, r3), empty);
    assertEquals(RegexUtil.noneMatches(s5, r3), s5);
    assertEquals(RegexUtil.noneMatches(s6, r3), s5);

    assertEquals(RegexUtil.noneMatches(s1, r4), empty);
    assertEquals(RegexUtil.noneMatches(s2, r4), onlyD);
    assertEquals(RegexUtil.noneMatches(s3, r4), empty);
    assertEquals(RegexUtil.noneMatches(s4, r4), empty);
    assertEquals(RegexUtil.noneMatches(s5, r4), s5);
    assertEquals(RegexUtil.noneMatches(s6, r4), s5);

    assertEquals(RegexUtil.noneMatches(s1, r5), empty);
    assertEquals(RegexUtil.noneMatches(s2, r5), empty);
    assertEquals(RegexUtil.noneMatches(s3, r5), empty);
    assertEquals(RegexUtil.noneMatches(s4, r5), empty);
    assertEquals(RegexUtil.noneMatches(s5, r5), empty);
    assertEquals(RegexUtil.noneMatches(s6, r5), empty);

    assertTrue(RegexUtil.noneMatch(s1, r1));
    assertTrue(RegexUtil.noneMatch(s2, r1));
    assertTrue(RegexUtil.noneMatch(s3, r1));
    assertTrue(RegexUtil.noneMatch(s4, r1));
    assertTrue(RegexUtil.noneMatch(s5, r1));
    assertTrue(RegexUtil.noneMatch(s6, r1));

    assertFalse(RegexUtil.noneMatch(s1, r2));
    assertFalse(RegexUtil.noneMatch(s2, r2));
    assertTrue(RegexUtil.noneMatch(s3, r2));
    assertFalse(RegexUtil.noneMatch(s4, r2));
    assertTrue(RegexUtil.noneMatch(s5, r2));
    assertFalse(RegexUtil.noneMatch(s6, r2));

    assertFalse(RegexUtil.noneMatch(s1, r3));
    assertFalse(RegexUtil.noneMatch(s2, r3));
    assertFalse(RegexUtil.noneMatch(s3, r3));
    assertFalse(RegexUtil.noneMatch(s4, r3));
    assertTrue(RegexUtil.noneMatch(s5, r3));
    assertFalse(RegexUtil.noneMatch(s6, r3));

    assertFalse(RegexUtil.noneMatch(s1, r4));
    assertFalse(RegexUtil.noneMatch(s2, r4));
    assertFalse(RegexUtil.noneMatch(s3, r4));
    assertFalse(RegexUtil.noneMatch(s4, r4));
    assertTrue(RegexUtil.noneMatch(s5, r4));
    assertFalse(RegexUtil.noneMatch(s6, r4));

    assertFalse(RegexUtil.noneMatch(s1, r5));
    assertFalse(RegexUtil.noneMatch(s2, r5));
    assertFalse(RegexUtil.noneMatch(s3, r5));
    assertFalse(RegexUtil.noneMatch(s4, r5));
    assertFalse(RegexUtil.noneMatch(s5, r5));
    assertFalse(RegexUtil.noneMatch(s6, r5));
  }

  @Test
  public void test_r6() {
    assertEquals(ab, RegexUtil.anyMatches(s1, r6));
    assertEquals(ab, RegexUtil.anyMatches(s2, r6));
    assertEquals(onlyAA, RegexUtil.anyMatches(s3, r6));
    assertEquals(aaab, RegexUtil.anyMatches(s4, r6));
    assertEquals(empty, RegexUtil.anyMatches(s5, r6));
    assertEquals(onlyA, RegexUtil.anyMatches(s6, r6));
    assertFalse(RegexUtil.allMatch(s1, r6));
    assertFalse(RegexUtil.allMatch(s2, r6));
    assertFalse(RegexUtil.allMatch(s3, r6));
    assertFalse(RegexUtil.allMatch(s4, r6));
    assertFalse(RegexUtil.allMatch(s5, r6));
    assertFalse(RegexUtil.allMatch(s6, r6));
    assertTrue(RegexUtil.allMatch(onlyA, r7));
    assertEquals(RegexUtil.noneMatches(s1, r6), onlyC);
    assertEquals(RegexUtil.noneMatches(s2, r6), cd);
    assertEquals(RegexUtil.noneMatches(s3, r6), bbcc);
    assertEquals(RegexUtil.noneMatches(s3, r7), onlyCC);
    assertEquals(RegexUtil.noneMatches(s4, r6), bbc);
    assertEquals(RegexUtil.noneMatches(s5, r6), s5);
    assertEquals(RegexUtil.noneMatches(s6, r6), s5);
    assertFalse(RegexUtil.noneMatch(s1, r6));
    assertFalse(RegexUtil.noneMatch(s2, r6));
    assertFalse(RegexUtil.noneMatch(s3, r6));
    assertFalse(RegexUtil.noneMatch(s4, r6));
    assertTrue(RegexUtil.noneMatch(s5, r6));
    assertFalse(RegexUtil.noneMatch(s6, r6));
  }
}
