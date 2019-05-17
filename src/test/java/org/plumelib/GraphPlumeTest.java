package org.plumelib.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class GraphPlumeTest {

  ///////////////////////////////////////////////////////////////////////////

  // Figure 1 from
  // http://www.boost.org/libs/graph/doc/lengauer_tarjan_dominator.htm#fig:dominator-tree-example
  private static @Nullable Map<Integer, List<@KeyFor("preds1") Integer>> preds1;
  private static @Nullable Map<Integer, List<@KeyFor("succs1") Integer>> succs1;

  @SuppressWarnings({"keyfor", "nullness"}) // test code
  @EnsuresNonNull({"preds1", "succs1"})
  private static void initializePreds1AndSucc1() {
    if (preds1 != null) {
      return;
    }

    preds1 = new LinkedHashMap<>();
    succs1 = new LinkedHashMap<>();
    for (int i = 0; i <= 7; i++) {
      preds1.put(i, new ArrayList<Integer>());
      succs1.put(i, new ArrayList<Integer>());
    }
    succs1.get(0).add(1);
    preds1.get(1).add(0);
    succs1.get(1).add(2);
    preds1.get(2).add(1);
    succs1.get(1).add(3);
    preds1.get(3).add(1);
    succs1.get(2).add(7);
    preds1.get(7).add(2);
    succs1.get(3).add(4);
    preds1.get(4).add(3);
    succs1.get(4).add(5);
    preds1.get(5).add(4);
    succs1.get(4).add(6);
    preds1.get(6).add(4);
    succs1.get(5).add(7);
    preds1.get(7).add(5);
    succs1.get(6).add(4);
    preds1.get(4).add(6);
  }

  @Test
  public void testGraphPlume() {

    initializePreds1AndSucc1();

    Map<Integer, List<Integer>> dom1post = GraphPlume.dominators(succs1);
    assert dom1post.get(0).toString().equals("[7, 1, 0]");
    assert dom1post.get(1).toString().equals("[7, 1]");
    assert dom1post.get(2).toString().equals("[7, 2]");
    assert dom1post.get(3).toString().equals("[7, 5, 4, 3]");
    assert dom1post.get(4).toString().equals("[7, 5, 4]");
    assert dom1post.get(5).toString().equals("[7, 5]");
    assert dom1post.get(6).toString().equals("[7, 5, 4, 6]");
    assert dom1post.get(7).toString().equals("[7]");

    Map<Integer, List<Integer>> dom1pre = GraphPlume.dominators(preds1);
    assert dom1pre.get(0).toString().equals("[0]");
    assert dom1pre.get(1).toString().equals("[0, 1]");
    assert dom1pre.get(2).toString().equals("[0, 1, 2]");
    assert dom1pre.get(3).toString().equals("[0, 1, 3]");
    assert dom1pre.get(4).toString().equals("[0, 1, 3, 4]");
    assert dom1pre.get(5).toString().equals("[0, 1, 3, 4, 5]");
    assert dom1pre.get(6).toString().equals("[0, 1, 3, 4, 6]");
    assert dom1pre.get(7).toString().equals("[0, 1, 7]");

    // I should add more tests.

  }
}
