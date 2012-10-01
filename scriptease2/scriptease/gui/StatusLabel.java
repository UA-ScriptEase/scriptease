package scriptease.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import scriptease.controller.observer.ObserverFactory;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.translator.TranslatorManager;

/**
 * The status bar. This is a singleton class. The status bar displays important
 * information when a status is passed to it.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 */

@SuppressWarnings("serial")
public class StatusLabel extends JPanel {

	private final TimedLabel statusLabel;

	private static final StatusLabel instance = new StatusLabel();

	/**
	 * Gets the sole instance of SEFrame.
	 * 
	 * @return
	 */
	public static final StatusLabel getInstance() {
		return StatusLabel.instance;
	}

	private StatusLabel() {
		super();

		this.statusLabel = new TimedLabel(1000, 3500);

		final String transPrefix = "Game: ";

		final JLabel currentTranslatorLabel;
		final JLabel currentTranslatorNameLabel;
		final TranslatorObserver translatorObserver;

		currentTranslatorLabel = new JLabel(transPrefix);
		currentTranslatorNameLabel = new JLabel("-None-");

		translatorObserver = ObserverFactory.getInstance()
				.buildStatusBarTranslatorObserver(currentTranslatorLabel);

		TranslatorManager.getInstance().addTranslatorObserver(
				translatorObserver);

		currentTranslatorNameLabel.setEnabled(false);
		currentTranslatorNameLabel.setBorder(BorderFactory.createEmptyBorder(0,
				5, 0, 5));

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		this.add(this.statusLabel);
		this.add(Box.createGlue());
		this.add(currentTranslatorLabel);
		this.add(currentTranslatorNameLabel);

		this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	}

	/**
	 * Sets the Status Label to the given message
	 * 
	 * @param message
	 *            The message to display.
	 */
	public void setStatus(final String message) {
		this.statusLabel.queueText(message);
	}

	private class TimedLabel extends JLabel {
		private Queue<String> messages;
		final Timer textQueue;
		final Timer textClear;

		public TimedLabel(int QueueTimer, int ClearTimer) {
			super();
			this.messages = new LinkedList<String>();
			this.textQueue = new Timer(QueueTimer, new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (!TimedLabel.this.messages.isEmpty())
						TimedLabel.this.setText(TimedLabel.this.messages.poll());
				};
			});
			this.textClear = new Timer(ClearTimer, new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					TimedLabel.super.setText("");
					TimedLabel.this.messages.clear();
				};
			});

			this.textQueue.setRepeats(false);
			this.textClear.setRepeats(false);
		}

		@Override
		public void setText(String text) {
			super.setText(text);
			if (this.textQueue != null)
				this.textQueue.restart();
			if (this.textClear != null)
				this.textClear.restart();
		};

		private void queueText(String text) {
			this.messages.add(text);
			if (this.getText().isEmpty())
				this.setText(text);
		}
	}
}