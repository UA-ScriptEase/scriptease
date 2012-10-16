package scriptease.gui.cell;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

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
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scriptease.ScriptEase;
import scriptease.controller.BindingAdapter;
import scriptease.controller.ObservedJPanel;
import scriptease.controller.StoryAdapter;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WindowFactory;
import scriptease.gui.control.ExpansionButton;
import scriptease.gui.transfer.BindingTransferHandlerExportOnly;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.tools.GameConstantFactory;
import scriptease.util.GUIOp;

/**
 * Simple Factory that constructs the various GUI components required for
 * editing script patterns.
 * 
 * @author remiller
 * @author kschenk
 */
public class ScriptWidgetFactory {
	/**
	 * The scaling factor to use for scaling fonts
	 */
	public static final float LABEL_FONT_SIZE_SCALE_FACTOR = 1.1f;

	public final static Color LABEL_TEXT_COLOUR = Color.WHITE;
	public final static Color LABEL_BACKGROUND_COLOUR = Color.GRAY;

	/**
	 * Size of the border applied to each row JComponent so that selection
	 * rectangles can be drawn.
	 */
	public static final int TOTAL_ROW_BORDER_SIZE = 4;

	/**
	 * Map for storing which JComponent edits which StoryComponent. Whenever a
	 * widget for editing a specific StoryComponent is created, this map should
	 * be updated.
	 */
	private static Map<Component, StoryComponent> widgetsToStoryComponents = new WeakHashMap<Component, StoryComponent>();

	/**
	 * Retrieves the StoryComponent that the given JComponent edits. This can
	 * return <code>null</code> if the given JComponent does not edit a
	 * StoryComponent.
	 * 
	 * @param editor
	 *            the JComponent whose edited StoryComponent is to be retrieved.
	 * @return the edited StoryComponent.
	 */
	public static StoryComponent getEditedStoryComponent(Component editor) {
		return widgetsToStoryComponents.get(editor);
	}

	/**
	 * Gets the JPanel for the given knowIt from the widgetsToStoryComponents
	 * map. Can return null, if the knowIt does not have a mapped Component.
	 * 
	 * @param component
	 * @return
	 */
	public static Collection<JPanel> getEditedJPanel(StoryComponent component) {
		Collection<JPanel> jPanels = new ArrayList<JPanel>();
		Set<Component> components = widgetsToStoryComponents.keySet();
		for (Component aComponent : components) {
			if (aComponent instanceof JPanel) {
				if (widgetsToStoryComponents.get(aComponent) == component) {
					jPanels.add(((JPanel) aComponent));
				}
			}
		}
		return jPanels;
	}

	/**
	 * Builds a button for displaying a particular game type. The created button
	 * will be round and appear mildly convex.
	 * 
	 * @param type
	 *            The type to represent.
	 * 
	 * @return A button that displays a type.
	 */
	public static TypeWidget getTypeWidget(final String type) {
		final TypeWidget typeWidget = new TypeWidget(type);
		typeWidget.setSize(typeWidget.getPreferredSize());
		return typeWidget;
	}

	/**
	 * Builds a BindingWidget from the given StoryPoint.
	 * 
	 * @param component
	 *            The story to build a binding widget for.
	 * @param editable
	 *            <code>true</code> means that the name is editable
	 *            <code>false</code> otherwise.
	 * @return The binding widget for displaying the given StoryComponent
	 */
	public static BindingWidget buildBindingWidget(StoryPoint component,
			boolean editable) {
		return BindingWidgetBuilder.buildBindingWidget(component, editable);
	}

