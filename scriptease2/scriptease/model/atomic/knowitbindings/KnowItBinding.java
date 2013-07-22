package scriptease.model.atomic.knowitbindings;

import java.util.Collection;

import scriptease.controller.BindingVisitor;
import scriptease.model.atomic.KnowIt;

/**
 * The abstract class that different <code>KnowIt</code> bindings should
 * implement.
 * 
 * @author graves
 * @see {@link scriptease.model.atomic.knowit}
 * 
 */
public abstract class KnowItBinding implements Cloneable {

	public abstract String getScriptValue();

	public abstract Object getValue();

	public abstract Collection<String> getTypes();

	@Override
	public abstract boolean equals(Object other);

	@Override
	public int hashCode() {
		final Object value = this.getValue();

		if (value != null)
			return this.getValue().hashCode();
		else
			return -1;
	}

	public abstract KnowItBinding resolveBinding();

	/**
	 * Checks if the KnowItBinding is compatible with the given KnowIt. Based
	 * off the KnowIt's acceptable types.
	 * 
	 * @param knowIt
	 * @return
	 */
	public boolean compatibleWith(KnowIt knowIt) {
		return typeMatches(knowIt.getAcceptableTypes());
	}

	/**
	 * Checks if the KnowItBinding is compatible with the given KnowIt. Based
	 * off the KnowIt's explcit types.
	 * 
	 * @param knowIt
	 * @return
	 */
	public boolean explicitlyCompatibleWith(KnowIt knowIt) {
		return typeMatches(knowIt.getTypes());
	}

	/**
	 * Substitutes the instanceof KnowItBindingNull checks.
	 * 
	 * @return <code>true</code>, unless the binding is an instance of
	 *         KnowItBindingNull
	 */
	public boolean isBound() {
		return true;
	}

	@Override
	public KnowItBinding clone() {
		KnowItBinding clone = null;
		try {
			clone = (KnowItBinding) super.clone();
		} catch (CloneNotSupportedException e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		}
		return clone;
	}

	/**
	 * Checks if the types match between the KnowItBinding and the given KnowIt
	 * 
	 * @param storyComponent
	 * @return
	 */
	protected boolean typeMatches(Collection<String> knowItTypes) {
		// Get this KnowItBinding's types.
		Collection<String> bindingTypes = this.getTypes();

		// TODO REMOVE THE IF!!
		// Check if any of the types match
		if (bindingTypes != null) {
			for (String bindingType : bindingTypes) {
				if (knowItTypes.contains(bindingType)) {
					return true;
				}
			}
		}
		
		// If the method has reached this point, there are no common types.
		return false;
	}

	// Gross there has to be a better way instead of blindly grabbing the first
	// type.
	public String getFirstType() {
		return this.getTypes().iterator().next();
	}

	/**
	 * This is a double-dispatch hook for the
	 * {@link scriptease.controller.BindingVisitor} family of controllers.
	 * <code>processController</code> implements each of: process[X] where [X]
	 * is each of the leaf members of the <code>KnowItBinding</code> family. <BR>
	 * <BR>
	 * To Use: Pass in a valid BindingVisitor to this method. The implementing
	 * atom of this method will dispatch the appropriate
	 * <code>BindingVisitor</code> method for the type. Voila! Double dispatch!
	 * :-)
	 * 
	 * @param processController
	 *            The <code>BindingVisitor</code> that will process just this
	 *            KnowItBinding.
	 */
	public abstract void process(BindingVisitor processController);

}
