package scriptease.gui.SETree.cell;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import scriptease.ScriptEase;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.ObservedJPanel;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SETree.transfer.BindingTransferHandlerExportOnly;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.control.ExpansionButton;
import scriptease.gui.control.editor.NameEditor;
import scriptease.gui.control.editor.ValueEditor;
import scriptease.gui.quests.QuestPoint;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingQuestPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.tools.GameConstantFactory;
import scriptease.util.StringOp;

/**
 * Simple Factory that constructs the various GUI components required for
 * editing script patterns.
 * 
 * @author remiller
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
	public static TypeWidget buildTypeWidget(final String type) {
		final TypeWidget typeWidget = new TypeWidget(type);
		typeWidget.setSize(typeWidget.getPreferredSize());
		return typeWidget;
	}

	/**
	 * Builds a BindingWidget from the given QuestPoint.
	 * 
	 * @param component
	 *            The quest to build a binding widget for.
	 * @param editable
	 *            <code>true</code> means that the name is editable
	 *            <code>false</code> otherwise.
	 * @return The binding widget for displaying the given StoryComponent
	 */
	public static BindingWidget buildBindingWidget(QuestPoint component,
			boolean editable) {
		return BindingWidgetBuilder.buildBindingWidget(component, editable);
	}

	/**
	 * Builds a BindingWidget from the given QuestPoint.
	 * 
	 * @param component
	 *            The quest to build a binding widget for.
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
	private static class BindingWidgetBuilder extends AbstractNoOpStoryVisitor {
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
				
				
				observedLabel = new ObservedJPanel(nameLabel, observer);

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
		public void processQuestPoint(QuestPoint questPoint) {
			this.bindingWidget = new BindingWidget(new KnowItBindingQuestPoint(
					questPoint));
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
		TypeWidget slotTypeWidget;
		final KnowItBinding binding = knowIt.getBinding();

		final Collection<String> types = knowIt.getAcceptableTypes();

		// for each type the KnowIt can accept
		// This is types for the other thing
		/*************************************************************************/
		for (String type : types) {
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
		/*************************************************************************/
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
			final GameConstant constantValue, final String bindingType) {
		final JComponent comp;
		final SpinnerNumberModel model;
		final JSpinner spinner;
		final JFormattedTextField spinnerTextEditor;
		float initVal;

		try {
			initVal = Float.parseFloat(constantValue.getResolutionText());
		} catch (NumberFormatException e) {
			initVal = 0;
		}

		/*
		 * extremly naive regex parsing since I'm unsure of a better way to
		 * determine the max, min value the spinner should have
		 * 
		 * @author mfchurch
		 */
		Comparable<?> min = null; // default to no min limit
		Comparable<?> max = null; // default to no max limit
		Number stepSize = 1; // default to int step size
		String regex = TranslatorManager.getInstance().getActiveTranslator()
				.getGameTypeManager().getReg(bindingType);
		final Pattern regexPattern = Pattern.compile(regex);
		if (regex != null && !regex.isEmpty()) {
			// if regex doesn't specify negative numbers, make min 0
			if (!regex.startsWith("[-]"))
				min = 0;
			// if regex specifies \. it wants a floating point
			if (regex.contains("\\."))
				stepSize = 0.1;
		}

		model = new SpinnerNumberModel(initVal, min, max, stepSize);
		spinner = new JSpinner(model);
		spinnerTextEditor = ((JSpinner.NumberEditor) spinner.getEditor())
				.getTextField();

		// Handle the initial value case
		final String scriptValue = knowIt.getBinding().getScriptValue();
		if (scriptValue == null || scriptValue.isEmpty()) {
			// Set the initial value
			final Float value = (Float) spinner.getValue();
			final GameConstant newBinding;
			String safeValue = StringOp.convertNumberToPattern(
					value.toString(), regexPattern);
			newBinding = GameConstantFactory.getInstance().getConstant(
					bindingType, safeValue);
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					knowIt.setBinding(newBinding);
				}
			});
		}

		spinnerTextEditor.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					// Commit changes to the model first because JSpinners suck
					spinner.commitEdit();
				} catch (ParseException pe) {
					return;
				}
				final Float value = (Float) spinner.getValue();
				final GameConstant newBinding;

				String safeValue = StringOp.convertNumberToPattern(
						value.toString(), regexPattern);
				newBinding = GameConstantFactory.getInstance().getConstant(
						bindingType, safeValue);
				spinner.setValue(Float.valueOf(safeValue));

				// null check
				if (newBinding == null)
					return;

				if (!knowIt.getBinding().getValue().equals(newBinding)) {
					if (!UndoManager.getInstance().hasOpenUndoableAction()) {
						UndoManager.getInstance().startUndoableAction(
								"Set " + knowIt.getDisplayText()
										+ "'s value to [" + safeValue + "]");
						knowIt.setBinding(newBinding);
						UndoManager.getInstance().endUndoableAction();
					}
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		comp = spinner;
		widgetsToStoryComponents.put(comp, knowIt);
		return comp;
	}

	public static JComponent buildComboEditor(final KnowIt knowIt,
			final String bindingType) {
		final JComponent comp;
		final JComboBox combo;
		final Map<String, String> enumMap = TranslatorManager.getInstance()
				.getActiveTranslator().getGameTypeManager()
				.getEnumMap(bindingType);

		// Sort alphabetically
		List<String> list = new ArrayList<String>(enumMap.values());
		Collections.sort(list);
		combo = new JComboBox(list.toArray());

		String scriptValue = knowIt.getBinding().getScriptValue();
		if (scriptValue != null && !scriptValue.isEmpty())
			combo.setSelectedItem(scriptValue);
		else
			combo.setSelectedIndex(-1);

		combo.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
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
					UndoManager.getInstance().endUndoableAction();
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		comp = combo;
		widgetsToStoryComponents.put(comp, knowIt);
		return comp;
	}

	/**
	 * Builds a name editor used to set the display name of a StoryComponent
	 * 
	 * @param storyComponent
	 * @return
	 */
	public static JComponent buildNameEditor(final StoryComponent storyComponent) {
		NameEditor nameEditor = new NameEditor(storyComponent);
		widgetsToStoryComponents.put(nameEditor, storyComponent);
		return nameEditor;
	}

	/**
	 * Builds a value editor used to set the script value of a KnowIt
	 * 
	 * @param knowIt
	 * @return
	 */
	public static JComponent buildValueEditor(final KnowIt knowIt) {
		ValueEditor valueEditor = new ValueEditor(knowIt);
		widgetsToStoryComponents.put(valueEditor, knowIt);
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
	 * Builds a stylized JPanel to represent the given fanIn.
	 * 
	 * @param fanIn
	 * @return
	 */
	public static JPanel buildFanInPanel(Integer fanIn) {
		JPanel fanInPanel = new JPanel();
		fanInPanel.setOpaque(false);
		fanInPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		fanInPanel.add(new JLabel(fanIn.toString()));
		return fanInPanel;
	}
}
