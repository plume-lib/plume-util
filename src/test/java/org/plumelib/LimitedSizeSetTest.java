package org.plumelib.util;

import java.util.Random;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;

/** Test code for the plume package. */
@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class LimitedSizeSetTest {

  // Add 100 elements randomly selected from the range 0..limit-1 to the set.
  private static void lsisAddElts(@Positive int limit, LimitedSizeSet<Integer> s) {
    Random r = new Random(20140613);
    for (int i = 0; i < 100; i++) {
      s.add(r.nextInt(limit));
    }
  }

  // Create a LimitedSizeSet of the given size, and add elements to it.
  private static void lsis_test(@Positive int maxSize) {
    LimitedSizeSet<Integer> s = new LimitedSizeSet<>(maxSize);
    for (int i = 1; i < 2 * maxSize; i++) {
      lsisAddElts(i, s);
      int size = s.size();
      assert ((i <= maxSize) ? (size == i) : (size == maxSize + 1))
          : String.format(
              "(%d<=%d) ? (%d==%d) : (%d==%d+1)   size=%d, i=%d, maxSize=%d, s=%s",
              i, maxSize, size, i, size, maxSize, size, i, maxSize, s);
    }
  }

  private static void lss_withNull_test() {
    LimitedSizeSet<@Nullable Integer> s = new LimitedSizeSet<>(10);
    s.add(1);
    s.add(2);
    s.add(null);
    assert s.size() == 3;
    assert s.contains(1);
    assert s.contains(null);
    s.add(3);
    assert s.size() == 4;
    assert s.contains(1);
    assert s.contains(null);
    assert s.contains(3);
  }

  @Test
  public void testLimitedSizeSet() {
    for (int i = 1; i < 10; i++) {
      lsis_test(i);
    }
    lss_withNull_test();
  }
}
