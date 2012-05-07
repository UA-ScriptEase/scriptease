package scriptease.gui.storycomponentpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;

public class StoryComponentPanelLayoutManager implements LayoutManager {
	// these are the constraints possible with the
	// StoryComponentPanelLayoutManager
	public static final String MAIN = "Main";
	public static final String CHILD = "Child";
	public static final String BUTTON = "Button";

	private boolean showChildren = true;

	private final int PARENT_CHILD_GAP = 5;
	private final int PRE_CHILD_INDENT = 20;
	private final int POST_CHILD_INDENT = 15;
	private final int CHILD_VGAP = 5;
	private final int TOP_GAP = 4;
	private final int BOTTOM_GAP = 4;
	private final int MAIN_INDENT = 5;
	private final int BUTTON_GAP = 3;

	// We keep handles to parent and children
	private JPanel mainPanel;
	private List<StoryComponentPanel> children = new ArrayList<StoryComponentPanel>();
	private JButton button;

	public JPanel getMainPanel() {
		return mainPanel;
	}

	// we need to be able to add components. if two components are added
	// with the same constraint we keep the last one
	@Override
	public void addLayoutComponent(String name, Component comp) {
		if (MAIN.equals(name) && comp instanceof JPanel) {
			mainPanel = (JPanel) comp;
		} else if (CHILD.equals(name)) {
			// Reflect the ordering of the model
			final StoryComponentPanel childPanel = (StoryComponentPanel) comp;
			final StoryComponent child = childPanel.getStoryComponent();
			final ComplexStoryComponent parent = (ComplexStoryComponent) child
					.getOwner();
			int index = parent.getChildIndex(child);
			children.add(index, childPanel);
		} else if (BUTTON.equals(name) && comp instanceof JButton) {
			button = (JButton) comp;
		} else {
			throw new IllegalArgumentException(
					"cannot add to StoryComponentPanelLayoutManager: unknown constraint "
							+ name);
		}
	}

	public boolean showChildren() {
		return showChildren;
	}

	public void setShowChildren(boolean showChildren) {
		this.showChildren = showChildren;
	}

	public List<StoryComponentPanel> getChildrenPanels() {
		return children;
	}

	// here we remove the component - first find it!
	public void removeLayoutComponent(Component comp) {
		if (comp == mainPanel) {
			mainPanel = null;
		} else {
			children.remove(comp);
		}
	}

	// The minimum dimension we're happy with is the preferred size
	// this could be more fancy by using the minimum sizes of each component
	@Override
	public Dimension minimumLayoutSize(Container container) {
		return preferredLayoutSize(container);
	}

	// Here we work out the preferred size of the component, which is used
	// by methods such as pack() to work out how big the window should be
	@Override
	public Dimension preferredLayoutSize(Container container) {
		// Determine if the expansion button is needed
		if (button != null)
			button.setVisible(!children.isEmpty());

		Dimension dim = new Dimension(0, 0);

		int widestWidth = 0;
		int sumHeight = 0;
		if ((mainPanel != null) && mainPanel.isVisible()) {
			int width = mainPanel.getPreferredSize().width + MAIN_INDENT;
			if (button != null && button.isVisible()) {
				widestWidth = width + button.getPreferredSize().width
						+ BUTTON_GAP;
				sumHeight += Math.max(mainPanel.getPreferredSize().height
						+ TOP_GAP, button.getPreferredSize().height / 2
						+ TOP_GAP + mainPanel.getPreferredSize().height / 2);
			} else {
				widestWidth = width;
				sumHeight += mainPanel.getPreferredSize().height + TOP_GAP;
			}
		}
		if (showChildren) {
			if (!children.isEmpty())
				sumHeight += PARENT_CHILD_GAP;
			for (Component child : children) {
				if (child.isVisible()) {
					widestWidth = Math.max(widestWidth, PRE_CHILD_INDENT
							+ child.getPreferredSize().width
							+ POST_CHILD_INDENT);
					sumHeight += child.getPreferredSize().height + CHILD_VGAP;
				}
			}
		}
		sumHeight += BOTTOM_GAP;

		Insets insets = container.getInsets();
		dim.width += insets.left + widestWidth + insets.right;
		dim.height += insets.top + sumHeight + insets.bottom;

		return dim;
	}

	@Override
	public void layoutContainer(Container target) {
		// these variables hold the position where we can draw components
		// taking into account insets
		Insets insets = target.getInsets();
		int north = insets.top;
		int west = insets.left;

		int sumHeight = 0;
		// parent
		if (mainPanel != null && mainPanel.isVisible()) {
			final int parentHeight = mainPanel.getPreferredSize().height
					+ TOP_GAP;
			final int vertIndent = north + TOP_GAP;
			int horIndent = west + MAIN_INDENT;
			if (button != null && button.isVisible()) {
				final int buttonWidth = button.getPreferredSize().width;
				final int buttonHeight = button.getPreferredSize().height;
				button.setBounds(horIndent,
						vertIndent + mainPanel.getPreferredSize().height / 2
								- buttonHeight / 2, buttonWidth, buttonHeight);
				horIndent += buttonWidth + BUTTON_GAP;
				sumHeight += Math.max(parentHeight,
						vertIndent + mainPanel.getPreferredSize().height / 2
								+ buttonHeight / 2);
			} else
				sumHeight += parentHeight;
			mainPanel.setBounds(horIndent, vertIndent,
					mainPanel.getPreferredSize().width,
					mainPanel.getPreferredSize().height);
		}

		if (showChildren) {
			// children
			if (!children.isEmpty())
				sumHeight += PARENT_CHILD_GAP;
			for (Component child : children) {
				if (child.isVisible()) {
					final int childHeight = child.getPreferredSize().height;
					child.setBounds(west + PRE_CHILD_INDENT, sumHeight,
							child.getPreferredSize().width + POST_CHILD_INDENT,
							childHeight);
					sumHeight += childHeight + CHILD_VGAP;
				}
			}
		} else {
			for (Component child : children) {
				child.setBounds(0, 0, 0, 0);
			}
		}
	}

	@Override
	public String toString() {
		return "StoryComponentPanelLayoutManager [" + mainPanel + ", "
				+ children + "]";
	}
}