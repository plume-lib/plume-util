import java.util.Comparator;
import java.util.Objects;

/** A comparator that orders values based on the lexicographic ordering of their toString(). */
public class ToStringComparator implements Comparator<Object> {
  /** The unique instance (this class is a singleton). */
  public static ToStringComparator instance = new ToStringComparator();

  /** Creates a ToStringComparator. */
  private ToStringComparator() {}

  @Override
  public int compare(Object o1, Object o2) {
    return Objects.toString(o1).compareTo(Objects.toString(o2));
  }
}
