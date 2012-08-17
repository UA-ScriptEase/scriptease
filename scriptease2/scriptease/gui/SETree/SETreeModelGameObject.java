package scriptease.gui.SETree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.gui.SETree.SETreeModel;
import scriptease.gui.SETree.Tree;
import scriptease.gui.SETree.filters.Filter;
import scriptease.model.StoryModel;
import scriptease.model.PatternModelPool;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameConversation;
import scriptease.translator.io.model.GameConversationNode;

public class SETreeModelGameObject extends SETreeModel {
	/*
	 * TODO Refactor and comment, or merge it with something else. Not sure what
	 * this class is even doing.
	 * 
	 * Actually, it looks like it's not even doing anything. Nothing calls it,
	 * so it's just sitting here, all sad and lonely. Either call it sometime or
	 * put it out of its misery.
	 */

	private final String DIALOGUE_TAG = "dialogue";

	private final StoryModel storyModel;

	public SETreeModelGameObject(StoryModel storyModel) {
		this.storyModel = storyModel;
		createAndPopulateTree();
	}

	public void setFilter(Filter addFilter) {
		filter = addFilter;
	}

	@SuppressWarnings("unchecked")
	private void addRecusivleyAllNodes(GameConversationNode parent) {
		// TODO: Look at this section, and the problem with infinitely looping
		// convos and such

		// if(parent.isTerminal() == true){
		// return;
		// }

		List<GameConversationNode> getChildren;
		getChildren = (List<GameConversationNode>) parent.getChildren();
		if (getChildren.size() == 0) {
			return;
		}

		for (GameConversationNode a : getChildren) {
			treeModel.addLeaf(parent, a);
		}

		for (GameConversationNode a : getChildren) {
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
		PatternModelPool.getInstance().getActiveModel();
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

			// List<GameConversationNode> conversationRoots;
			if (typeName.equals(DIALOGUE_TAG)) {
				for (GameConstant convo : gameObjs) {
					GameConversation dialogue = (GameConversation) convo;
					treeModel.addLeaf(typeName, dialogue);
					addAllChildren(dialogue);
				}

			}
			// NWN TEXT
			// NWNObject pc = new NWNObject();

			else {
				treeModel.addLeaf(treeModel.getHead(), typeName);
				for (GameConstant obj : gameObjs) {
					treeModel.addLeaf(typeName, obj);
				}
			}

		}
	}

	private Collection<GameConstant> getAllObjectsOfType(String type) {
		List<GameConstant> allGameObjects;

		allGameObjects = storyModel.getModule()
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
