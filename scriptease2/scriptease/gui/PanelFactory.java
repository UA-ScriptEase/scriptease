package scriptease.gui;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionListener;

import scriptease.ScriptEase;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.StoryVisitor;
import scriptease.controller.VisibilityManager;
import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.action.storycomponentbuilder.codeeditor.DeleteFragmentAction;
import scriptease.gui.action.storycomponentbuilder.codeeditor.InsertIndentAction;
import scriptease.gui.action.storycomponentbuilder.codeeditor.InsertLineAction;
import scriptease.gui.action.storycomponentbuilder.codeeditor.InsertLiteralAction;
import scriptease.gui.action.storycomponentbuilder.codeeditor.InsertReferenceAction;
import scriptease.gui.action.storycomponentbuilder.codeeditor.InsertScopeAction;
import scriptease.gui.action.storycomponentbuilder.codeeditor.InsertSeriesAction;
import scriptease.gui.action.storycomponentbuilder.codeeditor.InsertSimpleAction;
import scriptease.gui.action.storycomponentbuilder.codeeditor.MoveFragmentDownAction;
import scriptease.gui.action.storycomponentbuilder.codeeditor.MoveFragmentUpAction;
import scriptease.gui.action.typemenus.TypeSelectionAction;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.managers.FormatFragmentSelectionManager;
import scriptease.gui.pane.GameObjectPane;
import scriptease.gui.pane.LibraryPane;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.ScopeTypes;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.SeriesFilterType;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants.SeriesTypes;
import scriptease.translator.codegenerator.GameObjectPicker;
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
	 * Builds the library pane for the Story Component Editor. This pane allows
	 * loading of different translators through a ComboBox.
	 * 
	 * @param editorPanel
	 * @return
	 */
	public JPanel buildStoryComponentLibraryPanel(LibraryPane libraryPane) {
		final List<Translator> translators;
		final JComboBox libSelector;
		final JPanel libraryPanel;
		final JPanel translatorPanel;
		final Translator activeTranslator;

		libraryPanel = new JPanel();
		translatorPanel = new JPanel();
		translators = new ArrayList<Translator>();
		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

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

		libraryPanel.add(libraryPane);

		return libraryPanel;
	}

	/**
	 * Creates a JPanel with fields for Name, Labels, and a check box for
	 * Visibility. This JPanel is common to all story component editor panes.
	 * 
	 * @return
	 */
	public JComponent buildStoryComponentEditorComponent(
			final LibraryPane libraryPane) {
		final StoryVisitor storyVisitor;
		final TreeSelectionListener librarySelectionListener;

		final JPanel editorPanel;
		final JScrollPane editorScrollPane;
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
		editorScrollPane = new JScrollPane(editorPanel);
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

		editorScrollPane.getVerticalScrollBar().setUnitIncrement(16);

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

			private void setUpScriptItEditingPanel(final ScriptIt scriptIt) {
				final Collection<CodeBlock> codeBlocks;

				codeBlocks = scriptIt.getCodeBlocks();

				componentEditingPanel.removeAll();
				FormatFragmentSelectionManager.getInstance().setFormatFragment(
						null, null);

				for (CodeBlock codeBlock : codeBlocks) {
					componentEditingPanel.add(new CodeBlockComponent(codeBlock,
							scriptIt));
				}

				editorPanel.repaint();
				editorPanel.revalidate();
			}

			@Override
			public void processScriptIt(final ScriptIt scriptIt) {
				// Causes and effects are processed as ScriptIts
				addCodeBlockButton.setVisible(true);
				addCodeBlockButton.removeActionListener(addCodeBlockListener);
				addCodeBlockListener = addCodeBlockButtonListener(scriptIt,
						componentEditingPanel);
				addCodeBlockButton.addActionListener(addCodeBlockListener);
			
				setUpScriptItEditingPanel(scriptIt);
				updateComponents(scriptIt);

				// TODO Add more stuff here?
				scriptIt.addStoryComponentObserver(new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						System.out.println("Stuff happened!");
						setUpScriptItEditingPanel(scriptIt);
					}
				});

				editorPanel.repaint();
				editorPanel.revalidate();

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
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				// Descriptions are processed as KnowIts.
				componentEditingPanel.removeAll();
				FormatFragmentSelectionManager.getInstance().setFormatFragment(
						null, null);

				addCodeBlockButton.setVisible(false);

				componentEditingPanel.add(PanelFactory.getInstance()
						.buildDescriptionEditorPanel(knowIt));

				updateComponents(knowIt);

				editorPanel.repaint();
				editorPanel.revalidate();
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

			/**
			 * Updates the JComponents based on the selected StoryComponent.
			 * 
			 * @param component
			 */
			private void updateComponents(final StoryComponent component) {
				// Set up the default field values
				nameField.getDocument().removeDocumentListener(
						nameFieldListener);
				labelField.getDocument().removeDocumentListener(
						labelFieldListener);
				visibleBox.removeActionListener(visibleBoxListener);

				nameField.setText(component.getDisplayText());
				labelField.setText(StringOp.getCollectionAsString(
						component.getLabels(), ", "));
				labelField.setToolTipText(labelToolTip);
				visibleBox.setSelected(VisibilityManager.getInstance()
						.isVisible(component));

				nameFieldListener = nameFieldListener(nameField, component);
				labelFieldListener = labelFieldListener(labelField, component);
				visibleBoxListener = visibleBoxListener(visibleBox, component);

				nameField.getDocument().addDocumentListener(nameFieldListener);
				labelField.getDocument()
						.addDocumentListener(labelFieldListener);
				visibleBox.addActionListener(visibleBoxListener);

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
						VisibilityManager.getInstance().setVisibility(
								component, visibleBox.isSelected());
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
					final ScriptIt scriptIt, final JPanel componentEditingPanel) {
				return new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final CodeBlock codeBlock;

						codeBlock = new CodeBlockSource("", "",
								new ArrayList<String>(),
								new ArrayList<KnowIt>(),
								new ArrayList<String>(),
								new ArrayList<AbstractFragment>(),
								TranslatorManager.getInstance()
										.getActiveTranslator()
										.getApiDictionary()
										.getNextCodeBlockID());

						scriptIt.addCodeBlock(codeBlock);
						componentEditingPanel.add(new CodeBlockComponent(
								codeBlock, scriptIt));
						componentEditingPanel.repaint();
						componentEditingPanel.revalidate();
					}
				};
			}

			@Override
			public void defaultProcess(StoryComponent component) {
				editorPanel.setVisible(false);
			}
		};

		librarySelectionListener = UIListenerFactory.getInstance()
				.buildStoryComponentLibraryListener(storyVisitor);

		// Add the tree listener
		libraryPane.addTreeSelectionListener(librarySelectionListener);

		return editorScrollPane;
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
	 * A factory for builing code block components. Code block components are
	 * all very similar, but differ depending on if they are Cause or Effects.
	 * Within Effects, Code Blocks differ depending on if they are the first
	 * Code Block or the last one.
	 * 
	 * @author kschenk
	 * 
	 */
	@SuppressWarnings("serial")
	private class CodeBlockComponent extends JComponent implements
			StoryComponentObserver {
		private final CodeBlock codeBlock;
		private final ScriptIt scriptIt;

		private final JButton deleteCodeBlockButton;
		private final JComboBox subjectBox;
		private final JComboBox slotBox;
		private final JLabel availableImplicitsLabel;

		/**
		 * Sets up the JPanel used to edit CodeBlocks. This shows the id, slot,
		 * includes, types, parameters, and code for the passed in CodeBlock,
		 * and allows the user to edit it.
		 * 
		 * @param scriptIt
		 * 
		 * @param codeBlock
		 * @return
		 */
		private CodeBlockComponent(final CodeBlock codeBlock,
				final ScriptIt scriptIt) {
			this.codeBlock = codeBlock;
			this.scriptIt = scriptIt;

			this.deleteCodeBlockButton = new JButton("Delete CodeBlock");
			this.subjectBox = new JComboBox();
			this.slotBox = new JComboBox();
			this.availableImplicitsLabel = new JLabel();

			this.scriptIt.addStoryComponentObserver(this);

			this.setupMainComponent();
		}

		/**
		 * Sets up the main component of the Code Block Component. Call this to
		 * refresh it.
		 */
		private void setupMainComponent() {
			final JLabel idLabel;
			final JLabel subjectLabel;
			final JLabel slotLabel;
			final JLabel implicitsLabel;
			final JLabel includesLabel;
			final JLabel typesLabel;
			final JLabel parametersLabel;
			final JLabel codeLabel;

			final JButton addParameterButton;
			final JTextField includesField;
			final TypeSelectionAction typeAction;
			final JButton typesButton;
			final JScrollPane parameterScrollPane;
			final JPanel parameterPanel;
			final CodeEditorPanel codePanel;
			final GroupLayout codeBlockEditorLayout;
			final Font labelFont;
			final List<KnowIt> parameters;

			idLabel = new JLabel("ID# " + codeBlock.getId());
			subjectLabel = new JLabel("Subject: ");
			slotLabel = new JLabel("Slot: ");
			implicitsLabel = new JLabel("Implicits: ");
			includesLabel = new JLabel("Includes: ");
			typesLabel = new JLabel("Types: ");
			parametersLabel = new JLabel("Parameters: ");
			codeLabel = new JLabel("Code: ");

			includesField = new JTextField();
			typeAction = new TypeSelectionAction();
			typesButton = new JButton(typeAction);
			addParameterButton = new JButton("+");
			parameterPanel = new JPanel();
			parameterScrollPane = new JScrollPane(parameterPanel);
			codePanel = new CodeEditorPanel(codeBlock);
			codeBlockEditorLayout = new GroupLayout(this);
			labelFont = new Font("SansSerif", Font.BOLD,
					Integer.parseInt(ScriptEase.getInstance().getPreference(
							ScriptEase.FONT_SIZE_KEY)) + 1);
			parameters = codeBlock.getParameters();

			// Set up the codeBlockEditorPanel and the scroll pane
			this.removeAll();

			// TODO Make it so this doesn't reset to top when redrawn.
			// And remove some things from here. Not everything needs to be
			// recreated twice.
			this.setLayout(codeBlockEditorLayout);
			this.setBorder(new TitledBorder("Code Block: "));

			codeBlockEditorLayout.setAutoCreateGaps(true);
			codeBlockEditorLayout.setAutoCreateContainerGaps(true);
			codeBlockEditorLayout.setHonorsVisibility(true);

			parameterPanel.setLayout(new BoxLayout(parameterPanel,
					BoxLayout.PAGE_AXIS));

			parameterScrollPane.setPreferredSize(new Dimension(400, 250));
			parameterScrollPane.getVerticalScrollBar().setUnitIncrement(16);

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
			this.setupComponents(true);

			availableImplicitsLabel.setForeground(Color.DARK_GRAY);

			includesField.setText(StringOp.getCollectionAsString(
					codeBlock.getIncludes(), ", "));

			ArrayList<String> types = new ArrayList<String>();
			types.addAll(codeBlock.getTypes());

			typeAction.getTypeSelectionDialogBuilder().deselectAll();
			typeAction.getTypeSelectionDialogBuilder().selectTypes(types, true);

			deleteCodeBlockButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						scriptIt.removeCodeBlock(codeBlock);
					}
				});
				
			if (this.scriptIt.getCodeBlocks().size() < 2) {
				deleteCodeBlockButton.setVisible(false);
			}

			for (KnowIt parameter : parameters) {
				parameterPanel.add(new ParameterComponent(scriptIt, codeBlock,
						parameter, this));
			}

			includesField.getDocument().addDocumentListener(
					new DocumentListener() {
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
					codeBlock
							.setTypes(typeAction
									.getTypeSelectionDialogBuilder()
									.getSelectedTypes());

					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CODE_BLOCK_TYPES_SET));
				}
			});

			addParameterButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					parameters.add(new KnowIt());
					codeBlock.setParameters(parameters);

					CodeBlockComponent.this.setupMainComponent();
					CodeBlockComponent.this.repaint();
					CodeBlockComponent.this.revalidate();

					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD));
				}
			});

			codeBlockEditorLayout
					.setHorizontalGroup(codeBlockEditorLayout
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
											.addGroup(
													GroupLayout.Alignment.TRAILING,
													codeBlockEditorLayout
															.createSequentialGroup()
															.addComponent(
																	deleteCodeBlockButton)
															.addComponent(
																	idLabel))
											.addComponent(subjectBox)
											.addComponent(slotBox)
											.addComponent(
													availableImplicitsLabel)
											.addComponent(includesField)
											.addComponent(typesButton)
											.addComponent(parameterScrollPane)
											.addComponent(codePanel)));

			codeBlockEditorLayout
					.setVerticalGroup(codeBlockEditorLayout
							.createSequentialGroup()
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(deleteCodeBlockButton)
											.addComponent(idLabel))
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
											.addComponent(implicitsLabel)
											.addComponent(
													availableImplicitsLabel))
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

		/**
		 * Sets up the subject and slot boxes and implicits label.
		 * 
		 * @param initializeSlotBoxListener
		 *            Initializes the listeners for the slot box.
		 */
		private void setupComponents(boolean initializeSlotBoxListener) {

			final String initialSlot;
			final Translator active = TranslatorManager.getInstance()
					.getActiveTranslator();

			if (this.codeBlock.hasSlot())
				initialSlot = this.codeBlock.getSlot();
			else
				initialSlot = "";

			// Listeners are removed here, because if we don't, removeAllItems
			// fires them off.
			if (!initializeSlotBoxListener) {
				final ActionListener[] subjectBoxListeners = subjectBox
						.getListeners(ActionListener.class);
				if (subjectBoxListeners.length > 0) {
					for (ActionListener subjectBoxListener : subjectBoxListeners)
						subjectBox.removeActionListener(subjectBoxListener);
				}
				subjectBox.removeAllItems();
			}

			subjectBox.addItem(null);
			for (KnowIt parameter : this.scriptIt.getParameters()) {
				final Collection<String> slots = active.getGameTypeManager()
						.getSlots(parameter.getDefaultType());

				if (!slots.isEmpty())
					subjectBox.addItem(parameter.getDisplayText());
			}

			// Set up the listeners
			subjectBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					final String subjectName;

					subjectName = (String) subjectBox.getSelectedItem();

					slotBox.removeAllItems();
					availableImplicitsLabel.setText("");

					codeBlock.setSubject(subjectName);
					codeBlock.setSlot("");

					/*
					 * This looks weird because the subject we are setting is a
					 * string, and the get subject method in code blocks isn't
					 * just a getter, but an actual method that gets a KnowIt
					 */
					if (codeBlock.hasSubject()) {
						KnowIt subject = codeBlock.getSubject();
						if (subject != null) {
							final Collection<String> slots = active
									.getGameTypeManager().getSlots(
											subject.getDefaultType());

							for (String slot : slots) {
								slotBox.addItem(slot);
							}
							if (!slots.isEmpty())
								slotBox.setSelectedItem(slots.toArray()[0]);
						}
					}

					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET));
				}
			});
			if (initializeSlotBoxListener) {

				slotBox.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String selectedSlot = (String) slotBox
								.getSelectedItem();

						if (selectedSlot != null)
							codeBlock.setSlot((String) slotBox
									.getSelectedItem());
						else
							codeBlock.setSlot("");

						String implicits = "";
						for (KnowIt implicit : codeBlock.getImplicits())
							implicits += "[" + implicit.getDisplayText() + "] ";

						availableImplicitsLabel.setText(implicits.trim());

						scriptIt.notifyObservers(new StoryComponentEvent(
								scriptIt,
								StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET));
					}
				});
			}

			if (this.codeBlock.hasSubject()) {
				final KnowIt subject;
				subject = this.codeBlock.getSubject();

				if (subject != null) {
					final String subjectName;
					subjectName = this.codeBlock.getSubjectName();

					subjectBox.setSelectedItem(subjectName);
					slotBox.setSelectedItem(initialSlot);
				}
			} else if (!scriptIt.isCause()) {
				subjectBox.setSelectedItem(null);
			}
		}

		// TODO Do more stuff in here rather than elsewhere.
		@Override
		public void componentChanged(StoryComponentEvent event) {
			if (this.scriptIt.getCodeBlocks().size() > 1)
				this.deleteCodeBlockButton.setVisible(true);
			else
				this.deleteCodeBlockButton.setVisible(false);

			this.revalidate();
		}
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
	// TODO Implement StoryComponentObserver?
	@SuppressWarnings("serial")
	private class ParameterComponent extends JComponent {
		private final KnowIt parameter;

		/**
		 * Creates a new ParameterComponent with the passed in KnowIt parameter.
		 * 
		 * @param parameter
		 */
		private ParameterComponent(final ScriptIt scriptIt,
				final CodeBlock codeBlock, final KnowIt parameter,
				final CodeBlockComponent codeBlockComponent) {
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

			// Set up sizes
			// TODO Set up sizes for everything, so it doesn't look as horrible.
			this.setMaximumSize(new Dimension(1920, 100));

			// Set default values
			types.addAll(parameter.getTypes());

			typeAction.getTypeSelectionDialogBuilder().deselectAll();
			typeAction.getTypeSelectionDialogBuilder().selectTypes(types, true);

			for (String type : types)
				defaultTypeBox.addItem(type);

			defaultTypeBox.setSelectedItem(parameter.getDefaultType());

			updateBindingConstantComponent(bindingConstantComponent);

			// Set up listeners
			nameField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					final String newInput;
					newInput = nameField.getText();

					parameter.setDisplayText(newInput);
					codeBlock.setSubject(newInput);

					codeBlockComponent.setupComponents(false);
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

					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CHANGE_PARAMETER_TYPES_SET));
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

					codeBlockComponent.setupComponents(false);
					updateBindingConstantComponent(bindingConstantComponent);

					scriptIt.notifyObservers(new StoryComponentEvent(
							scriptIt,
							StoryComponentChangeEnum.CHANGE_PARAMETER_DEFAULT_TYPE_SET));
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

						codeBlockComponent.remove(ParameterComponent.this);

						codeBlockComponent.setupMainComponent();
						codeBlockComponent.repaint();
						codeBlockComponent.revalidate();

						scriptIt.notifyObservers(new StoryComponentEvent(
								scriptIt,
								StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE));
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
				final String bindingText;
				bindingText = parameter.getBinding().getScriptValue();

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
													parameter.getTypes(),
													bindingFieldText);
									parameter.setBinding(newConstant);
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
					final int bindingInt;

					bindingSpinner = new JSpinner();

					if (bindingText.equals("<unbound!>"))
						bindingSpinner.setValue(0);
					else {
						bindingInt = Integer.parseInt(bindingText);
						bindingSpinner.setValue(bindingInt);
					}
					bindingSpinner.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							final Object bindingFieldValue;
							bindingFieldValue = bindingSpinner.getValue();

							GameConstant newConstant = GameConstantFactory
									.getInstance().getConstant(
											parameter.getTypes(),
											bindingFieldValue.toString());
							parameter.setBinding(newConstant);
						}
					});
					bindingConstantComponent.add(bindingSpinner);
					break;
				case JCOMBOBOX:
					final Map<String, String> map;
					final JComboBox bindingBox;

					map = gameTypeManager
							.getEnumMap(parameter.getDefaultType());
					bindingBox = new JComboBox();

					bindingBox.addItem(null);

					for (String key : map.keySet())
						bindingBox.addItem(map.get(key));

					if (bindingText.equals("<unbound!>"))
						bindingBox.setSelectedItem(null);
					else
						bindingBox.setSelectedItem(bindingText);

					bindingBox.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							final Object bindingBoxValue;
							bindingBoxValue = bindingBox.getSelectedItem();

							GameConstant newConstant = GameConstantFactory
									.getInstance().getConstant(
											parameter.getTypes(),
											bindingBoxValue.toString());
							parameter.setBinding(newConstant);
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

		public CodeEditorPanel(CodeBlock codeBlock) {
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
			this.codeEditorScrollPane = new JScrollPane(codeEditorPanel);

			listerineButton.setOpaque(false);
			listerineButton.setContentAreaFilled(false);
			listerineButton.setBorderPainted(false);
			listerineButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					simplePanelName = "Listerine";
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
			this.codeEditorScrollPane.setPreferredSize(new Dimension(400, 300));

			this.setLayout(new BorderLayout());
			this.codeEditorPanel.setLayout(new BoxLayout(codeEditorPanel,
					BoxLayout.PAGE_AXIS));
			this.codeEditorPanel.setBorder(titledBorder);

			this.panelToFragmentMap.put(this.codeEditorPanel, null);
			this.add(toolbar, BorderLayout.PAGE_START);
			this.add(codeEditorScrollPane, BorderLayout.CENTER);

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
									panelToFragmentMap
											.get(objectContainerPanel),
									codeBlock);
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

				codeEditorPanel
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
					panelToFragmentMap.put(fragmentPanel,
							(LineFragment) codeFragment);
				} else if (codeFragment instanceof IndentFragment) {
					fragmentPanel = indentPanel((IndentFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.INDENT_FRAGMENT_COLOR, 1.2));

					panel.add(fragmentPanel);
					panelToFragmentMap.put(fragmentPanel,
							(IndentFragment) codeFragment);
				} else if (codeFragment instanceof LiteralFragment) {
					fragmentPanel = literalPanel((LiteralFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.LITERAL_FRAGMENT_COLOR, 1.7));

					panel.add(fragmentPanel);
					panelToFragmentMap.put(fragmentPanel,
							(LiteralFragment) codeFragment);
				} else if (codeFragment instanceof ScopeFragment) {
					fragmentPanel = scopePanel((ScopeFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.SCOPE_FRAGMENT_COLOR, 5.0));

					buildDefaultPanes(fragmentPanel,
							((ScopeFragment) codeFragment).getSubFragments());
					panel.add(fragmentPanel);
					panelToFragmentMap.put(fragmentPanel,
							(ScopeFragment) codeFragment);
				} else if (codeFragment instanceof SeriesFragment) {
					fragmentPanel = seriesPanel((SeriesFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.SERIES_FRAGMENT_COLOR, 3.0));

					buildDefaultPanes(fragmentPanel,
							((SeriesFragment) codeFragment).getSubFragments());
					panel.add(fragmentPanel);
					panelToFragmentMap.put(fragmentPanel,
							(SeriesFragment) codeFragment);
				} else if (codeFragment instanceof FormatReferenceFragment) {
					fragmentPanel = referencePanel((FormatReferenceFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.REFERENCE_FRAGMENT_COLOR, 3.0));

					panel.add(fragmentPanel);
					panelToFragmentMap.put(fragmentPanel,
							(FormatReferenceFragment) codeFragment);
				} else if (codeFragment instanceof SimpleDataFragment) {
					fragmentPanel = simplePanel((SimpleDataFragment) codeFragment);

					if (codeFragment == selectedFragment)
						fragmentPanel.setBackground(GUIOp.scaleWhite(
								ScriptEaseUI.SIMPLE_FRAGMENT_COLOR, 3.5));

					panel.add(fragmentPanel);
					panelToFragmentMap.put(fragmentPanel,
							(SimpleDataFragment) codeFragment);
				}

				if (selectedFragment == null)
					codeEditorPanel.setBackground(GUIOp.scaleWhite(
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
