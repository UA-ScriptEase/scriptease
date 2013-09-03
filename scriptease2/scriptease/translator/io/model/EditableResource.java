package scriptease.translator.io.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.ResourceObserver;

/**
 * This is a {@link Resource} whose fields we can edit. It knows about its
 * parents and its children. Setters will notify observers. If they are
 * overridden, the author should in most cases call their respective notify
 * methods.
 * 
 * @author kschenk
 * 
 */
public abstract class EditableResource extends Resource {
	private final ObserverManager<ResourceObserver> observerManager;
	private final List<Resource> children;

	private String name;

	public EditableResource() {
		this("", new ArrayList<Resource>());
	}

	public EditableResource(String name, List<Resource> children) {
		this.name = name.replaceAll("\n", " ");
		this.children = children;

		this.observerManager = new ObserverManager<ResourceObserver>();
	}

	/**
	 * Removes a child from the Dialogue Line. Calls
	 * {@link #notifyChildRemoved(Resource)} by default. If this method is
	 * overridden, you should likely call it as well.
	 * 
	 * @param dialogueLine
	 * @return
	 */
	public boolean removeChild(Resource child) {
		final boolean removed;

		if (removed = this.children.remove(child)) {
			this.notifyChildRemoved(child);
		}

		return removed;
	}

	public boolean addChildren(Collection<Resource> children) {
		return this.children.addAll(children);
	}

	/**
	 * Adds a child to the dialogue line. Calls
	 * {@link #notifyChildAdded(Resource)} by default. If this method is
	 * overridden, you should likely call it as well.
	 * 
	 * @param dialogueLine
	 * @return
	 */
	public boolean addChild(Resource child) {
		final boolean added;

		if (added = !this.children.contains(child) && this.children.add(child)) {
			this.notifyChildAdded(child);
		}

		return added;
	}

	public Collection<ResourceObserver> getObservers() {
		return this.observerManager.getObservers();
	}

	public void addObserver(Object weakReference, ResourceObserver observer) {
		this.observerManager.addObserver(weakReference, observer);
	}

	public void removeObserver(ResourceObserver observer) {
		this.observerManager.removeObserver(observer);
	}

	@Override
	public List<? extends Resource> getChildren() {
		return this.children;
	}

	/**
	 * Returns whether this editable resource is a root resource. That is, it
	 * does not have any parents.
	 * 
	 * @return
	 */
	public abstract boolean isRoot();

	/**
	 * Sets the name of a resource. Calls {@link #notifyNameChange()} by
	 * default. If this method is overridden, you should likely call it as well.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name.replaceAll("\n", " ");
		this.notifyNameChange();
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Notifies a name change. {@link #setName(String)} will call this by
	 * default, but if it was overridden, this method will need to be called
	 * manually.
	 * 
	 * @param resource
	 */
	protected void notifyNameChange() {
		for (ResourceObserver observer : this.observerManager.getObservers()) {
			observer.nameChanged(this, this.name);
		}
	}

	/**
	 * Notifies a child removed. {@link #removeChild(EditableResource)} will
	 * call this by default, this by default, but if it was overridden, this
	 * method will need to be called manually.
	 * 
	 * @param resource
	 */
	protected void notifyChildRemoved(Resource child) {
		for (ResourceObserver observer : this.observerManager.getObservers()) {
			observer.childRemoved(this, child);
		}
	}

	/**
	 * Notifies a child added. {@link #addChild(EditableResource)} will call
	 * this by default, but if it was overridden, this method will need to be
	 * called manually.
	 * 
	 * @param resource
	 */
	protected void notifyChildAdded(Resource child) {
		for (ResourceObserver observer : this.observerManager.getObservers()) {
			observer.childAdded(this, child);
		}
	}
}
