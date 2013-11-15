package scriptease.controller;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingAutomatic;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryGroup;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;

/**
 * Default implementation of BindingVisitor that does nothing. Ever. <br>
 * <br>
 * It is <b>stylistically required</b> that all other BindingVisitor
 * implementations extend this class, allowing us to avoid having to update all
 * of the visitors whenever the interface changes. Subclasses also get the perk
 * of only having to override the methods they <i>do</i> support.<br>
 * <br>
 * Subclasses that wish to provide default behaviour for processing can override
 * {@link #defaultProcess(KnowItBinding)}. <br>
 * <br>
 * AbstractNoOpBindingVisitor is an Adapter (of the Adapter design pattern) to
 * BindingVisitor - based off AbstractNoOpStoryVisitor.
 * 
 * @author mfchurch
 * 
 */
public abstract class BindingAdapter implements BindingVisitor {
	@Override
	public void processResource(KnowItBindingResource constant) {
		this.defaultProcess(constant);
	}

	@Override
	public void processFunction(KnowItBindingFunction function) {
		this.defaultProcess(function);
	}

	@Override
	public void processReference(KnowItBindingReference reference) {
		this.defaultProcess(reference);
	}

	@Override
	public void processNull(KnowItBindingNull nullBinding) {
		this.defaultProcess(nullBinding);
	}

	@Override
	public void processAutomatic(KnowItBindingAutomatic automatic) {
		this.defaultProcess(automatic);
	}

	@Override
	public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
		this.defaultProcess(storyPoint);
	}

	@Override
	public void processStoryGroup(KnowItBindingStoryGroup storyGroup) {
		this.defaultProcess(storyGroup);
	}

	@Override
	public void processUninitialized(KnowItBindingUninitialized uninitialized) {
		this.defaultProcess(uninitialized);
	}

	/**
	 * The default process method that is called by every
	 * process<i>X</i>(<i>X</i> <i>x</i>) method in this class' standard
	 * methods, where X is one of the types it can process. <br>
	 * <br>
	 * Override this method if you want to provide a non-null default behaviour
	 * for every non-overridden process<i>Z</i> method. Unless it is overridden,
	 * it does nothing.
	 * 
	 * @param binding
	 *            The KnowItBinding to process with a default behaviour.
	 */
	protected void defaultProcess(KnowItBinding binding) {
	}
}
