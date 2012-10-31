package scriptease.gui.libraryeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.ScriptEase;
import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.SetEffectObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.ToolBarFactory;
import scriptease.gui.SEGraph.DescribeItNodeGraphModel;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraph.SelectionMode;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.SEGraph.renderers.EditableDescribeItNodeRenderer;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction.ToolBarMode;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.pane.LibraryPanel;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.APIDictionary;
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
		final MouseListener librarySelectionListener;

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
							editingPanel.add(buildCodeBlockComponent(codeBlock,
									scriptIt));
						}

						editingPanel.revalidate();
					}
				};
			}

			@Override
			public void processScriptIt(final ScriptIt scriptIt) {
				this.defaultProcess(scriptIt);

				final JPanel codeBlockEditingPanel;
				final JPanel scriptItControlPanel;
				final JButton addCodeBlockButton;

				codeBlockEditingPanel = new JPanel();
				scriptItControlPanel = new JPanel();
				addCodeBlockButton = new JButton("Add CodeBlock");

				codeBlockEditingPanel.setLayout(new BoxLayout(
						codeBlockEditingPanel, BoxLayout.PAGE_AXIS));
				scriptItControlPanel.setLayout(new FlowLayout(
						FlowLayout.LEADING));

				// Causes and effects are processed as ScriptIts
				addCodeBlockButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().startUndoableAction(
									"Adding CodeBlock to "
											+ scriptIt.getDisplayText());

						final CodeBlock codeBlock;

						codeBlock = new CodeBlockSource(TranslatorManager
								.getInstance().getActiveTranslator()
								.getApiDictionary().getNextCodeBlockID());

						scriptIt.addCodeBlock(codeBlock);

						UndoManager.getInstance().endUndoableAction();
					}
				});

				scriptItControlPanel.setBorder(BorderFactory
						.createTitledBorder("ScriptIt Controls"));
				scriptItControlPanel.add(addCodeBlockButton);

				editorPanel.add(scriptItControlPanel);
				editorPanel.add(codeBlockEditingPanel);

				this.setUpCodeBlockPanels(scriptIt, codeBlockEditingPanel)
						.run();

				scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
						.getInstance().buildScriptItEditorObserver(
								setUpCodeBlockPanels(scriptIt,
										codeBlockEditingPanel)));

				editorPanel.revalidate();
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				editorPanel.removeAll();
				editorPanel.revalidate();
				editorPanel.repaint();

				final JPanel typePanel;
				final JPanel describeItEditingPanel;

				final TypeAction typeAction;
				final JButton typesButton;

				final JLabel typesLabel;
				final APIDictionary apiDictionary;
				final DescribeItManager describeItManager;

				final DescribeIt describeIt;

				typePanel = new JPanel();

				typeAction = new TypeAction();
				typesButton = new JButton(typeAction);

				typesLabel = new JLabel("Types: ");

				apiDictionary = TranslatorManager.getInstance()
						.getActiveTranslator().getApiDictionary();
				describeItManager = apiDictionary.getDescribeItManager();

				describeIt = describeItManager.findDescribeItForTypes(knowIt
						.getTypes());
				describeItEditingPanel = LibraryEditorPanelFactory
						.getInstance().buildDescribeItEditingPanel(describeIt,
								knowIt);

				typesLabel.setFont(labelFont);

				typeAction.getTypeSelectionDialogBuilder().deselectAll();
				typeAction.getTypeSelectionDialogBuilder().selectTypes(
						knowIt.getTypes(), true);

				typePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
				typePanel.setBorder(BorderFactory.createTitledBorder("Types"));

				typeAction.setAction(new Runnable() {
					@Override
					public void run() {
						final Collection<String> types;
						final Collection<String> properCaseTypes;

						types = typeAction.getTypeSelectionDialogBuilder()
								.getSelectedTypes();
						properCaseTypes = new ArrayList<String>();

						// Important: DescribeIt types MUST be set first because
						// KnowIts notify observers when their's are changed,
						// throwing NullPointExceptions everywhere!
						describeIt.setTypes(types);

						knowIt.setTypes(types);

						for (String type : types) {
							properCaseTypes.add(StringOp.toProperCase(type));
						}

						knowIt.setDisplayText(StringOp.getCollectionAsString(
								properCaseTypes, ", "));

					}
				});

				typePanel.add(typesLabel);
				typePanel.add(typesButton);

				editorPanel.add(typePanel);

				describeItEditingPanel.setLayout(new BoxLayout(
						describeItEditingPanel, BoxLayout.PAGE_AXIS));

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

		librarySelectionListener = LibraryEditorListenerFactory.getInstance()
				.buildStoryComponentMouseListener(storyVisitor);

		// Add the tree listener
		libraryPane.addListMouseListener(librarySelectionListener);

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
		final DescribeItNodeGraphModel describeItGraphModel;

		bindingPanel = new JPanel();
		describeItGraphPanel = new JPanel();
		graphToolBar = ToolBarFactory.getInstance().buildGraphEditorToolBar();

		// XXX I don't think the types are updated for this when KnowIt's types
		// are. Need to check, and possibly add a StoryComponentObserver.
		effectHolder = new EffectHolderPanel(describeIt.getTypes());
		describeItGraphModel = new DescribeItNodeGraphModel(
				describeIt.getStartNode());
		graph = new SEGraph<DescribeItNode>(describeItGraphModel,
				SelectionMode.SELECT_PATH);

		effectObserver = new SetEffectObserver() {
			@Override
			public void effectChanged(ScriptIt newEffect) {
				// We need to make a copy or else the path is ALWAYS the current
				// selected nodes, which is not what we want at all.
				final Collection<DescribeItNode> selectedNodes;

				selectedNodes = new ArrayList<DescribeItNode>(
						graph.getSelectedNodes());

				describeIt.assignScriptItToPath(selectedNodes, newEffect);

				knowIt.setBinding(describeIt.getScriptItForPath(describeIt
						.getShortestPath()));
			}
		};

		graph.setNodeRenderer(new EditableDescribeItNodeRenderer(graph));

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
		GraphToolBarModeAction.addJComponent(graph);
		GraphToolBarModeAction.setMode(ToolBarMode.SELECT);

		// Set up the JPanel containing the graph
		describeItGraphPanel.setLayout(new BorderLayout());
		describeItGraphPanel.add(graphToolBar, BorderLayout.PAGE_START);
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
	 * Builds a JTextField used to edit the name of the story component.
	 * 
	 * @param component
	 * @return
	 */
	private JTextField buildNameEditorField(final StoryComponent component) {
		final JTextField nameField;

		nameField = new JTextField(component.getDisplayText());

		// TODO: This is way too slow. See if we can just use the Name Editor in
		// ScriptWidgetFactory instead.
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				component.setDisplayText(nameField.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

		return nameField;
	}

	/**
	 * Builds a JTextField used to edit the labels of a story component.
	 * 
	 * @param component
	 * @return
	 */
	private JTextField buildLabelEditorField(final StoryComponent component) {
		final JTextField labelField;
		final String labelToolTip;

		labelField = new JTextField(StringOp.getCollectionAsString(
				component.getLabels(), ", "));
		labelToolTip = "<html><b>Labels</b> are seperated by commas.<br>"
				+ "Leading and trailing spaces are<br>"
				+ "removed automatically.</html>";

		labelField.setToolTipText(labelToolTip);

		// TODO Also too slow. See above.
		labelField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				final String labelFieldText;
				final String[] labelArray;
				final Collection<String> labels;

				labelFieldText = labelField.getText();
				labelArray = labelFieldText.split(",");
				labels = new ArrayList<String>();

				for (String label : labelArray) {
					labels.add(label.trim());
				}
				component.setLabels(labels);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

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
				component.setVisible(visibleBox.isSelected());
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

		nameField = this.buildNameEditorField(component);
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
	 * allows the user to edit it.
	 * 
	 * @param scriptIt
	 * 
	 * @param codeBlock
	 * @return
	 */
	private JComponent buildCodeBlockComponent(final CodeBlock codeBlock,
			final ScriptIt scriptIt) {
		final JLabel subjectLabel;
		final JLabel slotLabel;
		final JLabel implicitsLabelLabel;
		final JLabel includesLabel;
		final JLabel typesLabel;
		final JLabel parametersLabel;
		final JLabel codeLabel;

		final JPanel codeBlockPanel;
		final JPanel parameterPanel;
		final JScrollPane parameterScrollPane;

		final TypeAction typeAction;
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

		subjectLabel = new JLabel("Subject: ");
		slotLabel = new JLabel("Slot: ");
		implicitsLabelLabel = new JLabel("Implicits: ");
		includesLabel = new JLabel("Includes: ");
		typesLabel = new JLabel("Types: ");
		parametersLabel = new JLabel("Parameters: ");
		codeLabel = new JLabel("Code: ");

		codeBlockPanel = new JPanel();
		parameterPanel = new JPanel();
		parameterScrollPane = new JScrollPane(parameterPanel);

		typeAction = new TypeAction();
		includesField = new JTextField();
		subjectBox = new JComboBox();
		slotBox = new JComboBox();
		implicitsLabel = new JLabel();
		codePanel = new CodeEditorPanel(codeBlock);

		deleteCodeBlockButton = new JButton("Delete CodeBlock");
		addParameterButton = new JButton("+");
		typesButton = new JButton(typeAction);

		codeBlockEditorLayout = new GroupLayout(codeBlockPanel);
		labelFont = new Font("SansSerif", Font.BOLD,
				Integer.parseInt(ScriptEase.getInstance().getPreference(
						ScriptEase.FONT_SIZE_KEY)) + 1);

		parameters = codeBlock.getParameters();

		// Set up the layout
		codeBlockPanel.setLayout(codeBlockEditorLayout);
		codeBlockPanel.setBorder(new TitledBorder("Code Block #"
				+ codeBlock.getId()));

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
				.getInstance().buildParameterPanelObserver(codeBlock,
						parameterPanel, subjectBox));

		scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
				.getInstance().buildSubjectBoxObserver(codeBlock, subjectBox,
						slotBox));

		scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
				.getInstance().buildSlotBoxObserver(codeBlock, implicitsLabel));

		scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
				.getInstance()
				.buildParameterNameObserver(codeBlock, subjectBox));

		subjectBox.addItem(null);
		for (KnowIt parameter : scriptIt.getParameters()) {
			final Collection<String> slots = getCommonSlotsForTypes(parameter);

			if (!slots.isEmpty())
				subjectBox.addItem(parameter.getDisplayText());
		}

		implicitsLabel.setForeground(Color.DARK_GRAY);

		includesField.setText(StringOp.getCollectionAsString(
				codeBlock.getIncludes(), ", "));

		ArrayList<String> types = new ArrayList<String>();
		types.addAll(codeBlock.getTypes());

		typeAction.getTypeSelectionDialogBuilder().deselectAll();
		typeAction.getTypeSelectionDialogBuilder().selectTypes(types, true);

		final String initialSlot;

		if (codeBlock.hasSlot())
			initialSlot = codeBlock.getSlot();
		else
			initialSlot = "";

		if (codeBlock.hasSubject()) {
			KnowIt subject = codeBlock.getSubject();
			if (subject != null) {
				final Collection<String> slots;
				final String subjectName;

				subjectName = codeBlock.getSubjectName();
				slots = getCommonSlotsForTypes(subject);

				for (String slot : slots) {
					slotBox.addItem(slot);
				}
				subjectBox.setSelectedItem(subjectName);
				slotBox.setSelectedItem(initialSlot);
			}
		}

		String implicits = "";

		for (KnowIt implicit : codeBlock.getImplicits())
			implicits += "[" + implicit.getDisplayText() + "] ";

		implicitsLabel.setText(implicits.trim());

		deleteCodeBlockButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction(
							"Adding CodeBlock to " + scriptIt.getDisplayText());
				scriptIt.removeCodeBlock(codeBlock);
				UndoManager.getInstance().endUndoableAction();
			}
		});

		if (scriptIt.getCodeBlocks().size() < 2) {
			deleteCodeBlockButton.setEnabled(false);
		}

		for (KnowIt parameter : parameters) {
			parameterPanel.add(this.buildParameterPanel(scriptIt, codeBlock,
					parameter));
		}

		includesField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				final String labelFieldText;
				final String[] labelArray;
				final Collection<String> labels;

				labelFieldText = includesField.getText();
				labelArray = labelFieldText.split(",");
				labels = new ArrayList<String>();

				for (String label : labelArray) {
					labels.add(label.trim());
				}
				codeBlock.setIncludes(labels);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

		typeAction.setAction(new Runnable() {
			@Override
			public void run() {
				codeBlock.setTypes(typeAction.getTypeSelectionDialogBuilder()
						.getSelectedTypes());

				scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
						StoryComponentChangeEnum.CODE_BLOCK_TYPES_SET));
			}
		});

		addParameterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final KnowIt knowIt;
				final List<KnowIt> parameters;

				knowIt = new KnowIt();
				parameters = codeBlock.getParameters();

				parameters.add(knowIt);
				codeBlock.setParameters(parameters);

				scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
						StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD));
			}
		});

		subjectBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String subjectName;
				subjectName = (String) subjectBox.getSelectedItem();

				codeBlock.setSubject(subjectName);

				scriptIt.updateStoryChildren();
				scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
						StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET));
			}
		});

		slotBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedSlot = (String) slotBox.getSelectedItem();

				if (selectedSlot != null)
					codeBlock.setSlot((String) slotBox.getSelectedItem());
				else
					codeBlock.setSlot("");

				scriptIt.updateStoryChildren();
				scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
						StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET));
			}
		});

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
								.addComponent(subjectBox).addComponent(slotBox)
								.addComponent(implicitsLabel)
								.addComponent(includesField)
								.addComponent(typesButton)
								.addComponent(parameterScrollPane)
								.addComponent(codePanel)));

		codeBlockEditorLayout.setVerticalGroup(codeBlockEditorLayout
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
								.addComponent(slotLabel).addComponent(slotBox))
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
												.addComponent(parametersLabel)
												.addComponent(
														addParameterButton))
								.addComponent(parameterScrollPane))
				.addGroup(
						codeBlockEditorLayout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(codeLabel)
								.addComponent(codePanel)));
		return codeBlockPanel;
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

	protected JPanel buildParameterPanel(ScriptIt scriptIt,
			CodeBlock codeBlock, KnowIt knowIt) {
		return new ParameterPanel(scriptIt, codeBlock, knowIt);
	}
}
