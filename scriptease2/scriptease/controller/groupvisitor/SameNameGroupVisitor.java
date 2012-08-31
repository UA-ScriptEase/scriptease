package scriptease.controller.groupvisitor;

import scriptease.model.atomic.KnowIt;

public class SameNameGroupVisitor extends GroupVisitor {

	public SameNameGroupVisitor(KnowIt component) {
		super(component);
	}

	/**
	 * Checks if the given knowIt's name matches the original knowIts name
	 * 
	 * @param knowIt
	 * @return
	 */
	@Override
	protected boolean isPartOfGroup(KnowIt knowIt) {
		if (this.original != null) {
			return this.original.getDisplayText().equals(
					knowIt.getDisplayText())
					&& this.original != knowIt;
		}
		return false;
	}

}
