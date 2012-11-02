package scriptease.gui.describeIts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import scriptease.controller.BindingAdapter;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEGraph.DescribeItNodeGraphModel;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraph.SelectionMode;
import scriptease.gui.SEGraph.renderers.DescribeItNodeRenderer;
import scriptease.gui.cell.ScriptWidgetFactory;
import scriptease.gui.control.ExpansionButton;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.APIDictionary;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.DescribeItManager;
import scriptease.util.GUIOp;

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
	private final JPanel boundScriptItPanel;

	private boolean isCollapsed;

	/**
	 * Creates a new DescribeItPanel using the given KnowIt. If the KnowIt does
	 * not have a valid DescribeIt attached to it, this will throw a null
	 * pointer exception.
	 * 
	 * @param knowIt
	 */
	public DescribeItPanel(final KnowIt knowIt) {
		final APIDictionary dictionary;
		final DescribeItManager describeItManager;
		final DescribeIt describeIt;

		final DescribeItNodeGraphModel describeItGraphModel;

		this.isCollapsed = true;
		this.boundScriptItPanel = new JPanel();

		dictionary = TranslatorManager.getInstance().getActiveAPIDictionary();
		describeItManager = dictionary.getDescribeItManager();
		describeIt = describeItManager.getDescribeIt(knowIt);

		if (describeIt == null) {
			throw new NullPointerException("No DescribeIt found for " + knowIt
					+ " when attempting to create DescribeItPanel!");
		}

		describeItGraphModel = new DescribeItNodeGraphModel(
				describeIt.getStartNode());

		this.describeItGraph = new SEGraph<DescribeItNode>(
				describeItGraphModel, SelectionMode.SELECT_PATH_FROM_START,
				true);
		this.expansionButton = ScriptWidgetFactory
				.buildExpansionButton(this.isCollapsed);

		this.describeItGraph.setNodeRenderer(new DescribeItNodeRenderer(
				this.describeItGraph));

		this.describeItGraph.setBackground(GUIOp.scaleWhite(
				ScriptEaseUI.COLOUR_KNOWN_OBJECT, 3.5));

		this.expansionButton.addActionListener(this
				.buildExpansionButtonListener(describeIt, knowIt));

		knowIt.getBinding().process(new BindingAdapter() {
			@Override
			public void processFunction(KnowItBindingFunction function) {
				final ScriptIt scriptIt;

				scriptIt = function.getValue();

				StoryComponentPanelFactory.getInstance().parseDisplayText(
						DescribeItPanel.this.boundScriptItPanel, scriptIt);

			}

		});

		this.setOpaque(false);
		this.setLayout(new DescribeItPanelLayoutManager());

		this.add(this.expansionButton);
		this.add(this.describeItGraph);
		this.add(this.boundScriptItPanel);
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

						DescribeItPanel.this.boundScriptItPanel.removeAll();
						
						StoryComponentPanelFactory
								.getInstance()
								.parseDisplayText(
										DescribeItPanel.this.boundScriptItPanel,
										clone);

						UndoManager.getInstance().endUndoableAction();
					}
				}

				describeItGraph.setVisible(!collapsed);
				// boundScriptItPanel.setVisible(collapsed);
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

			// Expansion button
			buttonHeight += (int) DescribeItPanel.this.expansionButton
					.getPreferredSize().getHeight();
			buttonWidth += (int) DescribeItPanel.this.expansionButton
					.getPreferredSize().getWidth();
			DescribeItPanel.this.expansionButton
					.setBounds(xLocation,
							((int) DescribeItPanel.this.getPreferredSize()
									.getHeight() - buttonHeight) / 2,
							buttonWidth, buttonHeight);
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
			if (DescribeItPanel.this.expansionButton.isVisible()) {
				// Expansion button
				buttonHeight += (int) DescribeItPanel.this.expansionButton
						.getPreferredSize().getHeight();
				buttonWidth += (int) DescribeItPanel.this.expansionButton
						.getPreferredSize().getWidth();
			}

			// Add the button indent
			xSize += buttonWidth;
			ySize = Math.max(buttonHeight, ySize);

			// calculate the minimum size with the graphPanel
			Dimension minimumSize = describeItGraph.getMinimumSize();
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

			// Expansion button
			buttonHeight = (int) DescribeItPanel.this.expansionButton
					.getPreferredSize().getHeight();
			buttonWidth = (int) DescribeItPanel.this.expansionButton
					.getPreferredSize().getWidth();
			DescribeItPanel.this.expansionButton
					.setBounds(xLocation,
							(((int) DescribeItPanel.this.getPreferredSize()
									.getHeight() - buttonHeight) / 2),
							buttonWidth, buttonHeight);

			// Add the button indent
			xLocation += buttonWidth;

			// graphPanel does the rest
			describeItGraph.setBounds(xLocation, yLocation,
					(int) describeItGraph.getPreferredSize().getWidth(),
					(int) describeItGraph.getPreferredSize().getHeight());
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
