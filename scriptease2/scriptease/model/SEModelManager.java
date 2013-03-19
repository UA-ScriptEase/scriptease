package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.gui.StatusManager;
import scriptease.translator.Translator;

/**
 * Very simple model object for storing all of the {@link SEModel}s present in
 * ScriptEase. A model is designated as the "active" model and there can only be
 * <i>at most</i> one active model at any one time.<br>
 * <br>
 * Interested parties can register themselves with <code>PatternModelPool</code>
 * as {@link SEModelObserver}s and be notified of changes to the pool when they
 * occur.<br>
 * <br>
 * <code>PatternModelPool</code> is a Singleton class since it seems unlikely
 * that we will ever need more than one pool per application instance.
 * 
 * @author remiller
 * @author kschenk
 */
public final class SEModelManager {
	private final List<SEModel> models;
	private final ObserverManager<SEModelObserver> observerManager;
	private SEModel activeModel;

	private final static SEModelManager instance = new SEModelManager();

	/**
	 * Gets the sole instance of the PatternModelPool.
	 * 
	 * @return the PatternModel pool
	 */
	public static SEModelManager getInstance() {
		return SEModelManager.instance;
	}

	/**
	 * Builds a new SEModelManager that has no active model
	 */
	private SEModelManager() {
		this.models = new ArrayList<SEModel>();
		this.observerManager = new ObserverManager<SEModelObserver>();
		this.activeModel = null;
	}

	/**
	 * Gets the currently active model.
	 * 
	 * @return The active model. This can be null if no model has yet been
	 *         activated.
	 */
	public SEModel getActiveModel() {
		return this.activeModel;
	}

	/**
	 * Gets a collection of all of the models in the pool.
	 * 
	 * @return A collection of all of the models in the pool.
	 */
	public Collection<SEModel> getModels() {
		return new ArrayList<SEModel>(this.models);
	}

	/**
	 * Returns if the given Translator is being used by any of the models
	 * 
	 * @param translator
	 * @return
	 */
	public boolean usingTranslator(Translator translator) {
		for (SEModel model : this.models) {
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
	 * @see #add(SEModel, boolean)
	 */
	public void add(SEModel model) {
		this.add(model, false);
	}

	/**
	 * Adds the given <code>PatternModel</code> to the model pool and
	 * immediately activates it if <code>activate</code> is true.
	 * 
	 * @param model
	 *            The model to add.
	 * @param activate
	 *            Set this to true if the added model should also become the
	 *            active model or false to simply add the model without
	 *            activating it.
	 * @see #add(SEModel)
	 */
	public void add(SEModel model, boolean activate) {
		if (this.models.add(model))
			this.notifyChange(model, PatternModelEvent.PATTERN_MODEL_ADDED);

		if (activate)
			this.activate(model);
	}

	/**
	 * Removes the given <code>PatternModel</code> from the model pool.
	 * 
	 * @param model
	 *            The model to remove.
	 */
	public void remove(SEModel model) {
		if (this.activeModel == model)
			this.activeModel = null;
		if (this.models.remove(model))
			this.notifyChange(model, PatternModelEvent.PATTERN_MODEL_REMOVED);
	}

	/**
	 * Sets the given <code>PatternModel</code> to be the active model.
	 * 
	 * @param model
	 *            The model to activate.
	 */
	public void activate(SEModel model) {
		if (this.activeModel == model)
			return;
		this.activeModel = model;
		if (model != null)
			StatusManager.getInstance().setStatus(model + " activated");

		this.notifyChange(model, PatternModelEvent.PATTERN_MODEL_ACTIVATED);
	}

	/**
	 * Adds a PatternModelPoolObserver to this pool's list of observers to
	 * notify when a change to the pool occurs.
	 * 
	 * @param observer
	 *            the listener to add
	 */
	public void addPatternModelObserver(Object object, SEModelObserver value) {
		this.observerManager.addObserver(object, value);
	}

	/**
	 * Removes a specific PatternModelPoolObserver from this pool's list of
	 * observers to notify when a change to the pool occurs.
	 * 
	 * @param observer
	 *            the listener to remove
	 */
	public void removePatternModelObserver(SEModelObserver observer) {
		this.observerManager.removeObserver(observer);
	}

	private void notifyChange(SEModel model, short eventType) {
		for (SEModelObserver observer : this.observerManager.getObservers())
			observer.modelChanged(new PatternModelEvent(model, eventType));
	}
}
