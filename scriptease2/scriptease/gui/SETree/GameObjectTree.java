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
	private final String DIALOGUE_TAG = "dialogue";

	private StoryModel activeModel;

	public GameObjectTree() {
		activeModel = StoryModelPool.getInstance().getActiveModel();
		createAndPopulateTree();
	}

	public void setFilter(Filter addFilter) {
		filter = addFilter;
	}

	/**
	 * Recursively adds all nodes from a conversation tree as types.
	 * 
	 * @param parent
	 */
	@SuppressWarnings("unchecked")
	private void addRecusivleyAllNodes(GameConversationNode parent) {
		if (parent.isTerminal() == true) {
			return;
		}

		List<GameConversationNode> getChildren;
		getChildren = (List<GameConversationNode>) parent.getChildren();
		if (getChildren.size() == 0) {
			return;
		}

		for (GameConversationNode a : getChildren) {
			if (!a.isLink())
				treeModel.addLeaf(parent, a);
		}
		for (GameConversationNode a : getChildren) {
			if (!a.isLink())
				addRecusivleyAllNodes(a);
		}
	}

	private void addAllChildren(GameConversation parent) {
		List<GameConversationNode> conversationRoots;
		conversationRoots = parent.getConversationRoots();

		for (GameConversationNode root : conversationRoots) {
			treeModel.addLeaf(parent, root);
		}

		for (GameConversationNode childrenRoots : conversationRoots) {
			addRecusivleyAllNodes(childrenRoots);

		}

	}

	@Override
	protected void createAndPopulateTree() {
		final GameTypeManager typeManager;
		// Get the active model.
		StoryModelPool.getInstance().getActiveModel();
		// Build the tree:
		// Make a node for the root of the tree.
		String availObjects = "Available Game Objects";
		treeModel = new Tree<Object>(availObjects);

		// For each type in the translator.
		typeManager = TranslatorManager.getInstance().getActiveTranslator()
				.getGameTypeManager();
		Collection<String> keywords = typeManager.getKeywords();
		List<String> types = new ArrayList<String>(keywords);
		Collections.sort(types, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(
						typeManager.getDisplayText(o1),
						typeManager.getDisplayText(o2));
			}
		});

		for (String typeName : types) {
			ArrayList<GameConstant> gameObjs = (ArrayList<GameConstant>) getAllObjectsOfType(typeName);

			if (gameObjs.size() <= 0)
				continue;

			if (typeName.equals(DIALOGUE_TAG)) {
				for (GameConstant convo : gameObjs) {
					GameConversation dialogue = (GameConversation) convo;
					treeModel.addLeaf(typeName, dialogue);
					addAllChildren(dialogue);
				}
			} else {
				treeModel.addLeaf(treeModel.getHead(), typeName);
				for (GameConstant obj : gameObjs) {
					treeModel.addLeaf(typeName, obj);
					// if(((GameObject)obj).getAttributes().size() > 0){
					// for(GameObject attributes :
					// ((GameObject)obj).getAttributes()){
					// treeModel.addLeaf(obj,attributes);
					// }
					// }
				}
			}

		}
	}

	private Collection<GameConstant> getAllObjectsOfType(String type) {
		List<GameConstant> allGameObjects;

		allGameObjects = ((StoryModel) activeModel).getModule()
				.getResourcesOfType(type);
		allGameObjects = new ArrayList<GameConstant>(
				this.filterGameObjects(allGameObjects));

		Collections.sort(allGameObjects, new Comparator<GameConstant>() {
			@Override
			public int compare(GameConstant o1, GameConstant o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(),
						o2.getName());
			}
		});

		return allGameObjects;
	}

	/**
	 * Filter the StoryComponentPanelTree immediate children, does nothing if no
	 * filter is applied
	 * 
	 * @return
	 */
	public Collection<GameConstant> filterGameObjects(
			Collection<GameConstant> gameObjects) {
		Collection<GameConstant> filteredObjects = new ArrayList<GameConstant>();

		if (filter == null || gameObjects == null)
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
