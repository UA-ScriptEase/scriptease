package scriptease.gui.SETree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.gui.SETree.filters.Filter;
import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameConversation;
import scriptease.translator.io.model.GameConversationNode;

public class GameObjectTree extends SETreeModel {
	public GameObjectTree() {
		treeModel = new Tree<Object>("Available Game Objects");
		this.populate();
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	@Override
	protected void populate() {
		final GameTypeManager typeManager;
		final List<String> types;
		Collection<GameConstant> gameObjects;

		// For each type in the translator.
		typeManager = TranslatorManager.getInstance().getActiveTranslator()
				.getGameTypeManager();
		types = new ArrayList<String>(typeManager.getKeywords());

		Collections.sort(types, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(
						typeManager.getDisplayText(o1),
						typeManager.getDisplayText(o2));
			}
		});

		for (String typeName : types) {
			gameObjects = this.getObjectsOfType(typeName);

			// Ignore empty categories because they're confusing.
			if (gameObjects.size() <= 0)
				continue;

			treeModel.addChild(treeModel.getHead(), typeName);

			for (GameConstant object : gameObjects) {
				treeModel.addChild(typeName, object);

				if (object instanceof GameConversation) {
					addConversationLines((GameConversation) object);
				}
			}
		}
	}

	private Collection<GameConstant> getObjectsOfType(String type) {
		final StoryModel activeModel;
		List<GameConstant> allGameObjects;

		activeModel = StoryModelPool.getInstance().getActiveModel();

		allGameObjects = this.filterGameObjects(activeModel.getModule()
				.getResourcesOfType(type));

		Collections.sort(allGameObjects, new Comparator<GameConstant>() {
			@Override
			public int compare(GameConstant o1, GameConstant o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(),
						o2.getName());
			}
		});

		return allGameObjects;
	}
	
	private void addConversationLines(GameConversation parent) {
		final List<GameConversationNode> roots;

		roots = parent.getConversationRoots();

		for (GameConversationNode root : roots) {
			treeModel.addChild(parent, root);
			this.addLines(root);
		}
	}

	/**
	 * Recursively adds all nodes from a conversation tree as types.
	 * 
	 * @param parent
	 */
	private void addLines(GameConversationNode parent) {
		final List<? extends GameConversationNode> children;

		if (parent.isTerminal()) {
			return;
		}

		children = parent.getChildren();

		if (children.size() == 0) {
			return;
		}

		for (GameConversationNode child : children) {
			if (!child.isLink()) {
				treeModel.addChild(parent, child);
				addLines(child);
			}
		}
	}

	/**
	 * Filter the StoryComponentPanelTree immediate children, does nothing if no
	 * filter is applied
	 * 
	 * @return
	 */
	private List<GameConstant> filterGameObjects(List<GameConstant> gameObjects) {
		List<GameConstant> filteredObjects = new ArrayList<GameConstant>();

		if (filter == null)
			if (gameObjects == null)
				return filteredObjects;
			else
				return gameObjects;

		for (GameConstant gameObject : gameObjects) {
			// If the child was accepted by the filter
			boolean accepted = filter.isAcceptable(gameObject);

			if (accepted)
				filteredObjects.add(gameObject);
		}

		return filteredObjects;
	}
}
