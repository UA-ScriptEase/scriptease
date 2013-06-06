package scriptease.gui.libraryeditor;

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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import scriptease.ScriptEase;
import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.StoryComponentPanelJListObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.pane.LibraryPanel;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.util.StringOp;

@SuppressWarnings("serial")
public class LibraryEditorPanel extends JPanel implements
		StoryComponentPanelJListObserver {
	private final StoryVisitor panelBuilder;

	public LibraryEditorPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		LibraryPanel.getInstance().addStoryComponentPanelJListObserver(this);

		/*
		 * Create an AbstractNoOpStoryVisitor which calls an update on the
		 * editorPanel. This is used as a sort of Command Pattern with
		 * UIListenerFactory.
		 */
		panelBuilder = new StoryAdapter() {

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

				if (!(scriptIt instanceof CauseIt)) {
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
					LibraryEditorPanel.this.add(scriptItControlPanel);
				}

				LibraryEditorPanel.this.add(codeBlockEditingPanel);

				this.setUpCodeBlockPanels(scriptIt, codeBlockEditingPanel)
						.run();

				scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
						.getInstance().buildScriptItEditorObserver(
								setUpCodeBlockPanels(scriptIt,
										codeBlockEditingPanel)));

				LibraryEditorPanel.this.revalidate();
			}

			/**
			 * @param knowIt
			 */
			@Override
			public void processKnowIt(final KnowIt knowIt) {
				LibraryEditorPanel.this.removeAll();
				LibraryEditorPanel.this.revalidate();
				LibraryEditorPanel.this.repaint();

				final JPanel knowItPanel;
				final JPanel describeItEditingPanel;

				final GroupLayout knowItPanelLayout;
				final TypeAction typeAction;
				final Runnable commitText;

				final JButton typesButton;
				final JTextField nameField;

				final JLabel nameLabel;
				final JLabel typesLabel;

				final DescribeIt describeIt;

				knowItPanel = new JPanel();

				knowItPanelLayout = new GroupLayout(knowItPanel);
				typeAction = new TypeAction();
				typesButton = new JButton(typeAction);

				describeIt = knowIt.getLibrary().getDescribeIt(knowIt);

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

				typesLabel.setFont(LibraryEditorPanelFactory.labelFont);
				nameLabel.setFont(LibraryEditorPanelFactory.labelFont);

				typeAction.getTypeSelectionDialogBuilder().deselectAll();
				typeAction.getTypeSelectionDialogBuilder().selectTypes(
						knowIt.getTypes(), true);

				WidgetDecorator.decorateJTextFieldForFocusEvents(nameField,
						commitText, false, Color.white);

				nameField.setHorizontalAlignment(JTextField.LEADING);

				knowItPanel.setBorder(BorderFactory
						.createTitledBorder("DescribeIt"));

				typeAction.setAction(new Runnable() {
					@Override
					public void run() {
						final Collection<String> types = typeAction
								.getTypeSelectionDialogBuilder()
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

				LibraryEditorPanel.this.add(knowItPanel);
				LibraryEditorPanel.this.add(describeItEditingPanel);
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

				LibraryEditorPanel.this.removeAll();

				LibraryEditorPanel.this.add(LibraryEditorPanelFactory
						.getInstance().buildDescriptorPanel(component));

				LibraryEditorPanel.this.revalidate();
				LibraryEditorPanel.this.repaint();
			}
		};
	}

	private void clearPanel() {
		this.removeAll();
		this.revalidate();
		this.repaint();
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

			if (scriptIt instanceof CauseIt) {
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
					knowIt.setLibrary(codeBlock.getLibrary());
					if (!UndoManager.getInstance().hasOpenUndoableAction()) {
						UndoManager.getInstance().startUndoableAction(
								"Add parameter " + knowIt + " to " + codeBlock);
						codeBlock.addParameter(knowIt);
						UndoManager.getInstance().endUndoableAction();
					}
				}
			});

			slotBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final String selectedSlot = (String) slotBox
							.getSelectedItem();

					if (selectedSlot != null)
						codeBlock.setSlot((String) slotBox.getSelectedItem());
					else
						codeBlock.setSlot("");

					if (scriptIt instanceof CauseIt) {
						CauseIt causeIt = (CauseIt) scriptIt;
						causeIt.updateStoryChildren();
					}
					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET));
				}
			});

			if (scriptIt instanceof CauseIt) {
				deleteCodeBlockButton.setVisible(false);
				subjectLabel.setVisible(false);
				subjectBox.setVisible(false);

				slotLabel.setVisible(false);
				slotBox.setVisible(false);
				implicitsLabel.setVisible(false);
				implicitsLabelLabel.setVisible(false);
			} else {
				deleteCodeBlockButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().startUndoableAction(
									"Removing CodeBlock to "
											+ scriptIt.getDisplayText());
						scriptIt.removeCodeBlock(codeBlock);
						UndoManager.getInstance().endUndoableAction();
					}
				});

				if (!scriptIt.getMainCodeBlock().equals(codeBlock)) {
					subjectBox.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							final String subjectName;
							subjectName = (String) subjectBox.getSelectedItem();

							codeBlock.setSubject(subjectName);

							scriptIt.notifyObservers(new StoryComponentEvent(
									scriptIt,
									StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET));
						}
					});
				} else {
					subjectLabel.setVisible(false);
					subjectBox.setVisible(false);

					slotLabel.setVisible(false);
					slotBox.setVisible(false);
					implicitsLabel.setVisible(false);
					implicitsLabelLabel.setVisible(false);
				}

				if (scriptIt.getCodeBlocks().size() < 2) {
					deleteCodeBlockButton.setEnabled(false);
				}
			}

			for (KnowIt parameter : parameters) {
				parameterPanel.add(LibraryEditorPanelFactory.getInstance()
						.buildParameterPanel(scriptIt, codeBlock, parameter));
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

	/**
	 * Returns a list of slots that are common in all of the types in the knowit
	 * passed in.
	 * 
	 * @param subject
	 * @return
	 */
	private Collection<String> getCommonSlotsForTypes(KnowIt subject) {
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		final Collection<String> slots;

		slots = model.getTypeSlots(subject.getDefaultType());

		for (String type : subject.getTypes()) {
			final Collection<String> otherSlots;

			otherSlots = new ArrayList<String>();

			for (String slot : model.getTypeSlots(type)) {
				if (slots.contains(slot))
					otherSlots.add(slot);
			}

			slots.removeAll(slots);
			slots.addAll(otherSlots);
		}
		return slots;
	}

	@Override
	public void componentSelected(StoryComponent component) {
		if (component == null) {
			this.clearPanel();
		} else {
			component.process(this.panelBuilder);
		}
	}
}
