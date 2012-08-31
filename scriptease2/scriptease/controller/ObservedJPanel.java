package scriptease.controller;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * This class exists to allow strong references to be generated based on
 * JComponent. We sometimes want our observers to live for the lifetime of a
 * JComponent, which isn't possible when we're just using WeakReferences.
 * Creating a static map of these observers just leads to memory leaks and other
 * issues. By using this JPanel, the observers and the components will be garbage
 * collected when the component is removed.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class ObservedJPanel extends JPanel {
	private final JComponent component;
	private final Object observer;

	/**
	 * Creates a new observed JComponent.
	 * 
	 * @param component
	 * @param observer
	 */
	public ObservedJPanel(JComponent component, Object observer) {
		super();
		this.component = component;
		this.observer = observer;
		
		this.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		this.setOpaque(false);
		
		this.add(component);
	}

	/**
	 * Returns the observer.
	 * 
	 * @return
	 */
	public Object getObserver() {
		return this.observer;
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
