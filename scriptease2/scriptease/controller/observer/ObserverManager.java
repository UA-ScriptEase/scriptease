package scriptease.controller.observer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

/**
 * A manager for observers. We store a weak key so that the observer can get
 * garbage collected if it is no longer used. This class cannot be sub-classed.
 * Classes with an ObserverManager should implement their own ways of
 * interacting with their ObserverManager.
 * 
 * @author kschenk
 * 
 * @param <O>
 *            The observer type to observe
 */
public final class ObserverManager<O> {

	private final Map<Object, Collection<O>> observerMap;

	public ObserverManager() {
		this.observerMap = new WeakHashMap<Object, Collection<O>>();
	}

	/**
	 * Adds an observer to this pool's list of observers to notify when a change
	 * to the pool occurs.
	 * 
	 * @param observer
	 *            the listener to add
	 */
	public void addObserver(Object object, O observer) {
		Collection<O> value = this.observerMap.get(object);

		if (value == null) {
			value = new HashSet<O>();
		}

		value.add(observer);

		this.observerMap.put(object, value);
	}

	/**
	 * Removes a specific observer from this pool's list of observers to notify
	 * when a change to the pool occurs.
	 * 
	 * @param observer
	 *            the observer to remove
	 */
	public void removeObserver(O observer) {
		for (Entry<Object, Collection<O>> entry : this.observerMap.entrySet()) {
			final Collection<O> observers;

			observers = entry.getValue();

			if (observers.contains(observer)) {
				observers.remove(observer);
				break;
			}
		}
	}

	/**
	 * Returns all of the observers stored by the observer manager.
	 * 
	 * @return
	 */
	public Collection<O> getObservers() {
		final Collection<O> observers = new ArrayList<O>();

		for (Collection<O> value : this.observerMap.values()) {
			observers.addAll(value);
		}

		return observers;
	}
}
