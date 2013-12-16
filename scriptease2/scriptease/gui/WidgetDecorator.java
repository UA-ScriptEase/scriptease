package scriptease.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import scriptease.gui.ui.ScriptEaseUI;
import scriptease.util.GUIOp;

/**
 * This decorator decorates JComponents for common functionality, such as a
 * JTextField that updates its contents based on focus and pressing enter. This
 * class is a singleton.
 * 
 * @author kschenk
 * 
 */
public class WidgetDecorator {
	/**
	 * Sets the pane's divider to a more simple one.
	 * 
	 * @param pane
	 */
	public static void setSimpleDivider(JSplitPane pane) {
		for (Component component : pane.getComponents()) {
			if (component instanceof BasicSplitPaneDivider) {
				final BasicSplitPaneDivider divider;
				divider = (BasicSplitPaneDivider) component;
				divider.setBackground(ScriptEaseUI.SECONDARY_UI);
				divider.setBorder(null);
				break;
			}
		}
	}

	/**
	 * Adds focus and action listeners to a JTextField so that it commits its
	 * text to the model when focus is lost or enter is pressed. Also gives the
	 * JTextField a red border when commits have not been saved to the model.
	 * Automatically resizes to fit the text.
	 * 
	 * @param textField
	 *            The JTextField to decorate.
	 * @param commitText
	 *            This runnable defines how the TextField will commit its text
	 *            to the model.
	 * @return
	 */
	public static void decorateJTextFieldForFocusEvents(
			final JTextField textField, final Runnable commitText) {
		WidgetDecorator.decorateJTextFieldForFocusEvents(textField, commitText,
				true);
	}

	/**
	 * Adds focus and action listeners to a JTextField so that it commits its
	 * text to the model when focus is lost or enter is pressed. Also gives the
	 * JTextField a red border when commits have not been saved to the model.
	 * 
	 * @param textField
	 *            The JTextField to decorate.
	 * @param commitText
	 *            This runnable defines how the TextField will commit its text
	 *            to the model.
	 * @param resizing
	 *            Determines if the JTextField should resize itself or if the
	 *            caller will handle it.
	 * @return
	 */
	public static void decorateJTextFieldForFocusEvents(
			final JTextField textField, final Runnable commitText,
			final boolean resizing) {
		WidgetDecorator.decorateJTextFieldForFocusEvents(textField, commitText,
				resizing, Color.WHITE);
	}

	/**
	 * Adds focus and action listeners to a JTextField so that it commits its
	 * text to the model when focus is lost or enter is pressed. Also gives the
	 * JTextField a red border when commits have not been saved to the model.
	 * 
	 * @param textField
	 *            The JTextField to decorate.
	 * @param commitText
	 *            This runnable defines how the TextField will commit its text
	 *            to the model.
	 * @param resizing
	 *            Determines if the JTextField should resize itself or if the
	 *            caller will handle it.
	 * @param color
	 *            The color to set the background of the JTextField.
	 * @return
	 */
	public static void decorateJTextFieldForFocusEvents(
			final JTextField textField, final Runnable commitText,
			final boolean resizing, final Color color) {

		final Border defaultBorder;

		defaultBorder = textField.getBorder();

		textField.setBackground(color);
		textField.setHorizontalAlignment(JTextField.CENTER);

		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				commitText.run();
				textField.setBorder(defaultBorder);
			}

			@Override
			public void focusGained(FocusEvent e) {
				textField.setBorder(BorderFactory
						.createLineBorder(Color.RED, 1));
			}
		});

		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowFactory.getInstance().getCurrentFrame()
						.requestFocusInWindow();
			}
		});

		if (resizing) {
			textField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					GUIOp.resizeJTextField(textField);
				}
			});

			GUIOp.resizeJTextField(textField);
		}
	}
}
