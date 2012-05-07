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
		if (original != null && original instanceof KnowIt) {
			return original.getDisplayText().equals(knowIt.getDisplayText())
					&& original != knowIt;
		}
		return false;
	}

}
