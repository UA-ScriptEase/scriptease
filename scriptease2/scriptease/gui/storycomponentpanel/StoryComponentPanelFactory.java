package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.component.ExpansionButton;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.pane.DescribeItPanel;
import scriptease.gui.transfer.StoryComponentPanelTransferHandler;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.PickIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryPoint;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.util.GUIOp;

/**
 * Builds a pane filled with ScriptEase Pattern Constructor GUI widgets for
 * displaying and editing Story Components. <br>
 * <br>
 * It is a StoryVisitor which is what allows it to build GUI for any Story
 * Component.<br>
 * <br>
 * Use
 * {@link StoryComponentPanelFactory#buildStoryComponentPanel(StoryComponent, boolean)}
 * to have a GUIComponent Builder build a JPanel to represent the StoryComponent
 * supplied.
 * 
 * @author graves
 * @author remiller
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 */
public class StoryComponentPanelFactory {
	public static final String CURRENT_STORY_POINT_TAG = "#currentStoryPoint";

	private static final StoryComponentPanelFactory instance = new StoryComponentPanelFactory();
	private static final ImageIcon noteIcon;

	static {
		java.net.URL imgURL = StoryComponentPanelFactory.getInstance()
				.getClass()
				.getResource("/scriptease/resources/icons/noteicon.png");
		if (imgURL != null) {
			noteIcon = new ImageIcon(imgURL, "Note");
		} else {
			System.err.println("Could not find icon at " + imgURL);
			noteIcon = new ImageIcon();
		}
	}

