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
 * Generic controller object that is a collection of double dispatch methods to
 * correspond with a call to KnowItBinding.process(). Pass an implementation of
 * <code>BindingVisitor</code> to a {@link KnowItBinding}'s
 * {@link KnowItBinding#process()} method to get type-specific behaviour.<br>
 * <br>
 * Classes should not implement this interface directly since it is strongly
 * recommended (and stylistically required) that they subclass
 * {@link BindingAdapter}. <br>
 * <br>
 * <code>BindingVisitor</code> is an implementation of the Visitor design
 * pattern - Adapted from the implementation of StoryVisitor.
 * 
 * @author mfchurch
 * @author jyuen
 * 
 * @see StoryAdapter
 */
public interface BindingVisitor {
	public void processResource(KnowItBindingResource constant);

	public void processFunction(KnowItBindingFunction function);

	public void processReference(KnowItBindingReference reference);

	public void processNull(KnowItBindingNull nullBinding);

	public void processStoryPoint(KnowItBindingStoryPoint storyPoint);

	public void processStoryGroup(KnowItBindingStoryGroup storyGroup);

	public void processAutomatic(KnowItBindingAutomatic automatic);

	public void processUninitialized(KnowItBindingUninitialized uninitialized);
}
