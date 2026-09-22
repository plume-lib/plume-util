package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicLong;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.junit.jupiter.api.Test;

/** Test the UniqueId interface. */
final class UniqueIdTest {

  UniqueIdTest() {}

  /** A class that implements {@link UniqueId} using the recommended code snippet. */
  private static final class Widget implements UniqueId {

    /** The unique ID for the next-created object. */
    private static final AtomicLong nextUid = new AtomicLong(0);

    /** The unique ID of this object. */
    private final long uid = nextUid.getAndIncrement();

    /** Creates a Widget. */
    Widget() {}

    @Override
    public long getUid(@UnknownInitialization(UniqueId.class) Widget this) {
      return uid;
    }
  }

  @Test
  void uidsAreDistinctAndConsecutive() {
    Widget w1 = new Widget();
    Widget w2 = new Widget();
    Widget w3 = new Widget();
    assertNotEquals(w1.getUid(), w2.getUid());
    assertEquals(w1.getUid() + 1, w2.getUid());
    assertEquals(w2.getUid() + 1, w3.getUid());
  }

  @Test
  void uidIsStableAcrossCalls() {
    Widget w = new Widget();
    assertEquals(w.getUid(), w.getUid());
  }

  @Test
  void getClassAndUidCombinesSimpleNameAndUid() {
    Widget w = new Widget();
    assertEquals("Widget#" + w.getUid(), w.getClassAndUid());
  }

  @Test
  void uniqueIdIsAFunctionalInterface() {
    UniqueId u = () -> 42;
    assertEquals(42, u.getUid());
    assertTrue(u.getClassAndUid().endsWith("#42"));
  }
}
