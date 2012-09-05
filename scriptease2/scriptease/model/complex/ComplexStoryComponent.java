package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scriptease.controller.StoryAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.StoryComponent;
import sun.awt.util.IdentityArrayList;

public abstract class ComplexStoryComponent extends StoryComponent {
	public static int MAX_NUM_OF_ONE_TYPE = Integer.MAX_VALUE;

	protected final Map<Class<? extends StoryComponent>, Integer> allowableChildMap = new HashMap<Class<? extends StoryComponent>, Integer>();

	// not final to allow for cloning to deep clone
	protected List<StoryComponent> childComponents;

	/************* CONSTRUCTORS ********************/
	public ComplexStoryComponent() {
		this("");
	}

	protected ComplexStoryComponent(String name) {
		super(name);
		this.init();
	}

	/**
	 * Initialises the ComplexStoryComponent variables. This logic is separate
	 * from constructors to allow for Cloning to reset the variables the same
	 * way they are built in the constructor.
	 */
	@Override
	protected void init() {
		super.init();
		this.childComponents = new IdentityArrayList<StoryComponent>();
	}

	/************* IMPORTANT CODE ******************/
	/**
	 * This method is crucial to implement the Composite design pattern that
	 * we're using in the StoryComponent hierarchy. <BR>
	 * <BR>
	 * To Use: Pass a StoryComponent to this method The first StoryComponent
	 * will handle the addition of the second, except in the case where the
	 * first is an atomic element, where an Unsupported Operation will be
	 * thrown.
	 * 
	 * This method calls addStoryChildBefore(newChild, null).
	 * 
	 * @see #getOwner()
	 * @see #addStoryChildBefore(StoryComponent newChild, StoryComponent
	 *      sibling)
	 * 
	 * @param newChild
	 * @return Success = true Failure = false
	 */
	public boolean addStoryChild(StoryComponent newChild) {
		return this.addStoryChildBefore(newChild, null);
	}

	public void addStoryChildren(Collection<StoryComponent> children) {
		for (StoryComponent child : children) {
			this.addStoryChildBefore(child, null);
		}
	}

	/**
	 * This method is crucial to implement the Composite design pattern that
	 * we're using in the StoryComponent hierarchy. <BR>
	 * <BR>
	 * To Use: Pass a StoryComponent to this method. The receiver StoryComponent
	 * will handle the addition of the second at the supplied index, except in
	 * the case where the first is an atomic element, where an Unsupported
	 * Operation will be thrown. <BR>
	 * <BR>
	 * It is <b>critical</b> that implementations of this method also handle
	 * re-parenting <code>newChild</code> correctly to avoid any incorrect data
	 * bugs.
	 * 
	 * @param newChild
	 *            The child to add. Cannot be <code>null</code>.
	 * @param sibling
	 *            The sibling StoryComponent that the child should be added
	 *            before. This is used instead of an index because indexes may
	 *            become inconsistent between components.
	 * 
	 * @see #getOwner()
	 * @see #addStoryChild(StoryComponent newChild)
	 * 
	 * @return Success of the addition.
	 */
	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {
		int siblingIndex = -1;
		final StoryComponent previousOwner = newChild.getOwner();

		if (!this.canAcceptChild(newChild)) {
			System.err.println("ComplexStoryComponent '"
					+ this.getDisplayText() + "' has rejected '" + newChild
					+ "' because it is not a acceptable child type.");
			return false;
		}

		// don't allow double adds.
		if (previousOwner == this && this.hasChild(newChild)) {
			/*System.err.println("ComplexStoryComponent '"
					+ this.getDisplayText() + "' did not add '" + newChild
					+ "' because it is already a child.");*/ //Debug Code
			return false;
		}

		/*
		 * Check if newChild already has an owner that we might be stealing
		 * from. It's okay to take in a child whose parent has abandoned it, but
		 * if the parent still cares for the child it is illegal to assume
		 * ownership. - remiller
		 */
		if (previousOwner != null
				&& previousOwner instanceof ComplexStoryComponent
				&& ((ComplexStoryComponent) previousOwner).hasChild(newChild))
			throw new IllegalArgumentException(this.getDisplayText()
					+ " is kidnapping a child away from its rightful parent.");

		// We're clear now to actually add the new child.
		siblingIndex = this.childComponents.indexOf(sibling);

		if ((siblingIndex < 0) || (sibling == null))
			this.childComponents.add(newChild);
		else
			this.childComponents.add(siblingIndex, newChild);

		newChild.setOwner(this);

		super.notifyObservers(new StoryComponentEvent(newChild,
				StoryComponentChangeEnum.CHANGE_CHILD_ADDED));

		return true;
	}

