package scriptease.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.observer.StoryModelPoolEvent;
import scriptease.controller.observer.StoryModelPoolObserver;
import scriptease.gui.SEFrame;
import scriptease.translator.Translator;

/**
 * Very simple model object for storing all of the {@link StoryModel}s present
 * in ScriptEase. A model is designated as the "active" model and there can only
 * be <i>at most</i> one active model at any one time.<br>
 * <br>
 * Interested parties can register themselves with <code>StoryModelPool</code>
 * as {@link StoryModelPoolObserver}s and be notified of changes to the pool
 * when they occur.<br>
 * <br>
 * <code>StoryModelPool</code> is a Singleton class since it seems unlikely that
 * we will ever need more than one pool per application instance.
 * 
 * @author remiller
 */
public final class StoryModelPool {
	private final List<StoryModel> models;
	private final List<WeakReference<StoryModelPoolObserver>> observers;
	private StoryModel activeModel;

	private final static StoryModelPool instance = new StoryModelPool();

	/**
	 * Gets the sole instance of the StoryModelPool.
	 * 
	 * @return the StoryModel pool
	 */
	public static StoryModelPool getInstance() {
		return StoryModelPool.instance;
	}

	/**
	 * Builds a new StoryModelPool that has no active model
	 */
	private StoryModelPool() {
		this.models = new ArrayList<StoryModel>();
		this.observers = new ArrayList<WeakReference<StoryModelPoolObserver>>();
		this.activeModel = null;
	}

	/**
	 * Gets the currently active model.
	 * 
	 * @return The active model. This can be null if no model has yet been
	 *         activated.
	 */
	public StoryModel getActiveModel() {
		return this.activeModel;
	}

	/**
	 * Gets a collection of all of the models in the pool.
	 * 
	 * @return A collection of all of the models in the pool.
	 */
	public Collection<StoryModel> getModels() {
		return new ArrayList<StoryModel>(this.models);
	}

	/**
	 * Returns if the given Translator is being used by any of the models
	 * 
	 * @param translator
	 * @return
	 */
	public boolean usingTranslator(Translator translator) {
		for (StoryModel model : this.models) {
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
	 * @see #add(StoryModel, boolean)
	 */
	public void add(StoryModel model) {
		this.add(model, false);
	}

	/**
	 * Adds the given <code>StoryModel</code> to the model pool and immediately
	 * activates it if <code>activate</code> is true.
	 * 
	 * @param model
	 *            The model to add.
	 * @param activate
	 *            Set this to true if the added model should also become the
	 *            active model or false to simply add the model without
	 *            activating it.
	 * @see #add(StoryModel)
	 */
	public void add(StoryModel model, boolean activate) {
		if (this.models.add(model))
			this.notifyChange(model, StoryModelPoolEvent.STORY_MODEL_ADDED);

		if (activate)
			this.activate(model);
	}

	/**
	 * Removes the given <code>StoryModel</code> from the model pool.
	 * 
	 * @param model
	 *            The model to remove.
	 */
	public void remove(StoryModel model) {
		if (this.activeModel == model)
			this.activeModel = null;
		if (this.models.remove(model))
			this.notifyChange(model, StoryModelPoolEvent.STORY_MODEL_REMOVED);
	}

	/**
	 * Sets the given <code>StoryModel</code> to be the active model.
	 * 
	 * @param model
	 *            The model to activate.
	 */
	public void activate(StoryModel model) {
		if (this.activeModel == model)
			return;
		this.activeModel = model;
		if (model != null)
			SEFrame.getInstance().setStatus(model + " activated");

		this.notifyChange(model, StoryModelPoolEvent.STORY_MODEL_ACTIVATED);
	}

	/**
	 * Adds a StoryModelPoolObserver to this pool's list of observers to notify
	 * when a change to the pool occurs.
	 * 
	 * @param observer
	 *            the listener to add
	 */
	public void addPoolChangeObserver(StoryModelPoolObserver observer) {
		this.observers.add(new WeakReference<StoryModelPoolObserver>(observer));
	}

	/**
	 * Removes a specific StoryModelPoolObserver from this pool's list of
	 * observers to notify when a change to the pool occurs.
	 * 
	 * @param observer
	 *            the listener to remove
	 */
	public void removePoolChangeObserver(StoryModelPoolObserver observer) {
		for (WeakReference<StoryModelPoolObserver> reference : this.observers) {
			if (reference.get() == observer) {
				this.observers.remove(reference);
				return;
			}
		}
	}

	private void notifyChange(StoryModel model, short eventType) {
		Collection<WeakReference<StoryModelPoolObserver>> observersCopy = new ArrayList<WeakReference<StoryModelPoolObserver>>(
				this.observers);

		for (WeakReference<StoryModelPoolObserver> observerRef : observersCopy) {
			StoryModelPoolObserver storyModelPoolObserver = observerRef.get();
			if (storyModelPoolObserver != null)
				storyModelPoolObserver.modelChanged(new StoryModelPoolEvent(
						model, eventType));
		}
	}
}
