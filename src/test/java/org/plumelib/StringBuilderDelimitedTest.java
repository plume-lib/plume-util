package org.plumelib.util;

import org.junit.Test;

/** Test code for the plume package. */
@SuppressWarnings({
  "UseCorrectAssertInTests" // I don't see the problem with using `assert`
})
public final class StringBuilderDelimitedTest {

  @Test
  public void testStringBuilderDelimited() {
    compareJoinAndSBD(new String[] {"foo", "bar", "baz"});
    compareJoinAndSBD(new String[] {"foo"});
    compareJoinAndSBD(new String[] {});
  }

  public void compareJoinAndSBD(String[] strings) {
    StringBuilderDelimited sbd = new StringBuilderDelimited(",");
    for (String str : strings) {
      sbd.add(str);
    }
    assert sbd.toString().equals(UtilPlume.join(strings, ","));
  }
}