	public void removeStoryChildren(Collection<StoryComponent> children) {
		for (StoryComponent child : children) {
			this.removeStoryChild(child);
		}
	}

	/**
	 * This method is crucial to implement the Composite design pattern that
	 * we're using in the StoryComponent hierarchy. <BR>
	 * <BR>
	 * To Use: Pass a StoryComponent to via this method. The receiver will check
	 * its children and remove the child, firing the appropriate listeners. The
	 * receiver StoryComponent will handle the addition of <code>child</code>,
	 * except in the case where the receiver is an atomic element, where an
	 * Unsupported Operation will be thrown.
	 * 
	 * @param child
	 *            The child to be removed.
	 * @return Success = true, Failure = false
	 */
	public boolean removeStoryChild(StoryComponent child) {
		boolean success = false;

		if (!this.isValidChild(child))
			return success;

		// Do StoryComponent specific tasks before removal
		child.process(new StoryAdapter() {
			// Notify observers that the component has been removed
			@Override
			protected void defaultProcess(StoryComponent component) {
				component.notifyObservers(new StoryComponentEvent(component,
						StoryComponentChangeEnum.CHANGE_REMOVED));
			}
		});

		success = this.childComponents.remove(child);

		child.setOwner(null);

		if (success)
			super.notifyObservers(new StoryComponentEvent(child,
					StoryComponentChangeEnum.CHANGE_CHILD_REMOVED));

		return success;
	}

	/**
	 * This is a double-dispatch hook for the
	 * {@link scriptease.controller.StoryVisitor} family of controllers.
	 * <code>processController</code> implements each of: process[X] where [X]
	 * is each of the leaf members of the <code>StoryComponent</code> family. <BR>
	 * <BR>
	 * To Use: Pass in a valid StoryVisitor to this method. The implementing
	 * atom of this method will dispatch the appropriate
	 * <code>StoryVisitor</code> method for the atom's type. Voila! Double
	 * dispatch! :-)<br>
	 * <br>
	 * <code>processChildren</code> processes the StoryComponent's children with
	 * {@link StoryComponent#process(StoryVisitor)}.
	 * 
	 * @param processController
	 *            The <code>StoryVisitor</code> that will process this
	 *            StoryComponent's children.
	 * @see StoryComponent#process(StoryVisitor)
	 */
	public final void processChildren(StoryVisitor processController) {
		for (StoryComponent child : this.childComponents) {
			child.process(processController);
		}
	}

	/************* GETTERS/SETTERS ****************/
	/**
	 * Gets the children of this ComplexStoryComponent. The children are
	 * returned in a new list to prevent external access to the child list
	 * beyond knowing what the children are.
	 * 
	 * @return the children of this ComplexStoryComponent
	 */
	public List<StoryComponent> getChildren() {
		return new ArrayList<StoryComponent>(this.childComponents);
	}

	public final void registerChildType(
			Class<? extends StoryComponent> newType, Integer numAllowed) {
		this.allowableChildMap.put(newType, numAllowed);
	}

	public final void clearAllowableChildren() {
		this.allowableChildMap.clear();
	}

	/**
	 * Returns an array of the classes which are allowed to be children of this
	 * component
	 * 
	 * @return An array of Class<StoryComponent> which contains all the types
	 *         that are containable in this component
	 */
	@Override
	public final Set<Class<? extends StoryComponent>> getValidChildTypes() {
		return this.allowableChildMap.keySet();
	}

	/**
	 * Calculates and returns the number of children.
	 * 
	 * @return The number of children of this story component.
	 */
	public int getChildCount() {
		return this.childComponents.size();
	}

	/**
	 * Determines if the receiver contains the <code>child</code>.
	 * 
	 * @return Whether the receiver contains the <code>child</code>
	 */
	public boolean hasChild(StoryComponent child) {
		return this.childComponents.contains(child);
	}

