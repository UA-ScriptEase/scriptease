package scriptease.controller.get;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import scriptease.controller.StoryAdapter;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * Generic parent to all "getter" visitors. For convenience, the list "objects",
 * as well as it's getter method are created here. That way, the extending
 * classes can be short and sweet, and without duplicate code.
 * 
 * The extending classes, or "getters", are used by code generation to provide
 * access to internal ScriptEase data to translator authors. By invoking the
 * appropriate keyword in their translator, the authors can tell the code
 * generator to perform steps on any collection of like objects in the story
 * tree.
 * 
 * @author jason
 * 
 * @param <T>
 *            the type of StoryComponent that is collected
 */
public abstract class TypeGetterVisitor<T> extends StoryAdapter {
	/**
	 * A generic definition of the list that will be used by the extending
	 * getter to store it's results.
	 */
	protected final Collection<T> objects = new CopyOnWriteArrayList<T>();

	/**
	 * Returns the list of objects constructed during tree processing.
	 * 
	 * @return
	 */
	public Collection<T> getObjects() {
		return this.objects;
	}

	/**
	 * Resets the collection of collected objects to an empty list.
	 */
	public void reset() {
		this.objects.clear();
	}

	// we want to be recursive
	@Override
	protected void defaultProcessComplex(ComplexStoryComponent complex) {
		super.defaultProcessComplex(complex);
		complex.processChildren(this);
	}

}
