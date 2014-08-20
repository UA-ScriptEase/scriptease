package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.util.StringOp;

/**
 * Abstract Class that defines all pattern-related model components in
 * ScriptEase.<br>
 * <br>
 * <code>StoryComponents</code> have the following properties:<br>
 * <br>
 * <ol>
 * 
 * <li><b>Display Text</b> - The displayText is that which is parameterized so
 * that segments of the string can be filled in dynamically, using the parameter
 * bindings. i.e. <blockquote>When &lt;p1&gt; is used by &lt;p2&gt;
 * </blockquote> becomes <blockquote>When <u>Sword of Ungainly Smiting</u> is
 * used by <u>the PC</u></blockquote></li>
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
 * @author kschenk
 * @author jyuen
 */
public abstract class StoryComponent implements Cloneable {
	/**
	 * This is the user-facing type for story components.
	 * 
	 * @author kschenk
	 * 
	 */
	public enum Type {
		CAUSE("Cause"), EFFECT("Effect"), DESCRIPTION("Description"), BLOCK(
				"Block"), ACTIVITY("Activity", "Activities"), CONTROL("Control"), BEHAVIOUR(
				"Behaviour"), NOTE("Note");

		private final String readableName;
		private final String pluralName;

		private Type(String readableName) {
			this(readableName, "");
		}

		private Type(String readableName, String pluralName) {
			this.readableName = readableName;
			this.pluralName = pluralName;
		}

		public String getReadableName() {
			return this.readableName;
		}

		public String getReadableNamePlural() {
			if (StringOp.exists(pluralName))
				return this.pluralName;
			else
				return this.readableName + "s";
		}

		public static Type getType(StoryComponent component) {
			if (component instanceof CauseIt)
				return CAUSE;
			else if (component instanceof ActivityIt)
				return ACTIVITY;
			else if (component instanceof Note)
				return NOTE;
			else if (component instanceof Behaviour)
				return BEHAVIOUR;
			else if (component instanceof ControlIt)
				if (((ControlIt) component).getFormat() == ControlIt.ControlItFormat.BLOCK)
					return BLOCK;
				else
					return CONTROL;
			else if (component instanceof KnowIt)
				return DESCRIPTION;
			else if (component instanceof ScriptIt)
				return EFFECT;
			else
				return null;
		}
	}

	private String description;
	private String displayText;
	private Set<String> labels;
	private Boolean isVisible;
	private Boolean isEnabled;
	private boolean hasProblems = false;

	private LibraryModel library;

	public StoryComponent ownerComponent;

	private ObserverManager<StoryComponentObserver> observerManager;

	private static final Set<Class<? extends StoryComponent>> noValidChildren = new HashSet<Class<? extends StoryComponent>>();
	public static final String BLANK_TEXT = "";
	public static final String DISABLE_TEXT = "DISABLED";

	protected StoryComponent(LibraryModel library) {
		this(library, StoryComponent.BLANK_TEXT);
	}

	protected StoryComponent(LibraryModel library, String name) {
		this.init();
		this.displayText = name;
		this.library = library;
	}

