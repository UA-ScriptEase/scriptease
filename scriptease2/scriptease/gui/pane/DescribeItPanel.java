package scriptease.gui.pane;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JPanel;

import scriptease.controller.BindingAdapter;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.component.ExpansionButton;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
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
	private final SEGraph<DescribeItNode> describeItGraph;
	private final ExpansionButton expansionButton;
	private final JPanel scriptItPanel;

	private boolean isCollapsed;

	/**
	 * Creates a new DescribeItPanel using the given KnowIt and DescribeIt.
	 * 
	 * @param knowIt
	 */
	public DescribeItPanel(final KnowIt knowIt, final DescribeIt describeIt) {
		this.isCollapsed = true;
		this.scriptItPanel = new JPanel();
		this.expansionButton = ScriptWidgetFactory
				.buildExpansionButton(this.isCollapsed);
		this.describeItGraph = SEGraphFactory.buildDescribeItGraph(describeIt
				.getStartNode());

		knowIt.getBinding().process(new BindingAdapter() {
			@Override
			public void processFunction(KnowItBindingFunction function) {
				final Collection<DescribeItNode> path;
				final ScriptIt scriptIt;

				scriptIt = function.getValue();
				path = describeIt.getPath(scriptIt);

				StoryComponentPanelFactory.getInstance().parseDisplayText(
						scriptItPanel, scriptIt);

				if (path.size() > 0)
					describeItGraph.setSelectedNodes(path);
			}
		});

		this.setOpaque(false);
		this.setLayout(new DescribeItPanelLayoutManager());

		if (describeIt.getPaths().size() > 1) {
			this.expansionButton.addActionListener(this
					.buildExpansionButtonListener(describeIt, knowIt));

			this.add(this.expansionButton);
		} else
			this.expansionButton.setVisible(false);

		this.add(this.describeItGraph);
		this.add(this.scriptItPanel);
	}

	/**
	 * Builds a listener for the expansion button.
	 * 
	 * @param describeIt
	 * @param knowIt
	 * @return
	 */
	private ActionListener buildExpansionButtonListener(
			final DescribeIt describeIt, final KnowIt knowIt) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// toggle
				DescribeItPanel.this.isCollapsed = !DescribeItPanel.this.isCollapsed;
				boolean collapsed = DescribeItPanel.this.isCollapsed;
				if (collapsed) {
					final ScriptIt resolvedScriptIt;

					resolvedScriptIt = describeIt
							.getScriptItForPath(describeItGraph
									.getSelectedNodes());
					
					if(resolvedScriptIt == null)
						System.out.println("It's null.");

					bindingIf: if (resolvedScriptIt != null) {
						final KnowItBinding previousBinding;

						previousBinding = knowIt.getBinding();

						if (previousBinding != null
								&& previousBinding instanceof KnowItBindingFunction) {
							if (((ScriptIt) previousBinding.getValue())
									.getDisplayText().equals(
											resolvedScriptIt.getDisplayText()))
								break bindingIf;
						}

						if (!UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().startUndoableAction(
									"Bind DescribeIt");

						final ScriptIt clone;

						clone = resolvedScriptIt.clone();

						knowIt.setBinding(clone);

						scriptItPanel.removeAll();

						StoryComponentPanelFactory.getInstance()
								.parseDisplayText(scriptItPanel, clone);

						UndoManager.getInstance().endUndoableAction();
					}
				}

				final KnowItBinding binding;

				binding = knowIt.getBinding();

				if (binding instanceof KnowItBindingFunction) {
					describeItGraph.setSelectedNodes(describeIt
							.getPath((ScriptIt) binding.getValue()));
				}

				scriptItPanel.setVisible(collapsed);
				describeItGraph.setVisible(!collapsed);
				expansionButton.setCollapsed(collapsed);
				DescribeItPanel.this.revalidate();
			}
		};
	}

	/**
	 * DescribeItPanelLayoutManager handles laying out the describeItPanel in
	 * either it's text form, or graph form
	 * 
	 * @author mfchurch
	 * @author kschenk
	 * 
	 */
	private class DescribeItPanelLayoutManager implements LayoutManager {
		private static final int BUTTON_X_INDENT = 5;

		@Override
		public void layoutContainer(Container parent) {
			final Insets insets = parent.getInsets();

			if (DescribeItPanel.this.isCollapsed) {
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
			if (DescribeItPanel.this.isCollapsed)
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
			// Expansion button
			if (expansionButton.isVisible()) {
				final Dimension buttonSize = expansionButton.getPreferredSize();
				ySize = Math.max(ySize, buttonSize.height);
				xSize += buttonSize.width;
			}
			// Add the button indent
			xSize += BUTTON_X_INDENT;

			// Resolve the displayNamePanel's size
			if (scriptItPanel.isVisible()) {
				final Dimension panelSize = scriptItPanel.getPreferredSize();
				xSize += panelSize.width;
				ySize = Math.max(ySize, panelSize.height);
			}

			return new Dimension(xSize, ySize);
		}

		/**
		 * Layout the graph panel in it's collapsed form (text)
		 * 
		 * @param parent
		 */
		private void layoutCollapsed(int xLocation, int yLocation) {
			// Expansion button
			if (expansionButton.isVisible()) {
				final Dimension buttonSize = expansionButton.getPreferredSize();
				final int buttonHeight = buttonSize.height;
				final int buttonWidth = buttonSize.width;

				expansionButton
						.setBounds(
								xLocation,
								(DescribeItPanel.this.getPreferredSize().height - buttonHeight) / 2,
								buttonWidth, buttonHeight);

				xLocation += buttonWidth + BUTTON_X_INDENT;
			}

			if (scriptItPanel.isVisible()) {
				final Dimension panelSize = scriptItPanel.getPreferredSize();
				scriptItPanel.setBounds(xLocation, yLocation, panelSize.width,
						panelSize.height);
			}
		}

		/**
		 * Calculates the minimum layout size when the panel is expanded (graph)
		 * 
		 * @param parent
		 * @return
		 */
		protected Dimension minimumExpandedLayoutSize(int xSize, int ySize) {
			if (DescribeItPanel.this.expansionButton.isVisible()) {
				final Dimension buttonSize = expansionButton.getPreferredSize();
				// Expansion button
				ySize = Math.max(buttonSize.height, ySize);
				xSize += buttonSize.width;
			}

			// calculate the minimum size with the graphPanel
			final Dimension minimumSize = describeItGraph.getMinimumSize();
			minimumSize.setSize(minimumSize.width + xSize,
					Math.max(minimumSize.height, ySize));

			return minimumSize;
		}

		/**
		 * Layout the graph panel in it's expanded form (graph)
		 * 
		 * @param xLocation
		 * @param yLocation
		 */
		protected void layoutExpanded(int xLocation, int yLocation) {
			final Dimension buttonSize = expansionButton.getPreferredSize();
			final Dimension graphSize = describeItGraph.getPreferredSize();

			// Expansion button
			final int buttonHeight = buttonSize.height;
			final int buttonWidth = buttonSize.width;

			DescribeItPanel.this.expansionButton
					.setBounds(xLocation,
							(((int) DescribeItPanel.this.getPreferredSize()
									.getHeight() - buttonHeight) / 2),
							buttonWidth, buttonHeight);

			// Add the button indent
			xLocation += buttonWidth;

			// graphPanel does the rest
			describeItGraph.setBounds(xLocation, yLocation, graphSize.width,
					graphSize.height);
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
		return "DescribeItPanel [" + this.describeItGraph.getStartNode() + "]";
	}
}