	public static StoryComponentPanelFactory getInstance() {
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
	public StoryComponentPanel buildStoryComponentPanel(StoryComponent component) {
		final StoryComponentPanel panel = new StoryComponentPanel(component);

		if (component != null) {
			component.process(componentProcessor(panel));
			panel.setTransferHandler(StoryComponentPanelTransferHandler
					.getInstance());
		}

		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setAlignmentY(Component.TOP_ALIGNMENT);
		return panel;
	}

	/**
	 * Reconstructs the StoryComponentPanel labels
	 * 
	 * @param panel
	 */
	public void rebuildLabels(StoryComponentPanel panel) {
		final JPanel mainPanel = panel.getLayout().getMainPanel();
		final Component[] children = mainPanel.getComponents();
		final StoryComponent component = panel.getStoryComponent();

		// Remove all existing labels
		boolean isSpacer = false;
		for (Component child : children) {
			if (isSpacer) {
				// Remove the buffer after the label.
				mainPanel.remove(child);
				isSpacer = false;
			}
			if (child instanceof JLabel) {
				final JLabel label = (JLabel) child;

				// Terrible way to identify labels but it's the only way to
				// distinguish them from other JLabels right now
				if ((label.getBackground().equals(
						ScriptWidgetFactory.LABEL_BACKGROUND_COLOUR) || label
						.getBackground().equals(ScriptEaseUI.COLOUR_DISABLED))
						&& label.getForeground().equals(
								ScriptWidgetFactory.LABEL_TEXT_COLOUR)) {

					mainPanel.remove(child);
					isSpacer = true;
				}
			}
		}

		// Add the new labels.
		for (String label : component.getLabels()) {
			final Color bgColour;
			final JLabel newLabel;

			if (label.equals(StoryComponent.DISABLE_TEXT))
				bgColour = ScriptEaseUI.COLOUR_DISABLED;
			else
				bgColour = ScriptWidgetFactory.LABEL_BACKGROUND_COLOUR;

			newLabel = ScriptWidgetFactory.buildLabel(label,
					ScriptWidgetFactory.LABEL_TEXT_COLOUR, bgColour);

			mainPanel.add(newLabel, 0);
			mainPanel.add(Box.createHorizontalStrut(5), 1);
		}
	}

	/**
	 * Reconstructs the StoryComponentPanel display names
	 * 
	 * @param panel
	 */
	public void rebuildDisplayName(StoryComponentPanel panel) {
		final JPanel mainPanel = panel.getLayout().getMainPanel();
		final Component[] children = mainPanel.getComponents();
		final JLabel nameLabel = ScriptWidgetFactory.buildLabel(panel
				.getStoryComponent().getDisplayText());

		// Remove all existing display names
		for (Component child : children) {
			if (child instanceof JLabel) {
				final JLabel label = (JLabel) child;

				// Terrible way to identify display names but it's the only way
				// to distinguish them from other JLabels right now
				if (label.getForeground() == Color.BLACK) {
					mainPanel.remove(child);
				}
			}
		}
		nameLabel.setForeground(Color.BLACK);
		mainPanel.add(nameLabel, 0);
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
				StoryComponentPanel childPanel = buildStoryComponentPanel(child);

				// Add the child panel to the parent panel.
				panel.add(childPanel, StoryComponentPanelLayoutManager.CHILD,
						index);

				// Update the settings of the child panel to match the parents
				childPanel.updateComplexSettings();

				boolean select = false;
				// if the parent is selected, select the child as well
				if (!(parent instanceof StoryPoint)) {
					select = selectionManager.getSelectedPanels().contains(
							panel);
				}

				selectionManager.addPanel(childPanel, select);
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
	 * Converts the display text of a StoryComponent from a String into a
	 * graphical representation that uses different GUI components.
	 * 
	 * @param storyComponent
	 * @param addLabels
	 *            whether you want labels added to the
	 *            <code>StoryComponentPanel</code>
	 * 
	 * @author graves
	 * @author mfchurch
	 * @author jyuen
	 */
	public void parseDisplayText(JPanel displayNamePanel,
			StoryComponent storyComponent, boolean addLabels) {
		int paramTagStart;
		int paramTagEnd;
		String tagName;
		JLabel plainTextLabel;
		String plainText;
		StoryComponent knowIt = null;

		displayNamePanel.removeAll();

		displayNamePanel.setOpaque(false);
		displayNamePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

		if (addLabels) {
			// Add the StoryComponent's labels
			for (String labelText : storyComponent.getLabels()) {
				if (!labelText.isEmpty()) {
					final Color bgColour;
					if (labelText.equals(StoryComponent.DISABLE_TEXT))
						bgColour = ScriptEaseUI.COLOUR_DISABLED;
					else
						bgColour = ScriptWidgetFactory.LABEL_BACKGROUND_COLOUR;

					JLabel label = ScriptWidgetFactory.buildLabel(labelText,
							ScriptWidgetFactory.LABEL_TEXT_COLOUR, bgColour);

					displayNamePanel.add(label, 0);
					displayNamePanel.add(Box.createHorizontalStrut(5), 1);
				}
			}
		}

		String toParse = storyComponent.getDisplayText();
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

				if (storyComponent instanceof AskIt) {
					knowIt = ((AskIt) storyComponent).getCondition();
				} else if (tagName.equalsIgnoreCase(CURRENT_STORY_POINT_TAG)) {

					StoryComponent owner = storyComponent;
					while (!(owner instanceof StoryPoint) && owner != null) {
						owner = owner.getOwner();
					}

					if (owner instanceof StoryPoint) {
						knowIt = owner;
					}
				}
				// Now check for a parameter/implicit tag
				else {
					// Check for parameters
					if (knowIt == null && storyComponent instanceof ScriptIt) {
						knowIt = getParameterWithText(
								(ScriptIt) storyComponent, tagName);
					}

					// Invalid parameter tags are treated as plain text, valid
					// ones are represented with the appropriate widget.
					if (knowIt == null)
						plainText += toParse.substring(paramTagStart,
								paramTagEnd + 1);
				}
			}

			final Color textColor;
			if (storyComponent.isEnabled())
				textColor = Color.BLACK;
			else
				textColor = ScriptEaseUI.COLOUR_DISABLED;

			plainTextLabel = ScriptWidgetFactory.buildLabel(plainText);

			plainTextLabel.setForeground(textColor);

			displayNamePanel.add(plainTextLabel);

			if (knowIt instanceof KnowIt) {
				addWidget(displayNamePanel, (KnowIt) knowIt, false);
				knowIt = null;
			} else if (knowIt instanceof StoryPoint) {
				displayNamePanel.add(ScriptWidgetFactory.buildBindingWidget(
						(StoryPoint) knowIt, false));
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
	 * @param panel
	 * @param knowIt
	 */
	private void addWidget(final JPanel panel, final KnowIt knowIt,
			final boolean editable) {

		knowIt.getBinding().process(new BindingAdapter() {
			// functions and descriptions get a draggable bubble with no slot
			@Override
			public void processFunction(KnowItBindingFunction function) {
				panel.add(ScriptWidgetFactory.buildBindingWidget(knowIt,
						editable));
			}

			// everything else gets a regular slot
			@Override
			protected void defaultProcess(KnowItBinding binding) {
				panel.add(ScriptWidgetFactory.buildSlotPanel(knowIt, false));
			}
		});
	}

	private StoryAdapter componentProcessor(final StoryComponentPanel panel) {
		return new StoryAdapter() {
			@Override
			public void processStoryPoint(StoryPoint storyPoint) {
				final String tutorialUri = "https://sites.google.com/a/ualberta.ca/scriptease/development/scriptease2/tutorials";
				final String tutorialText = "Click here to go to find tutorials and other information on the ScriptEase II web site.";
				final String welcomeText = "Welcome to ScriptEase II!";
				final String getStartedText = "Add some nodes to the start node to get started. If you don't know how, go to the help menu for help.";
				final int fontSize = 18;

				// Add an expansion button
				final JPanel mainPanel;
				final JLabel welcomeLabel = new JLabel(welcomeText);
				final JLabel getStartedLabel = new JLabel(getStartedText);
				final JButton tutorialButton = ComponentFactory
						.buildLinkButton(tutorialUri, tutorialText);

				mainPanel = new JPanel();

				mainPanel.setOpaque(false);

				// Add a BindingWidget for the StoryPoint
				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);

				if (storyPoint.isRoot()) {
					mainPanel.setLayout(new BoxLayout(mainPanel,
							BoxLayout.PAGE_AXIS));

					GUIOp.resizeFont(fontSize, welcomeLabel);
					GUIOp.resizeFont(fontSize, getStartedLabel);
					GUIOp.resizeFont(fontSize, tutorialButton);

					mainPanel.add(welcomeLabel);
					mainPanel.add(getStartedLabel);
					mainPanel.add(tutorialButton);

				} else {
					// Add the children panels
					addChildrenPanels(storyPoint, panel);
				}

			}

			@Override
			public void processBehaviour(final Behaviour behaviour) {
				final JPanel mainPanel;
				mainPanel = new JPanel();

				parseDisplayText(mainPanel, behaviour, true);

				// Add a modification button
				final JButton modificationButton;
				mainPanel.add(Box.createHorizontalStrut(3));
				modificationButton = ComponentFactory.buildEditButton();
				mainPanel.add(modificationButton);

				// Add a listener for the modification button
				modificationButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						final SEModel model = SEModelManager.getInstance()
								.getActiveModel();

						if (model instanceof StoryModel) {
							((StoryModel) model)
									.notifyBehaviourEdited(behaviour);
						}
					}
				});

				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);
			}

