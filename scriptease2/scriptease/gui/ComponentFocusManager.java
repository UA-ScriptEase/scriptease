package scriptease.gui;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * A simple manager class that allows us to store which component currently has
 * focus. A common way to set focus is to use a FocusListener on a component.
 * This allows us to track focus only on specific components rather than every
 * component ever.
 * 
 * @see java.awt.event.FocusListener
 * 
 * @author kschenk
 */
public class ComponentFocusManager {
	private static Component focus;
	private static final ComponentFocusManager instance = new ComponentFocusManager();

	/**
	 * Gets the sole instance of the ComponentFocusManager.
	 * 
	 * @return
	 */
	public static ComponentFocusManager getInstance() {
		return instance;
	}

	/**
	 * Sets the current focus to the passed in component.
	 * 
	 * @param focus
	 */
	public void setFocus(Component focus) {
		ComponentFocusManager.focus = focus;
	}

	/**
	 * Gets the currently focused on component.
	 * 
	 * @return
	 */
	public Component getFocus() {
		return ComponentFocusManager.focus;
	}

	/**
	 * Sets up a default focus listener for the passed in component. This
	 * listener simple sets the focus to the passed in component when focus is
	 * gained. No events fire when focus is lost.
	 * 
	 * @param focus
	 * @return
	 */
	public FocusListener defaultFocusListener(final Component focus) {
		return new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				ComponentFocusManager.getInstance().setFocus(focus);
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		};
	}

}
