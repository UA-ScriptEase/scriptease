package scriptease.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
 * 
 */
public class VisibilityManager {
	private Map<StoryComponent, Boolean> visibilityMap;
	private Collection<VisibilityManagerObserver> listeners;
	private static VisibilityManager instance;

	private VisibilityManager() {
		this.visibilityMap = new HashMap<StoryComponent, Boolean>();
		this.listeners = new CopyOnWriteArraySet<VisibilityManagerObserver>();
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
		this.listeners.add(observer);
	}

	public void removeVisibilityManagerListener(
			VisibilityManagerObserver observer) {
		this.listeners.remove(observer);
	}

	private void notifyObservers(StoryComponent changed, short type) {
		for (VisibilityManagerObserver observer : this.listeners) {
			final VisibilityManagerEvent event = new VisibilityManagerEvent(
					changed, type);
			observer.visibilityChanged(event);
		}
	}

	public void setVisibility(StoryComponent component, boolean visible) {
		this.visibilityMap.put(component, visible);
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
		Boolean isVisible;
		if (!this.visibilityMap.containsKey(component))
			isVisible = true;
		else
			isVisible = this.visibilityMap.get(component);
		return isVisible;
	}
}
