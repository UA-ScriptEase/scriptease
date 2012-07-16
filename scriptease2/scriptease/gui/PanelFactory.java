package scriptease.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import scriptease.ScriptEase;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.StoryVisitor;
import scriptease.controller.VisibilityManager;
import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.action.typemenus.ShowTypeMenuAction;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.pane.GameObjectPane;
import scriptease.gui.pane.LibraryPane;
import scriptease.gui.storycomponentbuilder.TypeMenuComponent;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.GameObjectPicker;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.LineFragment;
import scriptease.translator.codegenerator.code.fragments.LiteralFragment;
import scriptease.translator.codegenerator.code.fragments.ScopeFragment;
import scriptease.translator.io.model.GameType.TypeValueWidgets;

/**
 * A factory class for different panels. All major panel construction should go
 * in here.
 * 
 * @author kschenk
 * 
 */
public class PanelFactory {
	private static PanelFactory instance = new PanelFactory();

	public static PanelFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a panel for editing Quests.
	 * 
	 * @param start
	 *            Start Point of the graph.
	 * @return
	 */
	public JPanel buildQuestPanel(final GraphNode start) {
		final JPanel questPanel = new JPanel(new BorderLayout(), true);
		final GraphPanel graphPanel = new GraphPanel(start);

		ToolBarButtonAction.addJComponent(graphPanel);

		final JToolBar graphToolBar = ToolBarFactory
				.buildGraphEditorToolBar(graphPanel);
		final JToolBar questToolBar = ToolBarFactory
				.buildQuestEditorToolBar(graphPanel);

		questPanel.add(graphToolBar.add(questToolBar), BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

		questPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

		return questPanel;
	}

	/**
	 * Creates a panel for editing DescribeIts.
	 * 
	 * @param start
	 *            Start Point of the graph
	 * @return
	 */
	public JPanel buildDescribeItPanel(final GraphNode start,
			final DescribeIt describeIt) {
		final JPanel describeItPanel = new JPanel(new BorderLayout(), true);
		final GraphPanel graphPanel = new GraphPanel(start);

		DescribeIt editedDescribeIt = describeIt.clone();
		editedDescribeIt.clearSelection();

		graphPanel.setHeadNode(editedDescribeIt.getHeadNode());

		ToolBarButtonAction.addJComponent(graphPanel);

		final JToolBar graphToolBar = ToolBarFactory
				.buildGraphEditorToolBar(graphPanel);

		final JToolBar describeItToolBar = ToolBarFactory
				.buildDescribeItToolBar(editedDescribeIt, graphPanel);

		describeItPanel.add(graphToolBar.add(describeItToolBar),
				BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

		describeItPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

		return describeItPanel;
	}

	/**
	 * Builds a pane containing all game objects in the active module, organized
	 * by category, allowing the user to drag them onto bindings in a Story.
	 * 
	 * @return A JPanel GameObject picker.
	 * @author graves
	 * @author mfchurch
	 */
	public JPanel buildGameObjectPane(StoryModel model) {
		GameObjectPicker picker;

		if (model != null) {
			Translator translator = model.getTranslator();
			if (translator != null) {
				// Get the picker
				if ((picker = translator.getCustomGameObjectPicker()) == null) {
					picker = new GameObjectPane();
				}
				return picker.getPickerPanel();
			}
		}
		// otherwise return an empty hidden JPanel
		JPanel jPanel = new JPanel();
		jPanel.setVisible(false);
		return jPanel;
	}

	/**
	 * Creates a new JPanel with nothing inside except a JLabel of the passed
	 * text.
	 * 
	 * @param text
	 * @return
	 */
	private JPanel buildEmptyPanelWithText(String text) {
		final JPanel nullPanel;
		final JLabel nullLabel;

		nullPanel = new JPanel();
		nullLabel = new JLabel(text);

		nullPanel.setLayout(new BorderLayout());

		nullPanel.add(nullLabel, BorderLayout.CENTER);

		return nullPanel;
	}

	/**
	 * Builds an empty StoryComponentEditorPanel.
	 * 
	 * @return
	 */
	public JPanel buildStoryComponentEditorPanel() {
		final JPanel editorPanel;

		editorPanel = new JPanel();

		editorPanel.setLayout(new BorderLayout());
		editorPanel.add(buildStoryComponentEditorPanelContents(null),
				BorderLayout.CENTER);

		return editorPanel;
	}

	/**
	 * Creates a JPanel with fields for Name, Types, Labels, and a check box for
	 * Visibility. This JPanel is common to all story component editor panes.
	 * 
	 * @return
	 */
	private JComponent buildStoryComponentEditorPanelContents(
			final StoryComponent storyComponent) {

		// TODO Add in the Save and Reset buttons here, too.
		// I'm not sure what SpringLayout looks like, so I'll hold off
		// on those buttons until I get SE running again.

		// TODO Save button should be easy.
		// TODO Reset button should just reload the translator and redraw the
		// entire component builder.

		// If storyComponent is null, show a blank JPanel.
		if (storyComponent == null) {
			return buildEmptyPanelWithText("Select a Story Component to begin editing it.");
		}

		final JPanel editorPanel;
		final JScrollPane editorScrollPane;
		final JPanel descriptorPanel;

		final JLabel nameLabel;
		final JLabel typeLabel;
		final JLabel labelLabel;
		final JLabel visibleLabel;

		final JTextField nameField;
		// TODO Refactor the TypeMenuComponent. Make it work like the
		// librarypane one, or just use that one if it's possible.
		final ShowTypeMenuAction typeAction;
		final JButton typeButton;
		final JTextField labelField;
		final JCheckBox visibleBox;

		final Font labelFont;

		final GroupLayout descriptorPanelLayout;

		editorPanel = new JPanel();
		editorScrollPane = new JScrollPane(editorPanel);
		descriptorPanel = new JPanel();

		nameLabel = new JLabel("Name: ");
		typeLabel = new JLabel("Types: ");
		labelLabel = new JLabel("Labels: ");
		visibleLabel = new JLabel("Visible: ");

		nameField = new JTextField();
		typeAction = new ShowTypeMenuAction();
		typeButton = new JButton(typeAction);
		labelField = new JTextField();
		visibleBox = new JCheckBox();

		labelFont = new Font("SansSerif", Font.BOLD,
				Integer.parseInt(ScriptEase.getInstance().getPreference(
						ScriptEase.FONT_SIZE_KEY)) + 1);
		descriptorPanelLayout = new GroupLayout(descriptorPanel);

		editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.PAGE_AXIS));

		editorScrollPane.getVerticalScrollBar().setUnitIncrement(16);

		// Set up the descriptorPanel

		descriptorPanel.setLayout(descriptorPanelLayout);
		descriptorPanel.setBorder(new TitledBorder("Component Descriptors"));

		descriptorPanelLayout.setAutoCreateGaps(true);
		descriptorPanelLayout.setAutoCreateContainerGaps(true);

		// Set up the labels

		nameLabel.setFont(labelFont);
		nameLabel.setLabelFor(nameField);

		typeLabel.setFont(labelFont);
		typeLabel.setLabelFor(typeButton);

		labelLabel.setFont(labelFont);
		labelLabel.setLabelFor(labelField);

		visibleLabel.setFont(labelFont);
		visibleLabel.setLabelFor(visibleBox);

		// Set up the default field values

		nameField.setText(storyComponent.getDisplayText());

		String label = "";
		for (String storyComponentLabel : storyComponent.getLabels()) {
			label += storyComponentLabel + ", ";
		}
		int labelLength = label.length();
		if (labelLength > 0) {
			String shortenedLabel = label.substring(0, labelLength - 2);
			labelField.setText(shortenedLabel);
		}
		labelField.setText(getCollectionAsString(storyComponent.getLabels()));

		visibleBox.setSelected(VisibilityManager.getInstance().isVisible(
				storyComponent));

		// Set up the field listeners
		/*
		 * TODO Set up the field listeners. May need to add them in the process
		 * part!
		 */

		nameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				storyComponent.setDisplayText(nameField.getText());
			}
		});

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

				storyComponent.setLabels(labels);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		
		typeAction.getTypeSelectionDialogBuilder().deselectAll();

		// Add JComponents to DescriptorPanel using GroupLayout
		descriptorPanelLayout.setHorizontalGroup(descriptorPanelLayout
				.createSequentialGroup()
				.addGroup(
						descriptorPanelLayout.createParallelGroup()
								.addComponent(nameLabel)
								.addComponent(visibleLabel)
								.addComponent(labelLabel)
								.addComponent(typeLabel))
				.addGroup(
						descriptorPanelLayout.createParallelGroup()
								.addComponent(visibleBox)
								.addComponent(nameField)
								.addComponent(labelField)
								.addComponent(typeButton)));

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
								.addComponent(labelField))
				.addGroup(
						descriptorPanelLayout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(typeLabel)
								.addComponent(typeButton)));

		editorPanel.add(descriptorPanel);

		// Determine what kind of component was selected, and display the
		// appropriate panel.

		storyComponent.process(new AbstractNoOpStoryVisitor() {

			@Override
			public void processScriptIt(final ScriptIt scriptIt) {
				for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
					editorPanel.add(PanelFactory.getInstance()
							.buildCodeBlockEditorPanel(codeBlock));
				}
				
				typeAction.getTypeSelectionDialogBuilder().selectTypes(scriptIt.getTypes(), true);

				typeAction.setAction(new Runnable() {
					@Override
					public void run() {
						scriptIt.setTypes(typeAction.getTypeSelectionDialogBuilder().getSelectedTypes());
					}
				});
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				editorPanel.add(PanelFactory.getInstance()
						.buildDescriptionEditorPanel(knowIt));

				typeAction.getTypeSelectionDialogBuilder().selectTypes(knowIt.getTypes(), true);

				typeAction.setAction(new Runnable() {
					@Override
					public void run() {
						knowIt.setTypes(typeAction.getTypeSelectionDialogBuilder().getSelectedTypes());
					}
				});
			}

			@Override
			public void defaultProcess(StoryComponent component) {
				editorPanel.removeAll();
				editorPanel
						.add(buildEmptyPanelWithText("Select a Story Component to begin editing it."));
			}

			// We may want to implement these later, so their default methods
			// are here in case.
			@Override
			public void processAskIt(AskIt questionIt) {
				// Unimplemented
				this.defaultProcess(questionIt);
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer container) {
				// Unimplemented
				this.defaultProcess(container);
			}
		});

		return editorScrollPane;

	}

	/**
	 * A JPanel used to edit CodeBlocks. This shows the id, slot, includes,
	 * types, parameters, and code for the passed in CodeBlock, and allows the
	 * user to edit it.
	 * 
	 * @param codeBlock
	 * @return
	 */
	private JPanel buildCodeBlockEditorPanel(CodeBlock codeBlock) {
		final JPanel codeBlockEditorPanel;

		final JLabel idLabel;
		final JLabel slotLabel;
		final JLabel includesLabel;
		final JLabel typesLabel;
		final JLabel parametersLabel;
		final JLabel codeLabel;

		final JButton addParameterButton;
		final JTextField slotField;
		final JTextField includesField;
		final TypeMenuComponent typeSelector;
		final JButton typesButton;
		final JScrollPane parameterScrollPane;
		final JPanel parameterPanel;
		final JComponent codePanel;

		final GroupLayout codeBlockEditorLayout;
		final Font labelFont;
		final List<KnowIt> parameters;

		codeBlockEditorPanel = new JPanel();

		idLabel = new JLabel("ID# 35235"); // TODO Implement ID checking. Will
											// do once merged with Robin's code.
		slotLabel = new JLabel("Slot: ");
		includesLabel = new JLabel("Includes: ");
		typesLabel = new JLabel("Types: ");
		parametersLabel = new JLabel("Parameters: ");
		codeLabel = new JLabel("Code");

		slotField = new JTextField();
		includesField = new JTextField();
		typeSelector = new TypeMenuComponent();
		typesButton = typeSelector.getRootButton();
		addParameterButton = new JButton("+");
		parameterPanel = new JPanel();
		parameterScrollPane = new JScrollPane(parameterPanel);
		codePanel = buildCodeInputComponent(codeBlock);
		;

		codeBlockEditorLayout = new GroupLayout(codeBlockEditorPanel);
		labelFont = new Font("SansSerif", Font.BOLD,
				Integer.parseInt(ScriptEase.getInstance().getPreference(
						ScriptEase.FONT_SIZE_KEY)) + 1);
		parameters = codeBlock.getParameters();

		// Set up the codeBlockEditorPanel and the scroll pane

		codeBlockEditorPanel.setLayout(codeBlockEditorLayout);
		codeBlockEditorPanel.setBorder(new TitledBorder("Code Block: "));

		codeBlockEditorLayout.setAutoCreateGaps(true);
		codeBlockEditorLayout.setAutoCreateContainerGaps(true);

		parameterPanel.setLayout(new BoxLayout(parameterPanel,
				BoxLayout.PAGE_AXIS));

		parameterScrollPane.setBorder(BorderFactory.createEmptyBorder());

		// Set up the labels

		idLabel.setForeground(Color.GRAY);

		slotLabel.setFont(labelFont);
		slotLabel.setLabelFor(slotField);

		includesLabel.setFont(labelFont);
		includesLabel.setLabelFor(includesField);

		typesLabel.setFont(labelFont);
		typesLabel.setLabelFor(typesButton);

		parametersLabel.setFont(labelFont);
		parametersLabel.setLabelFor(parameterScrollPane);

		codeLabel.setFont(labelFont);
		codeLabel.setLabelFor(codePanel);

		// Set up the default field values

		if (codeBlock.hasSlot())
			slotField.setText(codeBlock.getSlot());

		includesField.setText(getCollectionAsString(codeBlock.getIncludes()));

		ArrayList<String> types = new ArrayList<String>();
		types.addAll(codeBlock.getTypes());
		typeSelector.setTrueData(types);

		for (KnowIt parameter : parameters) {
			parameterPanel.add(buildParameterComponent(parameter));
		}

		// Set up the listeners

		/*
		 * slotField.addActionListener(new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent e) {
		 * codeBlock.setSlot(slotField.getText()); }
		 * 
		 * });
		 */
		// TODO Set the listeners
		// Here's some old code from the codePanel

		/*
		 * Old Code: final Collection<FormatFragment> codeFragments =
		 * codeBlock.getCode(); if (codeFragments.size() > 0)
		 * codePane.setCodeFragments(codeFragments);
		 * parameterList.updateBindingList(codeBlock.getParameters());
		 */

		codeBlockEditorLayout.setHorizontalGroup(codeBlockEditorLayout
				.createSequentialGroup()
				.addGroup(
						codeBlockEditorLayout.createParallelGroup()
								.addComponent(slotLabel)
								.addComponent(includesLabel)
								.addComponent(typesLabel)
								.addComponent(parametersLabel)
								.addComponent(addParameterButton)
								.addComponent(codeLabel))
				.addGroup(
						codeBlockEditorLayout
								.createParallelGroup()
								.addComponent(idLabel,
										GroupLayout.Alignment.TRAILING)
								.addComponent(slotField)
								.addComponent(includesField)
								.addComponent(typesButton)
								.addComponent(parameterScrollPane)
								.addComponent(codePanel)));

		codeBlockEditorLayout.setVerticalGroup(codeBlockEditorLayout
				.createSequentialGroup()
				.addComponent(idLabel)
				.addGroup(
						codeBlockEditorLayout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(slotLabel)
								.addComponent(slotField))
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

		return codeBlockEditorPanel;
	}

	/*
	 * TODO Will need to implement DescribeItGraphPanel here or just combine
	 * them
	 */
	public JPanel buildDescriptionEditorPanel(KnowIt knowIt) {
		final JPanel descriptionEditorPanel;
		// final StoryComponentBindingList bindingList;

		descriptionEditorPanel = new JPanel();
		// bindingList = new StoryComponentBindingList(BindingContext.BINDING);
		// TODO See effect editor to see how to do this

		descriptionEditorPanel.setLayout(new BoxLayout(descriptionEditorPanel,
				BoxLayout.PAGE_AXIS));
		descriptionEditorPanel.add(Box.createVerticalGlue());

		// Set text of NameField to knowIt.getDisplayText();
		String labelList = "";
		for (String label : knowIt.getLabels())
			label += label + ", ";
		// Set the Label list to labelList
		// Set the types to knowIt.getTypes()

		return descriptionEditorPanel;
	}

	/**
	 * Builds the library pane for the Story Component Editor. This pane allows
	 * loading of different translators through a ComboBox.
	 * 
	 * @param editorPanel
	 * @return
	 */
	public JPanel buildStoryComponentLibraryPanel(JPanel editorPanel) {
		final List<Translator> translators;
		final JComboBox<Translator> libSelector;
		final JPanel libraryPanel;
		final JPanel translatorPanel;
		final LibraryPane libraryPane;
		final Translator activeTranslator;
		final TreeSelectionListener librarySelectionListener;
		final StoryVisitor storyVisitor;

		libraryPanel = new JPanel();
		translatorPanel = new JPanel();
		translators = new ArrayList<Translator>();
		libraryPane = new LibraryPane();
		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		storyVisitor = editorPanelUpdater(editorPanel);

		libraryPanel.setLayout(new BoxLayout(libraryPanel, BoxLayout.Y_AXIS));

		translators.add(null);
		translators.addAll(TranslatorManager.getInstance().getTranslators());

		libSelector = new JComboBox<Translator>(new Vector<Translator>(
				translators));
		libSelector.setSelectedItem(activeTranslator);

		// TODO This should update the editorpanel, too!
		libSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setTranslator((Translator) libSelector.getSelectedItem());
			}
		});

		translatorPanel.add(new JLabel("Currently loaded translator: "));
		translatorPanel.add(libSelector);

		libraryPanel.add(translatorPanel);

		librarySelectionListener = UIListenerFactory.getInstance()
				.buildStoryComponentLibraryListener(storyVisitor);

		// Add the tree listener
		libraryPane.addTreeSelectionListener(librarySelectionListener);

		libraryPanel.add(libraryPane);

		return libraryPanel;
	}

	/**
	 * Creates an AbstractNoOpStoryVisitor which calls an update on the
	 * editorPanel. This is used as a sort of Command Pattern with
	 * UIListenerFactory.
	 * 
	 * @param editorPanel
	 *            The editorPanel to update.
	 * 
	 * @return
	 */
	public StoryVisitor editorPanelUpdater(final JPanel editorPanel) {
		StoryVisitor storyVisitor = new AbstractNoOpStoryVisitor() {
			@Override
			public void defaultProcess(StoryComponent component) {
				editorPanel.removeAll();
				editorPanel.add(
						buildStoryComponentEditorPanelContents(component),
						BorderLayout.CENTER);

				editorPanel.repaint();
				editorPanel.revalidate();
			}
		};
		return storyVisitor;
	}

	/**
	 * Method that returns a collection of Strings as a single String seperated
	 * by commas.
	 * 
	 * An example:
	 * 
	 * Collection<String> collection = ["First"], ["Second"], ["Third"]
	 * getCollectionAsString(collection) == "First, Second, Third"
	 * 
	 * @param strings
	 * @return
	 */
	private String getCollectionAsString(Collection<String> strings) {
		String collectionText = "";

		for (String includeText : strings) {
			collectionText += includeText + ", ";
		}
		int labelLength = collectionText.length();
		if (labelLength > 0) {
			return collectionText.substring(0, labelLength - 2);
		} else
			return "";
	}

	/**
	 * Parameter creator and editor component.
	 * 
	 * @param knowIt
	 * @return
	 */
	private JComponent buildParameterComponent(KnowIt knowIt) {
		final JPanel parameterComponent;

		final JTextField nameField;
		final TypeMenuComponent typeSelector;
		final ArrayList<String> types;
		final JButton typesButton;
		final JComboBox<String> defaultTypeBox;
		final JButton deleteButton;
		final GroupLayout groupLayout;
		final JComponent bindingConstantSettingComponent;

		final JTextField inactiveTextField;

		final Translator translator;
		final GameTypeManager gameTypeManager;
		final TypeValueWidgets defaultTypeGuiType;

		parameterComponent = new JPanel();

		nameField = new JTextField(knowIt.getDisplayText(), 10);
		typeSelector = new TypeMenuComponent();
		types = new ArrayList<String>();
		typesButton = typeSelector.getRootButton();
		defaultTypeBox = new JComboBox<String>();
		deleteButton = new JButton("-");
		groupLayout = new GroupLayout(parameterComponent);

		inactiveTextField = new JTextField(" Cannot set binding for ["
				+ knowIt.getDefaultType() + "]");

		translator = TranslatorManager.getInstance().getActiveTranslator();
		gameTypeManager = translator.getGameTypeManager();
		defaultTypeGuiType = gameTypeManager.getGui(knowIt.getDefaultType());

		parameterComponent.setLayout(groupLayout);
		parameterComponent.setBorder(new TitledBorder(knowIt.getDisplayText()));

		types.addAll(knowIt.getTypes());
		typeSelector.setTrueData(types);

		inactiveTextField.setEnabled(false);

		for (String type : types)
			defaultTypeBox.addItem(type);

		defaultTypeBox.setSelectedItem(knowIt.getDefaultType());

		// Determine what to do with parameters

		/*
		 * TODO BindingConstantSettingComponent should update when the default
		 * type is changed, since we could choose to set a default binding at
		 * that point.
		 * 
		 * This would also mean we don't have to worry about this when creating
		 * a new parameter, since the new one will just have a void type as
		 * default.
		 */

		if (defaultTypeGuiType == null)
			bindingConstantSettingComponent = inactiveTextField;
		else {
			switch (defaultTypeGuiType) {
			case JTEXTFIELD:
				bindingConstantSettingComponent = new JTextField(30);
				break;
			case JSPINNER:
				bindingConstantSettingComponent = new JSpinner();

				break;
			case JCOMBOBOX:
				final Map<String, String> map;
				final JComboBox<String> selectorBox;

				map = gameTypeManager.getEnumMap(knowIt.getDefaultType());
				selectorBox = new JComboBox<String>();

				for (String key : map.keySet())
					selectorBox.addItem(key + " [" + map.get(key) + "]");

				bindingConstantSettingComponent = selectorBox;
				break;
			default: {
				inactiveTextField.setText("Unimplemented GUI Type: "
						+ defaultTypeGuiType.toString());
				bindingConstantSettingComponent = inactiveTextField;
				break;
			}
			}
		}

		JPanel defaultTypeBoxPanel = new JPanel();
		JPanel nameFieldPanel = new JPanel();
		JPanel bindingPanel = new JPanel();

		defaultTypeBoxPanel.add(defaultTypeBox);
		nameFieldPanel.add(nameField);
		bindingPanel.add(bindingConstantSettingComponent);

		nameFieldPanel.setBorder(new TitledBorder("Name"));
		defaultTypeBoxPanel.setBorder(new TitledBorder("Default Type"));
		bindingPanel.setBorder(new TitledBorder("Default Binding"));

		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
		groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
				.addComponent(nameFieldPanel).addComponent(typesButton)
				.addComponent(defaultTypeBoxPanel).addComponent(bindingPanel)
				.addComponent(deleteButton));

		groupLayout.setVerticalGroup(groupLayout
				.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(nameFieldPanel).addComponent(typesButton)
				.addComponent(deleteButton).addComponent(defaultTypeBoxPanel)
				.addComponent(bindingPanel));

		return parameterComponent;
	}

	private JComponent buildCodeInputComponent(CodeBlock codeBlock) {
		final JTextPane codePane;
		final JScrollPane codeScrollPane;

		final StyledDocument codePaneDoc;
		final Style defaultStyle;
		final Style parameterStyle;

		final Collection<FormatFragment> codeFragments;

		codePane = new JTextPane();
		codeScrollPane = new JScrollPane(codePane);

		codePaneDoc = codePane.getStyledDocument();
		defaultStyle = codePane.addStyle("DEFAULT_STYLE", null);
		parameterStyle = codePane.addStyle("PARAMETER_STYLE", defaultStyle);

		codeFragments = codeBlock.getCode();

		// Set up the styles

		StyleConstants.setFontSize(defaultStyle, 12);
		StyleConstants.setFontFamily(defaultStyle, "Courier");

		StyleConstants.setForeground(parameterStyle, Color.BLUE);
		StyleConstants.setBold(parameterStyle, true);

		codePane.setLogicalStyle(defaultStyle);

		// Set up the ScrollPane

		codeScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// Write to the codePane.
		// TODO Extract method so it can be called by Listener. (? Necessary?)
		/*
		 * TODO Need to implement some way of getting code from fragments, and
		 * also to check what kind of fragment it is.
		 */
		for (FormatFragment codeFragment : codeFragments) {
			if (codeFragment instanceof LineFragment) {
				for (FormatFragment b : ((LineFragment) codeFragment)
						.getSubFragments()) {
					if (b instanceof LiteralFragment) {
						try {
							codePaneDoc.insertString(codePaneDoc.getLength(),
									b.getDirectiveText(), defaultStyle);
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (b instanceof ScopeFragment) {
						// int currentOffset = code.length();
						// String scopeString = ((ScopeFragment)
						// b).getNameRef();
						// int currentEnd = currentOffset +
						// scopeString.length();
						// StringIndx scopeIndexInfo = new StringIndx(
						// currentOffset, currentEnd);
						// scopeIndicies.add(scopeIndexInfo);
						// code += scopeString;
						try {
							codePaneDoc.insertString(codePaneDoc.getLength(),
									((ScopeFragment) b).getNameRef(),
									parameterStyle);
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
				try {
					codePaneDoc.insertString(codePaneDoc.getLength(), "\n",
							defaultStyle);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// codePane.setText(code);

		// Add a DocumentListener to codePaneDoc

		return codePane;
	}

	/**
	 * Method to set the translator. This is separate so that Progress.aj can
	 * show the loading bar when a new Translator is loading.
	 * 
	 * @author remiller
	 * 
	 * @param t
	 *            The Translator to load
	 */
	private void setTranslator(Translator t) {
		TranslatorManager.getInstance().setActiveTranslator(t);
	}
}
