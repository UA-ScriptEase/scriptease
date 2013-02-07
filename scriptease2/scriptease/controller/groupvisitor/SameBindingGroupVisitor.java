package scriptease.controller.groupvisitor;

import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;

public class SameBindingGroupVisitor extends GroupVisitor {

	public SameBindingGroupVisitor(KnowIt component) {
		super(component);
	}

	/**
	 * Checks if the given knowIt's binding matches the group's Binding
	 * 
	 * @param knowIt
	 * @return
	 */
	@Override
	protected boolean isPartOfGroup(KnowIt knowIt) {
		final KnowItBinding comparingBinding = knowIt.getBinding();
		if (this.original != null) {
			// Don't group constants
			if (comparingBinding instanceof KnowItBindingResource
					&& !((KnowItBindingResource) comparingBinding)
							.isIdentifiableGameConstant())
				return false;
			else
				return comparingBinding.equals((this.original).getBinding());
		}
		return false;
	}
}
