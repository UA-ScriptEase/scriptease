package scriptease.gui.libraryeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

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
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.ScriptEase;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.action.libraryeditor.codeeditor.DeleteFragmentAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertIndentAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertLineAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertLiteralAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertReferenceAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertScopeAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertSeriesAction;
import scriptease.gui.action.libraryeditor.codeeditor.InsertSimpleAction;
import scriptease.gui.action.libraryeditor.codeeditor.MoveFragmentDownAction;
import scriptease.gui.action.libraryeditor.codeeditor.MoveFragmentUpAction;
import scriptease.gui.action.typemenus.TypeSelectionAction;
import scriptease.gui.pane.LibraryPanel;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.ScopeTypes;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.SeriesFilterType;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.SeriesTypes;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;
import scriptease.translator.codegenerator.code.fragments.LiteralFragment;
import scriptease.translator.codegenerator.code.fragments.SimpleDataFragment;
import scriptease.translator.codegenerator.code.fragments.container.IndentFragment;
import scriptease.translator.codegenerator.code.fragments.container.LineFragment;
import scriptease.translator.codegenerator.code.fragments.container.ScopeFragment;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameType.TypeValueWidgets;
import scriptease.translator.io.tools.GameConstantFactory;
import scriptease.util.GUIOp;
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
		final JPanel descriptorPanel;
		final JPanel componentEditingPanel;

		final JLabel nameLabel;
		final JLabel labelLabel;
		final JLabel visibleLabel;

		final JTextField nameField;
		final JTextField labelField;
		final JCheckBox visibleBox;
		final JButton addCodeBlockButton;

		final Font labelFont;

		final GroupLayout descriptorPanelLayout;

		final String labelToolTip;

		editorPanel = new JPanel();
		descriptorPanel = new JPanel();
		componentEditingPanel = new JPanel();

		nameLabel = new JLabel("Name: ");
		labelLabel = new JLabel("Labels: ");
		visibleLabel = new JLabel("Visible: ");

		nameField = new JTextField();
		labelField = new JTextField();
		visibleBox = new JCheckBox();
		addCodeBlockButton = new JButton("Add CodeBlock");

		labelFont = new Font("SansSerif", Font.BOLD,
				Integer.parseInt(ScriptEase.getInstance().getPreference(
						ScriptEase.FONT_SIZE_KEY)) + 1);
		descriptorPanelLayout = new GroupLayout(descriptorPanel);

		labelToolTip = "<html><b>Labels</b> are seperated by commas.<br>"
				+ "Leading and trailing spaces are<br>"
				+ "removed automatically.</html>";

		editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.PAGE_AXIS));
		componentEditingPanel.setLayout(new BoxLayout(componentEditingPanel,
				BoxLayout.PAGE_AXIS));

		// Set up the descriptorPanel
		descriptorPanel.setLayout(descriptorPanelLayout);
		descriptorPanel.setBorder(new TitledBorder("Component Descriptors"));

		descriptorPanelLayout.setAutoCreateGaps(true);
		descriptorPanelLayout.setAutoCreateContainerGaps(true);
		descriptorPanelLayout.setHonorsVisibility(true);

		// Set up the labels
		nameLabel.setFont(labelFont);
		nameLabel.setLabelFor(nameField);

		labelLabel.setFont(labelFont);
		labelLabel.setLabelFor(labelField);
		labelLabel.setToolTipText(labelToolTip);

		visibleLabel.setFont(labelFont);
		visibleLabel.setLabelFor(visibleBox);

		addCodeBlockButton.setVisible(false);

		// Add JComponents to DescriptorPanel using GroupLayout
		descriptorPanelLayout
				.setHorizontalGroup(descriptorPanelLayout
						.createParallelGroup()
						.addGroup(
								descriptorPanelLayout
										.createSequentialGroup()
										.addGroup(
												descriptorPanelLayout
														.createParallelGroup()
														.addComponent(nameLabel)
														.addComponent(
																visibleLabel)
														.addComponent(
																labelLabel))
										.addGroup(
												descriptorPanelLayout
														.createParallelGroup()
														.addComponent(
																visibleBox)
														.addComponent(nameField)
														.addComponent(
																labelField)
														.addComponent(
																addCodeBlockButton,
																GroupLayout.Alignment.TRAILING))));

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
				.addComponent(addCodeBlockButton));

		editorPanel.add(descriptorPanel);
		editorPanel.add(componentEditingPanel);

		editorPanel.setVisible(false);

		/*
		 * Create an AbstractNoOpStoryVisitor which calls an update on the
		 * editorPanel. This is used as a sort of Command Pattern with
		 * UIListenerFactory.
		 */
		storyVisitor = new AbstractNoOpStoryVisitor() {
			private DocumentListener nameFieldListener;
			private DocumentListener labelFieldListener;
			private ActionListener visibleBoxListener;
			private ActionListener addCodeBlockListener;

			private Runnable setUpScriptItCodeBlocks(final ScriptIt scriptIt) {
				return new Runnable() {
					@Override
					public void run() {
						final Collection<CodeBlock> codeBlocks;

						codeBlocks = scriptIt.getCodeBlocks();

						componentEditingPanel.removeAll();
						FormatFragmentSelectionManager.getInstance()
								.setFormatFragment(null, null);

						for (CodeBlock codeBlock : codeBlocks) {
							componentEditingPanel.add(buildCodeBlockComponent(
									codeBlock, scriptIt));
						}

						editorPanel.repaint();
						editorPanel.revalidate();
					}
				};
			}

			@Override
			public void processScriptIt(final ScriptIt scriptIt) {
				LibraryEditorListenerFactory.getInstance()
						.refreshCodeBlockComponentObserverList();
				// Causes and effects are processed as ScriptIts
				addCodeBlockButton.setVisible(true);
				addCodeBlockButton
						.removeActionListener(this.addCodeBlockListener);
				this.addCodeBlockListener = addCodeBlockButtonListener(scriptIt);
				addCodeBlockButton.addActionListener(this.addCodeBlockListener);

				setUpScriptItCodeBlocks(scriptIt).run();
				updateComponents(scriptIt);

				scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
						.getInstance().buildScriptItEditorObserver(
								setUpScriptItCodeBlocks(scriptIt)));

				editorPanel.repaint();
				editorPanel.revalidate();
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				LibraryEditorListenerFactory.getInstance()
						.refreshCodeBlockComponentObserverList();

				// Descriptions are processed as KnowIts.
				componentEditingPanel.removeAll();
				FormatFragmentSelectionManager.getInstance().setFormatFragment(
						null, null);

				addCodeBlockButton.setVisible(false);

				componentEditingPanel.add(LibraryEditorPanelFactory
						.getInstance().buildDescriptionEditorPanel(knowIt));

				updateComponents(knowIt);

				editorPanel.repaint();
				editorPanel.revalidate();
			}

			// We may want to implement these later, so their default methods
			// are here in case.
			@Override
			public void processAskIt(AskIt questionIt) {
				this.defaultProcess(questionIt);
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer container) {
				this.defaultProcess(container);
			}

			@Override
			public void defaultProcess(StoryComponent component) {
				LibraryEditorListenerFactory.getInstance()
						.refreshCodeBlockComponentObserverList();
				updateComponents(component);

				addCodeBlockButton.setVisible(false);

				componentEditingPanel.removeAll();

				editorPanel.repaint();
				editorPanel.revalidate();
			}

			/**
			 * Updates the JComponents based on the selected StoryComponent.
			 * 
			 * @param component
			 */
			private void updateComponents(final StoryComponent component) {
				// Set up the default field values
				nameField.getDocument().removeDocumentListener(
						this.nameFieldListener);
				labelField.getDocument().removeDocumentListener(
						this.labelFieldListener);
				visibleBox.removeActionListener(this.visibleBoxListener);

				nameField.setText(component.getDisplayText());
				labelField.setText(StringOp.getCollectionAsString(
						component.getLabels(), ", "));
				labelField.setToolTipText(labelToolTip);
				visibleBox.setSelected(component.isVisible());

				this.nameFieldListener = nameFieldListener(nameField, component);
				this.labelFieldListener = labelFieldListener(labelField,
						component);
				this.visibleBoxListener = visibleBoxListener(visibleBox,
						component);

				nameField.getDocument().addDocumentListener(
						this.nameFieldListener);
				labelField.getDocument().addDocumentListener(
						this.labelFieldListener);
				visibleBox.addActionListener(this.visibleBoxListener);

				editorPanel.setVisible(true);
			}

			/**
			 * Listener for the name field for editing a story component.
			 * 
			 * @param component
			 * @return
			 */
			private DocumentListener nameFieldListener(
					final JTextField nameField, final StoryComponent component) {
				return new DocumentListener() {
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
				};
			}

			/**
			 * Listener for the label field for editing a story component.
			 * 
			 * @param labelField
			 * @param component
			 * @return
			 */
			private DocumentListener labelFieldListener(
					final JTextField labelField, final StoryComponent component) {
				return new DocumentListener() {
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
				};
			}

			/**
			 * Listener for the visible box for editing a story component.
			 * 
			 * @param visibleBox
			 * @param component
			 * @return
			 */
			private ActionListener visibleBoxListener(
					final JCheckBox visibleBox, final StoryComponent component) {
				return new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						component.setVisible(visibleBox.isSelected());
					}
				};
			}

			/**
			 * Listener for a button that adds code blocks to script its.
			 * 
			 * @param addCodeBlockButton
			 * @param scriptIt
			 * @return
			 */
			private ActionListener addCodeBlockButtonListener(
					final ScriptIt scriptIt) {
				return new ActionListener() {
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
				};
			}
		};

		librarySelectionListener = LibraryEditorListenerFactory.getInstance()
				.buildStoryComponentMouseListener(storyVisitor);

		// Add the tree listener
		libraryPane.addListMouseListener(librarySelectionListener);

		return editorPanel;
	}

	/*
	 * TODO Will need to implement DescribeItGraphPanel here or just combine
	 * them
	 */
	private JPanel buildDescriptionEditorPanel(KnowIt knowIt) {
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
		// String labelList = "";
		for (String label : knowIt.getLabels())
			label += label + ", ";
		// Set the Label list to labelList
		// Set the types to knowIt.getTypes()

		return descriptionEditorPanel;
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

		final TypeSelectionAction typeAction;
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

		typeAction = new TypeSelectionAction();
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

	/**
	 * ParameterPanelss are JPanels used to represent and edit parameters. <br>
	 * <br>
	 * Parameters have:
	 * <ul>
	 * <li>name</li>
	 * <li>types</li>
	 * <li>default type</li>
	 * <li>default binding constant</li>
	 * </ul>
	 * A ParameterPanel also has a delete button to remove the parameter from
	 * the CodeBlock.
	 * 
	 * @author kschenk
	 * 
	 */
	@SuppressWarnings("serial")
	private class ParameterPanel extends JPanel {
		private final KnowIt knowIt;

		/**
		 * Creates a new ParameterComponent with the passed in KnowIt parameter.
		 * 
		 * @param knowIt
		 */
		private ParameterPanel(final ScriptIt scriptIt,
				final CodeBlock codeBlock, final KnowIt knowIt) {
			super();
			this.knowIt = knowIt;

			final JTextField nameField;
			final TypeSelectionAction typeAction;
			final ArrayList<String> types;
			final JButton typesButton;
			final JComboBox defaultTypeBox;
			final JButton deleteButton;
			final GroupLayout groupLayout;
			final JComponent bindingConstantComponent;

			final JPanel typesPanel;
			final JPanel defaultTypeBoxPanel;
			final JPanel nameFieldPanel;
			final JPanel bindingPanel;

			final Translator activeTranslator;
			final GameTypeManager gameTypeManager;

			nameField = new JTextField(knowIt.getDisplayText(), 10);
			typeAction = new TypeSelectionAction();
			types = new ArrayList<String>();
			typesButton = new JButton(typeAction);
			defaultTypeBox = new JComboBox();
			// TODO Need a trash icon for deleteButton
			deleteButton = new JButton("-");
			groupLayout = new GroupLayout(this);
			bindingConstantComponent = new JPanel();

			typesPanel = new JPanel();
			defaultTypeBoxPanel = new JPanel();
			nameFieldPanel = new JPanel();
			bindingPanel = new JPanel();

			activeTranslator = TranslatorManager.getInstance()
					.getActiveTranslator();
			gameTypeManager = activeTranslator.getGameTypeManager();
			// Set up layouts and such
			this.setLayout(groupLayout);
			this.setBorder(BorderFactory.createEtchedBorder());
			this.setBackground(GUIOp.scaleColour(Color.GRAY, 1.9));

			typesPanel.setOpaque(false);
			defaultTypeBoxPanel.setOpaque(false);
			nameFieldPanel.setOpaque(false);
			bindingPanel.setOpaque(false);
			bindingConstantComponent.setOpaque(false);

			// Set up sizes
			this.setMaximumSize(new Dimension(1920, 100));

			// Set default values
			types.addAll(knowIt.getTypes());

			typeAction.getTypeSelectionDialogBuilder().deselectAll();
			typeAction.getTypeSelectionDialogBuilder().selectTypes(types, true);

			for (String type : types)
				defaultTypeBox.addItem(gameTypeManager.getDisplayText(type)
						+ " - " + type);

			defaultTypeBox.setSelectedItem(gameTypeManager
					.getDisplayText(knowIt.getDefaultType()));

			updateBindingConstantComponent(bindingConstantComponent);

			// Set up listeners
			knowIt.addStoryComponentObserver(LibraryEditorListenerFactory
					.getInstance().buildParameterTypeObserver(knowIt,
							defaultTypeBox));

			knowIt.addStoryComponentObserver(LibraryEditorListenerFactory
					.getInstance().buildParameterDefaultTypeObserver());

			nameField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					final String newInput;
					newInput = nameField.getText();

					if (codeBlock.hasSubject()) {
						KnowIt subject = codeBlock.getSubject();
						if (subject.equals(knowIt)) {
							knowIt.setDisplayText(newInput);
							codeBlock.setSubject(newInput);
						} else
							knowIt.setDisplayText(newInput);
					} else
						knowIt.setDisplayText(newInput);

					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CHANGE_PARAMETER_NAME_SET));
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
					knowIt.setTypes(typeAction.getTypeSelectionDialogBuilder()
							.getSelectedTypes());

					knowIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CHANGE_PARAMETER_TYPES_SET));
				}
			});

			defaultTypeBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final List<String> types;
					final Collection<String> newTypeList;
					final String selectedType;
					final String selectedItem;

					types = new ArrayList<String>();
					newTypeList = new ArrayList<String>();

					selectedItem = (String) defaultTypeBox.getSelectedItem();

					if (selectedItem != null) {

						selectedType = selectedItem.split(" - ")[1];

						types.addAll(knowIt.getTypes());

						if (selectedType != null)
							newTypeList.add(selectedType);

						for (String type : types) {
							if (!type.equals(selectedType))
								newTypeList.add(type);
						}

						knowIt.setTypes(newTypeList);

						updateBindingConstantComponent(bindingConstantComponent);

						scriptIt.notifyObservers(new StoryComponentEvent(
								scriptIt,
								StoryComponentChangeEnum.CHANGE_PARAMETER_DEFAULT_TYPE_SET));
					}
				}
			});

			deleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final List<KnowIt> parameters;
					parameters = codeBlock.getParameters();

					parameters.remove(knowIt);
					codeBlock.setParameters(parameters);

					scriptIt.notifyObservers(new StoryComponentEvent(
							scriptIt,
							StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE));
				}
			});

			typesPanel.add(typesButton);
			defaultTypeBoxPanel.add(defaultTypeBox);
			nameFieldPanel.add(nameField);
			bindingPanel.add(bindingConstantComponent);

			typesPanel.setBorder(new TitledBorder("Types"));
			nameFieldPanel.setBorder(new TitledBorder("Name"));
			defaultTypeBoxPanel.setBorder(new TitledBorder("Default Type"));
			bindingPanel.setBorder(new TitledBorder("Default Binding"));

			groupLayout.setAutoCreateGaps(true);
			groupLayout.setAutoCreateContainerGaps(true);
			groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
					.addComponent(nameFieldPanel).addComponent(typesPanel)
					.addComponent(defaultTypeBoxPanel)
					.addComponent(bindingPanel).addComponent(deleteButton));

			groupLayout.setVerticalGroup(groupLayout
					.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(nameFieldPanel).addComponent(typesPanel)
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
			defaultTypeGuiType = gameTypeManager.getGui(this.knowIt
					.getDefaultType());

			inactiveTextField = new JTextField(" Cannot set binding for ["
					+ this.knowIt.getDefaultType() + "]");

			inactiveTextField.setEnabled(false);

			bindingConstantComponent.removeAll();

			if (defaultTypeGuiType == null)
				bindingConstantComponent.add(inactiveTextField);
			else {
				final String bindingText;
				bindingText = this.knowIt.getBinding().getScriptValue();

				switch (defaultTypeGuiType) {
				case JTEXTFIELD:
					final JTextField bindingField;

					bindingField = new JTextField(30);

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

									GameConstant newConstant = GameConstantFactory
											.getInstance().getConstant(
													ParameterPanel.this.knowIt
															.getTypes(),
													bindingFieldText);
									ParameterPanel.this.knowIt
											.setBinding(newConstant);
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
					final JSpinner bindingSpinner;
					boolean isFloat = false;

					final SpinnerNumberModel model;
					final NumberEditor numberEditor;
					float initVal;

					Comparable<?> min = null; // default to no min limit
					Comparable<?> max = null; // default to no max limit
					Number stepSize = 1; // default to int step size
					String regex = TranslatorManager.getInstance()
							.getActiveTranslator().getGameTypeManager()
							.getReg(this.knowIt.getDefaultType());

					final Pattern regexPattern = Pattern.compile(regex);
					if (regex != null && !regex.isEmpty()) {
						// if regex doesn't specify negative numbers, make min 0
						if (!regex.startsWith("[-]"))
							min = 0;
						// if regex specifies \. it wants a floating point
						if (regex.contains("\\.")) {
							stepSize = 0.1;
							isFloat = true;
						}
					}

					if (bindingText.equals("<unbound!>"))
						initVal = 0;
					else {
						initVal = Float.parseFloat(bindingText);
					}

					model = new SpinnerNumberModel(initVal, min, max, stepSize);
					bindingSpinner = new JSpinner(model);
					numberEditor = (NumberEditor) bindingSpinner.getEditor();

					if (isFloat) {
						numberEditor.getFormat().setMinimumFractionDigits(1);
					}

					bindingSpinner.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							final Object bindingFieldValue;
							bindingFieldValue = bindingSpinner.getValue();

							String safeValue = StringOp.convertNumberToPattern(
									bindingFieldValue.toString(), regexPattern);

							GameConstant newConstant = GameConstantFactory
									.getInstance().getConstant(
											ParameterPanel.this.knowIt
													.getTypes(), safeValue);
							ParameterPanel.this.knowIt.setBinding(newConstant);
						}
					});
					bindingConstantComponent.add(bindingSpinner);

					break;
				case JCOMBOBOX:
					final Map<String, String> map;
					final JComboBox bindingBox;

					map = gameTypeManager.getEnumMap(this.knowIt
							.getDefaultType());
					bindingBox = new JComboBox();

					bindingBox.addItem(null);

					for (String key : map.keySet())
						bindingBox.addItem(map.get(key));

					if (bindingText.equals("<unbound!>"))
						bindingBox.setSelectedItem(null);
					else
						bindingBox.setSelectedItem(map.get(bindingText));

					bindingBox.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							final Object bindingBoxValue;

							String defaultBindingName = "";

							bindingBoxValue = bindingBox.getSelectedItem();

							if (bindingBoxValue == null)
								ParameterPanel.this.knowIt.clearBinding();
							else {
								for (Entry<String, String> entry : map
										.entrySet()) {
									if (entry.getValue()
											.equals(bindingBoxValue)) {
										defaultBindingName = entry.getKey();
										break;
									}
								}

								GameConstant newConstant = GameConstantFactory
										.getInstance().getConstant(
												ParameterPanel.this.knowIt
														.getTypes(),
												defaultBindingName);
								ParameterPanel.this.knowIt
										.setBinding(newConstant);
							}

						}
					});

					bindingConstantComponent.add(bindingBox);
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

	/**
	 * Panel used to edit code graphically.
	 * 
	 * @author kschenk
	 * 
	 */
	@SuppressWarnings("serial")
	private class CodeEditorPanel extends JPanel implements
			StoryComponentObserver {

		private final Map<JPanel, AbstractFragment> panelToFragmentMap;
		private final CodeBlock codeBlock;
		/**
		 * The top level JPanel.
		 */
		private final JPanel codeEditorPanel;
		private final JScrollPane codeEditorScrollPane;
		private String simplePanelName;

		private CodeEditorPanel(CodeBlock codeBlock) {
			super();
			this.codeBlock = codeBlock;
			this.panelToFragmentMap = new HashMap<JPanel, AbstractFragment>();
			this.codeBlock.addStoryComponentObserver(this);
			this.simplePanelName = "Simple Data";

			final String CODE_EDITOR_PANEL_NAME = "Code";

			final JToolBar toolbar;
			final JButton lineButton;
			final JButton indentButton;
			final JButton scopeButton;
			final JButton seriesButton;
			final JButton simpleButton;
			final JButton literalButton;
			final JButton referenceButton;
			final JButton deleteButton;
			final JButton moveUpButton;
			final JButton moveDownButton;
			final JButton listerineButton;

			final Border lineBorder;
			final Border titledBorder;

			toolbar = new JToolBar("Code Editor ToolBar");
			lineButton = new JButton(InsertLineAction.getInstance());
			indentButton = new JButton(InsertIndentAction.getInstance());
			scopeButton = new JButton(InsertScopeAction.getInstance());
			seriesButton = new JButton(InsertSeriesAction.getInstance());
			simpleButton = new JButton(InsertSimpleAction.getInstance());
			literalButton = new JButton(InsertLiteralAction.getInstance());
			referenceButton = new JButton(InsertReferenceAction.getInstance());
			deleteButton = new JButton(DeleteFragmentAction.getInstance());
			moveUpButton = new JButton(MoveFragmentUpAction.getInstance());
			moveDownButton = new JButton(MoveFragmentDownAction.getInstance());
			listerineButton = new JButton(" ");

			lineBorder = BorderFactory
					.createLineBorder(ScriptEaseUI.CODE_EDITOR_COLOR);
			titledBorder = BorderFactory.createTitledBorder(lineBorder,
					CODE_EDITOR_PANEL_NAME, TitledBorder.LEADING,
					TitledBorder.TOP, new Font("SansSerif", Font.PLAIN, 12),
					ScriptEaseUI.CODE_EDITOR_COLOR);

			this.codeEditorPanel = objectContainerPanel(CODE_EDITOR_PANEL_NAME);
			this.codeEditorScrollPane = new JScrollPane(this.codeEditorPanel);

			listerineButton.setOpaque(false);
			listerineButton.setContentAreaFilled(false);
			listerineButton.setBorderPainted(false);
			listerineButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					CodeEditorPanel.this.simplePanelName = "Listerine";
				}
			});

			toolbar.setFloatable(false);

			toolbar.add(lineButton);
			toolbar.add(indentButton);
			toolbar.add(scopeButton);
			toolbar.add(seriesButton);
			toolbar.add(simpleButton);
			toolbar.add(literalButton);
			toolbar.add(referenceButton);
			toolbar.add(deleteButton);
			toolbar.add(moveUpButton);
			toolbar.add(moveDownButton);
			toolbar.add(listerineButton);

			this.codeEditorScrollPane.getVerticalScrollBar().setUnitIncrement(
					16);
			this.codeEditorScrollPane.setPreferredSize(new Dimension(400, 400));

			this.setLayout(new BorderLayout());
			this.codeEditorPanel.setLayout(new BoxLayout(this.codeEditorPanel,
					BoxLayout.PAGE_AXIS));
			this.codeEditorPanel.setBorder(titledBorder);

			this.panelToFragmentMap.put(this.codeEditorPanel, null);
			this.add(toolbar, BorderLayout.PAGE_START);
			this.add(this.codeEditorScrollPane, BorderLayout.CENTER);

			this.fillCodeEditorPanel();

			FormatFragmentSelectionManager.getInstance().setFormatFragment(
					null, codeBlock);
		}

		/**
		 * Fills the code editor panel with FormatFragments present in the
		 * CodeBlock.
		 */
		private void fillCodeEditorPanel() {
			final Collection<AbstractFragment> codeFragments;
			final Rectangle visibleRectangle;

			codeFragments = this.codeBlock.getCode();
			visibleRectangle = this.codeEditorScrollPane.getVisibleRect();

			this.codeEditorPanel.removeAll();
			this.buildDefaultPanes(this.codeEditorPanel, codeFragments);
			this.codeEditorPanel.repaint();
			this.codeEditorPanel.revalidate();

			this.codeEditorScrollPane.scrollRectToVisible(visibleRectangle);
		}

		/**
		 * This creates a panel with the specified title, and using the passed
		 * in colour. It is used by the various Fragment Panels to create a
		 * common appearance between them.
		 * 
		 * @param title
		 * @param color
		 * @return
		 */
		private JPanel objectContainerPanel(final String title) {
			final JPanel objectContainerPanel;

			objectContainerPanel = new JPanel();
			objectContainerPanel.setName(title);

			objectContainerPanel.setOpaque(true);
			objectContainerPanel
					.setBackground(ScriptEaseUI.FRAGMENT_DEFAULT_COLOR);

			objectContainerPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					FormatFragmentSelectionManager.getInstance()
							.setFormatFragment(
									CodeEditorPanel.this.panelToFragmentMap
											.get(objectContainerPanel),
									CodeEditorPanel.this.codeBlock);
					fillCodeEditorPanel();
				}
			});
			return objectContainerPanel;
		}

		/**
		 * Creates a panel representing a LineFragment. This is a container
		 * fragment, meaning it can contain other fragments. LineFragments place
		 * whatever code is within them on its own line. Code does not
		 * automatically wrap, so using LineFragments can help with formatting.
		 * 
		 * @return
		 */
		private JPanel linePanel() {
			final String TITLE = "Line";
			final JPanel linePanel;
			final Border lineBorder;

			lineBorder = BorderFactory
					.createLineBorder(ScriptEaseUI.LINE_FRAGMENT_COLOR);

			linePanel = objectContainerPanel(TITLE);

			linePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
			linePanel.setBorder(lineBorder);

			return linePanel;
		}

		/**
		 * Creates a panel representing an IndentedFragment. This is a container
		 * fragment, meaning it can contain other fragments. IndentedFragments
		 * indent whatever code is within them using the indent string. Using
		 * IndentedFragments can help with formatting.<br>
		 * <br>
		 * The indent string is defined in the LanguageDictionary within the
		 * &lt;IndentString /&gt; tag.
		 * 
		 * @return
		 */
		private JPanel indentPanel(IndentFragment indentFragment) {
			final String TITLE = "Indent";

			final JPanel indentPanel;
			final JPanel subFragmentsPanel;
			final JLabel indentLabel;
			final Border lineBorder;

			indentPanel = objectContainerPanel(TITLE);
			subFragmentsPanel = new JPanel();
			indentLabel = new JLabel(String.valueOf('\u21e5'));
			lineBorder = BorderFactory
					.createLineBorder(ScriptEaseUI.INDENT_FRAGMENT_COLOR);

			indentLabel.setForeground(ScriptEaseUI.INDENT_FRAGMENT_COLOR);
			indentLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));

			indentPanel.setBorder(lineBorder);

			indentPanel.add(indentLabel);

			subFragmentsPanel.setLayout(new BoxLayout(subFragmentsPanel,
					BoxLayout.PAGE_AXIS));

			buildDefaultPanes(subFragmentsPanel,
					indentFragment.getSubFragments());

			indentPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
			indentPanel.add(subFragmentsPanel);

			return indentPanel;
		}

		/**
		 * Creates a panel representing a Scope Fragment.<br>
		 * <br>
		 * <b>How A User Will Insert a Parameter:</b> <br>
		 * <br>
		 * <ol>
		 * <li>Insert a ScopeFragment into the Code Editor.</li>
		 * <li>Set the Data box to "Argument".</li>
		 * <li>Set the NameRef field to the desired parameter name.</li>
		 * <li>Insert a SimpleFragment into the ScopeFragment</li>
		 * <li>Set the Data field to "Name"</li>
		 * <li>Set the LegalValues field to a relevant regular expression.<br>
		 * The RegEx commonly used in the Neverwinter Nights translator is
		 * <code>"^[a-zA-Z]+[0-9a-zA-Z_]*"</code>.
		 * </ol>
		 * The resulting code will look something like this:<br>
		 * <br>
		 * <code>
		 * &lt;Scope data="argument" ref="Plot"&gt;
		 * <br>
		 * &nbsp;&nbsp;&lt;Fragment data="name" legalValues="^[a-zA-Z]+[0-9a-zA-Z_]*"/&gt;
		 * <br>
		 * &lt;/Scope&gt;
		 * </code>
		 * 
		 * @param scopeFragment
		 *            The ScopeFragment to create a panel for. This can be a
		 *            completely new ScopeFragment.
		 * @return
		 */
		private JPanel scopePanel(final ScopeFragment scopeFragment) {
			final String TITLE = "Scope";

			final JPanel scopePanel;
			final JPanel scopeComponentPanel;
			final JComboBox directiveBox;
			final JTextField nameRefField;
			final JLabel directiveLabel;
			final JLabel nameRefLabel;
			final Border lineBorder;
			final Border titledBorder;

			lineBorder = BorderFactory
					.createLineBorder(ScriptEaseUI.SCOPE_FRAGMENT_COLOR);
			titledBorder = BorderFactory.createTitledBorder(lineBorder, TITLE,
					TitledBorder.LEADING, TitledBorder.TOP, new Font(
							"SansSerif", Font.BOLD, 12),
					ScriptEaseUI.SCOPE_FRAGMENT_COLOR);

			scopePanel = objectContainerPanel(TITLE);
			scopeComponentPanel = new JPanel();
			directiveBox = new JComboBox();
			nameRefField = new JTextField();
			directiveLabel = new JLabel("Data");
			nameRefLabel = new JLabel("NameRef");

			directiveLabel.setLabelFor(directiveBox);
			nameRefLabel.setLabelFor(nameRefField);

			for (ScopeTypes directiveType : ScopeTypes.values())
				directiveBox.addItem(directiveType.name());

			directiveBox.setSelectedItem(scopeFragment.getDirectiveText()
					.toUpperCase());

			directiveBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					scopeFragment.setDirectiveText((String) directiveBox
							.getSelectedItem());
				}
			});

			nameRefField.setText(scopeFragment.getNameRef());

			nameRefField.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void insertUpdate(DocumentEvent e) {
							scopeFragment.setNameRef(nameRefField.getText());

							scopePanel.revalidate();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							insertUpdate(e);
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
						}
					});

			scopePanel
					.setLayout(new BoxLayout(scopePanel, BoxLayout.PAGE_AXIS));
			scopePanel.setBorder(titledBorder);

			scopeComponentPanel.setOpaque(false);

			scopeComponentPanel.add(directiveLabel);
			scopeComponentPanel.add(directiveBox);
			scopeComponentPanel.add(nameRefLabel);
			scopeComponentPanel.add(nameRefField);

			scopePanel.add(scopeComponentPanel);

			return scopePanel;
		}

		/**
		 * Series panel for editing series fragments.
		 * 
		 * @param seriesFragment
		 * @return
		 */
		private JPanel seriesPanel(final SeriesFragment seriesFragment) {
			final String TITLE = "Series";

			final JPanel seriesPanel;
			final JPanel seriesComponentPanel;
			final JPanel filterComponentPanel;

			final JComboBox directiveBox;
			final JTextField separatorField;
			final JCheckBox uniqueCheckBox;
			final JTextField filterField;
			final JComboBox filterTypeBox;

			final JLabel directiveLabel;
			final JLabel separatorLabel;
			final JLabel uniqueLabel;
			final JLabel filterLabel;
			final JLabel filterTypeLabel;

			final Border lineBorder;
			final Border titledBorder;

			lineBorder = BorderFactory
					.createLineBorder(ScriptEaseUI.SERIES_FRAGMENT_COLOR);
			titledBorder = BorderFactory.createTitledBorder(lineBorder, TITLE,
					TitledBorder.LEADING, TitledBorder.TOP, new Font(
							"SansSerif", Font.BOLD, 12),
					ScriptEaseUI.SERIES_FRAGMENT_COLOR);

			seriesPanel = objectContainerPanel(TITLE);
			seriesComponentPanel = new JPanel();
			filterComponentPanel = new JPanel();

			directiveBox = new JComboBox();
			separatorField = new JTextField();
			uniqueCheckBox = new JCheckBox();
			filterField = new JTextField();
			filterTypeBox = new JComboBox();

			directiveLabel = new JLabel("Data");
			separatorLabel = new JLabel("Separator");
			uniqueLabel = new JLabel("Unique");
			filterLabel = new JLabel("Filter");
			filterTypeLabel = new JLabel("Filter Type");

			directiveLabel.setLabelFor(directiveBox);
			separatorLabel.setLabelFor(separatorField);
			uniqueLabel.setLabelFor(uniqueCheckBox);
			filterLabel.setLabelFor(filterField);
			filterTypeLabel.setLabelFor(filterTypeBox);

			for (SeriesTypes directiveType : SeriesTypes.values())
				directiveBox.addItem(directiveType.name());

			directiveBox.setSelectedItem(seriesFragment.getDirectiveText()
					.toUpperCase());

			directiveBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					seriesFragment.setDirectiveText((String) directiveBox
							.getSelectedItem());
				}
			});

			separatorField.setText(seriesFragment.getSeparator());

			separatorField.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void insertUpdate(DocumentEvent e) {
							seriesFragment.setSeparator(separatorField
									.getText());

							seriesPanel.revalidate();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							insertUpdate(e);
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
						}
					});

			uniqueCheckBox.setSelected(seriesFragment.isUnique());

			uniqueCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					seriesFragment.setUnique(uniqueCheckBox.isSelected());
				}
			});

			filterField.setText(seriesFragment.getFilter());

			filterField.getDocument().addDocumentListener(
					new DocumentListener() {
						@Override
						public void insertUpdate(DocumentEvent e) {
							seriesFragment.setFilter(filterField.getText());

							seriesPanel.revalidate();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							insertUpdate(e);
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
						}
					});

			for (SeriesFilterType filterType : SeriesFilterType.values()) {
				filterTypeBox.addItem(filterType);
			}

			filterTypeBox.setSelectedItem(seriesFragment.getFilterType());

			filterTypeBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					seriesFragment
							.setFilterType((SeriesFilterType) filterTypeBox
									.getSelectedItem());
				}
			});

			seriesPanel.setLayout(new BoxLayout(seriesPanel,
					BoxLayout.PAGE_AXIS));
			seriesPanel.setBorder(titledBorder);

			seriesComponentPanel.setOpaque(false);
			filterComponentPanel.setOpaque(false);

			seriesComponentPanel.add(directiveLabel);
			seriesComponentPanel.add(directiveBox);
			seriesComponentPanel.add(separatorLabel);
			seriesComponentPanel.add(separatorField);
			seriesComponentPanel.add(uniqueLabel);
			seriesComponentPanel.add(uniqueCheckBox);

			filterComponentPanel.add(filterLabel);
			filterComponentPanel.add(filterField);
			filterComponentPanel.add(filterTypeLabel);
			filterComponentPanel.add(filterTypeBox);

			seriesPanel.add(seriesComponentPanel);
			seriesPanel.add(filterComponentPanel);

			return seriesPanel;
		}

		/**
		 * Creates a panel representing a Simple Fragment.
		 * 
		 * @param simpleFragment
		 * @return
		 */
		private JPanel simplePanel(final SimpleDataFragment simpleFragment) {
			final String TITLE = "Simple Data";

			final JPanel simplePanel;
			final JComboBox directiveBox;
			final JTextField legalRangeField;
			final JLabel directiveLabel;
			final JLabel legalRangeLabel;
			final Border lineBorder;
			final Border titledBorder;

			simplePanel = objectContainerPanel(TITLE);

			lineBorder = BorderFactory
					.createLineBorder(ScriptEaseUI.SIMPLE_FRAGMENT_COLOR);
			titledBorder = BorderFactory.createTitledBorder(lineBorder,
					this.simplePanelName, TitledBorder.LEADING,
					TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12),
					ScriptEaseUI.SIMPLE_FRAGMENT_COLOR);

			directiveBox = new JComboBox();
			legalRangeField = new JTextField();
			directiveLabel = new JLabel("Data");
			legalRangeLabel = new JLabel("LegalRange");

			directiveLabel.setLabelFor(directiveBox);
			legalRangeLabel.setLabelFor(legalRangeField);

			for (CodeGenerationKeywordConstants.DataTypes directiveType : CodeGenerationKeywordConstants.DataTypes
					.values())
				directiveBox.addItem(directiveType.name());

			directiveBox.setSelectedItem(simpleFragment.getDirectiveText()
					.toUpperCase());

			directiveBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					simpleFragment.setDirectiveText((String) directiveBox
							.getSelectedItem());
				}
			});

			legalRangeField.setText(simpleFragment.getLegalRange().toString());

			legalRangeField.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void insertUpdate(DocumentEvent e) {
							simpleFragment.setLegalRange(legalRangeField
									.getText());

							simplePanel.revalidate();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							insertUpdate(e);
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
						}
					});

			simplePanel.setBorder(titledBorder);

			simplePanel.add(directiveLabel);
			simplePanel.add(directiveBox);
			simplePanel.add(legalRangeLabel);
			simplePanel.add(legalRangeField);

			return simplePanel;
		}

		/**
		 * Creates a panel representing a Literal Fragment.
		 * 
		 * @param literalFragment
		 * @return
		 */
		private JPanel literalPanel(final LiteralFragment literalFragment) {
			final String TITLE = "Literal";

			final JPanel literalPanel;
			final JTextField literalField;
			final Border lineBorder;
			final Border titledBorder;

			lineBorder = BorderFactory
					.createLineBorder(ScriptEaseUI.LITERAL_FRAGMENT_COLOR);
			titledBorder = BorderFactory.createTitledBorder(lineBorder, TITLE,
					TitledBorder.LEADING, TitledBorder.TOP, new Font(
							"SansSerif", Font.BOLD, 12),
					ScriptEaseUI.LITERAL_FRAGMENT_COLOR);

			literalPanel = objectContainerPanel(TITLE);
			literalField = new JTextField(literalFragment.getDirectiveText());

			literalField.setMinimumSize(new Dimension(15, literalField
					.getMinimumSize().height));

			literalField.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void insertUpdate(DocumentEvent e) {
							literalFragment.setDirectiveText(literalField
									.getText());

							literalPanel.revalidate();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							literalFragment.setDirectiveText(literalField
									.getText());
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
						}

					});

			literalPanel.setBorder(titledBorder);

			literalPanel.add(literalField);

			return literalPanel;
		}

		/**
		 * Creates a panel representing a Reference Fragment.
		 * 
		 * @param referenceFragment
		 * @return
		 */
		private JPanel referencePanel(
				final FormatReferenceFragment referenceFragment) {
			final String TITLE = "Format Reference";

			final JPanel referencePanel;
			final JTextField referenceField;
			final Border lineBorder;
			final Border titledBorder;

			lineBorder = BorderFactory
					.createLineBorder(ScriptEaseUI.REFERENCE_FRAGMENT_COLOR);
			titledBorder = BorderFactory.createTitledBorder(lineBorder, TITLE,
					TitledBorder.LEADING, TitledBorder.TOP, new Font(
							"SansSerif", Font.BOLD, 12),
					ScriptEaseUI.REFERENCE_FRAGMENT_COLOR);

			referencePanel = objectContainerPanel(TITLE);
			referenceField = new JTextField(
					referenceFragment.getDirectiveText());

			referenceField.setMinimumSize(new Dimension(15, referenceField
					.getMinimumSize().height));

			referenceField.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void insertUpdate(DocumentEvent e) {
							referenceFragment.setDirectiveText(referenceField
									.getText());

							referencePanel.revalidate();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							referenceFragment.setDirectiveText(referenceField
									.getText());
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
						}

					});

			referencePanel.setBorder(titledBorder);

			referencePanel.add(referenceField);

			return referencePanel;
		}

		/**
		 * Recursively builds the default panel according to the passed code
		 * fragments.
		 * 
		 * @param panel
		 * @param codeFragments
		 */
		private void buildDefaultPanes(JPanel panel,
				Collection<AbstractFragment> codeFragments) {

			for (AbstractFragment codeFragment : codeFragments) {
				JPanel fragmentPanel = new JPanel();

				final AbstractFragment selectedFragment;

				selectedFragment = FormatFragmentSelectionManager.getInstance()
						.getFormatFragment();

				this.codeEditorPanel
						.setBackground(ScriptEaseUI.FRAGMENT_DEFAULT_COLOR);

				if (codeFragment instanceof LineFragment) {
					final JLabel lineLabel;

					lineLabel = new JLabel("\\n");
					fragmentPanel = linePanel();

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.LINE_FRAGMENT_COLOR, 1.2));

					lineLabel.setForeground(ScriptEaseUI.LINE_FRAGMENT_COLOR);
					lineLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));

					buildDefaultPanes(fragmentPanel,
							((LineFragment) codeFragment).getSubFragments());

					fragmentPanel.add(lineLabel);

					panel.add(fragmentPanel);
					this.panelToFragmentMap.put(fragmentPanel, codeFragment);
				} else if (codeFragment instanceof IndentFragment) {
					fragmentPanel = indentPanel((IndentFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.INDENT_FRAGMENT_COLOR, 1.2));

					panel.add(fragmentPanel);
					this.panelToFragmentMap.put(fragmentPanel, codeFragment);
				} else if (codeFragment instanceof LiteralFragment) {
					fragmentPanel = literalPanel((LiteralFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.LITERAL_FRAGMENT_COLOR, 1.7));

					panel.add(fragmentPanel);
					this.panelToFragmentMap.put(fragmentPanel, codeFragment);
				} else if (codeFragment instanceof ScopeFragment) {
					fragmentPanel = scopePanel((ScopeFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.SCOPE_FRAGMENT_COLOR, 5.0));

					buildDefaultPanes(fragmentPanel,
							((ScopeFragment) codeFragment).getSubFragments());
					panel.add(fragmentPanel);
					this.panelToFragmentMap.put(fragmentPanel, codeFragment);
				} else if (codeFragment instanceof SeriesFragment) {
					fragmentPanel = seriesPanel((SeriesFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.SERIES_FRAGMENT_COLOR, 3.0));

					buildDefaultPanes(fragmentPanel,
							((SeriesFragment) codeFragment).getSubFragments());
					panel.add(fragmentPanel);
					this.panelToFragmentMap.put(fragmentPanel, codeFragment);
				} else if (codeFragment instanceof FormatReferenceFragment) {
					fragmentPanel = referencePanel((FormatReferenceFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.REFERENCE_FRAGMENT_COLOR, 3.0));

					panel.add(fragmentPanel);
					this.panelToFragmentMap.put(fragmentPanel, codeFragment);
				} else if (codeFragment instanceof SimpleDataFragment) {
					fragmentPanel = simplePanel((SimpleDataFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.SIMPLE_FRAGMENT_COLOR, 3.5));

					panel.add(fragmentPanel);
					this.panelToFragmentMap.put(fragmentPanel, codeFragment);
				}

				if (selectedFragment == null)
					this.codeEditorPanel.setBackground(GUIOp.scaleWhite(
							ScriptEaseUI.CODE_EDITOR_COLOR, 1.7));
			}
		}

		// TODO Might want to edit this a bit more.. add more things here
		// instead of up there. This gets fired when a fragment is added.
		@Override
		public void componentChanged(StoryComponentEvent event) {
			fillCodeEditorPanel();
		}
	}
}
