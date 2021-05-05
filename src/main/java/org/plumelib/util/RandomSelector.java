package org.plumelib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * RandomSelector selects k elements uniformly at random from an arbitrary iterator, using O(k)
 * space. A naive algorithm would use O(n) space. For example, selecting 1 element from a FileStream
 * containing 1000 elements will take O(1) space. The class takes as input the number k during
 * initialization and then can accept() any number of Objects in the future. At any point in time,
 * getValues() will either return k randomly selected elements from the elements previous accepted
 * or if accept() was called fewer than k times, will return all elements previously accepted.
 *
 * <p>The random selection is independent between every constructed instance of RandomSelector
 * objects, but for the same instance, multiple calls to getValues() are not independent. Making two
 * calls to consecutive getValues() without an accept() in between will return two new Lists
 * containing the same elements.
 *
 * <p>A second mode allows for a fixed probability of randomly keeping each item as opposed to a
 * fixed number of samples.
 *
 * <p>SPECFIELDS: <br>
 * values : Set : The values chosen based on the Objects observed <br>
 * observed : int : The number of Objects observed <br>
 * numElts : int : The number of elements to choose ('k' above) <br>
 * keepProbability: double : The percentage of elements to keep <br>
 * selector_mode : {FIXED,PERCENT} : either fixed amount of samples or fixed percent.
 *
 * <p>Example use:
 *
 * <pre>{@code
 * // randomly selects 100 lines of text from a file
 * List selectedLines = null;
 * try {
 *    BufferedReader br = new BufferedReader(new FileReader("myfile.txt"));
 *    RandomSelector selector = new RandomSelector(100);
 *    while (br.ready()) {
 *      selector.accept(br.readLine());
 *    }
 *    selectedLines = selector.getValues();
 *  }
 *  catch (IOException e2) { e2.printStackTrace(); }
 * }</pre>
 *
 * @param <T> the type of elements being selected over
 */
public class RandomSelector<T> {

  // Rep Invariant: values != null && values.size() <= numElts &&
  //                ((numElts == -1 && coinTossMode == true) ||
  //                 (keepProbability == -1.0 && coinTossMode == false))

  // Abstraction Function:
  // 1. for all elements, 'val' of AF(values),
  //    this.values.indexOf (val) != -1
  // 2. AF(observed) = this.observed
  // 3. AF(numElts) = this.numElts
  // 4. AF(keepProbability) = this.keepProbability
  // 5. AF(selector_mode) = fixed amount if coinTossMode == true
  //                        fixed percentage if coinTossMode == false

  /** If true, numElts and observers are -1. If false, keepProbability = -1. */
  private boolean coinTossMode;
  /** The percentage of elements to keep. */
  private double keepProbability = -1.0;
  /** The number of objects to choose, or -1. */
  private int numElts = -1;
  /** The number of objects observed. */
  private int observed = -1;

  /** The Random instance to use (for reproducibility). */
  private Random generator;

  /** The values chosen. */
  private ArrayList<T> values = new ArrayList<>();

  /**
   * @param numElts the number of elements intended to be selected from the input elements
   *     <p>Sets 'numElts' = numElts
   */
  public RandomSelector(int numElts) {
    this(numElts, new Random());
  }

  /**
   * @param numElts the number of elements intended to be selected from the input elements
   * @param r the seed to give for random number generation.
   *     <p>Sets 'numElts' = numElts.
   */
  public RandomSelector(int numElts, Random r) {
    coinTossMode = false;
    this.numElts = numElts;
    observed = 0;
    generator = r;
  }

  /**
   * @param keepProbability the probability that each element is selected from the oncoming
   *     Iteration
   * @param r the seed to give for random number generation
   */
  public RandomSelector(double keepProbability, Random r) {
    coinTossMode = true;
    this.keepProbability = keepProbability;
    generator = r;
  }

  /**
   * When in fixed sample mode, increments the number of observed elements i by 1, then with
   * probability k / i, the Object 'next' will be added to the currently selected values 'values'
   * where k is equal to 'numElts'. If the size of values exceeds numElts, then one of the existing
   * elements in values will be removed at random.
   *
   * <p>When in probability mode, adds next to 'values' with probability equal to 'keepProbability'.
   *
   * @param next value to be added to this selector
   */
  public void accept(T next) {

    // if we are in coin toss mode, then we want to keep
    // with probability == keepProbability.
    if (coinTossMode) {
      if (generator.nextDouble() < keepProbability) {
        values.add(next);
        // System.out.println ("ACCEPTED " + keepProbability );
      } else {
        // System.out.println ("didn't accept " + keepProbability );
      }
      return;
    }

    // in fixed sample mode, the i-th element has a k/i chance
    // of being accepted where k is numElts.
    if (generator.nextDouble() < ((double) numElts / ++observed)) {
      if (values.size() < numElts) {
        values.add(next);
      } else {
        @SuppressWarnings("lowerbound:argument") // no list support
        int rem = generator.nextInt(values.size());
        // values should be MinLen(1), meaning that values.size() is positive.
        values.set(rem, next);
      }
    }
    // do nothing if the probability condition is not met
  }

  /**
   * Returns values, modifies none.
   *
   * @return values
   */
  public List<T> getValues() {
    // avoid concurrent mod errors and rep exposure
    ArrayList<T> ret = new ArrayList<>();
    ret.addAll(values);
    return ret;
  }
}
