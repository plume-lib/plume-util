package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.checkerframework.checker.index.qual.IndexFor;
import org.junit.jupiter.api.Test;

/** Test (mainly) to make sure iterators over simple lists work. */
class SIListTest {

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
}
