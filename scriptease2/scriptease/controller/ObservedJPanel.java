package scriptease.controller;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * This class exists to allow strong references to be generated based on
 * JComponent. We sometimes want our observers to live for the lifetime of a
 * JComponent, which isn't possible when we're just using WeakReferences.
 * Creating a static map of these observers just leads to memory leaks and other
 * issues. By using this JPanel, the observers and the components will be
 * garbage collected when the component is removed.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class ObservedJPanel extends JPanel {
	private final JComponent component;
	private Collection<Object> observers;

	/**
	 * Creates a new observed JPanel with the added component.
	 * 
	 * @param component
	 */
	public ObservedJPanel(JComponent component) {
		this(component, new ArrayList<Object>());
	}

	/**
	 * Creates a new observed JPanel.
	 * 
	 * @param component
	 * @param observer
	 */
	public ObservedJPanel(JComponent component, Collection<Object> observers) {
		super();
		this.component = component;
		this.observers = new ArrayList<Object>();

		this.observers.addAll(observers);

		this.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		this.setOpaque(false);
		
		this.add(component);
	}

	/**
	 * Add the observer to the ObservedJPanel.
	 * 
	 * @param observer
	 * @return
	 */
	public boolean addObserver(Object observer) {
		return this.observers.add(observer);
	}

	/**
	 * Removes the observer from the ObservedJPanel.
	 * 
	 * @param observer
	 * @return
	 */
	public boolean removeObserver(Object observer) {
		return this.observers.remove(observer);
	}

	/**
	 * Returns the observers.
	 * 
	 * @return
	 */
	public Collection<Object> getObservers() {
		return this.observers;
	}

	/**
	 * Returns the JComponent.
	 * 
	 * @return
	 */
	public JComponent getObservedComponent() {
		return this.component;
	}
}
