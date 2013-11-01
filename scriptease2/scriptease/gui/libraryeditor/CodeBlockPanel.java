package scriptease.gui.libraryeditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.dialog.TypeDialogBuilder;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.util.ListOp;
import scriptease.util.StringOp;

/**
 * 
 * @author mfchurch
 * @author jyuen
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class CodeBlockPanel extends JPanel {

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

		final TypeAction typeAction;

		final List<KnowIt> parameters;

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
		includesField = this.buildIncludesField(codeBlock);

		codePanel = new CodeEditorPanel(codeBlock);

		deleteCodeBlockButton = new JButton("Delete CodeBlock");
		addParameterButton = ComponentFactory.buildAddButton();
		typesButton = new JButton(typeAction);

		codeBlockEditorLayout = new GroupLayout(this);
		labelFont = new Font("SansSerif", Font.BOLD,
				Integer.parseInt(ScriptEase.getInstance().getPreference(
						ScriptEase.FONT_SIZE_KEY)) + 1);

		parameters = codeBlock.getParameters();

		codeBlock.addStoryComponentObserver(new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				final ArrayList<String> types;
				final TypeDialogBuilder builder;

				types = new ArrayList<String>(codeBlock.getTypes());
				builder = typeAction.getTypeSelectionDialogBuilder();

				builder.deselectAll();
				builder.selectTypesByKeyword(types, true);

				typeAction.updateName();
			}
		});

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
				.getInstance()
				.buildParameterObserver(codeBlock, parameterPanel));

		scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
				.getInstance().buildSlotObserver(codeBlock, implicitsLabel));

		implicitsLabel.setForeground(Color.DARK_GRAY);

		final ArrayList<String> types = new ArrayList<String>(
				codeBlock.getTypes());
		typeAction.getTypeSelectionDialogBuilder().deselectAll();
		typeAction.getTypeSelectionDialogBuilder().selectTypesByKeyword(types,
				true);

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
							.getSelectedTypeKeywords();
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

		if (scriptIt instanceof CauseIt) {
			subjectBox = new JComboBox();
			slotBox = this.buildSlotComboBox(codeBlock);

			deleteCodeBlockButton.setVisible(false);
			subjectLabel.setVisible(false);
			subjectBox.setVisible(false);
		} else {
			implicitsLabel.setVisible(false);
			implicitsLabelLabel.setVisible(false);
			includesLabel.setVisible(false);
			includesField.setVisible(false);

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
				subjectBox = this.buildSubjectComboBox(codeBlock);
				slotBox = this.buildSlotComboBox(codeBlock);
			} else {
				subjectBox = new JComboBox();
				slotBox = new JComboBox();

				// TODO These shouldn't be hidden here!
				subjectLabel.setVisible(false);
				subjectBox.setVisible(false);

				deleteCodeBlockButton.setVisible(false);
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
	}

	/**
	 * Builds a JTextField used to edit the Includes of a CodeBlock.
	 * 
	 * @param codeBlock
	 * @return
	 */
	private JTextField buildIncludesField(final CodeBlock codeBlock) {
		final JTextField includesField;
		final Runnable updateIncludes;

		includesField = new JTextField();
		includesField.setText(StringOp.getCollectionAsString(
				codeBlock.getIncludes(), ", "));

		updateIncludes = new Runnable() {
			@Override
			public void run() {
				final String labelFieldText;
				final String[] labelArray;
				final Collection<String> labels;

				labelFieldText = includesField.getText();
				labelArray = labelFieldText.split(",");
				labels = new ArrayList<String>();

				for (String label : labelArray) {
					labels.add(label.trim());
				}

				if (!labels.equals(codeBlock.getIncludes())) {
					// mfchurch TODO method type erasure problem with
					// AspectJ
					// if
					// (!UndoManager.getInstance().hasOpenUndoableAction())
					// UndoManager.getInstance().startUndoableAction(
					// "Setting Codeblock Includes to " + labels);
					codeBlock.setIncludes(labels);
					// UndoManager.getInstance().endUndoableAction();
				}

			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(includesField,
				updateIncludes, false, Color.WHITE);
		includesField.setHorizontalAlignment(JTextField.LEFT);

		codeBlock.addStoryComponentObserver(new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				final StoryComponent source = event.getSource();

				if (source == codeBlock) {
					includesField.setText(StringOp.getCollectionAsString(
							codeBlock.getIncludes(), ", "));
					includesField.revalidate();
				}
			}
		});

		return includesField;
	}

	/**
	 * Combo box used to set the subject of a code block.
	 * 
	 * @return
	 * 
	 */
	private JComboBox buildSubjectComboBox(final CodeBlock codeBlock) {
		// TODO Should be disabled if there are no valid parameters.
		final JComboBox subjectBox = new JComboBox();
		final Runnable buildItems;
		final ActionListener listener;

		listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String currentSelected;
				final String currentSubject;

				currentSelected = (String) subjectBox.getSelectedItem();
				currentSubject = codeBlock.getSubjectName();

				if (!currentSubject.equals(currentSelected)) {
					if (!UndoManager.getInstance().hasOpenUndoableAction()) {
						UndoManager.getInstance().startUndoableAction(
								"Setting CodeBlock subject to "
										+ currentSelected);
					}
					codeBlock.setSubject(currentSelected);

					if (UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().endUndoableAction();
				}
			}
		};

		buildItems = new Runnable() {
			@Override
			public void run() {
				subjectBox.removeActionListener(listener);
				subjectBox.removeAllItems();
				final ScriptIt scriptIt = codeBlock.getOwner();

				if (scriptIt != null) {
					subjectBox.addItem(null);
					final Collection<KnowIt> parameters = scriptIt
							.getParameters();
					for (KnowIt parameter : parameters) {
						final Collection<String> slots = getCommonSlotsForTypes(parameter);

						if (!slots.isEmpty())
							subjectBox.addItem(parameter.getDisplayText());
					}
					subjectBox.setSelectedItem(codeBlock.getSubjectName());
				}

				subjectBox.addActionListener(listener);
			}
		};

		buildItems.run();

		codeBlock.addStoryComponentObserver(subjectBox,
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						switch (event.getType()) {
						case CODE_BLOCK_SUBJECT_SET:
						case CHANGE_PARAMETER_LIST_ADD:
						case CHANGE_PARAMETER_LIST_REMOVE:
						case CHANGE_PARAMETER_NAME_SET:
						case CHANGE_PARAMETER_DEFAULT_TYPE_SET:
							buildItems.run();
							subjectBox.revalidate();
						default:
							break;
						}
					}
				});

		return subjectBox;
	}

	/**
	 * Creates a combo box used to set the slot on a CodeBlock.
	 * 
	 * @author mfchurch
	 * @author kschenk
	 * 
	 */
	private JComboBox buildSlotComboBox(final CodeBlock codeBlock) {
		// TODO Should be disabled if there are no valid subjects
		final JComboBox slotBox = new JComboBox();
		final Runnable buildItems;
		final ActionListener listener;

		listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String currentSelected;
				final String currentSlot;

				currentSelected = (String) slotBox.getSelectedItem();

				if (codeBlock.hasSlot())
					currentSlot = codeBlock.getSlot();
				else
					currentSlot = "";

				if (!currentSlot.equals(currentSelected)) {
					// TODO Undo doesn't actually undo.
					if (!UndoManager.getInstance().hasOpenUndoableAction()) {
						UndoManager.getInstance().startUndoableAction(
								"Setting CodeBlock slot to " + currentSelected);
					}
					codeBlock.setSlot(currentSelected);
					UndoManager.getInstance().endUndoableAction();
				}
			}
		};

		buildItems = new Runnable() {
			@Override
			public void run() {
				slotBox.removeActionListener(listener);
				slotBox.removeAllItems();

				if (codeBlock.hasSubject()) {
					final Collection<String> slots;

					slots = getCommonSlotsForTypes(codeBlock.getSubject());

					for (String slot : slots) {
						slotBox.addItem(slot);
					}

					if (codeBlock.hasSlot())
						slotBox.setSelectedItem(codeBlock.getSlot());
					else
						slotBox.setSelectedItem(ListOp.getFirst(slots));
				}

				slotBox.addActionListener(listener);
			}
		};

		buildItems.run();

		codeBlock.addStoryComponentObserver(slotBox,
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						switch (event.getType()) {
						case CODE_BLOCK_SLOT_SET:
						case CODE_BLOCK_SUBJECT_SET:
						case CHANGE_PARAMETER_DEFAULT_TYPE_SET:
							// TODO Doesn't fire on change parameter type
							buildItems.run();
							slotBox.revalidate();
						default:
							break;
						}
					}
				});

		return slotBox;
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

}
