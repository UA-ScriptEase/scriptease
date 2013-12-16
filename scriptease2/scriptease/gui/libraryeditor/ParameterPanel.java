package scriptease.gui.libraryeditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.ParameterPanelObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.component.ComponentFactory;
import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.GameType.GUIType;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.GUIOp;

/**
 * ParameterPanels are JPanels used to represent and edit parameters. <br>
 * <br>
 * Parameters have:
 * <ul>
 * <li>name</li>
 * <li>types</li>
 * <li>default type</li>
 * <li>default binding constant</li>
 * </ul>
 * A ParameterPanel also has a delete button to remove the parameter from the
 * CodeBlock.
 * 
 * @author kschenk
 * @author jyuen
 */
@SuppressWarnings("serial")
public class ParameterPanel extends JPanel {
	private final ObserverManager<ParameterPanelObserver> observerManager;

	private final KnowIt knowIt;

	/**
	 * Creates a new ParameterComponent with the passed in KnowIt parameter.
	 */
	public ParameterPanel(final ScriptIt scriptIt, final CodeBlock codeBlock,
			final KnowIt knowIt) {
		this(scriptIt, codeBlock, knowIt, true);
	}

	public ParameterPanel(final ScriptIt scriptIt, final CodeBlock codeBlock,
			final KnowIt knowIt, boolean removable) {
		super();
		this.knowIt = knowIt;

		final TypeAction typeAction;
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

		final LibraryModel library;

		this.observerManager = new ObserverManager<ParameterPanelObserver>();

		typeAction = new TypeAction();
		types = new ArrayList<String>();
		typesButton = ComponentFactory.buildFlatButton(typeAction);
		defaultTypeBox = new JComboBox();
		deleteButton = ComponentFactory.buildRemoveButton();
		groupLayout = new GroupLayout(this);
		bindingConstantComponent = new JPanel();

		typesPanel = new JPanel();
		defaultTypeBoxPanel = new JPanel();
		nameFieldPanel = new JPanel();
		bindingPanel = new JPanel();

		library = codeBlock.getLibrary();

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
		typeAction.getTypeSelectionDialogBuilder().selectTypesByKeyword(types,
				true);

		for (String type : types)
			defaultTypeBox.addItem(library.getTypeDisplayText(type) + " - "
					+ type);

		defaultTypeBox.setSelectedItem(library.getTypeDisplayText(knowIt
				.getDefaultType()));

		updateBindingConstantComponent(bindingConstantComponent);

		typeAction.setAction(new Runnable() {
			@Override
			public void run() {
				knowIt.setTypes(typeAction.getTypeSelectionDialogBuilder()
						.getSelectedTypeKeywords());

				final String initialDefaultType;
				initialDefaultType = (String) defaultTypeBox.getSelectedItem();

				defaultTypeBox.removeAllItems();

				for (String type : knowIt.getTypes()) {
					defaultTypeBox.addItem(scriptIt.getLibrary()
							.getTypeDisplayText(type) + " - " + type);
				}

				defaultTypeBox.setSelectedItem(initialDefaultType);

				defaultTypeBox.revalidate();

				scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
						StoryComponentChangeEnum.CHANGE_PARAMETER_TYPE));
				
				notifyChange();
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
				}
			}
		});

		if (!this.isSubjectInCause(scriptIt, codeBlock)) {
			deleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!UndoManager.getInstance().hasOpenUndoableAction()) {
						UndoManager.getInstance().startUndoableAction(
								"Remove parameter " + knowIt + " to "
										+ codeBlock);
						codeBlock.removeParameter(knowIt);
						UndoManager.getInstance().endUndoableAction();
					}
				}
			});
		} else {
			deleteButton.setVisible(false);
		}

		typesPanel.add(typesButton);
		defaultTypeBoxPanel.add(defaultTypeBox);
		nameFieldPanel.add(this.buildNameField(scriptIt, codeBlock));
		bindingPanel.add(bindingConstantComponent);

		typesPanel.setBorder(new TitledBorder("Types"));
		nameFieldPanel.setBorder(new TitledBorder("Name"));
		defaultTypeBoxPanel.setBorder(new TitledBorder("Default Type"));
		bindingPanel.setBorder(new TitledBorder("Default Value"));

		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
		groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
				.addComponent(nameFieldPanel).addComponent(typesPanel)
				.addComponent(defaultTypeBoxPanel).addComponent(bindingPanel)
				.addComponent(deleteButton));

		groupLayout.setVerticalGroup(groupLayout
				.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(nameFieldPanel).addComponent(typesPanel)
				.addComponent(deleteButton).addComponent(defaultTypeBoxPanel)
				.addComponent(bindingPanel));
	}

	/**
	 * Builds a TextField used to edit the name of the KnowIt.
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @return
	 */
	private JTextField buildNameField(final ScriptIt scriptIt,
			final CodeBlock codeBlock) {

		final JTextField nameField;
		final Runnable commitText;

		nameField = new JTextField(this.knowIt.getDisplayText(), 10);

		if (this.isSubjectInCause(scriptIt, codeBlock)) {
			nameField.setEnabled(false);

			return nameField;
		}

		commitText = new Runnable() {
			@Override
			public void run() {
				// TODO Undoability
				final String newInput;
				newInput = nameField.getText();

				// TODO This probably doesn't do what we want it to.
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
				
				notifyChange();
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(nameField, commitText,
				false);

		return nameField;
	}

	/**
	 * Returns true if the ScriptIt is a cause, the CodeBlock is the first
	 * CodeBlock in it, and the KnowIt for the panel is the subject of the
	 * CodeBlock.
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @return
	 */
	private boolean isSubjectInCause(ScriptIt scriptIt, CodeBlock codeBlock) {
		return scriptIt instanceof CauseIt
				&& (scriptIt.getMainCodeBlock().equals(codeBlock))
				&& (this.knowIt.equals(codeBlock.getSubject()));
	}

	/**
	 * Updates the binding constant component, which is the component that is
	 * used to set default binding settings.
	 * 
	 * @param bindingConstantComponent
	 */
	private void updateBindingConstantComponent(
			JComponent bindingConstantComponent) {
		final JTextField inactiveTextField;
		final String defaultType;

		final SEModel activeModel;
		final LibraryModel library;
		final GUIType defaultTypeGuiType;

		activeModel = SEModelManager.getInstance().getActiveModel();
		if (!(activeModel instanceof LibraryModel))
			return;
		library = (LibraryModel) activeModel;

		defaultType = this.knowIt.getDefaultType();
		defaultTypeGuiType = library.getTypeGUI(defaultType);

		inactiveTextField = new JTextField(" Cannot set binding for ["
				+ defaultType + "]");

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

				if (bindingText.equals("null"))
					bindingField.setText("");
				else
					bindingField.setText(bindingText);

				this.setJTextFieldBinding(bindingField.getText());

				bindingField.getDocument().addDocumentListener(
						new DocumentListener() {
							@Override
							public void insertUpdate(DocumentEvent e) {
								ParameterPanel.this
										.setJTextFieldBinding(bindingField
												.getText());
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
				final SpinnerNumberModel model;
				final NumberEditor numberEditor;

				Comparable<?> min = null; // default to no min limit
				Comparable<?> max = null; // default to no max limit
				Number stepSize = 0.1; // default to float step size

				final float initVal;

				if (bindingText.equals("null") || bindingText.isEmpty())
					initVal = 0;
				else
					initVal = Float.parseFloat(bindingText);

				model = new SpinnerNumberModel(initVal, min, max, stepSize);
				bindingSpinner = new JSpinner(model);
				numberEditor = (NumberEditor) bindingSpinner.getEditor();

				numberEditor.getFormat().setMinimumFractionDigits(1);

				this.setJSpinnerBinding((Float) bindingSpinner.getValue());

				bindingSpinner.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						ParameterPanel.this
								.setJSpinnerBinding((Float) bindingSpinner
										.getValue());
					}
				});
				bindingConstantComponent.add(bindingSpinner);

				break;
			case JCOMBOBOX:
				final Map<String, String> map;
				final JComboBox bindingBox;

				map = library.getTypeEnumeratedValues(defaultType);
				bindingBox = new JComboBox();

				bindingBox.addItem(null);

				for (String key : map.keySet())
					bindingBox.addItem(map.get(key));

				if (bindingText.equals("null"))
					bindingBox.setSelectedItem(null);
				else
					bindingBox.setSelectedItem(map.get(bindingText));

				ParameterPanel.this.setJComboBoxBinding(
						bindingBox.getSelectedItem(), map);

				bindingBox.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ParameterPanel.this.setJComboBoxBinding(
								bindingBox.getSelectedItem(), map);
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

	/**
	 * Set the JTextField binding constant component to @param bindingText
	 * 
	 * @param bindingText
	 */
	private void setJTextFieldBinding(String bindingText) {
		final SimpleResource constant;

		constant = SimpleResource.buildSimpleResource(
				ParameterPanel.this.knowIt.getTypes(), bindingText);

		this.knowIt.setBinding(constant);
	}

	/**
	 * Set the JSpinner binding constant component to @param bindingValue
	 * 
	 * @param bindingValue
	 */
	private void setJSpinnerBinding(Float bindingValue) {
		final SimpleResource constant;

		constant = SimpleResource.buildSimpleResource(
				ParameterPanel.this.knowIt.getTypes(),
				Float.toString(bindingValue));

		this.knowIt.setBinding(constant);
	}

	/**
	 * Set the JComboBox binding constant component to @param binding
	 * 
	 * @param binding
	 * @param map
	 *            All the possible JComboBox values
	 */
	private void setJComboBoxBinding(Object binding, Map<String, String> map) {
		String defaultBindingName = "";

		if (binding != null) {
			for (Entry<String, String> entry : map.entrySet()) {
				if (entry.getValue().equals(binding)) {
					defaultBindingName = entry.getKey();
					break;
				}
			}
		} else {
			// Use the first binding by default if none is provided
			for (Entry<String, String> entry : map.entrySet()) {
				defaultBindingName = entry.getKey();
				break;
			}
		}

		final SimpleResource newConstant = SimpleResource.buildSimpleResource(
				ParameterPanel.this.knowIt.getTypes(), defaultBindingName);

		ParameterPanel.this.knowIt.setBinding(newConstant);
	}

	public void addListener(ParameterPanelObserver observer) {
		this.observerManager.addObserver(this, observer);
	}

	public void removeListener(ParameterPanelObserver observer) {
		this.observerManager.removeObserver(observer);
	}

	/**
	 * Notifies that the codeblock panel has changed.
	 */
	private void notifyChange() {
		for (ParameterPanelObserver observer : this.observerManager
				.getObservers()) {
			observer.parameterPanelChanged();
		}
	}
}