package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test the CombinationIterator class. */
@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class CombinationIteratorTest {

  List<String> a = Arrays.asList("a1", "a2");
  List<String> b = Arrays.asList("b1");
  List<String> c = Arrays.asList("c1", "c2", "c3");
  List<String> empty = Arrays.asList();

  @Test
  public void test1() {
    CombinationIterator<String> ci = new CombinationIterator<>(Arrays.asList(a, b, c));
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("a1", "b1", "c1"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("a1", "b1", "c2"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("a1", "b1", "c3"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("a2", "b1", "c1"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("a2", "b1", "c2"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("a2", "b1", "c3"), ci.next());
    assert !ci.hasNext();
    assert !ci.hasNext();
  }

  @Test
  public void test2() {
    CombinationIterator<String> ci = new CombinationIterator<>(Arrays.asList(c, b, a));
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("c1", "b1", "a1"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("c1", "b1", "a2"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("c2", "b1", "a1"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("c2", "b1", "a2"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("c3", "b1", "a1"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("c3", "b1", "a2"), ci.next());
    assert !ci.hasNext();
    assert !ci.hasNext();
  }

  @Test
  public void test3() {
    CombinationIterator<String> ci = new CombinationIterator<>(Arrays.asList(a));
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("a1"), ci.next());
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("a2"), ci.next());
    assert !ci.hasNext();
    assert !ci.hasNext();
  }

  @Test
  public void test4() {
    CombinationIterator<String> ci = new CombinationIterator<>(Arrays.asList(b));
    assert ci.hasNext();
    assert ci.hasNext();
    assertEquals(Arrays.asList("b1"), ci.next());
    assert !ci.hasNext();
    assert !ci.hasNext();
  }

  @Test
  public void testEmpty1() {
    CombinationIterator<String> ci = new CombinationIterator<>(Arrays.asList(a, empty, c));
    assert !ci.hasNext();
    assert !ci.hasNext();
  }

  @Test
  public void testEmpty2() {
    CombinationIterator<String> ci = new CombinationIterator<>(Arrays.asList(empty, b, c));
    assert !ci.hasNext();
    assert !ci.hasNext();
  }

  @Test
  public void testEmpty3() {
    CombinationIterator<String> ci = new CombinationIterator<>(Arrays.asList(a, b, empty));
    assert !ci.hasNext();
    assert !ci.hasNext();
  }

  @Test
  public void testEmpty4() {
    CombinationIterator<String> ci = new CombinationIterator<>(Arrays.asList(empty));
    assert !ci.hasNext();
    assert !ci.hasNext();
  }

  @Test
  public void testEmpty5() {
    CombinationIterator<String> ci = new CombinationIterator<>(Arrays.asList());
    assert !ci.hasNext();
    assert !ci.hasNext();
  }
}
