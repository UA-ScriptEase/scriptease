package scriptease.gui.SETree;

import java.util.Observable;

import scriptease.gui.SETree.filters.Filter;
import scriptease.gui.SETree.filters.Filterable;

enum ModelType {
	GAME_OBJECT,
	STORY_COMP;
}

/*
 * This does nothing..
 */

public abstract class SETreeModel extends Observable implements Filterable {
	// private TreeModel treeModel;
	protected ModelType modelType;
	protected Tree<Object> treeModel;
	protected Filter filter;

	public Tree<Object> getTree() {
		return this.treeModel;
	}

	protected SETreeModel() {

	}

	protected abstract void createAndPopulateTree();

	@Override
	public void updateFilter(Filter newFilter) {
		// TODO Auto-generated method stub

	}

}
