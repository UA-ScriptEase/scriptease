package scriptease.gui.describeIts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEGraph.DescribeItNodeGraphModel;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraph.SelectionMode;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.SEGraph.renderers.DescribeItNodeRenderer;
import scriptease.gui.cell.ScriptWidgetFactory;
import scriptease.gui.control.ExpansionButton;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.ScriptIt;

/**
 * This view is used to allow the user to select various pathways from
 * DescribeIts to determine what effect they should have.
 * 
 * @author mfchurch
 * @author kschenk
 */
@SuppressWarnings("serial")
public class DescribeItPanel extends JPanel {
	private SEGraph<DescribeItNode> expandedPanel;

	private DescribeIt describeIt;

	public DescribeItPanel(final DescribeIt describeIt, boolean collapsed) {
		final DescribeItNode headNode;
		final DescribeItNodeGraphModel describeItGraphModel;

		this.describeIt = describeIt.clone();

		headNode = this.describeIt.getStartNode();
		describeItGraphModel = new DescribeItNodeGraphModel(headNode);

		this.expandedPanel = new SEGraph<DescribeItNode>(describeItGraphModel,
				SelectionMode.SELECT_PATH);

		this.expandedPanel.setNodeRenderer(new DescribeItNodeRenderer(
				this.expandedPanel));

		this.expandedPanel
				.addSEGraphObserver(new SEGraphAdapter<DescribeItNode>() {

					@Override
					public void nodesSelected(Collection<DescribeItNode> nodes) {
						final Collection<DescribeItNode> selectedNodes;

						selectedNodes = new ArrayList<DescribeItNode>();

						selectedNodes.addAll(nodes);

						DescribeItPanel.this.describeIt
								.setSelectedPath(selectedNodes);
					}
				});

		this.setOpaque(false);
		this.setLayout(new DescribeItPanelLayoutManager(headNode, collapsed));
	}

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
		private JPanel collapsedPanel;
		private boolean collapsed;

		public DescribeItPanelLayoutManager(DescribeItNode headNode,
				boolean collapsed) {
			this.collapsed = collapsed;
			// initialize the panels
			this.collapsedPanel = new JPanel();

			// expansion button
			this.expansionButton = ScriptWidgetFactory
					.buildExpansionButton(this.collapsed);

			this.expansionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// toggle
					DescribeItPanelLayoutManager.this.collapsed = !DescribeItPanelLayoutManager.this.collapsed;
					boolean shouldCollapse = DescribeItPanelLayoutManager.this.collapsed;
					if (shouldCollapse) {
						// Start undo when we open graph
						if (!UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().startUndoableAction(
									"Bind DescribeIt");
					} else {
						// End undo when we close it.
						if (UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().endUndoableAction();
					}
					DescribeItPanelLayoutManager.this.expansionButton
							.setCollapsed(shouldCollapse);
					DescribeItPanel.this.revalidate();
				}
			});

			// add the components so they display
			DescribeItPanel.this.add(this.expansionButton);
			DescribeItPanel.this.add(this.collapsedPanel);
			DescribeItPanel.this.add(expandedPanel);
		}

		@Override
		public void layoutContainer(Container parent) {
			final Insets insets = parent.getInsets();

			// only show expansion if more than a single path exists
			final boolean moreThanOnePath = DescribeItPanel.this.describeIt
					.getPaths().size() > 1;
			this.expansionButton.setVisible(moreThanOnePath);

			// update the visibility
			this.collapsedPanel.setVisible(this.collapsed);
			expandedPanel.setVisible(!this.collapsed);

			if (this.collapsed) {
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
			if (this.collapsed)
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
			if (this.expansionButton.isVisible()) {
				buttonHeight += (int) this.expansionButton.getPreferredSize()
						.getHeight();
				buttonWidth += (int) this.expansionButton.getPreferredSize()
						.getWidth();
			}

			// Add the button indent
			xSize += buttonWidth + BUTTON_X_INDENT;
			ySize = Math.max(ySize, buttonHeight);

			// Resolve the displayNamePanel's size
			ScriptIt resolvedDoIt = DescribeItPanel.this.describeIt
					.getResolvedScriptIt();

			if (resolvedDoIt != null) {
				StoryComponentPanelFactory.getInstance().parseDisplayText(
						this.collapsedPanel, resolvedDoIt.clone());

				xSize += this.collapsedPanel.getPreferredSize().getWidth();
				ySize = Math.max(ySize, (int) this.collapsedPanel
						.getPreferredSize().getHeight());
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
			if (this.expansionButton.isVisible()) {
				// Expansion button
				buttonHeight += (int) this.expansionButton.getPreferredSize()
						.getHeight();
				buttonWidth += (int) this.expansionButton.getPreferredSize()
						.getWidth();
				this.expansionButton.setBounds(xLocation,
						((int) DescribeItPanel.this.getPreferredSize()
								.getHeight() - buttonHeight) / 2, buttonWidth,
						buttonHeight);
			}
			// Add the button indent
			xLocation += buttonWidth + BUTTON_X_INDENT;

			// Resolve the displayNamePanel size
			final ScriptIt resolvedDoIt = DescribeItPanel.this.describeIt
					.getResolvedScriptIt();

			if (resolvedDoIt != null) {
				StoryComponentPanelFactory.getInstance().parseDisplayText(
						this.collapsedPanel, resolvedDoIt.clone());

				this.collapsedPanel
						.setBounds(xLocation, yLocation,
								(int) this.collapsedPanel.getPreferredSize()
										.getWidth(), (int) this.collapsedPanel
										.getPreferredSize().getHeight());
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
			if (this.expansionButton.isVisible()) {
				// Expansion button
				buttonHeight += (int) this.expansionButton.getPreferredSize()
						.getHeight();
				buttonWidth += (int) this.expansionButton.getPreferredSize()
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
			if (this.expansionButton.isVisible()) {
				// Expansion button
				buttonHeight = (int) this.expansionButton.getPreferredSize()
						.getHeight();
				buttonWidth = (int) this.expansionButton.getPreferredSize()
						.getWidth();
				this.expansionButton.setBounds(xLocation,
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
