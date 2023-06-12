package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLen;
import org.junit.jupiter.api.Test;

/** Test the OrderedPairIterator class. */
public final class OrderedPairIteratorTest {

  @Test
  public void testOrderedPairIterator() {
    final int NULL = -2222;

    ArrayList<Integer> ones = new ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      ones.add(i);
    }
    ArrayList<Integer> twos = new ArrayList<>();
    for (int i = 2; i <= 30; i += 2) {
      twos.add(i);
    }
    ArrayList<Integer> threes = new ArrayList<>();
    for (int i = 3; i <= 30; i += 3) {
      threes.add(i);
    }

    compareOrderedPairIterator(
        new OrderedPairIterator<Integer>(ones.iterator(), ones.iterator()),
        new int[][] {
          {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}, {8, 8}, {9, 9}, {10, 10},
          {11, 11}, {12, 12}, {13, 13}, {14, 14}, {15, 15}, {16, 16}, {17, 17}, {18, 18}, {19, 19},
          {20, 20}, {21, 21}, {22, 22}, {23, 23}, {24, 24}, {25, 25}, {26, 26}, {27, 27}, {28, 28},
          {29, 29}, {30, 30},
        });

    compareOrderedPairIterator(
        new OrderedPairIterator<Integer>(ones.iterator(), twos.iterator()),
        new int[][] {
          {1, NULL},
          {2, 2},
          {3, NULL},
          {4, 4},
          {5, NULL},
          {6, 6},
          {7, NULL},
          {8, 8},
          {9, NULL},
          {10, 10},
          {11, NULL},
          {12, 12},
          {13, NULL},
          {14, 14},
          {15, NULL},
          {16, 16},
          {17, NULL},
          {18, 18},
          {19, NULL},
          {20, 20},
          {21, NULL},
          {22, 22},
          {23, NULL},
          {24, 24},
          {25, NULL},
          {26, 26},
          {27, NULL},
          {28, 28},
          {29, NULL},
          {30, 30},
        });

    compareOrderedPairIterator(
        new OrderedPairIterator<Integer>(twos.iterator(), ones.iterator()),
        new int[][] {
          {NULL, 1},
          {2, 2},
          {NULL, 3},
          {4, 4},
          {NULL, 5},
          {6, 6},
          {NULL, 7},
          {8, 8},
          {NULL, 9},
          {10, 10},
          {NULL, 11},
          {12, 12},
          {NULL, 13},
          {14, 14},
          {NULL, 15},
          {16, 16},
          {NULL, 17},
          {18, 18},
          {NULL, 19},
          {20, 20},
          {NULL, 21},
          {22, 22},
          {NULL, 23},
          {24, 24},
          {NULL, 25},
          {26, 26},
          {NULL, 27},
          {28, 28},
          {NULL, 29},
          {30, 30},
        });

    compareOrderedPairIterator(
        new OrderedPairIterator<Integer>(ones.iterator(), threes.iterator()),
        new int[][] {
          {1, NULL},
          {2, NULL},
          {3, 3},
          {4, NULL},
          {5, NULL},
          {6, 6},
          {7, NULL},
          {8, NULL},
          {9, 9},
          {10, NULL},
          {11, NULL},
          {12, 12},
          {13, NULL},
          {14, NULL},
          {15, 15},
          {16, NULL},
          {17, NULL},
          {18, 18},
          {19, NULL},
          {20, NULL},
          {21, 21},
          {22, NULL},
          {23, NULL},
          {24, 24},
          {25, NULL},
          {26, NULL},
          {27, 27},
          {28, NULL},
          {29, NULL},
          {30, 30},
        });

    compareOrderedPairIterator(
        new OrderedPairIterator<Integer>(twos.iterator(), threes.iterator()),
        new int[][] {
          {2, NULL},
          {NULL, 3},
          {4, NULL},
          {6, 6},
          {8, NULL},
          {NULL, 9},
          {10, NULL},
          {12, 12},
          {14, NULL},
          {NULL, 15},
          {16, NULL},
          {18, 18},
          {20, NULL},
          {NULL, 21},
          {22, NULL},
          {24, 24},
          {26, NULL},
          {NULL, 27},
          {28, NULL},
          {30, 30},
        });
  }

  /**
   * Throws an assertion unless the paired iterator contains the same values as the argument array.
   * Requires that size of opi = ints.length.
   *
   * @param opi an iterator over pairs of integers
   * @param ints an array of two-element arrays of integers
   * @throws AssertionError iff the iterator returns the same values as the argument array contains
   */
  @SuppressWarnings(
      "index:array.access.unsafe.high" // same length iterator and array, and while loop with ++ on
  // index
  )
  public static void compareOrderedPairIterator(
      OrderedPairIterator<Integer> opi, int[] @ArrayLen(2) [] ints) {
    int pairno = 0;
    while (opi.hasNext()) {
      MPair<@Nullable Integer, @Nullable Integer> pair = opi.next();
      if (false) {
        System.out.printf(
            "Iterator: <%s,%s>, array: <%s,%s>%n",
            pair.first, pair.second, ints[pairno][0], ints[pairno][1]);
      }
      assertTrue((pair.first == null) || (pair.first.intValue() == ints[pairno][0]));
      assertTrue((pair.second == null) || (pair.second.intValue() == ints[pairno][1]));
      pairno++;
    }
    assertTrue(pairno == ints.length);
  }
}
