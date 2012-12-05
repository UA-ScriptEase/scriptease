package scriptease.gui.cell;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JToggleButton;

import scriptease.ScriptEase;
import scriptease.controller.MouseForwardingAdapter;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.gui.ui.TypeWidgetUI;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;

/**
 * TypeWidget represents the JToggleButton drawn to show the Type of a slot. It
 * was abstracted into it's own class so it could maintain a weak referenced
 * observer with the Translator.
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class TypeWidget extends JToggleButton implements TranslatorObserver {
	private String type;

	public TypeWidget(final String type) {
		final int baseFontSize;
		final int fontSize;
		final String typeName;

		this.setUI(TypeWidgetUI.getInstance());
		this.setEnabled(false);
		this.setFocusable(false);

		// pass events to parent until consumed
		MouseForwardingAdapter mouseForwardingAdapter = MouseForwardingAdapter
				.getInstance();
		this.addMouseListener(mouseForwardingAdapter);
		this.addMouseMotionListener(mouseForwardingAdapter);

		// drawing settings
		this.setForeground(Color.WHITE);

		baseFontSize = Integer.parseInt(ScriptEase.getInstance().getPreference(
				ScriptEase.FONT_SIZE_KEY));
		fontSize = Math.round(ScriptWidgetFactory.LABEL_FONT_SIZE_SCALE_FACTOR
				* baseFontSize);
		this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));

		this.setBackground(ScriptEaseUI.COLOUR_GAME_OBJECT);

		this.type = type;

		Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		typeName = (activeTranslator != null && activeTranslator
				.loadedAPIDictionary()) ? activeTranslator.getGameTypeManager()
				.getDisplayText(this.type) : "";

		setTypeText(typeName);
		this.setToolTipText(typeName);
		TranslatorManager.getInstance().addTranslatorObserver(this);
	}

	private void setTypeText(String typeName) {
		if (typeName.equals("")) {
			// Unknown type. Translator is probably not loaded.
			this.setText("!");
			this.setEnabled(false);
		} else {
			if (typeName.equalsIgnoreCase(GameTypeManager.DEFAULT_BOOL_TYPE))
				// Duane wanted AskIts and booleans to have a question mark for
				// their type widget.
				this.setText("?");
			else
				this.setText(typeName.substring(0, 1).toUpperCase());
		}
	}

	/**
	 * Cannot enable TypeWidget's toggle button
	 */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(false);
	}

	/**
	 * When a new translator is loaded, update the TypeText to match whether the
	 * type exists or not.
	 */
	@Override
	public void translatorLoaded(Translator newTranslator) {
		String typeName = "";
		typeName = newTranslator != null ? newTranslator.getGameTypeManager()
				.getDisplayText(this.type) : "";

		setTypeText(typeName);
	}

	@Override
	public String toString() {
		return "TypeWidget [" + this.type + "]";
	}
}