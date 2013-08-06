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

/**
 * 
 * @author mfchurch
 */
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
	private final int CHILD_VGAP = 0;
	private final int TOP_GAP = 3;
	private final int BOTTOM_GAP = 3;
	private final int MAIN_INDENT = 5;
	private final int BUTTON_GAP = 3;

	// We keep handles to parent and children
	private JPanel mainPanel;
	private List<StoryComponentPanel> children = new ArrayList<StoryComponentPanel>();
	private JButton button;

	public JPanel getMainPanel() {
		return this.mainPanel;
	}

	// we need to be able to add components. if two components are added
	// with the same constraint we keep the last one
	@Override
	public void addLayoutComponent(String name, Component comp) {
		if (MAIN.equals(name) && comp instanceof JPanel) {
			this.mainPanel = (JPanel) comp;
		} else if (CHILD.equals(name)) {
			// Reflect the ordering of the model
			final StoryComponentPanel childPanel = (StoryComponentPanel) comp;
			final StoryComponent child = childPanel.getStoryComponent();
			final ComplexStoryComponent parent = (ComplexStoryComponent) child
					.getOwner();
			int index = parent.getChildIndex(child);
			this.children.add(index, childPanel);
		} else if (BUTTON.equals(name) && comp instanceof JButton) {
			this.button = (JButton) comp;
		} else {
			throw new IllegalArgumentException(
					"Cannot add to StoryComponentPanelLayoutManager: unknown combination of constraint "
							+ name + " with type " + comp.getClass());
		}
	}

	public boolean showChildren() {
		return this.showChildren;
	}

	public void setShowChildren(boolean showChildren) {
		this.showChildren = showChildren;
	}

	public List<StoryComponentPanel> getChildrenPanels() {
		return this.children;
	}

	// here we remove the component - first find it!
	public void removeLayoutComponent(Component comp) {
		if (comp == this.mainPanel) {
			this.mainPanel = null;
		} else {
			this.children.remove(comp);
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
		int widestWidth = 0;
		int sumHeight = 0;
		if ((this.mainPanel != null) && this.mainPanel.isVisible()) {
			final Dimension mainPanelSize = this.mainPanel.getPreferredSize();
			final int mainPanelWidth = mainPanelSize.width;
			final int mainPanelHeight = mainPanelSize.height;

			final int width = mainPanelWidth + this.MAIN_INDENT;
			if (this.button != null && this.button.isVisible()) {
				final Dimension buttonPreferredSize;

				buttonPreferredSize = this.button.getPreferredSize();

				widestWidth = width + buttonPreferredSize.width
						+ this.BUTTON_GAP;
				sumHeight += Math.max(mainPanelHeight + this.TOP_GAP,
						buttonPreferredSize.height / 2 + this.TOP_GAP
								+ mainPanelHeight / 2);
			} else {
				widestWidth = width;
				sumHeight += mainPanelHeight + this.TOP_GAP;
			}
		}
		if (this.showChildren) {
			if (!this.children.isEmpty())
				sumHeight += this.PARENT_CHILD_GAP;
			for (Component child : this.children) {
				if (child.isVisible()) {
					final Dimension childSize = child.getPreferredSize();
					widestWidth = Math.max(widestWidth, this.PRE_CHILD_INDENT
							+ childSize.width + this.POST_CHILD_INDENT);
					sumHeight += childSize.height + this.CHILD_VGAP;
				}
			}
		}
		sumHeight += this.BOTTOM_GAP;

		final Insets insets = container.getInsets();

		final int preferredWidth = insets.left + widestWidth + insets.right;
		final int preferredHeight = insets.top + sumHeight + insets.bottom;

		final Dimension preferredSize;

		preferredSize = new Dimension(preferredWidth, preferredHeight);

		return preferredSize;
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
		if (this.mainPanel != null && this.mainPanel.isVisible()) {
			final Dimension mainPanelSize = this.mainPanel.getPreferredSize();
			final int mainPanelHeight = mainPanelSize.height;
			final int mainPanelWidth = mainPanelSize.width;

			final int parentHeight = mainPanelHeight + this.TOP_GAP;
			final int vertIndent = north + this.TOP_GAP;
			int horIndent = west + this.MAIN_INDENT;
			if (this.button != null && this.button.isVisible()) {
				final Dimension buttonSize = this.button.getPreferredSize();
				final int buttonWidth = buttonSize.width;
				final int buttonHeight = buttonSize.height;

				this.button.setBounds(horIndent, vertIndent + mainPanelHeight
						/ 2 - buttonHeight / 2, buttonWidth, buttonHeight);
				horIndent += buttonWidth + this.BUTTON_GAP;
				sumHeight += Math.max(parentHeight, vertIndent
						+ mainPanelHeight / 2 + buttonHeight / 2);
			} else
				sumHeight += parentHeight;
			this.mainPanel.setBounds(horIndent, vertIndent, mainPanelWidth,
					mainPanelHeight);
		}

		if (this.showChildren) {
			// children
			if (!this.children.isEmpty())
				sumHeight += this.PARENT_CHILD_GAP;
			for (Component child : this.children) {
				if (child.isVisible()) {
					final Dimension childSize = child.getPreferredSize();
					final int childHeight = childSize.height;
					child.setBounds(west + this.PRE_CHILD_INDENT, sumHeight,
							childSize.width + this.POST_CHILD_INDENT,
							childHeight);
					sumHeight += childHeight + this.CHILD_VGAP;
				}
			}
		} else {
			for (Component child : this.children) {
				child.setBounds(0, 0, 0, 0);
			}
		}
	}

	@Override
	public String toString() {
		return "StoryComponentPanelLayoutManager [" + this.mainPanel + ", "
				+ this.children + "]";
	}
}