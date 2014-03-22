package net.thecodemaster.sap.utils;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Luciano Sampaio
 */
public class Creator {

  /**
   * Return a Collection of type T.
   * 
   * @return Collection<T>
   */
  public static <T> Collection<T> newCollection() {
    return new HashSet<T>();
  }

  /**
   * Return a Collection of type T.
   * 
   * @param initialCapacity the initial capacity of the Collection.
   * @return Collection<T>
   */
  public static <T> Collection<T> newCollection(int initialCapacity) {
    return new HashSet<T>(initialCapacity);
  }
}
