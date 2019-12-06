package org.plumelib.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

@SuppressWarnings({
  "deprecation" // StringBuilderDelimited is deprecated
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
    assertTrue(sbd.toString().equals(UtilPlume.join(strings, ",")));
  }
}
