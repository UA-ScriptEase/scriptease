package scriptease.gui.storycomponentpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
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
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

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
 */
public class StoryComponentPanelFactory {
	public static final String CURRENT_STORY_POINT_TAG = "#currentStoryPoint";

	private static final StoryComponentPanelFactory instance = new StoryComponentPanelFactory();
	private static final String QUESTION = "question";
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
				StoryComponentPanelTree parentTree = panel.getParentTree();
				if (parentTree != null) {
					childPanel.updateComplexSettings();
				}

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
	 * @author graves
	 * @author mfchurch
	 */
	public void parseDisplayText(JPanel displayNamePanel,
			StoryComponent storyComponent) {
		int paramTagStart;
		int paramTagEnd;
		String tagName;
		JLabel plainTextLabel;
		String plainText;
		StoryComponent knowIt = null;

		displayNamePanel.removeAll();

		displayNamePanel.setOpaque(false);
		displayNamePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

		// Add the StoryComponent's labels
		for (String labelText : storyComponent.getLabels()) {
			if (!labelText.isEmpty()) {
				JLabel label = ScriptWidgetFactory.buildLabel(labelText,
						ScriptWidgetFactory.LABEL_TEXT_COLOUR,
						ScriptWidgetFactory.LABEL_BACKGROUND_COLOUR);
				displayNamePanel.add(label);
				displayNamePanel.add(Box.createHorizontalStrut(5));
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

				if (tagName.equalsIgnoreCase(QUESTION)
						&& storyComponent instanceof AskIt) {
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

			plainTextLabel = ScriptWidgetFactory.buildLabel(plainText,
					Color.BLACK);

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
				// Add an expansion button
				final JPanel mainPanel;
				mainPanel = new JPanel();

				mainPanel.setOpaque(false);

				// Add a BindingWidget for the StoryPoint
				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);

				// Add the children panels
				addChildrenPanels(storyPoint, panel);
			}

			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				// Add an expansion button

				if (complex instanceof ScriptIt) {
					if (complex instanceof CauseIt
							|| complex instanceof ControlIt) {
						addExpansionButton(complex, panel);
					}
				} else
					addExpansionButton(complex, panel);

				final JPanel mainPanel;
				mainPanel = new JPanel();

				parseDisplayText(mainPanel, complex);

				// Add a label for the complex story component
				panel.add(mainPanel, StoryComponentPanelLayoutManager.MAIN);

				// Add the children panels
				addChildrenPanels(complex, panel);
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				final JPanel mainPanel;
				final BindingAdapter adapter;

				mainPanel = new JPanel();

				adapter = new BindingAdapter() {
					@Override
					public void processNull(KnowItBindingNull nullBinding) {
					}

					@Override
					public void processFunction(KnowItBindingFunction function) {
						mainPanel.add(ScriptWidgetFactory.buildLabel(
								" describes ", Color.black));

						final Translator active;
						final DescribeIt describeIt;

						active = TranslatorManager.getInstance()
								.getActiveTranslator();

						describeIt = active.getDescribeIt(knowIt);

						if (describeIt != null) {
							mainPanel.add(new DescribeItPanel(knowIt,
									describeIt));
						}
					}
				};

				mainPanel.setOpaque(false);
				mainPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

				StoryComponentPanelFactory.this.addWidget(mainPanel, knowIt,
						true);

				knowIt.getBinding().resolveBinding().process(adapter);
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
						StoryComponentPanel childPanel = StoryComponentPanelFactory
								.getInstance().buildStoryComponentPanel(
										component);
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
							StoryComponentPanelLayoutManager.BUTTON);
				}
			}

		};
	}
}
