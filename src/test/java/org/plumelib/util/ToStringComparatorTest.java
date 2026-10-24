package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

/** Test the ToStringComparator class. */
final class ToStringComparatorTest {

  ToStringComparatorTest() {}

  @Test
  void compareOrdersByPrintedRepresentation() {
    ToStringComparator c = ToStringComparator.INSTANCE;
    assertTrue(c.compare("abc", "abd") < 0);
    assertTrue(c.compare("abd", "abc") > 0);
    assertEquals(0, c.compare("abc", "abc"));

    // Values are ordered lexicographically by toString(), not by their natural ordering.
    assertTrue(c.compare(10, 9) < 0);
    assertTrue(c.compare(9, 10) > 0);

    // Values of different types are compared by their printed representations.
    assertTrue(c.compare(1, "0") > 0);
  }

  @SuppressWarnings("nullness:argument") // this class handles null, as its Javadoc says
  @Test
  void compareHandlesNull() {
    ToStringComparator c = ToStringComparator.INSTANCE;
    assertEquals(0, c.compare(null, null));
    // null is ordered as if it were the string "null".
    assertTrue(c.compare("a", null) < 0);
    assertTrue(c.compare(null, "a") > 0);
    assertTrue(c.compare("z", null) > 0);
    assertTrue(c.compare(null, "z") < 0);
    assertEquals(0, c.compare(null, "null"));
  }

  @Test
  @SuppressWarnings("deprecation") // tests the deprecated alias
  void instanceIsASingleton() {
    assertSame(ToStringComparator.INSTANCE, ToStringComparator.instance);
  }

  @Test
  void sortedSortsByToString() {
    List<@Nullable Object> in = new ArrayList<>();
    in.add(22);
    in.add(3);
    in.add(111);
    in.add("abc");
    in.add(null);

    List<@Nullable Object> expected = new ArrayList<>();
    expected.add(111);
    expected.add(22);
    expected.add(3);
    expected.add("abc");
    expected.add(null);

    List<@Nullable Object> out = ToStringComparator.sorted(in);
    assertEquals(expected, out);

    // sorted() does not modify its argument.
    List<@Nullable Object> unchanged = new ArrayList<>();
    unchanged.add(22);
    unchanged.add(3);
    unchanged.add(111);
    unchanged.add("abc");
    unchanged.add(null);
    assertEquals(unchanged, in);
  }

  @Test
  void sortedAcceptsAnyIterable() {
    Set<String> in = new LinkedHashSet<>();
    in.add("c");
    in.add("a");
    in.add("b");
    assertEquals(List.of("a", "b", "c"), ToStringComparator.sorted(in));

    assertEquals(Collections.emptyList(), ToStringComparator.sorted(Collections.emptyList()));
    assertEquals(List.of("a"), ToStringComparator.sorted(Collections.singletonList("a")));
  }
}
