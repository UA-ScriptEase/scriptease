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
import java.util.regex.Pattern;

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

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.translator.io.model.SimpleResource;
import scriptease.translator.io.model.GameType.TypeValueWidgets;
import scriptease.util.GUIOp;
import scriptease.util.StringOp;

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
 * A ParameterPanel also has a delete button to remove the parameter from the
 * CodeBlock.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
class ParameterPanel extends JPanel {
	private final KnowIt knowIt;

	/**
	 * Creates a new ParameterComponent with the passed in KnowIt parameter.
	 * 
	 * @param knowIt
	 */
	protected ParameterPanel(final ScriptIt scriptIt,
			final CodeBlock codeBlock, final KnowIt knowIt) {
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

		final Translator activeTranslator;
		final GameTypeManager gameTypeManager;

		typeAction = new TypeAction();
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
			defaultTypeBox.addItem(gameTypeManager.getDisplayText(type) + " - "
					+ type);

		defaultTypeBox.setSelectedItem(gameTypeManager.getDisplayText(knowIt
				.getDefaultType()));

		updateBindingConstantComponent(bindingConstantComponent);

		// Set up listeners
		knowIt.addStoryComponentObserver(LibraryEditorListenerFactory
				.getInstance().buildParameterTypeObserver(knowIt,
						defaultTypeBox));

		knowIt.addStoryComponentObserver(LibraryEditorListenerFactory
				.getInstance().buildParameterDefaultTypeObserver());

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

		if (!this.isSubjectInCause(scriptIt, codeBlock)) {
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
		bindingPanel.setBorder(new TitledBorder("Default Binding"));

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
			}
		};

		WidgetDecorator.getInstance().decorateJTextFieldForFocusEvents(
				nameField, commitText, false);

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
		return scriptIt.isCause()
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

								final SimpleResource newConstant;

								newConstant = SimpleResource
										.buildSimpleResource(
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

						final SimpleResource newConstant;

						newConstant = SimpleResource.buildSimpleResource(
								ParameterPanel.this.knowIt.getTypes(),
								safeValue);

						ParameterPanel.this.knowIt.setBinding(newConstant);
					}
				});
				bindingConstantComponent.add(bindingSpinner);

				break;
			case JCOMBOBOX:
				final Map<String, String> map;
				final JComboBox bindingBox;

				map = gameTypeManager.getEnumMap(this.knowIt.getDefaultType());
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
							for (Entry<String, String> entry : map.entrySet()) {
								if (entry.getValue().equals(bindingBoxValue)) {
									defaultBindingName = entry.getKey();
									break;
								}
							}

							final SimpleResource newConstant = SimpleResource
									.buildSimpleResource(
											ParameterPanel.this.knowIt
													.getTypes(),
											defaultBindingName);
							ParameterPanel.this.knowIt.setBinding(newConstant);
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