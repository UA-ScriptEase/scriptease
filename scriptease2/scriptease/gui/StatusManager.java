package scriptease.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.Timer;

import scriptease.controller.observer.StatusObserver;

/**
 * All {@link StatusObserver}s need to be registered with the StatusManager in
 * order for them to receive events. The StatusManager fires events when a
 * setStatus is called on it. Note that these observers are strongly referenced;
 * they will not get garbage collected without removing them from the manager
 * first.
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public class StatusManager {
	private final List<StatusObserver> observers;
	private final Queue<String> messages;
	private final Timer textQueue;
	private final Timer textClear;

	private final static StatusManager instance = new StatusManager();

	/**
	 * Gets the sole instance of the StatusManager.
	 * 
	 * @return
	 */
	public static StatusManager getInstance() {
		return StatusManager.instance;
	}

	private StatusManager() {
		this.observers = new ArrayList<StatusObserver>();
		this.messages = new LinkedList<String>();

		final int queueTimerDelay = 1000;
		final int clearTimerDelay = 3500;

		this.textQueue = new Timer(queueTimerDelay, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!messages.isEmpty()) {
					notifyObservers(messages.poll());
					textQueue.restart();
					textClear.restart();
				}
			};
		});

		this.textClear = new Timer(clearTimerDelay, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StatusManager.this.notifyObservers("");
				StatusManager.this.messages.clear();
			};
		});

		this.textQueue.setRepeats(false);
		this.textClear.setRepeats(false);
	}

	/**
	 * Registers an observer to be notified when there is a change in the status
	 * text.
	 * 
	 * @param observer
	 *            the observer to register
	 */
	public void addStatusObserver(StatusObserver observer) {
		this.observers.add(observer);
	}

	/**
	 * Unregisters a status observer
	 * 
	 * @param observer
	 *            the observer to unregister
	 */
	public void removeStatusObserver(StatusObserver observer) {
		this.observers.remove(observer);
	}

	/**
	 * Notifies the observers of a change to the text.
	 * 
	 * @param newText
	 */
	private void notifyObservers(String newText) {
		for (StatusObserver observer : this.observers) {
			observer.statusChanged(newText);
		}
	}

	/**
	 * Sets the Status Label to the given message
	 * 
	 * @param text
	 *            The message to display.
	 */
	public void setStatus(final String text) {
		this.messages.add(text);
		this.notifyObservers(text);
		this.textQueue.restart();
		this.textClear.restart();
	}
}
