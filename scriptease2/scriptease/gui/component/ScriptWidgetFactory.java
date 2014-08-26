package scriptease.gui.component;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scriptease.ScriptEase;
import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.transfer.BindingTransferHandlerExportOnly;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingAutomatic;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryGroup;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.GUIOp;

/**
 * Simple Factory that constructs the various GUI components required for
 * editing script patterns.
 * 
 * @author remiller
 * @author kschenk
 * @author jyuen
 */
public class ScriptWidgetFactory {
	/**
	 * The scaling factor to use for scaling fonts
	 */
	public final static Color LABEL_TEXT_COLOUR = Color.WHITE;
	public final static Color LABEL_BACKGROUND_COLOUR = Color.GRAY;

	/**
	 * Size of the border applied to each row JComponent so that selection
	 * rectangles can be drawn.
	 */
	public static final int TOTAL_ROW_BORDER_SIZE = 4;

	/**
	 * Builds a button for displaying a particular game type. The created button
	 * will be round and appear mildly convex.
	 * 
	 * @param type
	 *            The type to represent.
	 * 
	 * @return A button that displays a type.
	 */
	public static TypeWidget buildTypeWidget(final String keyword) {
		final SEModel model = SEModelManager.getInstance().getActiveModel();
		final GameType type;

		if (model != null)
			type = model.getType(keyword);
		else
			type = null;

		return new TypeWidget(type);
	}

	/**
	 * Builds a BindingWidget from the given Story Component.
	 * 
	 * @param component
	 *            The knowIt to build a binding widget for.
	 * @param editable
	 *            <code>true</code> means that the name is editable
	 *            <code>false</code> otherwise.
	 * @return The binding widget for displaying the given StoryComponent
	 */
	public static BindingWidget buildBindingWidget(StoryComponent component,
			boolean editable) {
		return BindingWidgetBuilder.buildBindingWidget(component, editable);
	}

	/**
	 * Builds a new binding widget for the passed in binding.
	 * 
	 * @param binding
	 * @return
	 */
	public static BindingWidget buildBindingWidget(KnowItBinding binding) {
		return new BindingWidget(binding);
	}