	/**
	 * Initializes the instance variables in an AbstractStoryComponent. If the
	 * instance variables were already initialized, then the old values will be
	 * lost. Subclasses that introduce new non-<code>StoryComponent</code>
	 * instance variables should initialize them in this method.<br>
	 * <br>
	 * This should be called only when creating new story components. <br>
	 * <br>
	 * Normally, instance variables should initialized in constructors. However,
	 * we require deep cloning (cloning where the instance variables are also
	 * cloned). This method allows us to init a clone to use new instances of
	 * its instance variables in the exact same way that constructors do.
	 */
	protected void init() {
		this.ownerComponent = null;
		this.observerManager = new ObserverManager<StoryComponentObserver>();
		this.description = StoryComponent.BLANK_TEXT;
		this.displayText = StoryComponent.BLANK_TEXT;
		this.labels = new HashSet<String>();
		this.isVisible = true;
		this.isEnabled = true;
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
	public StoryComponent getOwner() {
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
	 * would be <b>protected</b>, but because of Java's stupid package hierarchy
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
		return new HashSet<Class<? extends StoryComponent>>(
				StoryComponent.noValidChildren);
	}

	/**
	 * Gets the display text stored for this <code>StoryComponent</code>.
	 * 
	 * @return The parameterized display text string.
	 */
	public String getDisplayText() {
		return this.displayText;
	}

	public void setDescription(String description) {
		this.description = description;
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_TEXT_DESCRIPTION));
	}

	public String getDescription() {
		return this.description;
	}

	/**
	 * Gets the visibility of the StoryComponent
	 * 
	 * @return
	 */
	public Boolean isVisible() {
		return this.isVisible;
	}

	/**
	 * Returns whether the StoryComponent is enabled
	 * 
	 * @return
	 */
	public Boolean isEnabled() {
		return this.isEnabled;
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
				StoryComponentChangeEnum.CHANGE_LABELS_CHANGED));
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
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_LABELS_CHANGED));
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
					StoryComponentChangeEnum.CHANGE_LABELS_CHANGED));
	}

	/**
	 * Sets the labels to the passed in labels.
	 * 
	 * @param labels
	 */
	public void setLabels(Collection<String> labels) {
		final Set<String> labelSet = new HashSet<String>();
		labelSet.addAll(labels);
		this.labels = labelSet;
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_LABELS_CHANGED));
	}

	public void setHasProblems(boolean hasProblems) {
		this.hasProblems = hasProblems;

		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_PROBLEMS_SET));
	}

	public boolean hasProblems() {
		return this.hasProblems;
	}

	/**
	 * Sets the display text to the new string.
	 * 
	 * @param newDisplayText
	 *            The parameterized string to use as the new display text.
	 */
	public void setDisplayText(String newDisplayText) {
		this.displayText = newDisplayText;
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_TEXT_NAME));
	}

	/**
	 * Sets the visibility to the boolean.
	 * 
	 * @param isVisible
	 */
	public void setVisible(Boolean isVisible) {
		this.isVisible = isVisible;
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_VISIBILITY));
	}

	public void setEnabled(Boolean enable) {
		this.isEnabled = enable;

		if (!enable && !this.getLabels().contains(StoryComponent.DISABLE_TEXT))
			this.addLabel(StoryComponent.DISABLE_TEXT);
		else if (enable
				&& this.getLabels().contains(StoryComponent.DISABLE_TEXT))
			this.removeLabel(StoryComponent.DISABLE_TEXT);

		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_DISABILITY));
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
		this.observerManager.addObserver(this, observer);
	}

	/**
	 * Registers an instance of <code>StoryComponentObserver</code> as an
	 * observer of this <code>StoryComponent</code>. The given observer will be
	 * notified of changes made to this <code>StoryComponent</code>'s
	 * properties.<br>
	 * <br>
	 * This observer will only remain active for the lifetime of the reference
	 * object.
	 * 
	 * @param observer
	 *            The observer who will be notified of changes
	 */
	public final void addStoryComponentObserver(Object reference,
			StoryComponentObserver observer) {
		this.observerManager.addObserver(reference, observer);
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
		this.observerManager.removeObserver(observer);
	}

	/**
	 * Notify observers of this story component that there have been changes
	 * made to this component.
	 * 
	 * @param event
	 *            The information about the change made to this story component
	 * 
	 * @see StoryComponentEvent
	 */
	public final void notifyObservers(StoryComponentEvent event) {
		for (StoryComponentObserver observer : this.observerManager
				.getObservers())
			observer.componentChanged(event);
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
		clone.setDescription(new String(this.description));
		clone.setVisible(this.isVisible);
		clone.setOwner(this.ownerComponent);
		clone.setLibrary(this.library);

		// add all of the labels
		for (String label : this.labels) {
			clone.addLabel(new String(label));
		}

		if (clone.ownerComponent != null && !clone.ownerComponent.isEnabled)
			clone.setEnabled(false);
		else
			clone.setEnabled(this.isEnabled);

		return clone;
	}

	@Override
	public boolean equals(Object other) {
		return this.isEquivalent(other);
	}

	/**
	 * Returns whether the two story components have attributes. Not as picky as
	 * {@link #equals(Object)}.
	 * 
	 * @param other
	 * @return
	 */
	public boolean isEquivalent(Object other) {
		StoryComponent comp;
		boolean equal = (this == other);

		// continue if we're not equal yet
		if (!equal && other instanceof StoryComponent
				&& (other.getClass().equals(this.getClass()))) {
			comp = (StoryComponent) other;
			equal = comp.getDisplayText().equals(this.getDisplayText());
			equal &= comp.isVisible == this.isVisible;
			for (String label : comp.getLabels()) {
				equal &= this.labels.contains(label);
			}
		}

		return equal;
	}

	/**
	 * Removes the observer from the Story Component and all of its children.
	 * 
	 * @param observer
	 */
	public void removeStoryComponentObserverFromChildren(
			final StoryComponentObserver observer) {
		this.process(new StoryAdapter() {
			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				complex.removeStoryComponentObserver(observer);
				for (StoryComponent child : complex.getChildren()) {
					child.process(this);
				}
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer storyComponentContainer) {
				storyComponentContainer.removeStoryComponentObserver(observer);
				this.defaultProcessComplex(storyComponentContainer);
			}

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				scriptIt.removeStoryComponentObserver(observer);
				scriptIt.processParameters(this);
				scriptIt.processChildren(this);
				scriptIt.processSubjects(this);
			}

			@Override
			public void processKnowIt(KnowIt knowIt) {
				knowIt.removeStoryComponentObserver(observer);
			}

			@Override
			public void processAskIt(AskIt askIt) {
				askIt.removeStoryComponentObserver(observer);
				askIt.getCondition().process(this);
				this.defaultProcessComplex(askIt);
			}
		});
	}

	public void setLibrary(LibraryModel library) {
		this.library = library;
	}

	public LibraryModel getLibrary() {
		final StoryComponent owner = this.getOwner();
		if (this.library == null && owner != null)
			return owner.getLibrary();
		else
			return this.library;
	}

	/**
	 * This is a double-dispatch hook for the
	 * {@link scriptease.controller.StoryVisitor} family of controllers.
	 * <code>visitor</code> implements each of: process[X] where [X] is each of
	 * the leaf members of the <code>StoryComponent</code> family. <BR>
	 * <BR>
	 * To Use: Pass in a valid StoryVisitor to this method. The implementing
	 * atom of this method will dispatch the appropriate
	 * <code>StoryVisitor</code> method for the atom's type. Voila! Double
	 * dispatch! :-)
	 * 
	 * @param visitor
	 *            The <code>StoryVisitor</code> that will process this
	 *            StoryComponent.
	 */
	public abstract void process(StoryVisitor visitor);

	/**
	 * Checks all bindings that may exist and removes them if they are invalid.
	 * We need to do this, or else.
	 */
	public abstract void revalidateKnowItBindings();

}