			@Override
			public void processPickIt(final PickIt pickIt) {
				final JPanel mainPanel;
				mainPanel = new JPanel();

				parseDisplayText(mainPanel, pickIt, true);

				// Add a addition button
				final JButton additionButton;
				mainPanel.add(Box.createHorizontalStrut(3));
				additionButton = ComponentFactory.buildAddButton();
				mainPanel.add(additionButton);

				// Add a listener for the modification button
				additionButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						pickIt.addChoice(50);
					}
				});

				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);

				// Add the children panels
				addChildrenPanels(pickIt, panel);
			}

			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				// Add an expansion button
				if (!(complex instanceof ScriptIt)
						|| complex instanceof CauseIt
						|| complex instanceof ControlIt
						|| complex instanceof ActivityIt)
					addExpansionButton(complex, panel);

				// Add probability box if it is a children of PickIt
				if (complex.getOwner() instanceof PickIt) {
					addProbabilityBox(complex, panel);
				}

				final JPanel mainPanel;
				mainPanel = new JPanel();

				parseDisplayText(mainPanel, complex, true);

				// Add a label for the complex story component
				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);

				// Add a minus button if it is a children of PickIt
				if (complex.getOwner() instanceof PickIt
						&& !complex.getDisplayText().equals(PickIt.CHOICE_ONE)
						&& !complex.getDisplayText().equals(PickIt.CHOICE_TWO)) {
					addRemoveButton(complex, mainPanel);
				}

				// Add the children panels
				addChildrenPanels(complex, panel);
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				final JPanel mainPanel;

				mainPanel = new JPanel();

				mainPanel.setOpaque(false);
				mainPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

				knowIt.getBinding().resolveBinding()
						.process(new BindingAdapter() {
							@Override
							public void processNull(
									KnowItBindingNull nullBinding) {
								StoryComponentPanelFactory.this.addWidget(
										mainPanel, knowIt, true);
							}

							@Override
							public void processFunction(
									KnowItBindingFunction function) {

								for (String labelText : function.getValue()
										.getLabels()) {
									if (!labelText.isEmpty()) {
										final Color bgColor;
										if (labelText
												.equals(StoryComponent.DISABLE_TEXT))
											bgColor = ScriptEaseUI.COLOUR_DISABLED;
										else
											bgColor = ScriptWidgetFactory.LABEL_BACKGROUND_COLOUR;

										JLabel label = ScriptWidgetFactory
												.buildLabel(
														labelText,
														ScriptWidgetFactory.LABEL_TEXT_COLOUR,
														bgColor);
										mainPanel.add(label, 0);
										mainPanel.add(
												Box.createHorizontalStrut(5), 1);
									}
								}

								StoryComponentPanelFactory.this.addWidget(
										mainPanel, knowIt, true);

								final JLabel label;

								label = ScriptWidgetFactory
										.buildLabel(" describes ");
								if (knowIt.isEnabled())
									label.setForeground(Color.black);
								else
									label.setForeground(ScriptEaseUI.COLOUR_DISABLED);

								mainPanel.add(label);

								final DescribeIt describeIt;

								describeIt = TranslatorManager.getInstance()
										.getActiveTranslator()
										.getDescribeIt(knowIt);

								if (describeIt != null) {
									mainPanel.add(new DescribeItPanel(knowIt,
											describeIt));
								}
							}
						});

				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);
			}

			@Override
			public void processNote(Note note) {
				final JPanel mainPanel;
				final JLabel noteLabel;
				final JComponent nameEditor;

				noteLabel = new JLabel(StoryComponentPanelFactory.noteIcon);
				mainPanel = new JPanel();

				nameEditor = ScriptWidgetFactory.buildNameEditor(note);

				nameEditor.setForeground(ScriptEaseUI.COLOUR_NOTE_TEXT);
				noteLabel.setForeground(ScriptEaseUI.COLOUR_NOTE_TEXT);

				mainPanel.setOpaque(false);

				mainPanel.add(noteLabel);
				mainPanel.add(nameEditor);

				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);
			}

			private void addChildrenPanels(ComplexStoryComponent complex,
					StoryComponentPanel panel) {
				final boolean hasChildren = complex.getChildCount() > 0;
				if (hasChildren) {
					// Add child panels
					for (StoryComponent component : complex.getChildren()) {
						final StoryComponentPanel childPanel;

						childPanel = StoryComponentPanelFactory.getInstance()
								.buildStoryComponentPanel(component);

						panel.add(childPanel,
								StoryComponentPanelLayoutManager.CHILD);
					}
				}
			}

			private void addExpansionButton(ComplexStoryComponent complex,
					final StoryComponentPanel panel) {
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

					panel.setExpansionButton(expansionButton);
					panel.add(expansionButton,
							StoryComponentPanelLayoutManager.EXPANSION_BUTTON);
				}
			}

			private void addRemoveButton(final ComplexStoryComponent complex,
					final JPanel panel) {
				if (!(complex.getOwner() instanceof PickIt)
						&& !(complex instanceof StoryComponentContainer))
					return;

				final PickIt owner = (PickIt) complex.getOwner();

				final JButton removeButton;

				removeButton = ComponentFactory.buildRemoveButton();

				removeButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						owner.removeChoice((StoryComponentContainer) complex);
					}
				});

				panel.add(Box.createHorizontalStrut(2));
				panel.add(removeButton);
			}

			private void addProbabilityBox(final ComplexStoryComponent complex,
					final StoryComponentPanel panel) {
				if (!(complex.getOwner() instanceof PickIt)
						&& !(complex instanceof StoryComponentContainer))
					return;

				final PickIt owner = (PickIt) complex.getOwner();

				final JTextField probabilityField;

				probabilityField = ComponentFactory.buildNumberTextField();

				probabilityField.setMaximumSize(new Dimension(5, 20));
				probabilityField.setMinimumSize(new Dimension(5, 20));
				probabilityField.setPreferredSize(new Dimension(5, 20));

				probabilityField.setText(Integer.toString(owner.getChoices()
						.get(complex)));

				probabilityField.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						final String text = probabilityField.getText();

						if (text == null || text.equals("")) {
							owner.getChoices().put(
									(StoryComponentContainer) complex, 0);
						} else {
							owner.getChoices().put(
									(StoryComponentContainer) complex,
									Integer.parseInt(text));
						}
					}
				});

				panel.add(probabilityField,
						StoryComponentPanelLayoutManager.TEXTFIELD);
			}
		};
	}
}