	/**
	 * Builds a BindingWidget from the given StoryPoint.
	 * 
	 * @param component
	 *            The story to build a binding widget for.
	 * @param editable
	 *            <code>true</code> means that the name is editable
	 *            <code>false</code> otherwise.
	 * @return The binding widget for displaying the given StoryComponent
	 */
	public static BindingWidget buildBindingWidget(KnowIt component,
			boolean editable) {
		return BindingWidgetBuilder.buildBindingWidget(component, editable);
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
			} else {
				final JLabel nameLabel;
				final ObservedJPanel observedLabel;
				final StoryComponentObserver observer;

				nameLabel = ScriptWidgetFactory.buildLabel(
						storyComponent.getDisplayText(), Color.WHITE);
				observer = new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						// only update the name for now, but if anything else is
						// needed later, it should be added here. - remiller
						if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
							nameLabel.setText(event.getSource()
									.getDisplayText());
						}
					}
				};

				observedLabel = new ObservedJPanel(nameLabel);
				observedLabel.addObserver(observer);

				storyComponent.addStoryComponentObserver(observer);

				widget.add(observedLabel);
			}

			widget.setTransferHandler(BindingTransferHandlerExportOnly
					.getInstance());
			// Set an empty border to prevent line crowding.
			widget.setBorder(BorderFactory.createEmptyBorder(
					TOTAL_ROW_BORDER_SIZE, TOTAL_ROW_BORDER_SIZE,
					TOTAL_ROW_BORDER_SIZE, TOTAL_ROW_BORDER_SIZE));

			widgetsToStoryComponents.put(widget, storyComponent);

			return widget;
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			this.bindingWidget = new BindingWidget(new KnowItBindingReference(
					knowIt));
		}

		@Override
		public void processStoryPoint(StoryPoint storyPoint) {
			this.bindingWidget = new BindingWidget(new KnowItBindingStoryPoint(
					storyPoint));
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
	public static JComponent populateLegalTypesPanel(JPanel typePanel,
			KnowIt knowIt) {
		typePanel.removeAll();
		final KnowItBinding binding = knowIt.getBinding();

		final Collection<String> types = knowIt.getAcceptableTypes();

		// for each type the KnowIt can accept
		// This is types for the other thing
		for (String type : types) {
			final TypeWidget slotTypeWidget;

			slotTypeWidget = ScriptWidgetFactory.getTypeWidget(type);
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
	 * KnowIt, its legal types, and its current binding.
	 * 
	 * @param knowIt
	 * @param compressed
	 *            whether or not to draw the slot as its compressed version. If
	 *            <code>true</code>, then the slot will draw the binding,
	 *            otherwise it will only draw the types list.
	 * 
	 * @return
	 */
	public static JComponent buildSlotPanel(final KnowIt knowIt) {
		final SlotPanel slotPanel = new SlotPanel(knowIt);

		widgetsToStoryComponents.put(slotPanel, knowIt);

		return slotPanel;
	}

	/**
	 * Builds a label for displaying plain text in the ScriptEase pattern
	 * Constructor GUI.
	 * 
	 * @param text
	 *            the text to display
	 * @param textColor
	 *            the colour to use for the text. If <code>null</code>, then the
	 *            default colour for JLabels will be used.
	 * 
	 * @return An otherwise normal JLabel configured for display in the tree
	 */
	public static JLabel buildLabel(String text, Color textColor) {
		final JLabel label;
		final int fontSize;
		final Font font;

		label = new JLabel(text == null ? "" : text);
		fontSize = Integer.parseInt(ScriptEase.getInstance().getPreference(
				ScriptEase.FONT_SIZE_KEY));
		font = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);

		label.setFont(font);
		if (textColor != null)
			label.setForeground(textColor);

		return label;
	}

	/**
	 * Builds a label for displaying plain text in the ScriptEase pattern
	 * Constructor GUI.
	 * 
	 * @param text
	 *            the text to display
	 * @param textColor
	 *            the colour to use for the text. If <code>null</code>, then the
	 *            default colour for JLabels will be used.
	 * @param background
	 *            the background colour to use
	 * 
	 * @return An otherwise normal JLabel configured for display in the tree
	 */
	public static JLabel buildLabel(String text, Color textColor,
			Color background) {
		JLabel label = buildLabel(text, textColor);
		label.setBackground(background);
		label.setOpaque(true);

		// border
		label.setBorder(BorderFactory.createMatteBorder(1, 3, 1, 3, background));

		int fontSize = Integer.parseInt(ScriptEase.getInstance().getPreference(
				ScriptEase.FONT_SIZE_KEY));
		label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize--));

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
			final BindingWidget widget, final GameConstant constantValue,
			final String bindingType) {

		final Comparable<?> MIN = null; // default to no min limit
		final Comparable<?> MAX = null; // default to no max limit
		final Float STEP_SIZE = 1.0f; // default to int step size

		final ObservedJPanel observedPanel;
		final SpinnerNumberModel model;
		final JSpinner spinner;

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

		observedPanel = new ObservedJPanel(spinner);

		// Handle the initial value case
		if (scriptValue == null || scriptValue.isEmpty()) {
			// Set the initial value
			final Float value = (Float) spinner.getValue();
			final GameConstant newBinding;
			newBinding = GameConstantFactory.getInstance().getConstant(
					bindingType, value.toString());
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
				final GameConstant newBinding;

				spinner = (JSpinner) e.getSource();
				value = (Float) spinner.getValue();
				newBinding = GameConstantFactory.getInstance().getConstant(
						bindingType, value.toString());

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
					if (value instanceof GameConstant) {
						float newBinding = 0;

						try {
							newBinding = Float
									.parseFloat(((GameConstant) value)
											.getCodeText());
						} catch (NumberFormatException e) {
							newBinding = 0;
						}

						widget.setBinding(knowIt.getBinding());
						// spinner.removeChangeListener(changeListener);
						spinner.setValue(newBinding);
						// spinner.addChangeListener(changeListener);
					}
				}
			}
		};

		spinner.addChangeListener(changeListener);

		knowIt.addStoryComponentObserver(observer);
		observedPanel.addObserver(observer);

		widgetsToStoryComponents.put(spinner, knowIt);

		return observedPanel;
	}

	public static JComponent buildComboEditor(final KnowIt knowIt,
			final BindingWidget bindingWidget, final String bindingType) {
		final Map<String, String> enumMap;
		final List<String> list;

		final JComboBox combo;
		final ObservedJPanel observedPanel;

		final StoryComponentObserver observer;
		final ActionListener actionListener;

		final String scriptValue;

		enumMap = TranslatorManager.getInstance().getActiveTranslator()
				.getGameTypeManager().getEnumMap(bindingType);
		list = new ArrayList<String>(enumMap.values());
		Collections.sort(list);

		combo = new JComboBox(list.toArray());
		observedPanel = new ObservedJPanel(combo);

		scriptValue = knowIt.getBinding().getScriptValue();

		if (scriptValue != null && !scriptValue.isEmpty())
			combo.setSelectedItem(enumMap.get(scriptValue));
		else
			combo.setSelectedIndex(-1);

		actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				final GameConstant newBinding;

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

				// build a new game constant with that script value and type
				newBinding = GameConstantFactory.getInstance().getConstant(
						bindingType, resolvedValue);
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
		observedPanel.addObserver(observer);

		widgetsToStoryComponents.put(combo, knowIt);

		return observedPanel;
	}

	/**
	 * Builds a name editor used to set the display name of a StoryComponent
	 * 
	 * @param storyComponent
	 * @return
	 */
	public static JComponent buildNameEditor(final StoryComponent component) {
		final JTextField nameEditor;
		final ObservedJPanel panel;
		final StoryComponentObserver observer;

		final Border defaultBorder;

		nameEditor = new JTextField(component.getDisplayText());
		panel = new ObservedJPanel(nameEditor);
		observer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
					nameEditor.setText(component.getDisplayText());

					GUIOp.resizeJTextField(nameEditor);

				}
			}
		};

		defaultBorder = nameEditor.getBorder();

		component.addStoryComponentObserver(observer);
		panel.addObserver(observer);

		nameEditor.setBackground(Color.white);
		nameEditor.setHorizontalAlignment(JTextField.CENTER);

		nameEditor.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {

				final String newValue = nameEditor.getText();
				if (PatternModelManager.getInstance().hasActiveModel()) {
					final String oldValue = component.getDisplayText();
					if (!oldValue.equals(newValue)) {
						if (!UndoManager.getInstance().hasOpenUndoableAction()) {
							UndoManager.getInstance().startUndoableAction(
									"Change " + oldValue + " to " + newValue);
							component.setDisplayText(newValue);
							UndoManager.getInstance().endUndoableAction();
						}
					}
				}

				nameEditor.setBorder(defaultBorder);
			}

			@Override
			public void focusGained(FocusEvent e) {
				nameEditor.setBorder(BorderFactory.createLineBorder(Color.RED,
						1));
			}
		});

		nameEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowFactory.getInstance().getCurrentFrame()
						.requestFocusInWindow();
			}
		});

		nameEditor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				GUIOp.resizeJTextField(nameEditor);
			}
		});

		GUIOp.resizeJTextField(nameEditor);

		widgetsToStoryComponents.put(panel, component);
		return panel;
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
		final ObservedJPanel panel;
		final StoryComponentObserver observer;

		final Border defaultBorder;

		binding = knowIt.getBinding();
		if (!(binding instanceof KnowItBindingConstant))
			System.err
					.println("Warning: ValueEditor currently only supports KnowItBindingConstant");

		valueEditor = new JTextField(binding.getScriptValue());
		panel = new ObservedJPanel(valueEditor);

		observer = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND) {
					valueEditor.setText(knowIt.getBinding().getScriptValue());
					bindingWidget.setBinding(knowIt.getBinding());

					GUIOp.resizeJTextField(valueEditor);
				}
			}
		};

		knowIt.addStoryComponentObserver(observer);
		panel.addObserver(observer);

		defaultBorder = valueEditor.getBorder();

		valueEditor.setBackground(Color.white);
		valueEditor.setHorizontalAlignment(JTextField.CENTER);

		valueEditor.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {

				final String newValue = valueEditor.getText();
				if (PatternModelManager.getInstance().hasActiveModel()) {
					binding.process(new BindingAdapter() {
						@Override
						public void processConstant(
								KnowItBindingConstant constant) {
							final String oldValue = constant.getScriptValue();
							if (!oldValue.equals(newValue)) {
								if (!UndoManager.getInstance()
										.hasOpenUndoableAction()) {
									UndoManager
											.getInstance()
											.startUndoableAction(
													"Change " + oldValue
															+ " to " + newValue);
									GameConstant newConstant = GameConstantFactory
											.getInstance().getConstant(
													constant.getTypes(),
													newValue);
									knowIt.setBinding(newConstant);
									UndoManager.getInstance()
											.endUndoableAction();
								}
							}
						}
					});
				}
				valueEditor.setBorder(defaultBorder);
			}

			@Override
			public void focusGained(FocusEvent e) {
				valueEditor.setBorder(BorderFactory.createLineBorder(Color.RED,
						1));
			}
		});

		valueEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowFactory.getInstance().getCurrentFrame()
						.requestFocusInWindow();
			}
		});

		valueEditor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				GUIOp.resizeJTextField(valueEditor);
			}
		});

		GUIOp.resizeJTextField(valueEditor);

		widgetsToStoryComponents.put(panel, knowIt);
		return panel;
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
		final SpinnerNumberModel model;
		final JSpinner fanInSpinner;

		model = new SpinnerNumberModel(storyPoint.getFanIn(), 1, max, 1);
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
