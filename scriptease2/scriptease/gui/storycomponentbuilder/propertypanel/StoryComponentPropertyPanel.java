package scriptease.gui.storycomponentbuilder.propertypanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JPanel;

import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;

/**
 * StoryComponentPropertyPanel is a JPanel used to represent a editable
 * StoryComponent in a StoryComponentBuilder. It has a button and takes an
 * actionlistener so that when pressed it will execute the actionlistener.
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public abstract class StoryComponentPropertyPanel extends JPanel implements
		StoryComponentObserver {
	private Color HIGHLIGHT_COLOUR = Color.YELLOW;
	private Color BACKGROUND_COLOUR = Color.WHITE;
	private JPanel displayPanel;
	protected ExpansionButtonSCB expansionButton;
	protected StoryComponent component;
	protected StoryComponent parent;

	public StoryComponentPropertyPanel(StoryComponent component) {
		this.setComponent(component);
		this.setLayout(new StoryComponentPropertyPanelLayoutManager());
	}

	public void setComponent(StoryComponent component) {
		// remove old observer
		if (this.component != null)
			this.component.removeStoryComponentObserver(this);

		// observer new parameter
		this.component = component;
		
		this.component.addStoryComponentObserver(this);

		// rebuild the panel to reflect the parameter
		updatePanel();
	}

	/**
	 * Builds the displayPanel. Should be implemented by subclasses to specify
	 * what is displayed.
	 * 
	 * @return
	 */
	protected abstract JPanel buildDisplayPanel();

	/**
	 * Builds the panel to reflect the current parameter
	 */
	private void updatePanel() {
		// remove old panel
		if (this.displayPanel != null)
			this.remove(displayPanel);

		// add new panel
		this.displayPanel = buildDisplayPanel();
		this.add(displayPanel);

		// layout
		doLayout();
	}
	
	//why you so null....
	public void setButtonAction(ActionListener action, StoryComponent parent) {
		this.expansionButton.addActionListener(action);
		this.expansionButton.setComp(component);
		this.expansionButton.setParent(parent);
		//this.expansionButton.add(comp);
		////////CHECK IT OUT/////////
		//this.expansionButton.setActionCommand("EXPAND");
	}
	
	public ExpansionButtonSCB getExpansionBut(){
		return expansionButton;
	}
	
	public void setExpansionButtonComp(StoryComponent comp){
		
	}
	
	public void setExpansionButtonParentComp(StoryComponent parent){
		
	}
	
	public void setHighlight(boolean shouldHighlight) {
		final Color color = shouldHighlight ? HIGHLIGHT_COLOUR
				: BACKGROUND_COLOUR;
		this.setBackground(color);
		repaint();
	}

	private class StoryComponentPropertyPanelLayoutManager implements
			LayoutManager {
		private final int BUTTON_X_INDENT = 5;

		public StoryComponentPropertyPanelLayoutManager() {
			// button
			final Icon icon = ScriptEaseUI.EXPAND_ICON;
			//expansionButton = new JButton(icon);
			//expansionButton = new ExpansionButton(icon);
			expansionButton = new ExpansionButtonSCB(icon);
			Dimension iconDimension = new Dimension(icon.getIconWidth() + 1,
					icon.getIconHeight() + 1);
			expansionButton.setPreferredSize(iconDimension);
			expansionButton.setFocusable(false);

			// add the button so it will display
			//StoryComponentPropertyPanel.this.add(expansionButton);
			StoryComponentPropertyPanel.this.add(expansionButton);
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return minimumLayoutSize(parent);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			final Insets insets = parent.getInsets();
			int xSize = 0;
			int ySize = 0;

			// Add the KnowIt Panel size
			xSize += displayPanel.getPreferredSize().getWidth();
			ySize = Math.max(ySize, (int) displayPanel.getPreferredSize()
					.getHeight());

			// Expansion button
			int buttonHeight = (int) expansionButton.getPreferredSize()
					.getHeight();
			int buttonWidth = (int) expansionButton.getPreferredSize()
					.getWidth();

			// Add the button indent
			xSize += buttonWidth + BUTTON_X_INDENT;
			ySize = Math.max(ySize, buttonHeight);

			return new Dimension(xSize + insets.left + insets.right, ySize
					+ insets.top + insets.bottom);
		}

		@Override
		public void layoutContainer(Container parent) {
			final Insets insets = parent.getInsets();
			int xLocation = insets.left;
			int yLocation = insets.top;

			// component Panel
			int panelWidth = (int) displayPanel.getPreferredSize().getWidth();
			int panelHeight = (int) displayPanel.getPreferredSize().getHeight();
			displayPanel.setBounds(xLocation, yLocation, panelWidth,
					panelHeight);

			// Add the button indent
			xLocation += panelWidth + BUTTON_X_INDENT;

			// Expansion button
			int buttonHeight = (int) expansionButton.getPreferredSize()
					.getHeight();
			int buttonWidth = (int) expansionButton.getPreferredSize()
					.getWidth();
			expansionButton.setBounds(xLocation,
					((int) StoryComponentPropertyPanel.this.getPreferredSize()
							.getHeight() - buttonHeight) / 2, buttonWidth,
					buttonHeight);
		}

	}
	
	public void setParent(StoryComponent parentSet){
		parent = parentSet;
	}

	public StoryComponent getParentComponent(){
		return parent;
	}
	
	@Override
	public String toString() {
		return "StoryComponentPropertyPanel [" + this.component + "]";
	}

	@Override
	public void componentChanged(StoryComponentEvent event) {
		StoryComponent source = event.getSource();
		if (source == this.component)
			// rebuild the panel to reflect the parameter
			updatePanel();
	}
}


