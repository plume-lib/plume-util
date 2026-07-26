package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.checkerframework.checker.index.qual.IndexFor;
import org.junit.jupiter.api.Test;

/**
 * Tests the SIList class.
 *
 * <p>Focuses mainly on verifying that iterators over simple lists work correctly.
 */
final class SIListTest {

  SIListTest() {}

  @Test
  void testArrayList() {
    ArrayList<String> al = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      al.add("str" + i);
    }

    SIList<String> sl = SIList.from(al);

    for (int i = 0; i < sl.size(); i++) {
      assertTrue(al.contains(sl.get(i)));
    }
  }

  @Test
  void subList() {
    // Test `subList()` on each SIList implementation that can hold 10 elements.

    // A SimpleArrayList.
    checkSubLists(SIList.from(strings(0, 10)));
    // A OneMoreElementList.
    checkSubLists(SIList.from(strings(0, 9)).add("str9"));
    // A ListOfLists.
    checkSubLists(SIList.concat(List.of(SIList.from(strings(0, 4)), SIList.from(strings(4, 10)))));
    // A SimpleSubList.
    checkSubLists(checkSubList(SIList.from(strings(0, 12)), 1, 11));

    // The empty list and a 1-element list, which have their own implementations.
    SIList<String> empty = SIList.empty();
    checkSubList(empty, 0, 0);
    checkBadSubList(empty, 0, 1);
    SIList<String> singleton = SIList.singleton("str0");
    checkSubList(singleton, 0, 0);
    checkSubList(singleton, 1, 1);
    checkSubList(singleton, 0, 1);
    checkBadSubList(singleton, 0, 2);
  }

  @Test
  void oneMoreElement() {
    ArrayList<String> al = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      al.add("str" + i);
    }

    SIList<String> sl = SIList.from(al).add("str" + 100);

    al.add("str" + 100);

    for (int i = 0; i < sl.size(); i++) {
      assertTrue(al.contains(sl.get(i)));
    }
  }

  @Test
  void listOfList() {
    ArrayList<String> al = new ArrayList<>();
    ArrayList<String> sub = new ArrayList<>();
    List<SIList<String>> lists = new ArrayList<>();

    Set<Integer> partitions = new TreeSet<>();
    int sum = 0;
    while (sum < 100) {
      sum += (int) (Math.random() * 47);
      partitions.add(sum);
    }

    for (int i = 0; i < 100; i++) {
      if (partitions.contains(i)) {
        lists.add(SIList.from(sub));
        sub = new ArrayList<>();
      }
      String str = "str" + i;
      al.add(str);
      sub.add(str);
    }

    if (!sub.isEmpty()) {
      lists.add(SIList.from(sub));
    }

    SIList<String> sl = SIList.concat(lists);

    for (int i = 0; i < sl.size(); i++) {
      assertTrue(al.contains(sl.get(i)));
    }
  }

  @Test
  void listOfMixed() {

    List<SIList<String>> lists = new ArrayList<>();
    ArrayList<String> al = new ArrayList<>();

    SIList<String> base = SIList.from(new ArrayList<>());

    int i;
    for (i = 0; i < 50; i++) {
      String v = "str" + i;
      base = base.add(v);
      al.add(v);
    }
    lists.add(base);
    lists.add(SIList.from(new ArrayList<>()));
    base = SIList.concat(lists);
    for (i = 55; i < 70; i++) {
      String v = "str" + i;
      base = base.add(v);
      al.add(v);
    }

    @SuppressWarnings("index:assignment") // bug in CF? Why isn't SIList.size() recognized?
    @IndexFor("base") int baseSize = base.size();
    for (int j = 0; j < baseSize; j++) {
      assertTrue(al.contains(base.get(j)));
    }
  }

  @Test
  void emptyLOL() {
    List<SIList<String>> lists = Collections.singletonList(SIList.from(new ArrayList<>()));
    SIList<String> sl = SIList.concat(lists);

    assertTrue(sl.isEmpty());
  }

  /**
   * Checks {@code subList()} on the given 10-element list, on ranges that exercise the special
   * cases of {@code SIList.subList()}.
   *
   * @param sl a list with 10 elements
   */
  private void checkSubLists(SIList<String> sl) {
    assertEquals(10, sl.size());

    // A sublist that does not start at index 0.  (Regression test: this used to throw.)
    SIList<String> sub = checkSubList(sl, 2, 5);
    // A sublist that starts at index 0.
    checkSubList(sl, 0, 3);
    // A sublist that extends to the end of the list.
    checkSubList(sl, 7, 10);
    // The entire list.
    checkSubList(sl, 0, 10);
    // An empty sublist and a 1-element sublist, which take special code paths.
    checkSubList(sl, 4, 4);
    checkSubList(sl, 4, 5);
    // A sublist of a sublist.
    checkSubList(sub, 1, 3);

    // Invalid ranges are rejected.
    checkBadSubList(sl, -1, 5);
    checkBadSubList(sl, 5, 2);
    checkBadSubList(sl, 0, 11);
    // A range is checked against the sublist, not against the list that the sublist is a view of.
    checkBadSubList(sub, 0, 4);
  }

  /**
   * Checks that {@code sl.subList(fromIndex, toIndex)} is a view of the given range of {@code sl}:
   * it has the expected size and the expected elements, both via {@code get()} and via iteration.
   *
   * @param sl a list
   * @param fromIndex low endpoint (inclusive) of the sublist
   * @param toIndex high endpoint (exclusive) of the sublist
   * @return the sublist
   */
  @SuppressWarnings("index:argument") // the callers pass valid indices
  private SIList<String> checkSubList(SIList<String> sl, int fromIndex, int toIndex) {
    List<String> expected = new ArrayList<>();
    for (int i = fromIndex; i < toIndex; i++) {
      expected.add(sl.get(i));
    }

    SIList<String> sub = sl.subList(fromIndex, toIndex);
    assertEquals(expected.size(), sub.size());
    assertEquals(expected.isEmpty(), sub.isEmpty());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), sub.get(i));
    }

    List<String> iterated = new ArrayList<>();
    for (String s : sub) {
      iterated.add(s);
    }
    assertEquals(expected, iterated);

    return sub;
  }

  /**
   * Checks that {@code sl.subList(fromIndex, toIndex)} throws an exception because the range is not
   * valid for {@code sl}.
   *
   * @param sl a list
   * @param fromIndex low endpoint (inclusive) of the sublist
   * @param toIndex high endpoint (exclusive) of the sublist
   */
  @SuppressWarnings("index:argument") // the callers intentionally pass invalid indices
  private void checkBadSubList(SIList<String> sl, int fromIndex, int toIndex) {
    assertThrows(IllegalArgumentException.class, () -> sl.subList(fromIndex, toIndex));
  }

  /**
   * Returns a list containing the strings "str{fromIndex}" through "str{toIndex-1}".
   *
   * @param fromIndex the first index, inclusive
   * @param toIndex the last index, exclusive
   * @return a list of strings
   */
  private static List<String> strings(int fromIndex, int toIndex) {
    List<String> result = new ArrayList<>();
    for (int i = fromIndex; i < toIndex; i++) {
      result.add("str" + i);
    }
    return result;
  }
}
