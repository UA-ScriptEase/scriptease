package scriptease.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * Abstract Class that defines all pattern-related model components in
 * ScriptEase.<br>
 * <br>
 * <code>StoryComponents</code> have the following properties:<br>
 * <br>
 * <ol>
 * 
 * <li><b>Display Text</b> - The displayText is that which is parameterised so
 * that segments of the string can be filled in dynamically, using the parameter
 * bindings. ie <blockquote>When &lt;p1&gt; is used by &lt;p2&gt; </blockquote>
 * becomes <blockquote>When <u>Sword of Ungainly Smiting</u> is used by <u>the
 * PC</u></blockquote></li>
 * 
 * <li><b>Parameters</b> - A collection of {@link KnowIt}s that are used as
 * parameters for times that this StoryComponent requires extra information for
 * the code that it generates. Some StoryComponents that have parameters do not
 * use those parameters in their generated code directly, but instead pass them
 * along to their child StoryComponents. All parameters are either used in code
 * generation, or are ignored entirely.</li><br>
 * <br>
 * 
 * <li><b>Parent</b> - The StoryComponent that contains this StoryComponent.
 * Only {@link ComplexStoryComponent}s can be parents.</li><br>
 * <br>
 * </ol>
 * 
 * @author jtduncan
 * @author remiller
 */
public abstract class StoryComponent implements Cloneable {
	// these instance variables are not final because they need to be rebound in
	// a deep clone
	private String displayText;
	private Collection<String> labels;
	public StoryComponent ownerComponent;
	private Collection<WeakReference<StoryComponentObserver>> observers;

	private static final Set<Class<? extends StoryComponent>> noValidChildren = new HashSet<Class<? extends StoryComponent>>();
	public static final String BLANK_TEXT = "";

	protected StoryComponent() {
		this(StoryComponent.BLANK_TEXT);
	}

	protected StoryComponent(String name) {
		this.init();
		this.displayText = name;
	}

	/**
	 * Initialises the instance variables in an AbstractStoryComponent. If the
	 * instance variables were already initialised, then the old values will be
	 * lost. Subclasses that introduce new non-<code>StoryComponent</code>
	 * instance variables should initialise them in this method.<br>
	 * <br>
	 * This should be called only when creating new story components. <br>
	 * <br>
	 * Normally, instance variables should initialised in constructors. However,
	 * we require deep cloning (cloning where the instance variables are also
	 * cloned). This method allows us to init a clone to use new instances of
	 * its instance variables in the exact same way that constructors do.
	 */
	protected void init() {
		this.ownerComponent = null;
		this.observers = new ArrayList<WeakReference<StoryComponentObserver>>();
		this.displayText = StoryComponent.BLANK_TEXT;
		this.labels = new ArrayList<String>();
	}

	/**
	 * Gets the owner StoryComponent which can either be a Complex or regular
	 * StoryComponent.
	 * 
	 * @see #addStoryChild(StoryComponent newChild)
	 * @see #addStoryChildBefore(StoryComponent newChild, StoryComponent
	 *      sibling)
	 * @return The owner of the receiver StoryComponent.
	 */
	public final StoryComponent getOwner() {
		return this.ownerComponent;
	}

	/**
	 * Sets the owner StoryComponent to the receiver. This can <b>only</b> be
	 * called when the receiver is already a child of <code>newOwner</code>, and
	 * is intended only for the use of adding children.<BR>
	 * <BR>
	 * <u><i>Danger, Will Robinson! Danger!</i></u> <br>
	 * <b>Do not</b> use this method to <b>add</b> a child! It is
	 * <b>critical</b> that child additions are done via
	 * parent.addStoryChild(this) instead for consistency. Ideally this method
	 * would be <b>protected</b>, but because of Java's stupid package heiarchy
	 * decisions it must be public.<BR>
	 * <BR>
	 * 
	 * @param newOwner
	 *            The <code>ComplexStoryComponent</code> that will be set as
	 *            this <code>StoryComponent</code>'s owner.
	 * 
	 * @see #addStoryChild(StoryComponent newChild)
	 * @see #addStoryChildBefore(StoryComponent newChild, StoryComponent
	 *      sibling)
	 */
	public void setOwner(StoryComponent newOwner) {
		this.ownerComponent = newOwner;
	}

	/**
	 * Since most StoryComponents are atomic, and cannot have children, the
	 * default behaviour of this method is to return an empty list.
	 * 
	 * @return List of child types which have been registered with this
	 *         StoryComponent
	 */
	public Set<Class<? extends StoryComponent>> getValidChildTypes() {
		return new HashSet<Class<? extends StoryComponent>>(noValidChildren);
	}

	/**
	 * Gets the display text stored for this <code>StoryComponent</code>.
	 * 
	 * @return The parameterised display text string.
	 */

	public String getDisplayText() {
		return this.displayText;
	}

	/**
	 * Gets the collection of labels for this <code>StoryComponent</code>.
	 * 
	 * @return
	 */
	public Collection<String> getLabels() {
		return new ArrayList<String>(this.labels);
	}

