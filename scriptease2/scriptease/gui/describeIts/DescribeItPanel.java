package scriptease.gui.describeIts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeEvent.GraphNodeEventType;
import scriptease.controller.observer.GraphNodeObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.control.ExpansionButton;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.complex.ScriptIt;

/**
 * This view is used to allow the user to select various pathways from
 * DescribeIts to determine what effect they should have.
 * 
 * @author mfchurch
 */
@SuppressWarnings("serial")
public class DescribeItPanel extends JPanel implements GraphNodeObserver {

	private DescribeIt describeIt;

	public DescribeItPanel(DescribeIt describeIt, boolean collapsed) {
		final GraphNode headNode = describeIt.getHeadNode();
		this.describeIt = describeIt;
		this.setOpaque(false);
		this.setLayout(new DescribeItPanelLayoutManager(headNode, collapsed));
		// observer the graph nodes

		GraphNode.observeDepthMap(this, headNode);
	}

	@Override
	public void nodeChanged(GraphNodeEvent event) {
		if (event.getEventType() == GraphNodeEventType.SELECTED) {
			this.describeIt.selectFromHeadToNode(event.getSource());
		}
	}

	// TODO abstract a common layoutManager between this and ParameterPanel
	/**
	 * DescribeItPanelLayoutManager handles laying out the describeItPanel in
	 * either it's text form, or graph form
	 * 
	 * @author mfchurch
	 * 
	 */
	private class DescribeItPanelLayoutManager implements LayoutManager {
		private static final int BUTTON_X_INDENT = 5;

		private ExpansionButton expansionButton;
		private GraphPanel expandedPanel;
		private JPanel collapsedPanel;
		private boolean collapsed;

		public DescribeItPanelLayoutManager(GraphNode headNode,
				boolean collapsed) {
			this.collapsed = collapsed;
			// initialize the panels
			this.collapsedPanel = new JPanel();
			this.expandedPanel = new GraphPanel(headNode);

			// expansion button
			expansionButton = ScriptWidgetFactory
					.buildExpansionButton(this.collapsed);

			expansionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// toggle
					DescribeItPanelLayoutManager.this.collapsed = !DescribeItPanelLayoutManager.this.collapsed;
					boolean shouldCollapse = DescribeItPanelLayoutManager.this.collapsed;
					if (shouldCollapse) {

						// Record the rebinding
						if (!UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().startUndoableAction(
									"Bind DescribeIt");
						boolean commited = DescribeItPanel.this.describeIt
								.commitSelection();
						UndoManager.getInstance().endUndoableAction();
						if (!commited) {
							// if the path was incomplete, revert to the current
							// selected path
							describeIt.selectPath(describeIt.getSelectedPath());
						}
					}
					expansionButton.setCollapsed(shouldCollapse);
					DescribeItPanel.this.revalidate();
				}
			});

