package scriptease.controller;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingQuestPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingRunTime;

/**
 * Generic controller object that is a collection of double dispatch methods to
 * correspond with a call to KnowItBinding.process(). Pass an implementation of
 * <code>BindingVisitor</code> to a {@link KnowItBinding}'s
 * {@link KnowItBinding#process()} method to get type-specific behaviour.<br>
 * <br>
 * Classes should not implement this interface directly since it is strongly
 * recommended (and stylistically required) that they subclass
 * {@link AbstractNoOpBindingVisitor}. <br>
 * <br>
 * <code>BindingVisitor</code> is an implementation of the Visitor design
 * pattern - Adapted from the implementation of StoryVisitor.
 * 
 * @author mfchurch
 * 
 * @see AbstractNoOpStoryVisitor
 */
public interface BindingVisitor { 
	public void processConstant(KnowItBindingConstant constant);

	public void processFunction(KnowItBindingFunction function);

	public void processReference(KnowItBindingReference reference);

	public void processRunTime(KnowItBindingRunTime runTime);

	public void processNull(KnowItBindingNull nullBinding);

	public void processDescribeIt(KnowItBindingDescribeIt described);
	
	public void processQuestPoint(KnowItBindingQuestPoint questPoint);
}
