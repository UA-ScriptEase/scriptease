package scriptease.gui.component;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JToggleButton;

import scriptease.ScriptEase;
import scriptease.controller.MouseForwardingAdapter;
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
	private static final float LABEL_FONT_SIZE_SCALE_FACTOR = 1.1f;

	public TypeWidget(final String type) {
		final int baseFontSize;
		final int fontSize;
		final String typeName;
		final Translator activeTranslator;

		this.setUI(TypeWidgetUI.getInstance());
		this.setEnabled(false);
		this.setFocusable(false);

		// pass events to parent until consumed
		final MouseForwardingAdapter mouseForwardingAdapter;

		mouseForwardingAdapter = MouseForwardingAdapter.getInstance();

		this.addMouseListener(mouseForwardingAdapter);
		this.addMouseMotionListener(mouseForwardingAdapter);

		// drawing settings
		this.setForeground(Color.WHITE);

		baseFontSize = Integer.parseInt(ScriptEase.getInstance().getPreference(
				ScriptEase.FONT_SIZE_KEY));
		fontSize = Math.round(LABEL_FONT_SIZE_SCALE_FACTOR * baseFontSize);
		this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));

		this.setBackground(ScriptEaseUI.COLOUR_GAME_OBJECT);

		this.type = type;

		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		typeName = (activeTranslator != null && activeTranslator
				.defaultLibraryIsLoaded()) ? activeTranslator
				.getGameTypeManager().getDisplayText(this.type) : "";

		this.setTypeText(type);
		this.setToolTipText(typeName);

		this.setSize(this.getPreferredSize());
	}

	private void setTypeText(String type) {
		if (type.equals("")) {
			// Unknown type. Translator is probably not loaded.
			this.setText("!");
			this.setEnabled(false);
		} else {
			final Translator active;

			active = TranslatorManager.getInstance().getActiveTranslator();

			// Need to check these due to order of operations.
			if (active != null && active.defaultLibraryIsLoaded()) {
				final GameTypeManager gameTypeManager;

				gameTypeManager = active.getGameTypeManager();

				if (gameTypeManager != null
						&& gameTypeManager.hasWidgetName(type)) {
					this.setText(gameTypeManager.getWidgetName(type));
				} else {
					if (gameTypeManager != null
							&& gameTypeManager.hasEnum(type))
						this.setText(GameTypeManager.DEFAULT_LIST_WIDGET);
					else
						this.setText(type.substring(0, 2).toUpperCase());
				}
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