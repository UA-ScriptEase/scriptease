package scriptease.model.semodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.gui.StatusManager;
import scriptease.gui.WindowFactory;
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
	private final Set<SEModel> models;
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
		this.models = new HashSet<SEModel>();
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
	 * Adds the given <code>PatternModel</code> to the model pool and
	 * immediately activates it.
	 * 
	 * @param model
	 *            The model to add.
	 */
	public void addAndActivate(final SEModel model) {
		if (this.models.add(model))
			this.notifyChange(model, SEModelEvent.Type.ADDED);

		// We need to wait for all observers to be notified before calling
		// this, or we'll end up notifying them while they're being
		// notified. Otherwise we get a ConcurrentThreadException.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SEModelManager.this.activate(model);
			}
		});
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
			this.notifyChange(model, SEModelEvent.Type.REMOVED);
	}

	/**
	 * Sets the given <code>PatternModel</code> to be the active model.
	 * 
	 * @param model
	 *            The model to activate.
	 */
	public void activate(final SEModel model) {
		if (this.activeModel == model)
			return;
		this.activeModel = model;

		if (model != null && this.models.contains(model)) {
			StatusManager.getInstance().setStatus(model + " activated");
			WindowFactory.showProgressBar("Activating " + model.getName()
					+ "...", new Runnable() {

				@Override
				public void run() {
					SEModelManager.this.notifyChange(model,
							SEModelEvent.Type.ACTIVATED);
				}
			});
		} else {
			throw new IllegalArgumentException("Model " + model
					+ " not found in list of active models. "
					+ "Could not activate it.");
		}
	}

	/**
	 * Adds a PatternModelPoolObserver to this pool's list of observers to
	 * notify when a change to the pool occurs.
	 * 
	 * @param observer
	 *            the listener to add
	 */
	public void addSEModelObserver(Object object, SEModelObserver value) {
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

	private void notifyChange(SEModel model, SEModelEvent.Type eventType) {
		for (SEModelObserver observer : this.observerManager.getObservers()) {
			observer.modelChanged(new SEModelEvent(model, eventType));
		}
	}

	public boolean hasModel(final SEModel aModel) {
		for (final SEModel model : this.models) {
			if (aModel == model)
				return true;
		}
		return false;
	}
}
