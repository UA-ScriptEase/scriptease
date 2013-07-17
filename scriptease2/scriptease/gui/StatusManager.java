package scriptease.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import scriptease.controller.observer.StatusObserver;
import scriptease.util.StringOp;

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
	private static final double DEFAULT_DELAY = 3.5;
	private final static StatusManager instance = new StatusManager();

	private final List<StatusObserver> observers;

	private String text;

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
		this.text = "";
	}

	/**
	 * Sets the Status Label to the given message. It will be cleared when the
	 * manager is either {@link #clear()}ed or set to something else.
	 * 
	 * @param text
	 *            The message to display.
	 */
	public void set(final String text) {
		this.text = text;

		if (StringOp.exists(text))
			System.out.println(text);

		this.notifyObservers();
	}

	/**
	 * Sets the status temporarily to the passed in text. It will be cleared
	 * after the {@link #DEFAULT_DELAY} of 3.5 seconds passes.
	 * 
	 * @param text
	 */
	public void setTemp(final String text) {
		this.setTemp(text, DEFAULT_DELAY);
	}

	/**
	 * Sets the status temporarily to the passed in text for the passed in
	 * seconds. It will be cleared after "delay" seconds.
	 * 
	 * @param text
	 * @param delay
	 */
	public void setTemp(String text, double delay) {
		this.set(text);
		this.createClearTimer(text, delay).start();
	}

	/**
	 * Clears the status regardless of the passed in text.
	 */
	public void clear() {
		this.set("");
	}

	/**
	 * Clears the status if the text matches the passed in text.
	 * 
	 * @param text
	 */
	public void clear(final String text) {
		if (this.text.equals(text))
			this.clear();
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
	private void notifyObservers() {
		for (StatusObserver observer : this.observers) {
			observer.statusChanged(this.text);
		}
	}

	/**
	 * Creates a timer that will clear the text if it matches the passed in text
	 * value after the passed in number of seconds pass.
	 * 
	 * @param text
	 * @param delay
	 * @return
	 */
	private Timer createClearTimer(final String text, double delay) {
		final int delayInMilliseconds = (int) delay * 1000;

		final Timer timer;

		timer = new Timer(delayInMilliseconds, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				StatusManager.this.clear(text);
			}
		});

		timer.setRepeats(false);

		return timer;
	}

}