	/**
	 * Returns the child at the given index, otherwise returns null if that
	 * index is invalid
	 * 
	 * @param index
	 * @return
	 */
	public StoryComponent getChildAt(int index) {
		if (index >= 0 && index < this.childComponents.size())
			return this.childComponents.get(index);
		return null;
	}

	/**
	 * Returns the index of the parent's child. -1 if it is not a child of the
	 * parent.
	 * 
	 * @param child
	 * @return
	 */
	public int getChildIndex(StoryComponent child) {
		return this.childComponents.indexOf(child);
	}

	public StoryComponent getChildBefore(StoryComponent child) {
		int index = -1;

		index = this.childComponents.indexOf(child);

		if (index < 1 || index >= this.childComponents.size())
			return null;
		return this.childComponents.get(index - 1);
	}

	public StoryComponent getChildAfter(StoryComponent child) {
		int index = -1;

		index = this.childComponents.indexOf(child);

		if (index < 0 || index >= this.childComponents.size() - 1)
			return null;
		return this.childComponents.get(index + 1);
	}

	/**
	 * Determines whether the receiver could accept <code>potentialChild</code>.
	 * 
	 * @param potentialChild
	 *            The child that should be verified as being an acceptable
	 *            child.
	 * @return Whether the receiver could accept <code>potentialChild</code>
	 */
	public boolean canAcceptChild(StoryComponent potentialChild) {
		return this.isValidChild(potentialChild)
				&& this.roomForMoreOf(potentialChild)
				&& !this.isDescendantOf(potentialChild);
	}

	private boolean isDescendantOf(StoryComponent potentialChild) {
		if (potentialChild == this)
			return true;
		StoryComponent owner = this.ownerComponent;
		while (owner != null) {
			if (potentialChild == owner)
				return true;
			owner = owner.getOwner();
		}
		return false;
	}

	public boolean canAcceptChild(
			Class<? extends StoryComponent> potentialChildClass) {
		return this.isValidChild(potentialChildClass)
				&& this.roomForMoreOf(potentialChildClass);
	}

	@Override
	public boolean equals(Object other) {
		boolean equal = false;
		ComplexStoryComponent comp;

		// It is equal if the contents of its child list is the same as mine.
		if (other instanceof ComplexStoryComponent) {
			comp = (ComplexStoryComponent) other;

			equal = comp.getChildren().equals(this.childComponents);
		}

		equal = equal && super.equals(other);

		return equal;
	}

	@Override
	public ComplexStoryComponent clone() {
		ComplexStoryComponent clone = (ComplexStoryComponent) super.clone();

		clone.allowableChildMap.putAll(this.allowableChildMap);

		for (StoryComponent child : this.childComponents) {
			clone.addStoryChild(child.clone());
		}

		return clone;
	}

	/*************** HELPERS *************************/
	protected final boolean isValidChild(StoryComponent newChild) {
		boolean isValid = true;
		if (newChild == null)
			return false;

		/**
		 * If the newChild is a StoryComponentContainer, make sure it's children
		 * are all valid
		 */
		if (newChild instanceof StoryComponentContainer) {
			for (StoryComponent child : ((StoryComponentContainer) newChild)
					.getChildren()) {
				isValid &= isValidChild(child);
			}
		}

		final Class<? extends StoryComponent> newChildClass = newChild
				.getClass();
		return isValid && this.isValidChild(newChildClass);
	}

	protected final boolean isValidChild(
			Class<? extends StoryComponent> newChildClass) {
		return ((newChildClass != null) && this.allowableChildMap
				.containsKey(newChildClass));
	}

	protected final int allowableNumberOf(Class<? extends StoryComponent> type) {
		return this.allowableChildMap.get(type);
	}

	protected final boolean roomForMoreOf(StoryComponent candidate) {
		return candidate == null ? false : this.roomForMoreOf(candidate
				.getClass());
	}

	protected final boolean roomForMoreOf(
			Class<? extends StoryComponent> candidateType) {
		int allowableNumber = this.allowableNumberOf(candidateType);
		int numberCounted = 0;
		for (StoryComponent currentComponent : this.childComponents) {
			if (currentComponent.getClass() == candidateType) {
				numberCounted++;
			}
		}
		if (numberCounted >= allowableNumber) {
			return false;
		}
		return true;
	}
}