	/**
	 * Builds a name label for a component that updates itself based on the name
	 * passed to it.
	 * 
	 * @param storyComponent
	 * @return
	 */
	public static JComponent buildObservedNameLabel(
			final StoryComponent storyComponent) {
		final JLabel nameLabel;

		nameLabel = ScriptWidgetFactory.buildLabel(storyComponent
				.getDisplayText());

		storyComponent.addStoryComponentObserver(new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				// only update the name for now, but if anything else is
				// needed later, it should be added here. - remiller
				if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
					nameLabel.setText(event.getSource().getDisplayText());
				}
			}
		});

		if (storyComponent instanceof KnowIt) {
			final KnowIt knowIt = (KnowIt) storyComponent;
			final KnowItBinding binding = knowIt.getBinding();

			if (binding instanceof KnowItBindingUninitialized) {
				final KnowItBindingUninitialized uninit;
				final KnowIt refKnowIt;
				final Runnable changeText;

				uninit = (KnowItBindingUninitialized) binding;
				refKnowIt = uninit.getValue();
				changeText = new Runnable() {
					@Override
					public void run() {
						final KnowItBinding srcBinding = refKnowIt.getBinding();

						srcBinding.process(new BindingAdapter() {
							@Override
							public void processNull(
									KnowItBindingNull nullBinding) {
								nameLabel.setText(refKnowIt.getDisplayText());
							}

							@Override
							public void processReference(
									KnowItBindingReference reference) {
								nameLabel.setText(reference.getValue()
										.getDisplayText());
							}

							@Override
							public void processResource(
									KnowItBindingResource constant) {
								nameLabel
										.setText(constant.getValue().getName());
							}

							@Override
							public void processAutomatic(
									KnowItBindingAutomatic automatic) {
								System.err.println("IDK HOW TO DO THIS dba31");
							}

							@Override
							public void processFunction(
									KnowItBindingFunction function) {
								System.err.println("IDK HOW TO DO THIS dfa62");
							}

							public void processStoryGroup(
									KnowItBindingStoryGroup storyGroup) {
								nameLabel.setText(storyGroup.getValue()
										.getDisplayText());
							};

							@Override
							public void processStoryPoint(
									KnowItBindingStoryPoint storyPoint) {
								nameLabel.setText(storyPoint.getValue()
										.getDisplayText());
							}

							public void processUninitialized(
									KnowItBindingUninitialized uninitialized) {
								nameLabel.setText(uninitialized.getValue()
										.getDisplayText());
							};
						});

					}
				};

				changeText.run();

				refKnowIt
						.addStoryComponentObserver(new StoryComponentObserver() {
							@Override
							public void componentChanged(
									StoryComponentEvent event) {
								if (event.getType() == StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND) {
									changeText.run();
								}
							}
						});
			}
		}

		return nameLabel;
	}

	/**
	 * Visitor class that constructs {@link BindingWidget}s. <br>
	 * <br>
	 * The related methods in ScriptWidgetFactory are facades that forwarding to
	 * this class. They exist to be able to specify the acceptable types, and to
	 * act as a Facade pattern for building this stuff. Yes,
	 * BindingWidgetBuilder could handle multiple types but those are the only
	 * ones that make sense.
	 * 
	 * @author mfchurch
	 * @author remiller
	 */
	private static class BindingWidgetBuilder extends StoryAdapter {
		private BindingWidget bindingWidget;

		/**
		 * Builds a BindingWidget from the given StoryComponent, and whether
		 * it's name is editable specified.
		 * 
		 * @param storyComponent
		 * @return
		 */
		public static BindingWidget buildBindingWidget(
				final StoryComponent storyComponent, boolean editable) {
			final BindingWidgetBuilder builder = new BindingWidgetBuilder();
			final BindingWidget widget;

			storyComponent.process(builder);
			widget = builder.bindingWidget;

			if (editable) {
				widget.add(ScriptWidgetFactory.buildNameEditor(storyComponent));
				// This is another one of those moments where I wish we could
				// use CSS. The blank JLabel adds some extra space at the end
				// after a name editor so that we can still drag the widget.
				widget.add(new JLabel("   "));
			} else {
				widget.add(ScriptWidgetFactory
						.buildObservedNameLabel(storyComponent));
			}

			widget.setTransferHandler(BindingTransferHandlerExportOnly
					.getInstance());

			// Set an empty border to prevent line crowding.
			widget.setBorder(BorderFactory.createEmptyBorder(
					TOTAL_ROW_BORDER_SIZE, TOTAL_ROW_BORDER_SIZE,
					TOTAL_ROW_BORDER_SIZE, TOTAL_ROW_BORDER_SIZE));

			return widget;
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			final String name = knowIt.getDisplayText();
			if ((knowIt.getOwner().getOwner() instanceof Behaviour && (name
					.equalsIgnoreCase(Behaviour.INITIATOR) || name
					.equalsIgnoreCase(Behaviour.RESPONDER)))
					|| (knowIt.getBinding() instanceof KnowItBindingNull))
				this.bindingWidget = new BindingWidget(
						new KnowItBindingUninitialized(
								new KnowItBindingReference(knowIt)));
			else
				this.bindingWidget = new BindingWidget(
						new KnowItBindingReference(knowIt));
		}

		@Override
		public void processStoryPoint(StoryPoint storyPoint) {
			this.bindingWidget = new BindingWidget(new KnowItBindingStoryPoint(
					storyPoint));
		}

		@Override
		public void processStoryGroup(StoryGroup storyGroup) {
			this.bindingWidget = new BindingWidget(new KnowItBindingStoryGroup(
					storyGroup));
		}
	}

	/**
	 * Creates a panel that contains widgets for displaying the legal types of a
	 * particular KnowIt.
	 * 
	 * @param knowIt
	 *            the KnowIt whose legal types are to be listed
	 * @return the component that contains the collection of legal type widgets
	 *         for the given knowIt
	 */
	public static JPanel buildLegalTypesPanel(KnowIt knowIt) {
		final KnowItBinding binding = knowIt.getBinding();
		final Collection<String> types = knowIt.getAcceptableTypes();
		final JPanel typePanel;

		typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

		typePanel.setOpaque(false);

		// for each type the KnowIt can accept
		// This is types for the other thing
		for (String type : types) {
			final TypeWidget slotTypeWidget;

			slotTypeWidget = ScriptWidgetFactory.buildTypeWidget(type);
			slotTypeWidget.setSelected(true);

			// the colour depends on the actual binding of the KnowIt
			if (!binding.isBound()) {
				slotTypeWidget.setBackground(ScriptEaseUI.COLOUR_UNBOUND);
			} else
				slotTypeWidget.setBackground(ScriptEaseUI.COLOUR_BOUND);

			// only show types that are unbound possibilities
			if (!binding.isBound() || !binding.getTypes().contains(type)) {
				typePanel.add(slotTypeWidget);
			}
		}
		return typePanel;
	}

	/**
	 * Builds a widget that represents a location where game data must be
	 * specified via a user's drag & drop. The slot widget is meant to display a
	 * KnowIt, its legal types, and its current binding. Name editable
	 * determines if the knowits name is editable when no knowit is bound.
	 * 
	 * @param knowIt
	 * @param isNameEditable
	 * @return
	 */
	public static JComponent buildSlotPanel(final KnowIt knowIt,
			boolean isNameEditable) {
		return new SlotPanel(knowIt, isNameEditable);
	}

	/**
	 * Builds a label for displaying plain text in the ScriptEase pattern
	 * Constructor GUI.
	 * 
	 * @param text
	 * @return An otherwise normal JLabel configured for display in the tree
	 */
	public static JLabel buildLabel(String text) {
		final JLabel label = new JLabel(text == null ? "" : text);
		final int fontSize = Integer.parseInt(ScriptEase.getInstance()
				.getPreference(ScriptEase.FONT_SIZE_KEY));

		label.setFont(new Font(Font.SANS_SERIF, Font.ROMAN_BASELINE, fontSize));
		label.setForeground(Color.WHITE);

		return label;
	}

	/**
	 * Builds a label for displaying plain text in the ScriptEase pattern
	 * Constructor GUI.
	 * 
	 * @param text
	 *            the text to display
	 * 
	 * @param background
	 *            the background colour to use
	 * 
	 * @return An otherwise normal JLabel configured for display in the tree
	 */
	public static JLabel buildMonospaceLabel(String text, Color background) {
		final JLabel label = new JLabel(text == null ? "" : text);
		final int fontSize = Integer.parseInt(ScriptEase.getInstance()
				.getPreference(ScriptEase.FONT_SIZE_KEY));

		label.setForeground(ScriptWidgetFactory.LABEL_TEXT_COLOUR);

		label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize - 1));
		label.setBackground(background);
		label.setOpaque(true);

		// border
		label.setBorder(BorderFactory.createMatteBorder(1, 3, 1, 3, background));

		return label;
	}

	/**
	 * Creates a new Spinner Editor for a KnowIt.
	 * 
	 * @param knowIt
	 * @param constantValue
	 * @param bindingType
	 * @return
	 */
	public static JComponent buildSpinnerEditor(final KnowIt knowIt,
			final BindingWidget widget, final Resource constantValue,
			final String bindingType) {

		// This comes from finding the highest number allowed for JSpinners.
		final float MAX_NUMBER = 16777216.0f;

		final Comparable<?> MAX = MAX_NUMBER;
		final Comparable<?> MIN = -MAX_NUMBER;
		final Float STEP_SIZE = 1.0f;

		final SpinnerNumberModel model;
		final JSpinner spinner;
		final JTextField textField;

		final String scriptValue;

		final StoryComponentObserver observer;
		final ChangeListener changeListener;

		float initVal;
		try {
			initVal = Float.parseFloat(constantValue.getCodeText());
		} catch (NumberFormatException e) {
			initVal = 0;
		}

		model = new SpinnerNumberModel(initVal, MIN, MAX, STEP_SIZE);
		spinner = new JSpinner(model);
		scriptValue = knowIt.getBinding().getScriptValue();

		textField = ((JSpinner.DefaultEditor) spinner.getEditor())
				.getTextField();
		textField.setBackground(ScriptEaseUI.COLOUR_SIMPLE_TEXT);

		// For some annoying reason, JSpinners don't automatically resize when
		// you set their max and min values...
		int length = textField.getText().length();
		if (length % 2 != 0) {
			textField.setColumns((length + 1) / 2);
		} else
			textField.setColumns(length / 2);

		// Handle the initial value case
		if (scriptValue == null || scriptValue.isEmpty()) {
			// Set the initial value
			final Float value = (Float) spinner.getValue();
			final Resource newBinding;
			newBinding = SimpleResource.buildSimpleResource(bindingType,
					value.toString());
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					knowIt.setBinding(newBinding);
				}
			});
		}

		changeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final JSpinner spinner;
				final Float value;
				final SimpleResource newBinding;

				spinner = (JSpinner) e.getSource();
				value = (Float) spinner.getValue();
				newBinding = SimpleResource.buildSimpleResource(bindingType,
						value.toString());

				int length = textField.getText().length();
				if (length % 2 != 0) {
					textField.setColumns((length + 1) / 2);
				} else
					textField.setColumns(length / 2);

				// null check
				if (newBinding == null)
					return;

				if (!knowIt.getBinding().getValue().equals(newBinding)) {
					if (!UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().startUndoableAction(
								"Set " + knowIt.getDisplayText()
										+ "'s value to [" + value + "]");

					knowIt.setBinding(newBinding);

					if (UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().endUndoableAction();
				}
			}
		};

		observer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND) {
					final Object value;
					value = knowIt.getBinding().getValue();
					if (value instanceof Resource) {
						float newBinding = 0;

						try {
							newBinding = Float.parseFloat(((Resource) value)
									.getCodeText());
						} catch (NumberFormatException e) {
							newBinding = 0;
						}

						widget.setBinding(knowIt.getBinding());

						spinner.removeChangeListener(changeListener);
						spinner.setValue(newBinding);
						spinner.addChangeListener(changeListener);
					}

					spinner.getEditor().revalidate();
					spinner.getEditor().repaint();

					spinner.revalidate();
					spinner.repaint();
				}
			}
		};

		spinner.addChangeListener(changeListener);

		knowIt.addStoryComponentObserver(observer);

		return spinner;
	}

	public static JComponent buildComboEditor(final KnowIt knowIt,
			final BindingWidget bindingWidget, final String bindingType) {
		final Map<String, String> enumMap;
		final List<String> list;

		final JComboBox combo;

		final StoryComponentObserver observer;
		final ActionListener actionListener;

		final String scriptValue;

		enumMap = knowIt.getLibrary().getType(bindingType).getEnumMap();
		list = new ArrayList<String>(enumMap.values());
		Collections.sort(list);

		combo = new JComboBox(list.toArray());
		combo.setBackground(ScriptEaseUI.COLOUR_SIMPLE_TEXT);

		scriptValue = knowIt.getBinding().getScriptValue();

		if (scriptValue != null && !scriptValue.isEmpty())
			combo.setSelectedItem(enumMap.get(scriptValue));
		else
			combo.setSelectedIndex(-1);

		actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				final Resource newBinding;

				// check the selected value
				String selectedItem = (String) combo.getSelectedItem();
				if (selectedItem == null || selectedItem.isEmpty())
					return;

				// resolve to the correct script value
				String resolvedValue = null;
				for (Entry<String, String> entry : enumMap.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (value.equals(selectedItem))
						resolvedValue = key;
				}

				// build a new resource with that script value and type
				newBinding = SimpleResource.buildSimpleResource(bindingType,
						resolvedValue);
				// null check
				if (newBinding == null)
					return;

				// set the binding
				KnowItBinding binding = knowIt.getBinding();
				if (!binding.isBound()
						|| !binding.getValue().equals(newBinding)) {
					if (!UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().startUndoableAction(
								"Set " + knowIt.getDisplayText() + "'s value");
					knowIt.setBinding(newBinding);
					if (UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().endUndoableAction();
				}
			}
		};

		observer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND) {
					final String scriptValue = knowIt.getBinding()
							.getScriptValue();

					if (scriptValue != null && !scriptValue.isEmpty())
						combo.setSelectedItem(enumMap.get(scriptValue));
					else
						combo.setSelectedIndex(-1);
					bindingWidget.setBinding(knowIt.getBinding());
				}
			}
		};

		combo.addActionListener(actionListener);

		knowIt.addStoryComponentObserver(observer);

		return combo;
	}

	/**
	 * Builds a name editor used to set the display name of a StoryComponent
	 * 
	 * @param storyComponent
	 * @return
	 */
	public static JComponent buildNameEditor(final StoryComponent component) {
		final JTextField nameEditor;
		final StoryComponentObserver observer;
		final Runnable commitText;
		final String displayText;
		final Color color;

		displayText = component.getDisplayText();

		if (component instanceof Note) {
			nameEditor = ComponentFactory.buildJTextFieldWithTextBackground(0,
					"Note", displayText);
			color = ScriptEaseUI.COLOUR_NOTE_TEXT_BG;
		} else {
			nameEditor = new JTextField(displayText);
			color = ScriptEaseUI.COLOUR_KNOWN_OBJECT_INNER;
		}

		observer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
					nameEditor.setText(component.getDisplayText());

					GUIOp.resizeJTextField(nameEditor);
				}
			}
		};
		commitText = new Runnable() {
			@Override
			public void run() {
				final String newValue = nameEditor.getText();
				if (SEModelManager.getInstance().hasActiveModel()) {
					if (!component.getDisplayText().equals(newValue)) {
						if (!UndoManager.getInstance().hasOpenUndoableAction()) {
							UndoManager.getInstance().startUndoableAction(
									"Change " + component.getDisplayText()
											+ " to " + newValue);
							component.setDisplayText(newValue);
							UndoManager.getInstance().endUndoableAction();
						}
					}
				}
			}
		};

		final boolean resizing;

		if (component instanceof StoryNode)
			resizing = false;
		else
			resizing = true;

		WidgetDecorator.decorateJTextFieldForFocusEvents(nameEditor,
				commitText, resizing, color);

		component.addStoryComponentObserver(observer);

		return nameEditor;
	}

	/**
	 * Builds a value editor used to set the script value of a KnowIt.
	 * 
	 * @param knowIt
	 * @return
	 */
	public static JComponent buildValueEditor(final KnowIt knowIt,
			final BindingWidget bindingWidget) {
		final KnowItBinding binding;

		final JTextField valueEditor;
		final StoryComponentObserver observer;
		final Runnable commitText;

		binding = knowIt.getBinding();
		if (!(binding instanceof KnowItBindingResource))
			System.err
					.println("Warning: ValueEditor currently only supports KnowItBindingConstant");

		valueEditor = new JTextField(binding.getScriptValue());

		observer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND) {
					valueEditor.setText(knowIt.getBinding().getScriptValue());
					bindingWidget.setBinding(knowIt.getBinding());

					GUIOp.resizeJTextField(valueEditor);
					valueEditor.revalidate();
					valueEditor.repaint();
				}
			}
		};
		commitText = new Runnable() {
			public void run() {
				final String newValue = valueEditor.getText();
				if (SEModelManager.getInstance().hasActiveModel()) {
					binding.process(new BindingAdapter() {
						@Override
						public void processResource(
								KnowItBindingResource constant) {
							if (!UndoManager.getInstance()
									.hasOpenUndoableAction()) {
								UndoManager.getInstance().startUndoableAction(
										"Change text to " + newValue);

								final Resource newResource;

								newResource = SimpleResource
										.buildSimpleResource(
												constant.getTypes(), newValue);

								knowIt.setBinding(newResource);
								UndoManager.getInstance().endUndoableAction();
							}
						}
					});
				}
			};
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(valueEditor,
				commitText, true, ScriptEaseUI.COLOUR_SIMPLE_TEXT);

		knowIt.addStoryComponentObserver(observer);
		return valueEditor;
	}

	/**
	 * Build a expansion button using the Java Look and Feel. The icon will
	 * depend on whether the button is expanded or collapsed.
	 * 
	 * @param collapsed
	 * @return
	 */
	public static ExpansionButton buildExpansionButton(Boolean collapsed) {
		return new ExpansionButton(collapsed);
	}

	/**
	 * Builds a JSpinner to represent and edit the given fanIn.
	 * 
	 * @param fanIn
	 * @param editable
	 * @return
	 */
	public static JSpinner buildFanInSpinner(final StoryPoint storyPoint,
			Comparable<?> max) {
		SpinnerNumberModel model;
		final JSpinner fanInSpinner;

		try {
			model = new SpinnerNumberModel(storyPoint.getFanIn(), 1, max, 1);
		} catch (Exception e) {
			model = new SpinnerNumberModel(1, 1, 1, 1);
		}

		fanInSpinner = new JSpinner(model);

		fanInSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				SpinnerModel spinnerModel = fanInSpinner.getModel();
				Integer spinnerValue = (Integer) spinnerModel.getValue();

				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction(
							"Change " + storyPoint.getFanIn() + " to "
									+ spinnerValue);
				storyPoint.setFanIn(spinnerValue);
				UndoManager.getInstance().endUndoableAction();
			}
		});

		return fanInSpinner;
	}
}
