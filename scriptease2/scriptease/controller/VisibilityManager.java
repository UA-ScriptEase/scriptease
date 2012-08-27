package scriptease.controller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.observer.VisibilityManagerEvent;
import scriptease.controller.observer.VisibilityManagerObserver;
import scriptease.model.StoryComponent;

/**
 * VisibilityManager keeps track of the visibility of StoryComponents in
 * ScriptEase. Default visibility for a StoryComponent is true, unless otherwise
 * specified.
 * 
 * Implements the singleton design pattern.
 * 
 * @author mfchurch
 */
public class VisibilityManager {
	private Map<WeakReference<StoryComponent>, Boolean> visibilityMap;
	private Collection<WeakReference<VisibilityManagerObserver>> observers;
	private static VisibilityManager instance;

	private VisibilityManager() {
		this.visibilityMap = new IdentityHashMap<WeakReference<StoryComponent>, Boolean>();
		this.observers = new CopyOnWriteArraySet<WeakReference<VisibilityManagerObserver>>();
	}

	public static VisibilityManager getInstance() {
		if (instance == null)
			instance = new VisibilityManager();
		return instance;
	}

	/**
	 * Adds the given observer as a observer of all LibraryManager's libraries
	 * 
	 * @param observer
	 */
	public void addVisibilityManagerListener(VisibilityManagerObserver observer) {
		for (WeakReference<VisibilityManagerObserver> observerRef : this.observers) {
			VisibilityManagerObserver visibilityManagerObserver = observerRef
					.get();
			if (visibilityManagerObserver != null
					&& visibilityManagerObserver == observer)
				return;
		}
		this.observers.add(new WeakReference<VisibilityManagerObserver>(
				observer));
	}

	public void removeVisibilityManagerListener(
			VisibilityManagerObserver observer) {
		for (WeakReference<VisibilityManagerObserver> reference : this.observers) {
			if (reference.get() == observer) {
				this.observers.remove(reference);
				return;
			}
		}
	}

	private void notifyObservers(StoryComponent changed, short type) {
		Collection<WeakReference<VisibilityManagerObserver>> observersCopy = new ArrayList<WeakReference<VisibilityManagerObserver>>(
				this.observers);

		for (WeakReference<VisibilityManagerObserver> observerRef : observersCopy) {
			VisibilityManagerObserver visibilityManagerObserver = observerRef
					.get();
			if (visibilityManagerObserver != null) {
				final VisibilityManagerEvent event = new VisibilityManagerEvent(
						changed, type);
				visibilityManagerObserver.visibilityChanged(event);
			} else
				this.observers.remove(observerRef);
		}
	}

	public void setVisibility(StoryComponent component, boolean visible) {
		this.visibilityMap.put(new WeakReference<StoryComponent>(component),
				visible);
		final short change;
		if (visible) {
			change = VisibilityManagerEvent.VISIBILITY_ADDED;
		} else
			change = VisibilityManagerEvent.VISIBILITY_REMOVED;
		this.notifyObservers(component, change);
	}

	/**
	 * Checks if the given StoryComponent is visible. Defaults to true, if it
	 * has not been registered with the VisibilityManager.
	 * 
	 * @param component
	 * @return
	 */
	public Boolean isVisible(StoryComponent component) {
		for (Entry<WeakReference<StoryComponent>, Boolean> entry : this.visibilityMap
				.entrySet()) {
			if (entry.getKey().get() == component)
				return entry.getValue();
		}
		return true;
	}
}
