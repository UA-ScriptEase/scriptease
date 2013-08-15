package scriptease.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Common list operations such as identity comparisons and equality comparisons
 * 
 * @author mfchurch
 * 
 */
public class ListOp {

	/**
	 * Returns if each element in the given lists are .equal
	 * 
	 * @param <T>
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static <T> boolean equalLists(List<T> list1, List<T> list2) {
		final int size = list1.size();
		boolean equal = (size == list2.size());
		for (int i = 0; i < size; i++) {
			if (equal) {
				final T t1 = list1.get(i);
				final T t2 = list2.get(i);
				equal &= t1.equals(t2);
			} else
				break;
		}
		return equal;
	}

	/**
	 * Returns if each element in the given lists are identity equal (==)
	 * 
	 * @param <T>
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static <T> boolean identityEqualLists(List<T> list1, List<T> list2) {
		final int size = list1.size();
		boolean equal = (size == list2.size());
		for (int i = 0; i < size; i++) {
			if (equal) {
				final T t1 = list1.get(i);
				final T t2 = list2.get(i);
				equal &= (t1 == t2);
			} else
				break;
		}
		return equal;
	}

	/**
	 * Returns if the given collection contains an object .equal to the given
	 * key
	 * 
	 * @param <T>
	 * @param collection
	 * @param key
	 * @return
	 */
	public static <T> boolean equalsContains(Collection<T> collection, T key) {
		return ListOp.equalsRetrieve(collection, key) != null;
	}

	/**
	 * Returns if the given collection of lists contains a list identity equal
	 * (=) to the given list key
	 * 
	 * @param <T>
	 * @param collection
	 * @param key
	 * @return
	 */
	public static <T> boolean identityContains(Collection<List<T>> collection,
			List<T> key) {
		for (List<T> subCollection : collection) {
			if (ListOp.identityEqualLists(subCollection, key))
				return true;
		}
		return false;
	}

	/**
	 * Returns if the given collection contains an object identity equal (==) to
	 * the given key
	 * 
	 * @param <T>
	 * @param collection
	 * @param key
	 * @return
	 */
	public static <T> boolean identityContains(Collection<T> collection, T key) {
		return ListOp.identityEqualsRetrieve(collection, key) != null;
	}

	/**
	 * Returns the object in the given collection which is .equals to the key.
	 * Returns null if none are found
	 * 
	 * @param <T>
	 * @param collection
	 * @param key
	 * @return
	 */
	public static <T> T equalsRetrieve(Collection<T> collection, T key) {
		for (T object : collection)
			if (object.equals(key))
				return object;
		return null;
	}

	/**
	 * Returns the object in the given collection which is identity equal (==)
	 * to the key. Returns null if none are found
	 * 
	 * @param <T>
	 * @param collection
	 * @param key
	 * @return
	 */
	public static <T> T identityEqualsRetrieve(Collection<T> collection, T key) {
		for (T object : collection)
			if (object == key)
				return object;
		return null;
	}
	
	public static <T> ArrayList<T> createList(T... contents) {
		final ArrayList<T> list = new ArrayList<T>();
		
		for(T content : contents) {
			list.add(content);
		}
		
		return list;
	}
}
