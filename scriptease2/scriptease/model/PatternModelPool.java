package scriptease.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.observer.PatternModelPoolEvent;
import scriptease.controller.observer.PatternModelPoolObserver;
import scriptease.gui.SEFrame;
import scriptease.translator.Translator;

/**
 * Very simple model object for storing all of the {@link PatternModel}s present
 * in ScriptEase. A model is designated as the "active" model and there can only
 * be <i>at most</i> one active model at any one time.<br>
 * <br>
 * Interested parties can register themselves with <code>PatternModelPool</code>
 * as {@link PatternModelPoolObserver}s and be notified of changes to the pool
 * when they occur.<br>
 * <br>
 * <code>PatternModelPool</code> is a Singleton class since it seems unlikely that
 * we will ever need more than one pool per application instance.
 * 
 * @author remiller
 * @author kschenk
 */
public final class PatternModelPool {
	private final List<PatternModel> models;
	private final List<WeakReference<PatternModelPoolObserver>> observers;
	private PatternModel activeModel;

	private final static PatternModelPool instance = new PatternModelPool();

	/**
	 * Gets the sole instance of the PatternModelPool.
	 * 
	 * @return the PatternModel pool
	 */
	public static PatternModelPool getInstance() {
		return PatternModelPool.instance;
	}

	/**
	 * Builds a new PatternModelPool that has no active model
	 */
	private PatternModelPool() {
		this.models = new ArrayList<PatternModel>();
		this.observers = new ArrayList<WeakReference<PatternModelPoolObserver>>();
		this.activeModel = null;
	}

	/**
	 * Gets the currently active model.
	 * 
	 * @return The active model. This can be null if no model has yet been
	 *         activated.
	 */
	public PatternModel getActiveModel() {
		return this.activeModel;
	}

	/**
	 * Gets a collection of all of the models in the pool.
	 * 
	 * @return A collection of all of the models in the pool.
	 */
	public Collection<PatternModel> getModels() {
		return new ArrayList<PatternModel>(this.models);
	}

	/**
	 * Returns if the given Translator is being used by any of the models
	 * 
	 * @param translator
	 * @return
	 */
	public boolean usingTranslator(Translator translator) {
		for (PatternModel model : this.models) {
			if (model.getTranslator() == translator)
				return true;
		}
		return false;
	}

	/**
	 * Gets whether or not the model pool has an active model.
	 * 
	 * @return True if there is an active model.
	 */
	public boolean hasActiveModel() {
		return this.activeModel != null;
	}

	/**
	 * Adds the given <code>PatternModel</code> to the model pool without
	 * activating it.
	 * 
	 * @param model
	 *            The model to add.
	 * @see #add(PatternModel, boolean)
	 */
	public void add(PatternModel model) {
		this.add(model, false);
	}

	/**
	 * Adds the given <code>PatternModel</code> to the model pool and immediately
	 * activates it if <code>activate</code> is true.
	 * 
	 * @param model
	 *            The model to add.
	 * @param activate
	 *            Set this to true if the added model should also become the
	 *            active model or false to simply add the model without
	 *            activating it.
	 * @see #add(PatternModel)
	 */
	public void add(PatternModel model, boolean activate) {
		if (this.models.add(model))
			this.notifyChange(model, PatternModelPoolEvent.PATTERN_MODEL_ADDED);

		if (activate)
			this.activate(model);
	}

	/**
	 * Removes the given <code>PatternModel</code> from the model pool.
	 * 
	 * @param model
	 *            The model to remove.
	 */
	public void remove(PatternModel model) {
		if (this.activeModel == model)
			this.activeModel = null;
		if (this.models.remove(model))
			this.notifyChange(model, PatternModelPoolEvent.PATTERN_MODEL_REMOVED);
	}

	/**
	 * Sets the given <code>PatternModel</code> to be the active model.
	 * 
	 * @param model
	 *            The model to activate.
	 */
	public void activate(PatternModel model) {
		if (this.activeModel == model)
			return;
		this.activeModel = model;
		if (model != null)
			SEFrame.getInstance().setStatus(model + " activated");

		this.notifyChange(model, PatternModelPoolEvent.PATTERN_MODEL_ACTIVATED);
	}

	/**
	 * Adds a PatternModelPoolObserver to this pool's list of observers to notify
	 * when a change to the pool occurs.
	 * 
	 * @param observer
	 *            the listener to add
	 */
	public void addPoolChangeObserver(PatternModelPoolObserver observer) {
		this.observers.add(new WeakReference<PatternModelPoolObserver>(observer));
	}

	/**
	 * Removes a specific PatternModelPoolObserver from this pool's list of
	 * observers to notify when a change to the pool occurs.
	 * 
	 * @param observer
	 *            the listener to remove
	 */
	public void removePoolChangeObserver(PatternModelPoolObserver observer) {
		for (WeakReference<PatternModelPoolObserver> reference : this.observers) {
			if (reference.get() == observer) {
				this.observers.remove(reference);
				return;
			}
		}
	}

	private void notifyChange(PatternModel model, short eventType) {
		Collection<WeakReference<PatternModelPoolObserver>> observersCopy = new ArrayList<WeakReference<PatternModelPoolObserver>>(
				this.observers);

		for (WeakReference<PatternModelPoolObserver> observerRef : observersCopy) {
			PatternModelPoolObserver patternModelPoolObserver = observerRef.get();
			if (patternModelPoolObserver != null)
				patternModelPoolObserver.modelChanged(new PatternModelPoolEvent(
						model, eventType));
		}
	}
}
