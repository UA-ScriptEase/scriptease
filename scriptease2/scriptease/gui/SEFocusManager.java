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
	private static Component focus;
	private static final SEFocusManager instance = new SEFocusManager();

	private static Map<Component, SEFocusObserver> observerMap = new WeakHashMap<Component, SEFocusObserver>();

	/**
	 * Gets the sole instance of the ComponentFocusManager.
	 * 
	 * @return
	 */
	public static SEFocusManager getInstance() {
		return SEFocusManager.instance;
	}

	/**
	 * Gets the currently focused on component.
	 * 
	 * @return
	 */
	public Component getFocus() {
		return SEFocusManager.focus;
	}

	/**
	 * Sets the current focus to the passed in component.
	 * 
	 * @param focus
	 */
	public void setFocus(Component focus) {
		final Component oldFocus;

		oldFocus = SEFocusManager.focus;

		SEFocusManager.focus = focus;

		for (Entry<Component, SEFocusObserver> entry : observerMap.entrySet()) {
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
	 * @param component
	 * @param observer
	 */
	public void addObserver(Component component, SEFocusObserver observer) {
		SEFocusManager.observerMap.put(component, observer);
	}
}
