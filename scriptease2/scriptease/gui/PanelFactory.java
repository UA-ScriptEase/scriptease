package scriptease.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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
import javax.swing.SwingUtilities;
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
import scriptease.controller.apimanagers.EventSlotManager;
import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.action.typemenus.TypeSelectionAction;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.pane.GameObjectPane;
import scriptease.gui.pane.LibraryPane;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
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
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameType.TypeValueWidgets;
import scriptease.translator.io.model.Slot;
import scriptease.translator.io.tools.GameConstantFactory;

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

		nullPanel.setLayout(new BoxLayout(nullPanel, BoxLayout.PAGE_AXIS));

		nullLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		nullPanel.add(Box.createVerticalGlue());
		nullPanel.add(nullLabel);
		nullPanel.add(Box.createVerticalGlue());

		return nullPanel;
	}

	/**
	 * Creates a JPanel with fields for Name, Labels, and a check box for
	 * Visibility. This JPanel is common to all story component editor panes.
	 * 
	 * @return
	 */
	public JComponent buildStoryComponentEditorComponent(
			final StoryComponent storyComponent) {

		// TODO A Save button should go into Menu Bar!

		// If storyComponent is null, show a blank JPanel.
		if (storyComponent == null) {
			return buildEmptyPanelWithText("");
		}

		final JPanel editorPanel;
		final JScrollPane editorScrollPane;
		final JPanel descriptorPanel;

		final JLabel nameLabel;
		final JLabel labelLabel;
		final JLabel visibleLabel;

		final JTextField nameField;
		final JTextField labelField;
		final JCheckBox visibleBox;

		final Font labelFont;

		final GroupLayout descriptorPanelLayout;

		final String labelToolTip;

		editorPanel = new JPanel();
		editorScrollPane = new JScrollPane(editorPanel);
		descriptorPanel = new JPanel();

		nameLabel = new JLabel("Name: ");
		labelLabel = new JLabel("Labels: ");
		visibleLabel = new JLabel("Visible: ");

		nameField = new JTextField();
		labelField = new JTextField();
		visibleBox = new JCheckBox();

		labelFont = new Font("SansSerif", Font.BOLD,
				Integer.parseInt(ScriptEase.getInstance().getPreference(
						ScriptEase.FONT_SIZE_KEY)) + 1);
		descriptorPanelLayout = new GroupLayout(descriptorPanel);

		labelToolTip = "<html><b>Labels</b> are seperated by commas.<br>"
				+ "Leading and trailing spaces are<br>"
				+ "removed automatically.</html>";

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

		labelLabel.setFont(labelFont);
		labelLabel.setLabelFor(labelField);
		labelLabel.setToolTipText(labelToolTip);
		visibleLabel.setFont(labelFont);
		visibleLabel.setLabelFor(visibleBox);

		// Set up the default field values
		nameField.setText(storyComponent.getDisplayText());

		labelField.setText(getCollectionAsString(storyComponent.getLabels()));
		labelField.setToolTipText(labelToolTip);

		visibleBox.setSelected(VisibilityManager.getInstance().isVisible(
				storyComponent));

		// Set up the field listeners
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				storyComponent.setDisplayText(nameField.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
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
				this.insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

		visibleBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisibilityManager.getInstance().setVisibility(storyComponent,
						visibleBox.isSelected());
			}
		});

		// Add JComponents to DescriptorPanel using GroupLayout
		descriptorPanelLayout.setHorizontalGroup(descriptorPanelLayout
				.createSequentialGroup()
				.addGroup(
						descriptorPanelLayout.createParallelGroup()
								.addComponent(nameLabel)
								.addComponent(visibleLabel)
								.addComponent(labelLabel))
				.addGroup(
						descriptorPanelLayout.createParallelGroup()
								.addComponent(visibleBox)
								.addComponent(nameField)
								.addComponent(labelField)));

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
								.addComponent(labelField)));

		editorPanel.add(descriptorPanel);

		// Display appropriate panel for component.
		storyComponent.process(new AbstractNoOpStoryVisitor() {

			@Override
			public void processScriptIt(final ScriptIt scriptIt) {
				final Collection<CodeBlock> codeBlocks = scriptIt
						.getCodeBlocks();

				if (scriptIt.isCause()) {
					for (CodeBlock codeBlock : codeBlocks) {

						editorPanel.add(PanelFactory.getInstance()
								.buildCodeBlockEditorPanel(codeBlock, scriptIt,
										CodeBlockType.CAUSE));
					}
				} else {
					// This makes sure the first code block is set as main.
					CodeBlockType type = CodeBlockType.EFFECT_MAIN;
					for (CodeBlock codeBlock : codeBlocks) {
						editorPanel.add(PanelFactory.getInstance()
								.buildCodeBlockEditorPanel(codeBlock, scriptIt,
										type));

						type = CodeBlockType.EFFECT;
					}
				}
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				// TODO We will need to be able to edit implicits eventually.
				// They are knowits.
				editorPanel.add(PanelFactory.getInstance()
						.buildDescriptionEditorPanel(knowIt));
			}

			@Override
			public void defaultProcess(StoryComponent component) {
				// By default, an empty editor panel with text is created.
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

		/*
		 * Sets the scroll bar back to the top of the screen.
		 * 
		 * See http://bit.ly/3OXFet for more info.
		 */
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				editorScrollPane.getVerticalScrollBar().setValue(0);
			}
		});

		return editorScrollPane;
	}

	private static enum CodeBlockType {
		CAUSE, EFFECT_MAIN, EFFECT
	}

	/**
	 * A JPanel used to edit CodeBlocks. This shows the id, slot, includes,
	 * types, parameters, and code for the passed in CodeBlock, and allows the
	 * user to edit it.
	 * 
	 * @param scriptIt
	 * 
	 * @param codeBlock
	 * @return
	 */
	private JPanel buildCodeBlockEditorPanel(final CodeBlock codeBlock,
			final ScriptIt ownerScriptIt, final CodeBlockType codeBlockType) {

		// TODO If cause, then set up a combo box for subject/slot that
		// disallows emptiness.
		// TODO If effect, for the first one, disable subject/slot fields.
		// TODO If second effect, set up combo box as in cause, but with an
		// empty option.

		final JPanel codeBlockEditorPanel;

		final JLabel idLabel;
		final JLabel subjectLabel;
		final JLabel slotLabel;
		final JLabel implicitsLabel;
		final JLabel includesLabel;
		final JLabel typesLabel;
		final JLabel parametersLabel;
		final JLabel codeLabel;

		final JButton addParameterButton;
		final JComboBox subjectBox;
		final JComboBox slotBox;
		final JLabel availableImplicitsLabel;
		final JTextField includesField;
		final TypeSelectionAction typeAction;
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
		subjectLabel = new JLabel("Subject: ");
		slotLabel = new JLabel("Slot: ");
		implicitsLabel = new JLabel("Implicits: ");
		includesLabel = new JLabel("Includes: ");
		typesLabel = new JLabel("Types: ");
		parametersLabel = new JLabel("Parameters: ");
		codeLabel = new JLabel("Code");

		subjectBox = new JComboBox();
		slotBox = new JComboBox();
		availableImplicitsLabel = new JLabel();
		includesField = new JTextField();
		typeAction = new TypeSelectionAction();
		typesButton = new JButton(typeAction);
		addParameterButton = new JButton("+");
		parameterPanel = new JPanel();
		parameterScrollPane = new JScrollPane(parameterPanel);
		codePanel = buildCodeInputComponent(codeBlock);

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

		subjectLabel.setFont(labelFont);
		subjectLabel.setLabelFor(subjectLabel);

		slotLabel.setFont(labelFont);
		slotLabel.setLabelFor(slotBox);

		implicitsLabel.setFont(labelFont);
		implicitsLabel.setLabelFor(availableImplicitsLabel);

		includesLabel.setFont(labelFont);
		includesLabel.setLabelFor(includesField);

		typesLabel.setFont(labelFont);
		typesLabel.setLabelFor(typesButton);

		parametersLabel.setFont(labelFont);
		parametersLabel.setLabelFor(parameterScrollPane);

		codeLabel.setFont(labelFont);
		codeLabel.setLabelFor(codePanel);

		// Set up the default field values
		availableImplicitsLabel.setForeground(Color.DARK_GRAY);

		if (!(codeBlockType == CodeBlockType.EFFECT_MAIN)) {
			if (codeBlock.hasSubject()) {
				KnowIt subject = codeBlock.getSubject();
				if (subject != null) {
					final String subjectName = codeBlock.getSubjectName();

					final Translator active = TranslatorManager.getInstance()
							.getActiveTranslator();

					if (codeBlockType == CodeBlockType.EFFECT) {
						subjectBox.addItem("");
					}
					for (KnowIt parameter : ownerScriptIt.getParameters()) {
						subjectBox.addItem(parameter.getDisplayText());
					}
					subjectBox.setSelectedItem(subjectName);

					for (String slot : active.getGameTypeManager().getSlots(
							subject.getDefaultType()))
						slotBox.addItem(slot);

					String implicits = "";
					for (KnowIt implicit : codeBlock.getImplicits())
						implicits += "[" + implicit.getDisplayText() + "] ";

					availableImplicitsLabel.setText(implicits.trim());
				}
			}
		} else {
			subjectBox.setEnabled(false);
			slotBox.setEnabled(false);
			availableImplicitsLabel.setEnabled(false);
		}

		if (codeBlock.hasSlot()) {
			slotBox.setSelectedItem(codeBlock.getSlot());
		}

		includesField.setText(getCollectionAsString(codeBlock.getIncludes()));

		ArrayList<String> types = new ArrayList<String>();
		types.addAll(codeBlock.getTypes());

		typeAction.getTypeSelectionDialogBuilder().deselectAll();
		typeAction.getTypeSelectionDialogBuilder().selectTypes(types, true);

		for (KnowIt parameter : parameters) {
			parameterPanel.add(new ParameterComponent(codeBlock, parameter));
		}

		// Set up the listeners
		subjectBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Subject Box Action Called");

				final String subjectName;

				subjectName = (String) subjectBox.getSelectedItem();
				System.out.println(subjectName);

				slotBox.removeAllItems();
				availableImplicitsLabel.setText("");

				KnowIt subject = codeBlock.getSubject();
				if (subject != null) {
					final Translator active = TranslatorManager.getInstance()
							.getActiveTranslator();

					for (String slot : active.getGameTypeManager().getSlots(
							subject.getDefaultType()))
						slotBox.addItem(slot);
				}
				codeBlock.setSubject(subjectName);
			}
		});
		
		slotBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				codeBlock.setSlot((String)slotBox.getSelectedItem());

				System.out.println("Slot Box Action Called");

				String implicits = "";
				for (KnowIt implicit : codeBlock.getImplicits())
					implicits += "[" + implicit.getDisplayText() + "] ";
				
				availableImplicitsLabel.setText(implicits.trim());
			}
		});
		/*
		 * slotField.getDocument().addDocumentListener(new DocumentListener() {
		 * 
		 * @Override public void insertUpdate(DocumentEvent e) {
		 * codeBlock.setSlot(slotField.getText());
		 * 
		 * String implicits = ""; for (KnowIt implicit :
		 * codeBlock.getImplicits()) implicits += "[" +
		 * implicit.getDisplayText() + "] ";
		 * 
		 * availableImplicitsLabel.setText(implicits.trim());
		 * availableImplicitsLabel.repaint(); }
		 * 
		 * @Override public void removeUpdate(DocumentEvent e) {
		 * insertUpdate(e); }
		 * 
		 * @Override public void changedUpdate(DocumentEvent e) { } });
		 */

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
			}
		});

		// TODO Set up the listener to add a new parameter

		// TODO Set the listeners for code panel
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
								.addComponent(subjectLabel)
								.addComponent(slotLabel)
								.addComponent(implicitsLabel)
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
								.addComponent(subjectBox)
								.addComponent(slotBox)
								.addComponent(availableImplicitsLabel)
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
								.addComponent(implicitsLabel)
								.addComponent(availableImplicitsLabel))
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

		// Should have the type selection here!

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
	public JPanel buildStoryComponentLibraryPanel(JComponent editorPanel) {
		final List<Translator> translators;
		final JComboBox libSelector;
		final JPanel libraryPanel;
		final JPanel translatorPanel;
		final LibraryPane libraryPane;
		final Translator activeTranslator;
		final TreeSelectionListener librarySelectionListener;
		final StoryVisitor storyVisitor;

		libraryPanel = new JPanel();
		translatorPanel = new JPanel();
		translators = new ArrayList<Translator>();
		// TODO We need to also load invisible story components.
		libraryPane = new LibraryPane();
		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		storyVisitor = editorPanelUpdater(editorPanel);

		libraryPanel.setLayout(new BoxLayout(libraryPanel, BoxLayout.Y_AXIS));

		translators.add(null);
		translators.addAll(TranslatorManager.getInstance().getTranslators());

		libSelector = new JComboBox(new Vector<Translator>(translators));
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
	public StoryVisitor editorPanelUpdater(final JComponent editorPanel) {
		StoryVisitor storyVisitor = new AbstractNoOpStoryVisitor() {
			@Override
			public void defaultProcess(StoryComponent component) {
				editorPanel.removeAll();

				editorPanel.add(buildStoryComponentEditorComponent(component));

				editorPanel.repaint();
				editorPanel.revalidate();
			}
		};
		return storyVisitor;
	}

	/**
	 * Method that returns a collection of Strings as a single String separated
	 * by commas.<br>
	 * <br>
	 * An example: <br>
	 * If<br>
	 * <code>Collection&lt;String&gt; collection = ["First"], ["Second"], ["Third"];<br></code>
	 * then<br>
	 * <code>getCollectionAsString(collection) == "First, Second, Third"</code>
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
	 * ParameterComponents are JComponents used to represent and edit
	 * parameters. <br>
	 * <br>
	 * Parameters have:
	 * <ul>
	 * <li>name</li>
	 * <li>types</li>
	 * <li>default type</li>
	 * <li>default binding constant</li>
	 * </ul>
	 * A ParameterComponent also has a delete button to remove the parameter
	 * from the CodeBlock.
	 * 
	 * @author kschenk
	 * 
	 */
	@SuppressWarnings("serial")
	private class ParameterComponent extends JComponent {
		private final KnowIt parameter;

		/**
		 * Creates a new ParameterComponent with the passed in KnowIt parameter.
		 * 
		 * @param parameter
		 */
		private ParameterComponent(final CodeBlock codeBlock,
				final KnowIt parameter) {
			super();
			this.parameter = parameter;

			final JTextField nameField;
			final TypeSelectionAction typeAction;
			final ArrayList<String> types;
			final JButton typesButton;
			final JComboBox defaultTypeBox;
			final JButton deleteButton;
			final GroupLayout groupLayout;
			final JComponent bindingConstantComponent;

			nameField = new JTextField(parameter.getDisplayText(), 10);
			typeAction = new TypeSelectionAction();
			types = new ArrayList<String>();
			typesButton = new JButton(typeAction);
			defaultTypeBox = new JComboBox();
			// TODO Need a trash icon for deleteButton
			deleteButton = new JButton("-");
			groupLayout = new GroupLayout(this);
			bindingConstantComponent = new JPanel();

			this.setLayout(groupLayout);
			this.setBorder(new TitledBorder(parameter.getDisplayText()));

			this.setMaximumSize(new Dimension(1920, 150));

			// Set default values
			types.addAll(parameter.getTypes());

			typeAction.getTypeSelectionDialogBuilder().deselectAll();
			typeAction.getTypeSelectionDialogBuilder().selectTypes(types, true);

			for (String type : types)
				defaultTypeBox.addItem(type);

			defaultTypeBox.setSelectedItem(parameter.getDefaultType());

			updateBindingConstantComponent(bindingConstantComponent);

			// Set up listeners
			nameField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					parameter.setDisplayText(nameField.getText());
				}
			});

			typeAction.setAction(new Runnable() {
				// TODO This isn't updating the LibraryPane, but it should.
				@Override
				public void run() {
					Collection<String> newTypes = typeAction
							.getTypeSelectionDialogBuilder().getSelectedTypes();
					/*
					 * Note: This event also causes the check box's action to
					 * fire. This is why we save the currently selected type
					 * before adding new items to the list; so we can set it
					 * again when the items have been added.
					 * 
					 * Thankfully, JCheckBox checks if it's in the list before
					 * setting it, so we don't need to worry about that.
					 * 
					 * -kschenk
					 */
					String defaultType = parameter.getDefaultType();

					parameter.setTypes(newTypes);
					defaultTypeBox.removeAllItems();

					for (String type : newTypes)
						defaultTypeBox.addItem(type);

					defaultTypeBox.setSelectedItem(defaultType);
				}
			});

			defaultTypeBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final List<String> types;
					final Collection<String> newTypeList;
					final String selectedType;

					types = new ArrayList<String>();
					newTypeList = new ArrayList<String>();
					selectedType = (String) defaultTypeBox.getSelectedItem();

					types.addAll(parameter.getTypes());

					if (selectedType != null)
						newTypeList.add(selectedType);

					for (String type : types) {
						if (!type.equals(selectedType))
							newTypeList.add(type);
					}

					parameter.setTypes(newTypeList);

					updateBindingConstantComponent(bindingConstantComponent);
				}
			});

			deleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final String[] options = { "Yes", "No" };

					int removeConfirmation = WindowManager
							.getInstance()
							.showOptionsDialog(
									"Are you sure you want to remove this parameter?",
									"Remove Parameter", options);

					if (removeConfirmation == 0) {
						List<KnowIt> parameters = codeBlock.getParameters();
						parameters.remove(parameter);

						codeBlock.setParameters(parameters);
						removeComponentFromComponent(ParameterComponent.this,
								ParameterComponent.this.getParent());
					}
				}
			});

			JPanel defaultTypeBoxPanel = new JPanel();
			JPanel nameFieldPanel = new JPanel();
			JPanel bindingPanel = new JPanel();

			defaultTypeBoxPanel.add(defaultTypeBox);
			nameFieldPanel.add(nameField);
			bindingPanel.add(bindingConstantComponent);

			nameFieldPanel.setBorder(new TitledBorder("Name"));
			defaultTypeBoxPanel.setBorder(new TitledBorder("Default Type"));
			bindingPanel.setBorder(new TitledBorder("Default Binding"));

			groupLayout.setAutoCreateGaps(true);
			groupLayout.setAutoCreateContainerGaps(true);
			groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
					.addComponent(nameFieldPanel).addComponent(typesButton)
					.addComponent(defaultTypeBoxPanel)
					.addComponent(bindingPanel).addComponent(deleteButton));

			groupLayout.setVerticalGroup(groupLayout
					.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(nameFieldPanel).addComponent(typesButton)
					.addComponent(deleteButton)
					.addComponent(defaultTypeBoxPanel)
					.addComponent(bindingPanel));
		}

		/**
		 * Updates the binding constant component, which is the component that
		 * is used to set default binding settings.
		 * 
		 * @param bindingConstantComponent
		 */
		private void updateBindingConstantComponent(
				JComponent bindingConstantComponent) {
			final JTextField inactiveTextField;
			final Translator translator;
			final TypeValueWidgets defaultTypeGuiType;
			final GameTypeManager gameTypeManager;

			translator = TranslatorManager.getInstance().getActiveTranslator();
			gameTypeManager = translator.getGameTypeManager();
			defaultTypeGuiType = gameTypeManager.getGui(parameter
					.getDefaultType());

			inactiveTextField = new JTextField(" Cannot set binding for ["
					+ parameter.getDefaultType() + "]");

			inactiveTextField.setEnabled(false);

			bindingConstantComponent.removeAll();

			if (defaultTypeGuiType == null)
				bindingConstantComponent.add(inactiveTextField);
			else {
				switch (defaultTypeGuiType) {
				// TODO Add listeners to these spinners and combo
				// boxes so the default binding is set!
				case JTEXTFIELD:
					final JTextField bindingField;
					final String bindingText;

					bindingField = new JTextField(30);
					bindingText = parameter.getBinding().getScriptValue();

					if (bindingText.equals("<unbound!>"))
						bindingField.setText("");
					else
						bindingField.setText(bindingText);

					bindingField.getDocument().addDocumentListener(
							new DocumentListener() {
								@Override
								public void insertUpdate(DocumentEvent e) {
									final String bindingFieldText;
									bindingFieldText = bindingField.getText();

									if (bindingFieldText.length() > 0) {
										GameConstant newConstant = GameConstantFactory
												.getInstance().getConstant(
														parameter.getTypes(),
														bindingField.getText());
										parameter.setBinding(newConstant);
									} else
										parameter
												.setBinding(new KnowItBindingNull());
								}

								@Override
								public void removeUpdate(DocumentEvent e) {
									insertUpdate(e);
								}

								@Override
								public void changedUpdate(DocumentEvent e) {
								}
							});
					bindingConstantComponent.add(bindingField);
					break;
				case JSPINNER:
					bindingConstantComponent.add(new JSpinner());
					break;
				case JCOMBOBOX:
					final Map<String, String> map;
					final JComboBox selectorBox;

					map = gameTypeManager
							.getEnumMap(parameter.getDefaultType());
					selectorBox = new JComboBox();

					selectorBox.addItem(null);

					for (String key : map.keySet())
						selectorBox.addItem(key + " [" + map.get(key) + "]");

					bindingConstantComponent.add(selectorBox);
					break;
				default: {
					inactiveTextField.setText("Unimplemented GUI Type: "
							+ defaultTypeGuiType.toString());
					bindingConstantComponent.add(inactiveTextField);
					break;
				}
				}
			}
			bindingConstantComponent.repaint();
			bindingConstantComponent.revalidate();
		}
	}

	/*
	 * TODO This may need to be an inner class, like parameter component.
	 */
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
	 * This method allows us to easily remove a component from another
	 * component, and then repaint and revalidate the removed from component.
	 * 
	 * @param component1
	 * @param component2
	 */
	private void removeComponentFromComponent(JComponent component,
			Container container) {
		container.remove(component);

		container.repaint();
		// TODO Enable when ant is in Java 1.7
		// container.revalidate();
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
