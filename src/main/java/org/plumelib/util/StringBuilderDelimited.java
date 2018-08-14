package org.plumelib.util;

// NEEDS DOCUMENTATION!
// (Probably mostly Javadoc "see" directives, possibly with first line of relevant method doc.)
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Like StringBuilder, but adds a delimiter between each pair of strings that are inserted into the
 * Stringbuilder. This can simplify the logic of programs and also avoid errors.
 *
 * <p>Does not extend StringBuilder because that would probably break, due to the possibility of
 * calling the wrong version of append. Also, I don't (yet) want to override all the methods; this
 * simpler version seems sufficient for the time being.
 *
 * <p>Obsoleted by the Java 8 StringJoiner class. Use only if your code must compile or run under
 * Java 7.
 *
 * @deprecated use StringJoiner
 */
@Deprecated // use StringJoiner
public class StringBuilderDelimited implements Appendable, CharSequence {

  /** The StringBuilder to which this delegates work. */
  private StringBuilder delegate = new StringBuilder();
  /** False iff some text has already been appended to this. */
  private boolean empty = true;
  /** The delimiter put between strings appended to this. */
  private final String delimiter;

  /**
   * Create a new StringBuilderDelimited.
   *
   * @param delimiter the delimiter to be put between strings that are appended to this
   */
  public StringBuilderDelimited(String delimiter) {
    this.delimiter = delimiter;
  }

  /** Append the delimiter to this. */
  private void appendDelimiter(@GuardSatisfied StringBuilderDelimited this) {
    if (empty) {
      empty = false;
    } else {
      delegate.append(delimiter);
    }
  }

  /**
   * Appends the specified character sequence to this.
   *
   * @param newElement the character sequence to append to this StringBuilderDelimited
   * @return a reference to this StringBuilderDelimited
   */
  public StringBuilderDelimited add(@Nullable CharSequence newElement) {
    appendDelimiter();
    delegate.append(newElement);
    return this;
  }

  /**
   * Appends the specified string to this.
   *
   * @param str the string to append to this StringBuilderDelimited
   * @return a reference to this StringBuilderDelimited
   * @deprecated Use #add(CharSequence); change sbd.append(str) into sbd.add(str)
   */
  @Deprecated
  public StringBuilderDelimited append(@Nullable String str) {
    appendDelimiter();
    delegate.append(str);
    return this;
  }

  /**
   * Appends the specified object's printed representatioin to this.
   *
   * @param o the object whose toString() to append to this StringBuilderDelimited
   * @return a reference to this StringBuilderDelimited
   * @deprecated Use #add(CharSequence); change sbd.append(o) into sbd.add(o.toString())
   */
  @Deprecated
  public StringBuilderDelimited append(@Nullable Object o) {
    appendDelimiter();
    delegate.append(o);
    return this;
  }

  /**
   * Appends the specified character to this.
   *
   * @param c the character to append to this StringBuilderDelimited
   * @return a reference to this StringBuilderDelimited
   * @deprecated Use #add(CharSequence); change sbd.append(c) into sbd.add(c.toString())
   */
  @Deprecated
  @Override
  public StringBuilderDelimited append(char c) {
    appendDelimiter();
    delegate.append(c);
    return this;
  }

  /**
   * Appends the specified character sequence to this.
   *
   * @param csq the character sequence to append to this StringBuilderDelimited
   * @return a reference to this StringBuilderDelimited
   * @deprecated Use #add(CharSequence); change sbd.append(csq) into sbd.add(csq)
   */
  @Deprecated
  @Override
  public StringBuilderDelimited append(@Nullable CharSequence csq) {
    appendDelimiter();
    delegate.append(csq);
    return this;
  }

  /**
   * @deprecated Use #add(CharSequence); change sbd.append(csq, start, end) into
   *     sbd.add(csq.subSequence(start, end)
   */
  @Deprecated
  @Override
  public StringBuilderDelimited append(
      @Nullable CharSequence csq, @IndexOrHigh("#1") int start, @IndexOrHigh("#1") int end) {
    appendDelimiter();
    delegate.append(csq, start, end);
    return this;
  }

  /** @deprecated Not supported by StringJoiner which will supersede this */
  @Deprecated
  @Override
  public char charAt(@IndexFor("this") int index) {
    return delegate.charAt(index);
  }

  @Pure
  @Override
  @SuppressWarnings("upperbound:override.return.invalid") // mutable-length subclass of CharSequence
  public @NonNegative int length(@GuardSatisfied StringBuilderDelimited this) {
    return delegate.length();
  }

  /** @deprecated Not supported by StringJoiner which will supersede this */
  @Deprecated
  @Override
  public CharSequence subSequence(@IndexOrHigh("this") int start, @IndexOrHigh("this") int end) {
    return delegate.subSequence(start, end);
  }

  @SideEffectFree
  @Override
  @SuppressWarnings("samelen:override.return.invalid") // mutable-length subclass of CharSequence
  public String toString(@GuardSatisfied StringBuilderDelimited this) {
    return delegate.toString();
  }
}
