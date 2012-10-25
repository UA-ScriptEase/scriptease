package scriptease.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;

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
	private static WidgetDecorator instance = new WidgetDecorator();

	/**
	 * Returns the sole instance of WidgetDecorator.
	 * 
	 * @return
	 */
	public static WidgetDecorator getInstance() {
		return instance;
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
	public void decorateJTextFieldForFocusEvents(final JTextField textField,
			final Runnable commitText, final boolean resizing) {

		final Border defaultBorder;

		defaultBorder = textField.getBorder();

		textField.setBackground(Color.white);
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
