package scriptease.gui.component;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JToggleButton;

import scriptease.ScriptEase;
import scriptease.controller.MouseForwardingAdapter;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.gui.ui.TypeWidgetUI;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.translator.io.model.GameType;
import scriptease.util.StringOp;

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
	private final String type;
	private static final float LABEL_FONT_SIZE_SCALE_FACTOR = 1.1f;

	public TypeWidget(final String type) {
		final int baseFontSize;
		final int fontSize;
		final String typeName;
		final String widgetText;

		final SEModel model;

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

		model = SEModelManager.getInstance().getActiveModel();
		baseFontSize = Integer.parseInt(ScriptEase.getInstance().getPreference(
				ScriptEase.FONT_SIZE_KEY));
		fontSize = Math.round(LABEL_FONT_SIZE_SCALE_FACTOR * baseFontSize);
		this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));

		this.setBackground(ScriptEaseUI.COLOUR_GAME_OBJECT);

		this.type = type;

		if (model != null && StringOp.exists(type)) {
			final String widgetName = model.getTypeWidgetName(type);

			typeName = model.getTypeDisplayText(this.type);

			if (StringOp.exists(widgetName)) {
				widgetText = widgetName;
			} else {
				if (!model.getTypeEnumeratedValues(type).isEmpty())
					widgetText = GameType.DEFAULT_LIST_WIDGET;
				else
					widgetText = type.substring(0, 2).toUpperCase();
			}
		} else {
			widgetText = "!";
			typeName = "Type " + type + " not found.";
		}

		this.setEnabled(false);
		this.setText(widgetText);
		this.setToolTipText(typeName);

		this.setSize(this.getPreferredSize());
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