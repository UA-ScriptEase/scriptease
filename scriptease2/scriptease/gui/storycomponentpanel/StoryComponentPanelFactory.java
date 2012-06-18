package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.controller.AbstractNoOpBindingVisitor;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.SETree.transfer.StoryComponentPanelTransferHandler;
import scriptease.gui.control.ExpansionButton;
import scriptease.gui.describeIts.DescribeItPanel;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelSetting;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingRunTime;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * Builds a pane filled with ScriptEase Pattern Constructor GUI widgets for
 * displaying and editing Story Components. <br>
 * <br>
 * It is a StoryVisitor which is what allows it to build GUI for any Story
 * Component.<br>
 * <br>
 * Use {@link StoryComponentPanelFactory#buildPanel(StoryComponent, boolean)} to
 * have a GUIComponent Builder build a JPanel to represent the StoryComponent
 * supplied.
 * 
 * @author graves
 * @author remiller
 * @author mfchurch
 */
public class StoryComponentPanelFactory {
	private static StoryComponentPanelFactory instance;
	private final static String QUESTION = "question";

	private StoryComponentPanelFactory() {
	}

	public static StoryComponentPanelFactory getInstance() {
		if (instance == null)
			instance = new StoryComponentPanelFactory();
		return instance;
	}

	/**
	 * Creates, populates and returns a StoryComponentPanel visually
	 * representing the current state of the StoryComponent.
	 * 
	 * @param component
	 * @param editable
	 * @param collapsed
	 * @return
	 */
	public StoryComponentPanel buildPanel(StoryComponent component) {
		// make a new builder to construct the panel
		StoryComponentPanelBuilder builder = new StoryComponentPanelBuilder();
		return builder.build(component);
	}

	/**
	 * Reconstruct the StoryComponentPanel with the given child added
	 * 
	 * @param panel
	 * @param child
	 */
	public void addChild(StoryComponentPanel panel, StoryComponent child) {
		final StoryComponent parent = panel.getStoryComponent();
		final StoryComponentPanelManager selectionManager = panel
				.getSelectionManager();

		if (parent instanceof ComplexStoryComponent) {
			if (selectionManager != null) {
				int index = ((ComplexStoryComponent) parent)
						.getChildIndex(child);
				if (index == -1)
					throw new IllegalStateException(child
							+ " is not a child of " + parent);
				StoryComponentPanel childPanel = buildPanel(child);

				// Add the child panel to the parent panel.
				panel.add(childPanel, StoryComponentPanelLayoutManager.CHILD,
						index);

				// Update the settings of the child panel to match the parents
				StoryComponentPanelTree parentTree = panel.getParentTree();
				if (parentTree != null) {
					StoryComponentPanelSetting settings = parentTree
							.getSettings();
					if (settings != null)
						settings.updateComplexSettings(childPanel);
				}
				// if the parent is selected, select the child as well
				boolean select = selectionManager.getSelectedPanels().contains(
						panel);
				selectionManager.addComplexPanel(childPanel, select);
			}
		} else {
			throw new IllegalStateException(parent
					+ " is not a ComplexStoryComponent and cannot have "
					+ child + " added");
		}
	}

	/**
	 * Reconstruct the StoryComponentPanel with the given child removed
	 * 
	 * @param panel
	 * @param child
	 */
	public void removeChild(StoryComponentPanel panel, StoryComponent child) {
		final StoryComponent parent = panel.getStoryComponent();

		// Get the StoryComponentPanel with the child component
		if (parent instanceof ComplexStoryComponent) {
			StoryComponentPanel childPanel = null;
			for (StoryComponentPanel aPanel : panel.getChildrenPanels()) {
				if (aPanel.getStoryComponent() == child) {
					childPanel = aPanel;
					break;
				}
			}

			if (childPanel != null) {
				panel.remove(childPanel);
				StoryComponentPanelManager selectionManager = panel
						.getSelectionManager();
				if (selectionManager != null)
					selectionManager.cleanUpPanel(childPanel);
			} else
				System.err.println("Attempted to remove " + child
						+ "'s StoryComponentPanel when it is not a child of "
						+ parent);
		} else
			throw new IllegalStateException(parent
					+ " is not a ComplexStoryComponent and cannot have "
					+ child + " removed");
	}

	/**
	 * Reconstructs the main panel of the given StoryComponentPanel with the
	 * current state of the model
	 * 
	 * @param panel
	 */
	public void refreshMain(StoryComponentPanel panel) {
		// make a new builder to refresh the main panel
		StoryComponentPanelBuilder builder = new StoryComponentPanelBuilder();
		builder.refreshMain(panel);
	}

	/**
	 * Adds the StoryComponent's Labels to the given JPanel
	 * 
	 * @param displayNamePanel
	 * @param storyComponent
	 */
	private static void addLabels(JPanel displayNamePanel,
			StoryComponent storyComponent) {
		for (String labelText : storyComponent.getLabels()) {
			if (!labelText.isEmpty()) {
				JLabel label = ScriptWidgetFactory.buildLabel(labelText,
						ScriptWidgetFactory.LABEL_TEXT_COLOUR,
						ScriptWidgetFactory.LABEL_BACKGROUND_COLOUR);
				displayNamePanel.add(label);
				displayNamePanel.add(Box.createHorizontalStrut(5));
			}
		}
	}

	/**
	 * Converts the display text of a StoryComponent from a String into a
	 * graphical representation that uses different GUI components.
	 * 
	 * @param storyComponent
	 * @author graves
	 * @author mfchurch
	 */
	public static void parseDisplayText(JPanel displayNamePanel,
			StoryComponent storyComponent) {
		String toParse = storyComponent.getDisplayText();
		int paramTagStart;
		int paramTagEnd;
		String tagName;
		JLabel plainTextLabel;
		String plainText;
		KnowIt knowIt = null;
		displayNamePanel.removeAll();
		displayNamePanel.setOpaque(false);
		displayNamePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

		// Add the StoryComponent's labels
		addLabels(displayNamePanel, storyComponent);

		// Loop through the display text until there is no more to parse.
		while (toParse.length() > 0) {
			// Get the first occurrence of a parameter name.
			paramTagStart = toParse.indexOf("<");
			paramTagEnd = toParse.indexOf(">", paramTagStart);

			if (paramTagStart == -1 || paramTagEnd == -1) {
				// No more parameter references found, so the rest is text.
				plainText = toParse;
			} else {
				// Parameter tag was found. Check to see if it's valid.
				tagName = toParse.substring(paramTagStart + 1, paramTagEnd);

				plainText = toParse.substring(0, paramTagStart);

				if (tagName.equalsIgnoreCase(QUESTION)
						&& storyComponent instanceof AskIt) {
					knowIt = ((AskIt) storyComponent).getCondition();
				}
				// Now check for a parameter/implicit tag
				else {
					// Check for parameters
					if (knowIt == null && storyComponent instanceof ScriptIt) {
						knowIt = getParameterWithText(
								(ScriptIt) storyComponent, tagName);
					}

					// Invalid parameter tags are treated as plain text,
					// valid
					// ones are represented with the appropriate widget.
					if (knowIt == null)
						plainText += toParse.substring(paramTagStart,
								paramTagEnd + 1);
				}
			}

			plainTextLabel = ScriptWidgetFactory.buildLabel(plainText, null);

			displayNamePanel.add(plainTextLabel);

			if (knowIt != null) {
				addWidget(displayNamePanel, knowIt, false);
				knowIt = null;
			}

			// Update toParse for the next iteration of the loop.
			toParse = paramTagEnd < 0 ? "" : toParse.substring(paramTagEnd + 1);
		}

		displayNamePanel.setMaximumSize(displayNamePanel.getPreferredSize());
	}

	/**
	 * Finds and returns the parameter of <code>component</code> with display
	 * text equal to <code>parameterName</code> (case insensitive), if it
	 * exists.
	 * 
	 * @param component
	 * @param parameterName
	 * @return the parameter of <code>component</code> with display text
	 *         <code>parameterName</code>
	 */
	private static KnowIt getParameterWithText(ScriptIt component,
			String parameterName) {
		final Collection<KnowIt> parameters = component.getParameters();
		for (KnowIt parameter : parameters) {
			if (parameter.getDisplayText().equalsIgnoreCase(parameterName)) {
				return parameter;
			}
		}

		// Otherwise check implicits
		final Collection<KnowIt> implicits = component.getImplicits();
		for (KnowIt implicit : implicits) {
			if (implicit.getDisplayText().equalsIgnoreCase(parameterName)) {
				return implicit;
			}
		}
		return null;
	}

	/**
	 * Adds the resolved widget for the given knowIt to the given JPanel
	 * 
	 * @param displayNamePanel
	 * @param knowIt
	 */
	private static void addWidget(final JPanel displayNamePanel,
			final KnowIt knowIt, final boolean editable) {
		final KnowItBinding binding = knowIt.getBinding();
		binding.process(new AbstractNoOpBindingVisitor() {
			@Override
			public void processFunction(KnowItBindingFunction function) {
				displayNamePanel.add(ScriptWidgetFactory.buildBindingWidget(
						knowIt, editable));
			}

			@Override
			public void processRunTime(KnowItBindingRunTime runTime) {
				displayNamePanel.add(ScriptWidgetFactory.buildBindingWidget(
						knowIt, editable));
			}

			@Override
			public void processDescribeIt(KnowItBindingDescribeIt described) {
				displayNamePanel.add(ScriptWidgetFactory.buildBindingWidget(
						knowIt, editable));
			}

			@Override
			protected void defaultProcess(KnowItBinding binding) {
				displayNamePanel.add(ScriptWidgetFactory.buildSlotPanel(knowIt));
			}
		});
	}

	private static void createKnowItPanel(final JPanel mainPanel,
			final KnowIt knowIt) {
		mainPanel.removeAll();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

		addWidget(mainPanel, knowIt, true);
		final KnowItBinding binding = knowIt.getBinding().resolveBinding();

		binding.process(new AbstractNoOpBindingVisitor() {
			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				// do nothing for KnowItBindingNull. Not even the default.
				// That's right. We hate empty bindings so much that we won't
				// even talk to them.
			}

			private void processDefault() {
				mainPanel.add(ScriptWidgetFactory.buildLabel(" "
						+ DescribeIt.DESCRIBES + " ", Color.black));
			}

			@Override
			public void processDescribeIt(KnowItBindingDescribeIt described) {
				processDefault();
				DescribeIt describeIt = described.getValue();
				DescribeItPanel describeItPanel = new DescribeItPanel(
						describeIt, true);
				mainPanel.add(describeItPanel);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				processDefault();
				ScriptIt scriptIt = function.getValue();
				JPanel displayNamePanel = new JPanel();
				parseDisplayText(displayNamePanel, scriptIt);
				mainPanel.add(displayNamePanel);
			}
		});
	}

	/**
	 * Builder used to populate the contents of a StoryComponentPanel. Singleton
	 * 
	 * @author mfchurch
	 * 
	 */
	private class StoryComponentPanelBuilder extends AbstractNoOpStoryVisitor {
		private StoryComponentPanel panel;

		public StoryComponentPanelBuilder() {
		}

		/**
		 * Builds and returns a StoryComponentPanel which represents the given
		 * StoryComponent
		 * 
		 * @param component
		 * @return
		 */
		public StoryComponentPanel build(StoryComponent component) {
			if (component == null)
				throw new IllegalArgumentException(
						"Cannot build a StoryComponentPanel for a null StoryComponent");

			panel = new StoryComponentPanel(component);
			component.process(this);
			panel.setTransferHandler(StoryComponentPanelTransferHandler
					.getInstance());

			panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.setAlignmentY(Component.TOP_ALIGNMENT);
			return panel;
		}

		/**
		 * Refreshs only the Main Panel in the StoryComponentPanel
		 * 
		 * @param panel
		 */
		public void refreshMain(StoryComponentPanel panel) {
			final StoryComponent storyComponent = panel.getStoryComponent();
			final StoryComponentPanelLayoutManager layout = panel.getLayout();
			if (layout != null) {
				final JPanel mainPanel = layout.getMainPanel();
				if (mainPanel != null) {
					// clear the panel
					mainPanel.removeAll();
					// rebuild the panel according to it's storycomponent type
					storyComponent.process(new AbstractNoOpStoryVisitor() {
						@Override
						protected void defaultProcessComplex(
								ComplexStoryComponent complex) {
							buildMainComplexPanel(complex, mainPanel);
						}

						@Override
						public void processQuestPoint(QuestPoint questPoint) {
							buildMainQuestPointPanel(questPoint, mainPanel);
						}

						@Override
						public void processKnowIt(KnowIt knowIt) {
							buildMainKnowItPanel(knowIt, mainPanel);
						}
					});
				} else
					throw new IllegalStateException(
							"Attempted to refresh a null Main Panel");
			}
		}

		@Override
		public void processQuestPoint(QuestPoint questPoint) {
			// Add an expansion button
			this.addExpansionButton(questPoint);

			JPanel mainPanel = new JPanel();
			buildMainQuestPointPanel(questPoint, mainPanel);

			// Add a BindingWidget for the QuestPoint
			this.panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);

			// Add the children panels
			this.addChildrenPanels(questPoint);
		}

		private void addExpansionButton(ComplexStoryComponent complex) {
			// Add expansion button if you are not root
			if (complex.getOwner() != null) {
				final ExpansionButton expansionButton = ScriptWidgetFactory
						.buildExpansionButton(!panel.showChildren());
				expansionButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean showChildren = !panel.showChildren();
						panel.setShowChildren(showChildren);
						expansionButton.setCollapsed(!showChildren);
						panel.revalidate();
					}
				});
				panel.add(expansionButton,
						StoryComponentPanelLayoutManager.BUTTON);
			}
		}

		private void addChildrenPanels(ComplexStoryComponent complex) {
			final boolean hasChildren = complex.getChildCount() > 0;
			if (hasChildren) {
				// Add child panels
				for (StoryComponent component : complex.getChildren()) {
					StoryComponentPanel childPanel = StoryComponentPanelFactory
							.getInstance().buildPanel(component);
					this.panel.add(childPanel,
							StoryComponentPanelLayoutManager.CHILD);
				}
			}
		}

		private void buildMainQuestPointPanel(QuestPoint questPoint,
				JPanel mainPanel) {
			// Add a BindingWidget for the QuestPoint
			mainPanel.add(ScriptWidgetFactory.buildBindingWidget(questPoint,
					false));
			mainPanel.setOpaque(false);
		}

		private void buildMainKnowItPanel(KnowIt knowIt, JPanel mainPanel) {
			// Add displayName panel
			createKnowItPanel(mainPanel, knowIt);
		}

		private void buildMainComplexPanel(ComplexStoryComponent complex,
				JPanel mainPanel) {
			// Add a label for the complex story component
			parseDisplayText(mainPanel, complex);
		}

		@Override
		protected void defaultProcessComplex(ComplexStoryComponent complex) {
			// Add an expansion button
			this.addExpansionButton(complex);

			JPanel mainPanel = new JPanel();
			buildMainComplexPanel(complex, mainPanel);

			// Add a label for the complex story component
			this.panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);

			// Add the children panels
			this.addChildrenPanels(complex);
		}

		@Override
		public void processKnowIt(final KnowIt knowIt) {
			JPanel mainPanel = new JPanel();
			buildMainKnowItPanel(knowIt, mainPanel);
			this.panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);
		}
	}
}