			// add the components so they display
			DescribeItPanel.this.add(expansionButton);
			DescribeItPanel.this.add(collapsedPanel);
			DescribeItPanel.this.add(expandedPanel);
		}

		@Override
		public void layoutContainer(Container parent) {
			final Insets insets = parent.getInsets();

			// only show expansion if more than a single path exists
			final boolean moreThanOnePath = describeIt.getPaths().size() > 1;
			this.expansionButton.setVisible(moreThanOnePath);

			// update the visibility
			collapsedPanel.setVisible(collapsed);
			expandedPanel.setVisible(!collapsed);

			if (collapsed) {
				layoutCollapsed(insets.left + insets.right, insets.top
						+ insets.bottom);
			} else {
				layoutExpanded(insets.left + insets.right, insets.top
						+ insets.bottom);
			}
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			final Insets insets = parent.getInsets();
			if (collapsed)
				return minimumCollapsedLayoutSize(insets.left + insets.right,
						insets.top + insets.bottom);
			else
				return minimumExpandedLayoutSize(insets.left + insets.right,
						insets.top + insets.bottom);
		}

		/**
		 * Calculates the minimum layout size when the panel is collapsed (text)
		 * 
		 * @param parent
		 * @return
		 */
		private Dimension minimumCollapsedLayoutSize(int xSize, int ySize) {
			int buttonHeight = 0;
			int buttonWidth = 0;
			// Expansion button
			if (expansionButton.isVisible()) {
				buttonHeight += (int) expansionButton.getPreferredSize()
						.getHeight();
				buttonWidth += (int) expansionButton.getPreferredSize()
						.getWidth();
			}

			// Add the button indent
			xSize += buttonWidth + BUTTON_X_INDENT;
			ySize = Math.max(ySize, buttonHeight);

			// Resolve the displayNamePanel's size
			ScriptIt resolvedDoIt = describeIt.getResolvedScriptIt();

			if (resolvedDoIt != null) {
				StoryComponentPanelFactory.getInstance().parseDisplayText(
						collapsedPanel, resolvedDoIt);

				xSize += collapsedPanel.getPreferredSize().getWidth();
				ySize = Math.max(ySize, (int) collapsedPanel.getPreferredSize()
						.getHeight());
			}

			return new Dimension(xSize, ySize);
		}

		/**
		 * Layout the graph panel in it's collapsed form (text)
		 * 
		 * @param parent
		 */
		private void layoutCollapsed(int xLocation, int yLocation) {
			int buttonHeight = 0;
			int buttonWidth = 0;
			if (expansionButton.isVisible()) {
				// Expansion button
				buttonHeight += (int) expansionButton.getPreferredSize()
						.getHeight();
				buttonWidth += (int) expansionButton.getPreferredSize()
						.getWidth();
				expansionButton.setBounds(xLocation,
						((int) DescribeItPanel.this.getPreferredSize()
								.getHeight() - buttonHeight) / 2, buttonWidth,
						buttonHeight);
			}
			// Add the button indent
			xLocation += buttonWidth + BUTTON_X_INDENT;

			// Resolve the displayNamePanel size
			final ScriptIt resolvedDoIt = describeIt.getResolvedScriptIt();

			if (resolvedDoIt != null) {
				StoryComponentPanelFactory.getInstance().parseDisplayText(
						collapsedPanel, resolvedDoIt);

				collapsedPanel.setBounds(xLocation, yLocation,
						(int) collapsedPanel.getPreferredSize().getWidth(),
						(int) collapsedPanel.getPreferredSize().getHeight());
			}
		}

		/**
		 * Calculates the minimum layout size when the panel is expanded (graph)
		 * 
		 * @param parent
		 * @return
		 */
		protected Dimension minimumExpandedLayoutSize(int xSize, int ySize) {
			int buttonHeight = 0;
			int buttonWidth = 0;
			if (expansionButton.isVisible()) {
				// Expansion button
				buttonHeight += (int) expansionButton.getPreferredSize()
						.getHeight();
				buttonWidth += (int) expansionButton.getPreferredSize()
						.getWidth();
			}

			// Add the button indent
			xSize += buttonWidth;
			ySize = Math.max(buttonHeight, ySize);

			// calculate the minimum size with the graphPanel
			Dimension minimumSize = expandedPanel.getMinimumSize();
			minimumSize.setSize(minimumSize.getWidth() + xSize,
					Math.max(minimumSize.getHeight(), ySize));

			return minimumSize;
		}

		/**
		 * Layout the graph panel in it's expanded form (graph)
		 * 
		 * @param xLocation
		 * @param yLocation
		 */
		protected void layoutExpanded(int xLocation, int yLocation) {
			int buttonHeight = 0;
			int buttonWidth = 0;
			if (expansionButton.isVisible()) {
				// Expansion button
				buttonHeight = (int) expansionButton.getPreferredSize()
						.getHeight();
				buttonWidth = (int) expansionButton.getPreferredSize()
						.getWidth();
				expansionButton.setBounds(xLocation,
						(((int) DescribeItPanel.this.getPreferredSize()
								.getHeight() - buttonHeight) / 2), buttonWidth,
						buttonHeight);
			}
			// Add the button indent
			xLocation += buttonWidth;

			// graphPanel does the rest
			expandedPanel.setBounds(xLocation, yLocation, (int) expandedPanel
					.getPreferredSize().getWidth(), (int) expandedPanel
					.getPreferredSize().getHeight());
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
	}

	@Override
	public String toString() {
		return "DescribeItPanel [" + this.describeIt + "]";
	}
}
