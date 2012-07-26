package scriptease.gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
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

		// Set up the labels
		nameLabel.setFont(labelFont);
		nameLabel.setLabelFor(nameField);

		labelLabel.setFont(labelFont);
		labelLabel.setLabelFor(labelField);
		labelLabel.setToolTipText(labelToolTip);
		visibleLabel.setFont(labelFont);
		visibleLabel.setLabelFor(visibleBox);

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
												.addComponent(labelField))));

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

			@Override
			public void processScriptIt(final ScriptIt scriptIt) {
				// Causes and effects are processed as ScriptIts
				componentEditingPanel.removeAll();

				final Collection<CodeBlock> codeBlocks = scriptIt
						.getCodeBlocks();

				for (CodeBlock codeBlock : codeBlocks) {
					componentEditingPanel.add(new CodeBlockComponent(codeBlock,
							scriptIt));
				}

				updateComponents(scriptIt);

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
				labelField
						.setText(getCollectionAsString(component.getLabels()));
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

	/**
	 * Listener for the name field for editing a story component.
	 * 
	 * @param component
	 * @return
	 */
	private DocumentListener nameFieldListener(final JTextField nameField,
			final StoryComponent component) {
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
	private DocumentListener labelFieldListener(final JTextField labelField,
			final StoryComponent component) {
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
	private ActionListener visibleBoxListener(final JCheckBox visibleBox,
			final StoryComponent component) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisibilityManager.getInstance().setVisibility(component,
						visibleBox.isSelected());
			}
		};
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
	public JPanel buildStoryComponentLibraryPanel(LibraryPane libraryPane) {
		final List<Translator> translators;
		final JComboBox libSelector;
		final JPanel libraryPanel;
		final JPanel translatorPanel;
		final Translator activeTranslator;

		libraryPanel = new JPanel();
		translatorPanel = new JPanel();
		translators = new ArrayList<Translator>();
		// TODO We need to also load invisible story components.
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
	private static String getCollectionAsString(Collection<String> strings) {
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
	private class CodeBlockComponent extends JComponent {
		private final CodeBlock codeBlock;
		private final ScriptIt scriptIt;

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

			this.redrawComponents();
		}

		private void redrawComponents() {
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

			idLabel = new JLabel("ID# " + codeBlock.getId());
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

			codeBlockEditorLayout = new GroupLayout(this);
			labelFont = new Font("SansSerif", Font.BOLD,
					Integer.parseInt(ScriptEase.getInstance().getPreference(
							ScriptEase.FONT_SIZE_KEY)) + 1);
			parameters = codeBlock.getParameters();

			// Set up the codeBlockEditorPanel and the scroll pane
			this.removeAll();

			this.setLayout(codeBlockEditorLayout);
			this.setBorder(new TitledBorder("Code Block: "));

			codeBlockEditorLayout.setAutoCreateGaps(true);
			codeBlockEditorLayout.setAutoCreateContainerGaps(true);
			codeBlockEditorLayout.setHonorsVisibility(true);

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
			if (scriptIt.isCause())
				setCauseSubjectBoxes(subjectBox, slotBox,
						availableImplicitsLabel);
			else
				setEffectSubjectBoxes(subjectBox, slotBox,
						availableImplicitsLabel);

			availableImplicitsLabel.setForeground(Color.DARK_GRAY);

			includesField
					.setText(getCollectionAsString(codeBlock.getIncludes()));

			ArrayList<String> types = new ArrayList<String>();
			types.addAll(codeBlock.getTypes());

			typeAction.getTypeSelectionDialogBuilder().deselectAll();
			typeAction.getTypeSelectionDialogBuilder().selectTypes(types, true);

			for (KnowIt parameter : parameters) {
				parameterPanel.add(new ParameterComponent(scriptIt, codeBlock, parameter,
						this));
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

							scriptIt.notifyObservers(new StoryComponentEvent(
									scriptIt,
									StoryComponentChangeEnum.CODE_BLOCK_INCLUDES_SET));
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
					
					CodeBlockComponent.this.redrawComponents();
					
					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD));
				}
			});

			// TODO Set up the code panel
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

			codeBlockEditorLayout
					.setVerticalGroup(codeBlockEditorLayout
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
		 * Sets up the subject and slot boxes and implicits label for causes.
		 * 
		 * @param subjectBox
		 * @param slotBox
		 * @param implicitsLabel
		 */
		private void setCauseSubjectBoxes(final JComboBox subjectBox,
				final JComboBox slotBox, final JLabel implicitsLabel) {

			final String initialSlot;
			final Translator active = TranslatorManager.getInstance()
					.getActiveTranslator();

			if (this.codeBlock.hasSlot())
				initialSlot = this.codeBlock.getSlot();
			else
				initialSlot = "";

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
					implicitsLabel.setText("");

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

			slotBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String selectedSlot = (String) slotBox.getSelectedItem();

					if (selectedSlot != null)
						codeBlock.setSlot((String) slotBox.getSelectedItem());
					else
						codeBlock.setSlot("");

					String implicits = "";
					for (KnowIt implicit : codeBlock.getImplicits())
						implicits += "[" + implicit.getDisplayText() + "] ";

					implicitsLabel.setText(implicits.trim());

					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET));
				}
			});

			if (this.codeBlock.hasSubject()) {
				final KnowIt subject;
				subject = this.codeBlock.getSubject();

				if (subject != null) {
					final String subjectName;
					subjectName = this.codeBlock.getSubjectName();

					subjectBox.setSelectedItem(subjectName);
					slotBox.setSelectedItem(initialSlot);
				}
			}

		}

		private void setEffectSubjectBoxes(JComboBox subjectBox,
				JComboBox slotBox, JLabel implicitsLabel) {
			subjectBox.addItem("");
			setCauseSubjectBoxes(subjectBox, slotBox, implicitsLabel);

			if (!this.codeBlock.hasSubject()) {
				subjectBox.setSelectedItem("");
			}
		}

		/*
		 * TODO This be needin' an inner class, like parameter component.
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
			// TODO Extract method so it can be called by Listener. (?
			// Necessary?)
			/*
			 * TODO Need to implement some way of getting code from fragments,
			 * and also to check what kind of fragment it is.
			 */
			for (FormatFragment codeFragment : codeFragments) {
				if (codeFragment instanceof LineFragment) {
					for (FormatFragment b : ((LineFragment) codeFragment)
							.getSubFragments()) {
						if (b instanceof LiteralFragment) {
							try {
								codePaneDoc.insertString(
										codePaneDoc.getLength(),
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
								codePaneDoc.insertString(
										codePaneDoc.getLength(),
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
		private ParameterComponent(final ScriptIt scriptIt, final CodeBlock codeBlock,
				final KnowIt parameter,
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

					codeBlockComponent.redrawComponents();
					updateBindingConstantComponent(bindingConstantComponent);
					
					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
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
						codeBlockComponent.redrawComponents();
						

						scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
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
				// TODO Add listeners to these spinners and combo
				// boxes so the default binding is set!
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

							/*
							 * TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
							 * Good morning! TODO TODO Issue: ComboBox default
							 * not set :( TODO TODO TODO TODO TODO TODO TODO
							 * TODO TODO TODO
							 */

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
}
