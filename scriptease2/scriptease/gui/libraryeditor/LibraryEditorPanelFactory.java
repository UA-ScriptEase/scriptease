package scriptease.gui.libraryeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

import scriptease.ScriptEase;
import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.SetEffectObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction.ToolBarMode;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.pane.LibraryPanel;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.LibraryModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.DescribeItManager;
import scriptease.util.StringOp;

/**
 * A factory used to create a library editor. This is a singleton class, so use
 * the {@link #getInstance()} method to work with it.
 * 
 * @author kschenk
 * 
 */
public class LibraryEditorPanelFactory {
	private static LibraryEditorPanelFactory instance = new LibraryEditorPanelFactory();
	private static Font labelFont = new Font("SansSerif", Font.BOLD,
			Integer.parseInt(ScriptEase.getInstance().getPreference(
					ScriptEase.FONT_SIZE_KEY)) + 1);

	// Stores the current observers for the selected StoryComponent so that they
	// do not get garbage collected.
	private StoryComponentObserver currentNameObserver;
	private StoryComponentObserver currentLabelObserver;

	/**
	 * Returns the single instance of StoryComponentBuilderPanelFactory
	 * 
	 * @return
	 */
	public static LibraryEditorPanelFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a JPanel with fields for Name, Labels, and a check box for
	 * Visibility. This JPanel is common to all library editor panes.
	 * 
	 * @param libraryPane
	 *            The LibraryPanel to be acted on.
	 * 
	 * @return
	 */
	public JPanel buildLibraryEditorPanel(final LibraryPanel libraryPane) {
		final StoryVisitor storyVisitor;

		final JPanel editorPanel;
		editorPanel = new JPanel();
		editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.PAGE_AXIS));

		/*
		 * Create an AbstractNoOpStoryVisitor which calls an update on the
		 * editorPanel. This is used as a sort of Command Pattern with
		 * UIListenerFactory.
		 */
		storyVisitor = new StoryAdapter() {

			private Runnable setUpCodeBlockPanels(final ScriptIt scriptIt,
					final JPanel editingPanel) {
				return new Runnable() {
					@Override
					public void run() {
						final Collection<CodeBlock> codeBlocks;
						codeBlocks = scriptIt.getCodeBlocks();

						editingPanel.removeAll();
						FormatFragmentSelectionManager.getInstance()
								.setFormatFragment(null, null);

						for (CodeBlock codeBlock : codeBlocks) {
							editingPanel.add(new CodeBlockPanel(codeBlock,
									scriptIt));
						}

						editingPanel.revalidate();
					}
				};
			}

			@Override
			public void processScriptIt(final ScriptIt scriptIt) {
				// Causes and effects are processed as ScriptIts
				this.defaultProcess(scriptIt);

				final JPanel codeBlockEditingPanel;

				codeBlockEditingPanel = new JPanel();

				codeBlockEditingPanel.setLayout(new BoxLayout(
						codeBlockEditingPanel, BoxLayout.PAGE_AXIS));

				if (!scriptIt.isCause()) {
					final JPanel scriptItControlPanel;
					final JButton addCodeBlockButton;

					scriptItControlPanel = new JPanel();
					addCodeBlockButton = new JButton("Add CodeBlock");

					scriptItControlPanel.setLayout(new FlowLayout(
							FlowLayout.LEADING));

					scriptItControlPanel.setBorder(BorderFactory
							.createTitledBorder("Effect Control"));

					addCodeBlockButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (!UndoManager.getInstance()
									.hasOpenUndoableAction())
								UndoManager.getInstance().startUndoableAction(
										"Adding CodeBlock to "
												+ scriptIt.getDisplayText());

							final CodeBlock codeBlock;

							codeBlock = new CodeBlockSource(
									((LibraryModel) SEModelManager
											.getInstance().getActiveModel())
											.getNextCodeBlockID());

							scriptIt.addCodeBlock(codeBlock);

							UndoManager.getInstance().endUndoableAction();
						}
					});
					scriptItControlPanel.add(addCodeBlockButton);
					editorPanel.add(scriptItControlPanel);
				}

				editorPanel.add(codeBlockEditingPanel);

				this.setUpCodeBlockPanels(scriptIt, codeBlockEditingPanel)
						.run();

				scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
						.getInstance().buildScriptItEditorObserver(
								setUpCodeBlockPanels(scriptIt,
										codeBlockEditingPanel)));

				editorPanel.revalidate();
			}

			/**
			 * @param knowIt
			 */
			@Override
			public void processKnowIt(final KnowIt knowIt) {
				editorPanel.removeAll();
				editorPanel.revalidate();
				editorPanel.repaint();

				final JPanel knowItPanel;
				final JPanel describeItEditingPanel;

				final GroupLayout knowItPanelLayout;
				final TypeAction typeAction;
				final Runnable commitText;

				final JButton typesButton;
				final JTextField nameField;

				final JLabel nameLabel;
				final JLabel typesLabel;

				final DescribeItManager describeItManager;

				final DescribeIt describeIt;

				knowItPanel = new JPanel();

				knowItPanelLayout = new GroupLayout(knowItPanel);
				typeAction = new TypeAction();
				typesButton = new JButton(typeAction);

				describeItManager = TranslatorManager.getInstance()
						.getActiveDescribeItManager();

				describeIt = describeItManager.getDescribeIt(knowIt);

				nameField = new JTextField(describeIt.getName());

				nameLabel = new JLabel("Name: ");
				typesLabel = new JLabel("Types: ");

				commitText = new Runnable() {
					@Override
					public void run() {
						describeIt.setName(nameField.getText());
						knowIt.setDisplayText(nameField.getText());
					}
				};

				describeItEditingPanel = LibraryEditorPanelFactory
						.getInstance().buildDescribeItEditingPanel(describeIt,
								knowIt);

				knowItPanel.setLayout(knowItPanelLayout);

				typesLabel.setFont(labelFont);
				nameLabel.setFont(labelFont);

				typeAction.getTypeSelectionDialogBuilder().deselectAll();
				typeAction.getTypeSelectionDialogBuilder().selectTypes(
						knowIt.getTypes(), true);

				WidgetDecorator.decorateJTextFieldForFocusEvents(nameField,
						commitText, false);

				nameField.setHorizontalAlignment(JTextField.LEADING);

				knowItPanel.setBorder(BorderFactory
						.createTitledBorder("DescribeIt"));

				typeAction.setAction(new Runnable() {
					@Override
					public void run() {
						final Collection<String> types;

						types = typeAction.getTypeSelectionDialogBuilder()
								.getSelectedTypes();

						// Important: DescribeIt types MUST be set first because
						// KnowIts notify observers when their's are changed,
						// throwing NullPointExceptions everywhere!
						describeIt.setTypes(types);

						knowIt.setTypes(types);
					}
				});

				knowItPanelLayout
						.setHorizontalGroup(knowItPanelLayout
								.createParallelGroup()
								.addGroup(
										knowItPanelLayout
												.createSequentialGroup()
												.addGroup(
														knowItPanelLayout
																.createParallelGroup()
																.addComponent(
																		nameLabel)
																.addComponent(
																		typesLabel))
												.addGroup(
														knowItPanelLayout
																.createParallelGroup()
																.addComponent(
																		nameField)
																.addComponent(
																		typesButton))));

				knowItPanelLayout.setVerticalGroup(knowItPanelLayout
						.createSequentialGroup()
						.addGroup(
								knowItPanelLayout
										.createParallelGroup(
												GroupLayout.Alignment.BASELINE)
										.addComponent(nameLabel)
										.addComponent(nameField))
						.addGroup(
								knowItPanelLayout
										.createParallelGroup(
												GroupLayout.Alignment.BASELINE)
										.addComponent(typesLabel)
										.addComponent(typesButton)));

				editorPanel.add(knowItPanel);

				editorPanel.add(describeItEditingPanel);
			}

			// We may want to implement these later, so their default methods
			// are here in case.
			@Override
			public void processAskIt(AskIt questionIt) {
				this.defaultProcess(questionIt);
			}

			@Override
			public void defaultProcess(StoryComponent component) {
				LibraryEditorListenerFactory.getInstance()
						.refreshCodeBlockComponentObserverList();
				FormatFragmentSelectionManager.getInstance().setFormatFragment(
						null, null);

				editorPanel.removeAll();

				editorPanel.add(LibraryEditorPanelFactory.this
						.buildDescriptorPanel(component));

				editorPanel.revalidate();
				editorPanel.repaint();
			}
		};

		// Add the tree listener
		// mfchurch TODO we shouldn't do this multiple times
		libraryPane.assignListSelectionListener(storyVisitor);

		return editorPanel;
	}

	/**
	 * Builds a panel used to edit a KnowItBindingDescribeIt.
	 * 
	 * @param describeIt
	 * @param knowIt
	 * @return
	 */
	private JPanel buildDescribeItEditingPanel(final DescribeIt describeIt,
			final KnowIt knowIt) {
		final JPanel bindingPanel;
		final JPanel describeItGraphPanel;
		final JToolBar graphToolBar;

		final EffectHolderPanel effectHolder;
		final SetEffectObserver effectObserver;
		final SEGraph<DescribeItNode> graph;

		bindingPanel = new JPanel();
		describeItGraphPanel = new JPanel();
		graphToolBar = ComponentFactory.buildGraphEditorToolBar();

		effectHolder = new EffectHolderPanel(describeIt.getTypes());

		graph = SEGraphFactory.buildDescribeItEditorGraph(describeIt
				.getStartNode());

		// Set the effectHolder to reflect the initial path of the describeIt
		// (since it doesn't throw a path selection even in SEGraph the
		// constructor)
		final ScriptIt initialScriptIt = describeIt.getScriptItForPath(graph
				.getSelectedNodes());
		effectHolder.setEffect(initialScriptIt);

		effectObserver = new SetEffectObserver() {
			@Override
			public void effectChanged(ScriptIt newEffect) {
				// We need to make a copy or else the path is ALWAYS the current
				// selected nodes, which is not what we want at all.
				final Collection<DescribeItNode> selectedNodes;

				selectedNodes = new ArrayList<DescribeItNode>(
						graph.getSelectedNodes());

				describeIt.assignScriptItToPath(selectedNodes, newEffect);

				final ScriptIt scriptItForPath = describeIt
						.getScriptItForPath(describeIt.getShortestPath());
				if (scriptItForPath != null) {
					knowIt.setBinding(scriptItForPath);
				} else {
					knowIt.clearBinding();
				}

			}
		};

		graph.addSEGraphObserver(new SEGraphAdapter<DescribeItNode>() {

			@Override
			public void nodesSelected(Collection<DescribeItNode> nodes) {
				final ScriptIt pathScriptIt;
				pathScriptIt = describeIt.getScriptItForPath(nodes);

				effectHolder.removeSetEffectObserver(effectObserver);
				effectHolder.setEffect(pathScriptIt);
				effectHolder.addSetEffectObserver(effectObserver);
			}
		});

		effectHolder.addSetEffectObserver(effectObserver);

		/*
		 * TODO We may need a listener that updates the graph on model changes.
		 * Not implementing this unless it's necessary, because the only case
		 * where this should happen is if we have two library editors open. In
		 * that case, we need to refactor a lot of code here anyways, incl this
		 */

		// Reset the ToolBar to select and add the Graph to it.
		GraphToolBarModeAction.setMode(ToolBarMode.SELECT);

		// Set up the JPanel containing the graph
		describeItGraphPanel.setLayout(new BorderLayout());
		describeItGraphPanel.add(graphToolBar, BorderLayout.WEST);
		describeItGraphPanel.add(new JScrollPane(graph), BorderLayout.CENTER);

		bindingPanel
				.setLayout(new BoxLayout(bindingPanel, BoxLayout.PAGE_AXIS));
		bindingPanel.setBorder(BorderFactory
				.createTitledBorder("DescribeIt Binding"));

		bindingPanel.add(effectHolder);
		bindingPanel.add(describeItGraphPanel);

		return bindingPanel;
	}

	/**
	 * Builds a JTextField used to edit the name of the story component. The
	 * TextField gets updated if the Story Component's name changes for other
	 * reasons, such as undoing.
	 * 
	 * @param component
	 * @return
	 */
	private JTextField buildNameEditorPanel(final StoryComponent component) {
		final JTextField nameField;
		final Runnable commitText;

		nameField = new JTextField(component.getDisplayText());

		commitText = new Runnable() {
			@Override
			public void run() {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					String text = nameField.getText();
					if (!text.equals(component.getDisplayText())) {
						UndoManager.getInstance().startUndoableAction(
								"Change " + component + "'s display text to "
										+ text);
						component.setDisplayText(text);

						if (UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().endUndoableAction();
					}
				}
			}
		};

		this.currentNameObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
					nameField.setText(component.getDisplayText());
				}
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(nameField, commitText,
				false);

		nameField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(this.currentNameObserver);

		return nameField;
	}

	/**
	 * Builds a JTextField used to edit the labels of a story component.
	 * 
	 * @param component
	 * @return
	 */
	private JTextField buildLabelEditorField(final StoryComponent component) {
		final String SEPARATOR = ", ";

		final JTextField labelField;
		final String labelToolTip;
		final Runnable commitText;

		labelField = new JTextField(StringOp.getCollectionAsString(
				component.getLabels(), SEPARATOR));
		labelToolTip = "<html><b>Labels</b> are seperated by commas.<br>"
				+ "Leading and trailing spaces are<br>"
				+ "removed automatically.</html>";

		commitText = new Runnable() {
			@Override
			public void run() {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					final Collection<String> labels = new ArrayList<String>();
					final String labelFieldText = labelField.getText();
					if (!labelFieldText.isEmpty()) {
						final String[] labelArray = labelFieldText
								.split(SEPARATOR);
						for (String label : labelArray) {
							labels.add(label.trim());
						}
					}

					final Collection<String> oldLabels = component.getLabels();
					if (!oldLabels.containsAll(labels)) {
						UndoManager.getInstance().startUndoableAction(
								"Setting " + component + "'s labels to "
										+ labelFieldText);
						component.setLabels(labels);
						UndoManager.getInstance().endUndoableAction();
					}
				}
			}
		};

		this.currentLabelObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				StoryComponentChangeEnum eventType = event.getType();
				if (eventType == StoryComponentChangeEnum.CHANGE_LABELS_CHANGED) {
					labelField.setText(StringOp.getCollectionAsString(
							component.getLabels(), SEPARATOR));
				}
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(labelField,
				commitText, false);

		labelField.setToolTipText(labelToolTip);

		labelField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(this.currentLabelObserver);

		return labelField;
	}

	/**
	 * Builds a JCheckBox to set a component's visibility.
	 * 
	 * @param component
	 * @return
	 */
	private JCheckBox buildVisibleBox(final StoryComponent component) {
		final JCheckBox visibleBox;

		visibleBox = new JCheckBox();
		visibleBox.setSelected(component.isVisible());
		visibleBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					UndoManager.getInstance().startUndoableAction(
							"Toggle " + component + "'s visiblity");
					component.setVisible(visibleBox.isSelected());
					UndoManager.getInstance().endUndoableAction();
				}
			}
		});
		component.addStoryComponentObserver(new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_VISIBILITY) {
					visibleBox.setSelected(component.isVisible());
				}
			}
		});

		return visibleBox;
	}

	/**
	 * Builds a panel containing a name, label, and visibility editor.
	 * 
	 * @param component
	 * @return
	 */
	private JPanel buildDescriptorPanel(StoryComponent component) {
		final JPanel descriptorPanel;
		final GroupLayout descriptorPanelLayout;

		final JLabel nameLabel;
		final JLabel labelLabel;
		final JLabel visibleLabel;

		final JTextField nameField;
		final JTextField labelsField;
		final JCheckBox visibleBox;

		descriptorPanel = new JPanel();
		descriptorPanelLayout = new GroupLayout(descriptorPanel);

		nameLabel = new JLabel("Name: ");
		labelLabel = new JLabel("Labels: ");
		visibleLabel = new JLabel("Visible: ");

		nameField = this.buildNameEditorPanel(component);
		labelsField = this.buildLabelEditorField(component);
		visibleBox = this.buildVisibleBox(component);

		// Set up the labels
		nameLabel.setFont(labelFont);
		nameLabel.setLabelFor(nameField);

		labelLabel.setFont(labelFont);
		labelLabel.setLabelFor(labelsField);
		labelLabel.setToolTipText(labelsField.getToolTipText());

		visibleLabel.setFont(labelFont);
		visibleLabel.setLabelFor(visibleBox);

		// Set up the descriptorPanel
		descriptorPanel.setLayout(descriptorPanelLayout);
		descriptorPanel.setBorder(new TitledBorder("Component Descriptors"));

		descriptorPanelLayout.setAutoCreateGaps(true);
		descriptorPanelLayout.setAutoCreateContainerGaps(true);
		descriptorPanelLayout.setHonorsVisibility(true);

		// Add JComponents to DescriptorPanel using GroupLayout
		descriptorPanelLayout.setHorizontalGroup(descriptorPanelLayout
				.createParallelGroup().addGroup(
						descriptorPanelLayout
								.createSequentialGroup()
								.addGroup(
										descriptorPanelLayout
												.createParallelGroup()
												.addComponent(nameLabel)
												.addComponent(visibleLabel)
												.addComponent(labelLabel))
								.addGroup(
										descriptorPanelLayout
												.createParallelGroup()
												.addComponent(visibleBox)
												.addComponent(nameField)
												.addComponent(labelsField))));

		descriptorPanelLayout.setVerticalGroup(descriptorPanelLayout
				.createSequentialGroup()
				.addGroup(
						descriptorPanelLayout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(visibleLabel)
								.addComponent(visibleBox))
				.addGroup(
						descriptorPanelLayout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(nameLabel)
								.addComponent(nameField))
				.addGroup(
						descriptorPanelLayout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(labelLabel)
								.addComponent(labelsField)));

		return descriptorPanel;
	}

	/**
	 * Sets up a JPanel used to edit CodeBlocks. This shows the id, slot,
	 * includes, types, parameters, and code for the passed in CodeBlock, and
	 * allows the user to edit it. The panel also observes the provided
	 * codeBlock for changes
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @return
	 */
	@SuppressWarnings("serial")
	private class CodeBlockPanel extends JPanel implements
			StoryComponentObserver {
		private TypeAction typeAction;
		private CodeBlock codeBlock;

		public CodeBlockPanel(final CodeBlock codeBlock, final ScriptIt scriptIt) {
			final JLabel subjectLabel;
			final JLabel slotLabel;
			final JLabel implicitsLabelLabel;
			final JLabel includesLabel;
			final JLabel typesLabel;
			final JLabel parametersLabel;
			final JLabel codeLabel;

			final JPanel parameterPanel;
			final JScrollPane parameterScrollPane;

			final JTextField includesField;
			final JComboBox subjectBox;
			final JComboBox slotBox;
			final JLabel implicitsLabel;
			final CodeEditorPanel codePanel;

			final JButton deleteCodeBlockButton;
			final JButton addParameterButton;
			final JButton typesButton;

			final GroupLayout codeBlockEditorLayout;
			final Font labelFont;

			final List<KnowIt> parameters;

			this.codeBlock = codeBlock;
			codeBlock.addStoryComponentObserver(this);

			subjectLabel = new JLabel("Subject: ");
			slotLabel = new JLabel("Slot: ");
			implicitsLabelLabel = new JLabel("Implicits: ");
			includesLabel = new JLabel("Includes: ");
			typesLabel = new JLabel("Types: ");
			parametersLabel = new JLabel("Parameters: ");
			codeLabel = new JLabel("Code: ");
			implicitsLabel = new JLabel();

			parameterPanel = new JPanel();
			parameterScrollPane = new JScrollPane(parameterPanel);

			typeAction = new TypeAction();
			includesField = new IncludesField(codeBlock);

			if (scriptIt.isCause()) {
				subjectBox = new SubjectComboBox(codeBlock);
				slotBox = new SlotComboBox(codeBlock);
			} else {
				subjectBox = new JComboBox();
				subjectLabel.setVisible(false);
				subjectBox.setVisible(false);
				slotLabel.setVisible(false);
				slotBox = new JComboBox();
				slotBox.setVisible(false);
				implicitsLabel.setVisible(false);
				implicitsLabelLabel.setVisible(false);
				includesLabel.setVisible(false);
				includesField.setVisible(false);
			}

			codePanel = new CodeEditorPanel(codeBlock);

			deleteCodeBlockButton = new JButton("Delete CodeBlock");
			addParameterButton = new JButton("+");
			typesButton = new JButton(typeAction);

			codeBlockEditorLayout = new GroupLayout(this);
			labelFont = new Font("SansSerif", Font.BOLD,
					Integer.parseInt(ScriptEase.getInstance().getPreference(
							ScriptEase.FONT_SIZE_KEY)) + 1);

			parameters = codeBlock.getParameters();

			// Set up the layout
			this.setLayout(codeBlockEditorLayout);
			this.setBorder(new TitledBorder("Code Block #" + codeBlock.getId()));

			codeBlockEditorLayout.setAutoCreateGaps(true);
			codeBlockEditorLayout.setAutoCreateContainerGaps(true);
			codeBlockEditorLayout.setHonorsVisibility(true);

			parameterPanel.setLayout(new BoxLayout(parameterPanel,
					BoxLayout.PAGE_AXIS));

			parameterScrollPane.setPreferredSize(new Dimension(400, 250));
			parameterScrollPane.getVerticalScrollBar().setUnitIncrement(16);

			// Set up the label fonts and colors
			subjectLabel.setFont(labelFont);
			slotLabel.setFont(labelFont);
			implicitsLabelLabel.setFont(labelFont);
			includesLabel.setFont(labelFont);
			typesLabel.setFont(labelFont);
			parametersLabel.setFont(labelFont);
			codeLabel.setFont(labelFont);

			scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
					.getInstance().buildCodeBlockComponentObserver(
							deleteCodeBlockButton));

			scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
					.getInstance().buildParameterObserver(codeBlock,
							parameterPanel));

			scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
					.getInstance().buildSlotObserver(codeBlock, implicitsLabel));

			implicitsLabel.setForeground(Color.DARK_GRAY);

			final ArrayList<String> types = new ArrayList<String>(
					codeBlock.getTypes());
			typeAction.getTypeSelectionDialogBuilder().deselectAll();
			typeAction.getTypeSelectionDialogBuilder().selectTypes(types, true);

			String implicits = "";

			for (KnowIt implicit : codeBlock.getImplicits())
				implicits += "[" + implicit.getDisplayText() + "] ";

			implicitsLabel.setText(implicits.trim());

			typeAction.setAction(new Runnable() {
				@Override
				public void run() {
					if (!UndoManager.getInstance().hasOpenUndoableAction()) {
						final Collection<String> selectedTypes = typeAction
								.getTypeSelectionDialogBuilder()
								.getSelectedTypes();
						UndoManager.getInstance().startUndoableAction(
								"Setting CodeBlock " + codeBlock + " types to "
										+ selectedTypes);
						codeBlock.setTypes(selectedTypes);
						UndoManager.getInstance().endUndoableAction();
					}
				}
			});

			addParameterButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final KnowIt knowIt = new KnowIt();
					if (!UndoManager.getInstance().hasOpenUndoableAction()) {
						UndoManager.getInstance().startUndoableAction(
								"Add parameter " + knowIt + " to " + codeBlock);
						codeBlock.addParameter(knowIt);
						UndoManager.getInstance().endUndoableAction();
					}
				}
			});

			deleteCodeBlockButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().startUndoableAction(
								"Removing CodeBlock from "
										+ scriptIt.getDisplayText());
					scriptIt.removeCodeBlock(codeBlock);
					UndoManager.getInstance().endUndoableAction();
				}
			});

			if (scriptIt.getCodeBlocks().size() < 2) {
				deleteCodeBlockButton.setEnabled(false);
				deleteCodeBlockButton.setVisible(false);
			}

			for (KnowIt parameter : parameters) {
				parameterPanel.add(buildParameterPanel(scriptIt, codeBlock,
						parameter));
			}

			codeBlockEditorLayout.setHorizontalGroup(codeBlockEditorLayout
					.createSequentialGroup()
					.addGroup(
							codeBlockEditorLayout.createParallelGroup()
									.addComponent(subjectLabel)
									.addComponent(slotLabel)
									.addComponent(implicitsLabelLabel)
									.addComponent(includesLabel)
									.addComponent(typesLabel)
									.addComponent(parametersLabel)
									.addComponent(addParameterButton)
									.addComponent(codeLabel))
					.addGroup(
							codeBlockEditorLayout
									.createParallelGroup()
									.addComponent(deleteCodeBlockButton,
											GroupLayout.Alignment.TRAILING)
									.addComponent(subjectBox)
									.addComponent(slotBox)
									.addComponent(implicitsLabel)
									.addComponent(includesField)
									.addComponent(typesButton)
									.addComponent(parameterScrollPane)
									.addComponent(codePanel)));

			codeBlockEditorLayout
					.setVerticalGroup(codeBlockEditorLayout
							.createSequentialGroup()
							.addComponent(deleteCodeBlockButton)
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(subjectLabel)
											.addComponent(subjectBox))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(slotLabel)
											.addComponent(slotBox))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(implicitsLabelLabel)
											.addComponent(implicitsLabel))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(includesLabel)
											.addComponent(includesField))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(typesLabel)
											.addComponent(typesButton))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addGroup(
													codeBlockEditorLayout
															.createSequentialGroup()
															.addComponent(
																	parametersLabel)
															.addComponent(
																	addParameterButton))
											.addComponent(parameterScrollPane))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(codeLabel)
											.addComponent(codePanel)));
		}

		@Override
		public void componentChanged(StoryComponentEvent event) {
			final ArrayList<String> types = new ArrayList<String>(
					this.codeBlock.getTypes());
			typeAction.getTypeSelectionDialogBuilder().deselectAll();
			typeAction.getTypeSelectionDialogBuilder().selectTypes(types, true);
			typeAction.updateName();
		}
	}

	/**
	 * Returns a list of slots that are common in all of the types in the knowit
	 * passed in.
	 * 
	 * @param subject
	 * @return
	 */
	private Collection<String> getCommonSlotsForTypes(KnowIt subject) {
		final Collection<String> slots;

		slots = TranslatorManager.getInstance().getActiveTranslator()
				.getGameTypeManager().getSlots(subject.getDefaultType());

		for (String type : subject.getTypes()) {
			final Collection<String> otherSlots;

			otherSlots = new ArrayList<String>();

			for (String slot : TranslatorManager.getInstance()
					.getActiveTranslator().getGameTypeManager().getSlots(type)) {
				if (slots.contains(slot))
					otherSlots.add(slot);
			}

			slots.removeAll(slots);
			slots.addAll(otherSlots);
		}

		return slots;
	}

	/**
	 * Builds a parameter panel.
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @param knowIt
	 * @return
	 */
	protected JPanel buildParameterPanel(ScriptIt scriptIt,
			CodeBlock codeBlock, KnowIt knowIt) {
		return new ParameterPanel(scriptIt, codeBlock, knowIt);
	}

	@SuppressWarnings("serial")
	private class SlotComboBox extends JComboBox implements
			StoryComponentObserver {
		private boolean backgroundUpdate;
		private CodeBlock codeBlock;

		public SlotComboBox(final CodeBlock codeBlock) {
			this.codeBlock = codeBlock;
			backgroundUpdate = false;
			buildItems();
			this.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!backgroundUpdate) {
						final String currentlySelected = (String) getSelectedItem();
						if (!isCurrentSlotSelected(currentlySelected)) {
							if (!UndoManager.getInstance()
									.hasOpenUndoableAction()) {
								UndoManager.getInstance().startUndoableAction(
										"Setting CodeBlock slot to "
												+ currentlySelected);
							}
							codeBlock.setSlot(currentlySelected);
							UndoManager.getInstance().endUndoableAction();
						}
					}
				}
			});
			this.codeBlock.addStoryComponentObserver(this);
		}

		private void buildItems() {
			this.removeAllItems();
			final KnowIt subject = codeBlock.getSubject();
			if (subject != null) {
				final Collection<String> slots = getCommonSlotsForTypes(subject);
				for (String slot : slots) {
					this.addItem(slot);
				}
				this.setSelectedItem(codeBlock.getSlot());
			}
		}

		private boolean isCurrentSlotSelected(String value) {
			final String currentSlot = codeBlock.getSlot();
			if (currentSlot != null) {
				return currentSlot.equals(value);
			} else {
				return currentSlot == value;
			}
		}

		@Override
		public void componentChanged(StoryComponentEvent event) {
			backgroundUpdate = true;
			final StoryComponentChangeEnum type = event.getType();
			if (type == StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_DEFAULT_TYPE_SET) {
				buildItems();
			}
			this.revalidate();
			backgroundUpdate = false;
		}
	}

	@SuppressWarnings("serial")
	private class SubjectComboBox extends JComboBox implements
			StoryComponentObserver {
		private boolean backgroundUpdate;
		private CodeBlock codeBlock;

		public SubjectComboBox(final CodeBlock codeBlock) {
			this.codeBlock = codeBlock;
			backgroundUpdate = false;
			buildItems();
			this.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!backgroundUpdate) {
						final String currentlySelected = (String) getSelectedItem();
						if (!isCurrentSubjectSelected(currentlySelected)) {
							if (!UndoManager.getInstance()
									.hasOpenUndoableAction()) {
								UndoManager.getInstance().startUndoableAction(
										"Setting CodeBlock subject to "
												+ currentlySelected);
							}
							codeBlock.setSubject(currentlySelected);
							UndoManager.getInstance().endUndoableAction();
						}
					}
				}
			});
			this.codeBlock.addStoryComponentObserver(this);
			final ScriptIt scriptIt = codeBlock.getOwner();
			if (scriptIt != null) {
				scriptIt.addStoryComponentObserver(this);
			} else {
				throw new IllegalArgumentException("CodeBlock " + codeBlock
						+ " has no owner");
			}
		}

		private void buildItems() {
			this.removeAllItems();
			final ScriptIt scriptIt = codeBlock.getOwner();
			if (scriptIt != null) {
				final Collection<KnowIt> parameters = scriptIt.getParameters();
				for (KnowIt parameter : parameters) {
					final Collection<String> slots = getCommonSlotsForTypes(parameter);

					if (!slots.isEmpty())
						this.addItem(parameter.getDisplayText());
				}
				// this.addItem(null);
				this.setSelectedItem(codeBlock.getSubjectName());
			}
		}

		private boolean isCurrentSubjectSelected(String value) {
			final String currentSubject = codeBlock.getSubjectName();
			if (currentSubject != null) {
				return currentSubject.equals(value);
			} else {
				return currentSubject == value;
			}
		}

		@Override
		public void componentChanged(StoryComponentEvent event) {
			backgroundUpdate = true;
			final StoryComponentChangeEnum type = event.getType();
			if (type == StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_NAME_SET) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_DEFAULT_TYPE_SET) {
				buildItems();
			}
			this.revalidate();
			backgroundUpdate = false;
		}
	}

	@SuppressWarnings("serial")
	private class IncludesField extends JTextField implements
			StoryComponentObserver, ActionListener, FocusListener {
		private CodeBlock codeBlock;

		public IncludesField(CodeBlock codeBlock) {
			this.codeBlock = codeBlock;
			codeBlock.addStoryComponentObserver(this);
			this.addActionListener(this);
			this.addFocusListener(this);
		}

		@Override
		public void focusGained(FocusEvent e) {
		}

		@Override
		public void focusLost(FocusEvent e) {
			updateIncludes();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			updateIncludes();
		}

		@Override
		public void componentChanged(StoryComponentEvent event) {
			final StoryComponent source = event.getSource();
			if (source == codeBlock) {
				updateField();
			}
		}

		private void updateField() {
			this.setText(StringOp.getCollectionAsString(
					codeBlock.getIncludes(), ", "));
			this.revalidate();
		}

		private void updateIncludes() {
			final String labelFieldText;
			final String[] labelArray;
			final Collection<String> labels;

			labelFieldText = this.getText();
			labelArray = labelFieldText.split(",");
			labels = new ArrayList<String>();

			for (String label : labelArray) {
				labels.add(label.trim());
			}

			if (!labels.equals(codeBlock.getIncludes())) {
				// mfchurch TODO method type erasure problem with AspectJ
				// if (!UndoManager.getInstance().hasOpenUndoableAction())
				// UndoManager.getInstance().startUndoableAction(
				// "Setting Codeblock Includes to " + labels);
				codeBlock.setIncludes(labels);
				// UndoManager.getInstance().endUndoableAction();
			}
		}
	}
}
