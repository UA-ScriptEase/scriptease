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
public class TypeWidget extends JToggleButton {
	private String type;

	public TypeWidget(final String type) {
		final int baseFontSize;
		final int fontSize;
		final String typeName;
		final Translator activeTranslator;
		final TranslatorObserver observer;

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

		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		typeName = (activeTranslator != null && activeTranslator
				.loadedAPIDictionary()) ? activeTranslator.getGameTypeManager()
				.getDisplayText(this.type) : "";
		observer = new TranslatorObserver() {
			/**
			 * When a new translator is loaded, update the TypeText to match
			 * whether the type exists or not.
			 */
			@Override
			public void translatorLoaded(Translator newTranslator) {
				String typeName = "";
				typeName = newTranslator != null ? newTranslator
						.getGameTypeManager().getDisplayText(
								TypeWidget.this.type) : "";

				if (typeName == null || typeName.equals(""))
					setTypeText("");
			}
		};

		setTypeText(type);
		this.setToolTipText(typeName);
		TranslatorManager.getInstance().addTranslatorObserver(this, observer);
	}

	private void setTypeText(String type) {
		if (type.equals("")) {
			// Unknown type. Translator is probably not loaded.
			this.setText("!");
			this.setEnabled(false);
		} else {
			final GameTypeManager gameTypeManager;

			gameTypeManager = TranslatorManager.getInstance()
					.getActiveGameTypeManager();

			if (gameTypeManager != null && gameTypeManager.hasWidgetName(type)) {
				this.setText(gameTypeManager.getWidgetName(type));
			} else {
				if (gameTypeManager != null && gameTypeManager.hasEnum(type))
					this.setText(GameTypeManager.DEFAULT_LIST_WIDGET);
				else
					this.setText(type.substring(0, 2).toUpperCase());
			}
		}
	}

	/**
	 * Cannot enable TypeWidget's toggle button
	 */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(false);
	}

	@Override
	public String toString() {
		return "TypeWidget [" + this.type + "]";
	}
}