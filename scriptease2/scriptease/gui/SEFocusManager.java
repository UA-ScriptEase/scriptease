package scriptease.gui;

import java.awt.Component;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import scriptease.controller.observer.SEFocusObserver;

/**
 * A simple manager class that allows us to store which component currently has
 * focus. A common way to set focus is to use a FocusListener on a component.
 * This allows us to track focus only on specific components rather than every
 * component ever.<br>
 * <br>
 * We also keep our collection of SEFocusObservers here in a weak hashmap.
 * 
 * @see java.awt.event.FocusListener
 * 
 * @author kschenk
 */
public class SEFocusManager {
	private Component focus;
	private final Map<Object, SEFocusObserver> observerMap;

	private static final SEFocusManager instance = new SEFocusManager();

	/**
	 * Gets the sole instance of the ComponentFocusManager.
	 * 
	 * @return
	 */
	public static SEFocusManager getInstance() {
		return SEFocusManager.instance;
	}

	private SEFocusManager() {
		observerMap = new WeakHashMap<Object, SEFocusObserver>();
	}

	/**
	 * Gets the currently focused on component.
	 * 
	 * @return
	 */
	public Component getFocus() {
		return this.focus;
	}

	/**
	 * Sets the current focus to the passed in component.
	 * 
	 * @param focus
	 */
	public void setFocus(Component focus) {
		final Component oldFocus;

		oldFocus = this.focus;

		this.focus = focus;

		for (Entry<Object, SEFocusObserver> entry : observerMap.entrySet()) {
			if (entry.getKey() == focus)
				entry.getValue().gainFocus(oldFocus);
			else
				entry.getValue().loseFocus(oldFocus);
		}
	}

	/**
	 * Add an observer to the SEFocusManager so that it will fire events when
	 * SEFocus changes. This observer will not get garbage collected until the
	 * component disappears.
	 * 
	 * @param object
	 * @param observer
	 */
	public void addSEFocusObserver(Object object, SEFocusObserver observer) {
		this.observerMap.put(object, observer);
	}

	/**
	 * Add an observer to the SEFocusManager so that it will fire events when
	 * SEFocus changes. This observer will get garbage collected if its
	 * reference is removed elsewhere. If we are not storing this observer
	 * elsewhere, use {@link #addSEFocusObserver(Object, SEFocusObserver)}
	 * instead.
	 * 
	 * @param object
	 * @param observer
	 */
	public void addSEFocusObserver(SEFocusObserver observer) {
		this.observerMap.put(observer, observer);
	}
}