	/**
	 * Adds the given label to this <code>StoryComponent</code>.
	 * 
	 * @param label
	 */
	public void addLabel(String label) {
		this.labels.add(label);
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.LABEL_ADDED));
	}

	/**
	 * Adds the given labels to this <code>StoryComponent</code>.
	 * 
	 * @see addLabel
	 * @param labels
	 */
	public void addLabels(Collection<String> labels) {
		for (String label : labels)
			this.addLabel(label);
	}

	/**
	 * Removes the specified label from this <code>StoryComponent</code>.
	 * 
	 * @param label
	 */
	public void removeLabel(String label) {
		boolean success = this.labels.remove(label);
		if (success)
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.LABEL_REMOVED));
	}

	/**
	 * Sets the display text to the new string.
	 * 
	 * @param newDisplayText
	 *            The parameterised string to use as the new display text.
	 */
	public void setDisplayText(String newDisplayText) {
		this.displayText = newDisplayText;
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_TEXT_NAME));
	}

	/**
	 * Registers an instance of <code>StoryComponentObserver</code> as an
	 * observer of this <code>StoryComponent</code>. The given observer will be
	 * notified of changes made to this <code>StoryComponent</code>'s
	 * properties.<br>
	 * <br>
	 * Once an observer is registered, it cannot be registered again; attempting
	 * to do so will have no effect.
	 * 
	 * @param observer
	 *            The observer who will be notified of changes
	 */
	public final void addStoryComponentObserver(StoryComponentObserver observer) {
		for (WeakReference<StoryComponentObserver> observerRef : this.observers) {
			StoryComponentObserver storyComponentObserver = observerRef.get();
			if (storyComponentObserver != null
					&& storyComponentObserver == observer)
				return;
		}

		this.observers.add(new WeakReference<StoryComponentObserver>(observer));
	}

	/**
	 * Unregisters an observer with the this <code>StoryComponent</code>. The
	 * given observer will no longer be notified of changes made to this
	 * <code>StoryComponent</code>'s properties after this method is called.<br>
	 * <br>
	 * Attempting to unregister a <code>StoryComponentObserver</code> that was
	 * not originally registered to this <code>StoryComponent</code> has no
	 * effect.
	 * 
	 * @param observer
	 *            The observer who will no longer be notified of changes
	 */
	public final void removeStoryComponentObserver(
			StoryComponentObserver observer) {
		for (WeakReference<StoryComponentObserver> reference : this.observers) {
			if (reference.get() == observer) {
				this.observers.remove(reference);
				return;
			}
		}
	}

	public final void notifyObservers(StoryComponentEvent event) {
		Collection<WeakReference<StoryComponentObserver>> observersCopy = new ArrayList<WeakReference<StoryComponentObserver>>(
				this.observers);

		for (WeakReference<StoryComponentObserver> observerRef : observersCopy) {
			StoryComponentObserver storyComponentObserver = observerRef.get();
			if (storyComponentObserver != null)
				storyComponentObserver.componentChanged(event);
		}
	}

	/**
	 * Gets a list of observers who are currently observing this StoryComponent.
	 * This is a copy of the internal list, so changes made to the returned list
	 * will not affect the StoryComponent's state.
	 * 
	 * @return A list of observers who are currently observing this
	 *         StoryComponent.
	 */
	public final Collection<WeakReference<StoryComponentObserver>> getObservers() {
		return new ArrayList<WeakReference<StoryComponentObserver>>(
				this.observers);
	}

	/**
	 * StoryComponent clones are deep clones. <br>
	 * <br>
	 * If a subclass introduces a new non-primitive instance variable, it should
	 * override <code>clone()</code> and update the clone's instance variable to
	 * mirror its own.
	 */
	@Override
	public StoryComponent clone() {
		StoryComponent clone = null;
		try {
			clone = (StoryComponent) super.clone();
		} catch (CloneNotSupportedException e) {
			// I can't think of a better way to deal with this -- remiller
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		}

		// super.clone() is a shallow copy. We don't want to share any
		// parameters or observers with the clone, so we reset the clone to its
		// initial state
		clone.init();

		// make them the same again, now that they're less conjoined.
		clone.setDisplayText(new String(this.displayText));
		clone.setOwner(this.ownerComponent);

		// add all of the labels
		for (String label : this.labels) {
			clone.addLabel(new String(label));
		}

		return clone;
	}

	@Override
	public boolean equals(Object other) {
		StoryComponent comp;
		boolean equal = (this == other);

		// continue if we're not equal yet
		if (!equal && other instanceof StoryComponent
				&& (other.getClass().equals(this.getClass()))) {
			comp = (StoryComponent) other;
			equal = comp.getDisplayText().equals(this.displayText);

			for (String label : comp.getLabels()) {
				equal &= this.labels.contains(label);
			}
		}

		return equal;
	}

	/************** Abstract Methods ******************/

	/**
	 * This is a double-dispatch hook for the
	 * {@link scriptease.controller.StoryVisitor} family of controllers.
	 * <code>processController</code> implements each of: process[X] where [X]
	 * is each of the leaf members of the <code>StoryComponent</code> family. <BR>
	 * <BR>
	 * To Use: Pass in a valid StoryVisitor to this method. The implementing
	 * atom of this method will dispatch the appropriate
	 * <code>StoryVisitor</code> method for the atom's type. Voila! Double
	 * dispatch! :-)
	 * 
	 * @param processController
	 *            The <code>StoryVisitor</code> that will process just this
	 *            StoryComponent.
	 * @see StoryComponent#processRecursively(StoryVisitor)
	 */
	public abstract void process(StoryVisitor processController);
}
