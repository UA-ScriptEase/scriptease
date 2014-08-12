package scriptease.gui.libraryeditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.ScriptEase;
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
import scriptease.model.atomic.knowitbindings.KnowItBindingAutomatic;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.GameType.GUIType;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.GUIOp;
import scriptease.util.ListOp;

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

	private ArrayList<String> selectedTypes = new ArrayList<String>();

	/**
	 * Creates a new ParameterComponent with the passed in KnowIt parameter.
	 */
	public ParameterPanel(final ScriptIt scriptIt, final CodeBlock codeBlock,
			final KnowIt knowIt) {
		this(scriptIt, codeBlock, knowIt, true, true);
	}

	/**
	 * Creates a new ParameterPanel with just the KnowIt, used for behaviour
	 * implicits.
	 */
	public ParameterPanel(final KnowIt knowIt, final boolean isEditable) {
		this(null, null, knowIt, false, isEditable);
	}

	public ParameterPanel(final ScriptIt scriptIt, final CodeBlock codeBlock,
			final KnowIt knowIt, boolean removable, boolean isEditable) {
		super(new FlowLayout(FlowLayout.LEADING));
		this.knowIt = knowIt;

		final TypeAction typeAction;
		final ArrayList<String> types;
		final JCheckBox automaticCheckBox;
		final JButton typesButton;
		final JComboBox defaultTypeBox;
		final JButton deleteButton;
		final JComponent bindingConstantComponent;
		final JTextField nameField;

		final JPanel automaticPanel;
		final JPanel defaultTypeBoxPanel;
		final JPanel bindingPanel;

		this.observerManager = new ObserverManager<ParameterPanelObserver>();

		typeAction = new TypeAction();
		types = new ArrayList<String>();
		typesButton = ComponentFactory.buildFlatButton(typeAction);
		defaultTypeBox = new JComboBox();
		deleteButton = ComponentFactory.buildRemoveButton();
		bindingConstantComponent = new JPanel();
		automaticCheckBox = new JCheckBox();

		defaultTypeBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		automaticPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		bindingPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

		// Behaviours currently use a null ScriptIt to generate their parameter
		// panel, so we have to account for that when checking read only. When
		// behaviours are more implemented this will probably need to be
		// reworked.
		// -zturchan
		if (scriptIt != null) {
			isEditable = ScriptEase.DEBUG_MODE
					|| !scriptIt.getLibrary().isReadOnly();
		} else {
			isEditable = true;
			// isEditable = ScriptEase.DEBUG_MODE;
		}

		this.setBorder(BorderFactory.createEtchedBorder());
		this.setBackground(GUIOp.scaleColour(Color.GRAY, 1.9));

		defaultTypeBoxPanel.setOpaque(false);
		bindingPanel.setOpaque(false);
		bindingConstantComponent.setOpaque(false);

		// Set up sizes
		this.setMaximumSize(new Dimension(1920, 50));

		// Set default values
		types.addAll(knowIt.getTypes());

		typeAction.deselectAll();
		typeAction.selectTypesByKeyword(types, true);

		for (String type : types)
			defaultTypeBox.addItem(type);

		defaultTypeBox.setSelectedItem(knowIt.getDefaultType());

		updateBindingConstantComponent(bindingConstantComponent);

		typeAction.setAction(new Runnable() {
			@Override
			public void run() {
				knowIt.setTypes(typeAction.getSelectedTypes());

				final String initialDefaultType;
				final List<String> types;

				initialDefaultType = (String) defaultTypeBox.getSelectedItem();
				types = new ArrayList<String>(knowIt.getTypes());

				defaultTypeBox.removeAllItems();

				Collections.sort(types);

				for (String type : types) {
					defaultTypeBox.addItem(type);
				}

				if (types.contains(initialDefaultType))
					defaultTypeBox.setSelectedItem(initialDefaultType);
				else
					defaultTypeBox.setSelectedItem(ListOp.head(types));

				defaultTypeBox.revalidate();

				if (scriptIt != null) {
					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CHANGE_PARAMETER_TYPE));
				}

				notifyChange();
			}
		});

		defaultTypeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Collection<String> newTypeList;
				final String selectedType;

				newTypeList = new ArrayList<String>();
				selectedType = (String) defaultTypeBox.getSelectedItem();

				if (selectedType != null) {
					final List<String> types;

					types = new ArrayList<String>(knowIt.getTypes());

					newTypeList.add(selectedType);

					for (String type : types) {
						if (!type.equals(selectedType))
							newTypeList.add(type);
						selectedTypes.add(type);
					}
					knowIt.setTypesByName(newTypeList);
					updateBindingConstantComponent(bindingConstantComponent);
				}
			}
		});

		if ((scriptIt != null && codeBlock != null)
				&& !this.isSubjectInCause(scriptIt, codeBlock)) {
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

		automaticCheckBox
				.setSelected(knowIt.getBinding() instanceof KnowItBindingAutomatic);

		automaticCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (automaticCheckBox.isSelected()) {
					knowIt.setBinding(new KnowItBindingAutomatic(knowIt));
				} else {
					knowIt.clearBinding();
				}

				updateBindingConstantComponent(bindingConstantComponent);
			}
		});

		defaultTypeBoxPanel.add(new JLabel("First Type:"));
		defaultTypeBox.setEnabled(isEditable);
		defaultTypeBoxPanel.add(defaultTypeBox);

		automaticPanel.add(new JLabel("Bind Automatically"));
		automaticCheckBox.setEnabled(isEditable);
		automaticPanel.add(automaticCheckBox);

		bindingPanel.add(new JLabel("Default Value:"));
		bindingConstantComponent.setEnabled(isEditable);
		bindingPanel.add(bindingConstantComponent);

		defaultTypeBoxPanel.setPreferredSize(new Dimension(350,
				defaultTypeBoxPanel.getPreferredSize().height));
		typesButton.setPreferredSize(new Dimension(100, typesButton
				.getPreferredSize().height));
		bindingPanel.setPreferredSize(new Dimension(350, bindingPanel
				.getPreferredSize().height));

		typesButton.setEnabled(isEditable);
		defaultTypeBoxPanel.setEnabled(isEditable);
		deleteButton.setEnabled(isEditable);
		bindingPanel.setEnabled(isEditable);

		if (scriptIt != null && codeBlock != null) {
			nameField = this.buildNameField(scriptIt, codeBlock);
			nameField.setEnabled(isEditable);
			this.add(nameField);
		}
		this.getPreferredSize();
		this.add(typesButton);
		this.add(defaultTypeBoxPanel);
		if (scriptIt != null && codeBlock != null) {
			this.add(automaticPanel);
			this.add(bindingPanel);
			this.add(Box.createHorizontalGlue());
			this.add(deleteButton);
		}
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

		// nameField.setMaximumSize(new Dimension(35000, nameField
		// .getPreferredSize().height));
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
		final LibraryModel library;
		final GUIType defaultTypeGuiType;
		final boolean isEditable;

		library = this.knowIt.getLibrary();

		defaultType = this.knowIt.getDefaultType();
		defaultTypeGuiType = library.getType(defaultType).getGui();

		inactiveTextField = new JTextField(" Cannot set binding for ["
				+ defaultType + "]");

		inactiveTextField.setEnabled(false);

		isEditable = ScriptEase.DEBUG_MODE || !library.isReadOnly();

		bindingConstantComponent.removeAll();

		if (this.knowIt.getBinding() instanceof KnowItBindingAutomatic) {
			inactiveTextField.setText("Binding will be added automatically.");
			bindingConstantComponent.add(inactiveTextField);
		} else if (defaultTypeGuiType == null)
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
				final List<String> values;
				final JComboBox bindingBox;

				map = library.getType(defaultType).getEnumMap();
				bindingBox = new JComboBox();
				values = new ArrayList<String>(map.values());

				Collections.sort(values);

				bindingBox.addItem(null);
				for (String value : values)
					bindingBox.addItem(value);

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

				bindingBox.setEnabled(isEditable);
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

	/**
	 * Returns the list of types for the current KnowIt
	 * 
	 * @return
	 */
	public ArrayList<String> getSelectedTypes() {
		return this.selectedTypes;
	}

	/**
	 * returns the KnowIt that the ParameterPanel modifies.
	 * 
	 * @return
	 */
	public KnowIt getKnowIt() {
		return this.knowIt;
	}
}