package scriptease.model.complex.behaviours;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * A task is a series of effects. Each task has a probability of execution
 * depending on it's sibling tasks.
 * 
 * @author jyuen
 * 
 */
public abstract class Task extends ComplexStoryComponent implements
		Comparable<Task> {
	private static int uniqueIDCounter = 0;
	private final int uniqueID;

	private Set<Task> successors;
	private Set<Task> parents;

	private int chance;

	/**
	 * Constructor. Creates a new task with the given name.
	 * 
	 * @param name
	 */
	protected Task(LibraryModel library, String name) {
		super(library, name);

		this.init();
		this.chance = 100;

		this.uniqueID = uniqueIDCounter++;

		// Tasks don't need to have childrens - yet.
		this.registerChildTypes(
				new ArrayList<Class<? extends StoryComponent>>(), 0);
	}

	@Override
	protected void init() {
		super.init();
		this.successors = new HashSet<Task>();
		this.parents = new HashSet<Task>();
	}

	/**
	 * Adds a successor to this task.
	 * 
	 * @param successor
	 * 
	 */
	public boolean addSuccessor(Task successor) {

		// Prevent self assignment
		if (successor == this)
			return false;

		// Prevent adding duplicate successors
		if (this.successors.contains(this))
			return false;

		this.successors.add(successor);

		successor.setOwner(this);
		successor.parents.add(this);

		this.notifyObservers(new StoryComponentEvent(successor,
				StoryComponentChangeEnum.TASK_SUCCESSOR_ADDED));

		return true;
	}

	/**
	 * Adds a collection of successors to this task.
	 * 
	 * @param successors
	 */
	public void addSuccessors(Collection<Task> successors) {
		for (Task successor : successors) {
			this.addSuccessor(successor);
		}
	}

	/**
	 * Removes a successor from this task.
	 * 
	 * @param successor
	 */
	public boolean removeSuccessor(Task successor) {
		if (this.successors.remove(successor)) {
			successor.parents.remove(this);

			this.notifyObservers(new StoryComponentEvent(successor,
					StoryComponentChangeEnum.TASK_SUCCESSOR_REMOVED));
			return true;
		}

		return false;
	}

	// ************* GETTERS AND SETTERS ********************//

	/**
	 * @return the chance
	 */
	public int getChance() {
		return chance;
	}

	/**
	 * @param chance
	 *            the chance to set
	 */
	public void setChance(int chance) {
		this.chance = chance;
	}

	/**
	 * @return the successors
	 */
	public Set<Task> getSuccessors() {
		return successors;
	}

	/**
	 * Adds descendants to a collection to return them.
	 * 
	 * @param descendants
	 * @return
	 */
	protected <E extends Collection<Task>> E addDescendants(E descendants) {
		descendants.add(this);

		for (Task successor : this.getSuccessors()) {
			/*
			 * This check prevents us from going over paths twice, which saves a
			 * ton of time in complex stories. Note that the contains
			 * implementation in Sets is much faster, which is why
			 * getDescendants is faster than getOrderedDescendants.
			 */
			if (!descendants.contains(successor))
				successor.addDescendants(descendants);
		}

		return descendants;
	}

	/**
	 * Returns all descendant tasks in an unordered collection.
	 * 
	 * @return
	 */
	public Collection<Task> getDescendants() {
		return this.addDescendants(new HashSet<Task>());
	}

	/**
	 * @param successors
	 *            the successors to set
	 */
	public void setSuccessors(Collection<Task> successors) {
		for (Task successor : new ArrayList<Task>(this.successors))
			this.removeSuccessor(successor);

		for (Task successor : successors) {
			this.addSuccessor(successor);
		}
	}

	/**
	 * @return the parents
	 */
	public Set<Task> getParents() {
		return this.parents;
	}

	/**
	 * Clones this task, its successors, each of its successors' successors, and
	 * so on. It's automatically done in {@link Behaviour#clone()} for the start
	 * task, which is likely the only place we want to be doing it anyways.
	 * 
	 * @return
	 */
	public Task cloneWithDescendants() {
		return cloneWithDescendants(this, new HashMap<Task, Task>());
	}

	/**
	 * Use {@link #cloneWithDescendants()} instead, as this is just a recursive
	 * helper method for that.
	 * 
	 * @param root
	 * @param map
	 * @return
	 */
	private Task cloneWithDescendants(Task root, Map<Task, Task> map) {
		if (root == null)
			return null;

		if (map.containsKey(root))
			return map.get(root);

		Task newNode = root.clone();

		map.put(root, newNode);

		for (Task t : root.getSuccessors()) {
			newNode.addSuccessor(cloneWithDescendants(t, map));
		}
		return newNode;
	}

	/**
	 * Note that this does not clone the successors. Use
	 * {@link #cloneWithDescendants()} to also clone all descendants and
	 * properly apply graph connections to them. It's automatically done in
	 * {@link Behaviour#clone()} for the start task, which is likely the only
	 * place we want to be doing it anyways.
	 */
	@Override
	public Task clone() {
		final Task clone = (Task) super.clone();

		clone.chance = this.chance;

		return clone;
	}

	@Override
	public String toString() {
		return "Task [ Children: [" + this.getChildren() + "] Successors: ["
				+ this.getSuccessors() + "]]";
	}

	@Override
	public int compareTo(Task o) {
		return Integer.valueOf(this.getChance()).compareTo(
				Integer.valueOf(o.getChance()));
	}

	@Override
	public void revalidateKnowItBindings() {
		for (Task successor : this.successors)
			successor.revalidateKnowItBindings();

		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}

	public int getUniqueID() {
		return this.uniqueID;
	}
}
