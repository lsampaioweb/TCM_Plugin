package net.thecodemaster.sap.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Luciano Sampaio
 */
public class Creator {

  /**
   * Return a List of type T.
   * 
   * @return List<T>
   */
  public static <T> List<T> newList() {
    return new ArrayList<T>();
  }

  /**
   * Return a List of type T.
   * 
   * @param initialCapacity the initial capacity of the List.
   * @return List<T>
   */
  public static <T> List<T> newList(int initialCapacity) {
    return new ArrayList<T>(initialCapacity);
  }

  /**
   * Return a Map of type T and W.
   * 
   * @return Map<T, W>
   */
  public static <T, W> Map<T, W> newMap() {
    return new LinkedHashMap<T, W>();
  }
}
