package net.thecodemaster.esvd.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class knows how to create generic lists for our application. We are using ArrayList for lists and LinkedHashMap
 * for maps, if we change the type here, all the application will be instantly affected.
 * 
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
	 * @param initialCapacity
	 *          the initial capacity of the List.
	 * @return List<T>
	 */
	public static <T> List<T> newList(int initialCapacity) {
		return new ArrayList<T>(initialCapacity);
	}

	/**
	 * Constructs a list containing the elements of the specified collection, in the order they are returned by the
	 * collection's iterator.
	 * 
	 * @param: collection The collection whose elements are to be placed into this list
	 * @return List<T>
	 */
	public static <T> List<T> newList(Collection<T> collection) {
		List<T> list = newList(collection.size());
		list.addAll(collection);
		return list;
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
