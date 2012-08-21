package scriptease.gui.SETree;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.SETree.filters.Filter;
import scriptease.gui.SETree.filters.Filterable;
import scriptease.gui.SETree.filters.GameConstantFilter;
import scriptease.model.StoryModel;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameConversation;
import scriptease.translator.io.model.GameConversationNode;

@SuppressWarnings("serial")
public class GameObjectTreeModel extends DefaultTreeModel implements Filterable {
	protected Filter filter;
	private JTree tree;
	private final StoryModel storyModel;

	/*
	 * Unsurprisingly, this class is NEVER USED. But I didn't delete it yet.
	 * Why? Because this class is what implements the search for Game Object
	 * Tree. Of course, it doesn't do anything since it's not implemented, but
	 * we might be able to use some of the code (if we can make sense of it).
	 * -kschenk
	 */
	public GameObjectTreeModel(TreeNode root, StoryModel storyModel) {
		super(root);
		this.storyModel = storyModel;
		populateGameObjects();
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * Give the model the JTree so it can expand the rows.. HACK
	 * 
	 * @param tree
	 */
	public void setTree(JTree tree) {
		this.tree = tree;
	}

	/**
	 * Amends the filter GameObjectTreeModel's Filter to the new desired filter.
	 * If the given type of filter rule already exists, it replaces it.
	 * Otherwise it adds the newFilter as an additional filter on top of the
	 * other filter types. It then rebuilds the tree with the given filter.
	 * 
	 * @param newFilterRule
	 *            The new rule to obey.
	 */
	@Override
	public void updateFilter(Filter newFilterRule) {
		if (newFilterRule == null
				|| !(newFilterRule instanceof GameConstantFilter))
			return;

		if (this.filter == null)
			this.filter = newFilterRule;
		else
			this.filter.addRule(newFilterRule);

		populateGameObjects();
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

		if (this.filter == null || gameObjects == null)
			return gameObjects;

		for (GameConstant gameObject : gameObjects) {
			// If the child was accepted by the filter
			boolean accepted = this.filter.isAcceptable(gameObject);

			if (accepted)
				filteredObjects.add(gameObject);
		}

		return filteredObjects;
	}

	/**
	 * Builds the Tree based on the active model and GameObjectFilter
	 */
	private void populateGameObjects() {
		final GameTypeManager typeManager;
		// Build the tree:
		// Make a node for the root of the tree.
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new JLabel(
				"Available Game Objects"));

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

		for (final String type : types) {
			// Make a node for the type.
			DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(
					new JLabel(typeManager.getDisplayText(type)));

			List<GameConstant> gameObjects;

			// Get all GameObjects of the type.
			if (storyModel == null) {
				gameObjects = new ArrayList<GameConstant>(0);
			} else {
				gameObjects = storyModel.getModule().getResourcesOfType(type);

				gameObjects = new ArrayList<GameConstant>(
						this.filterGameObjects(gameObjects));

				Collections.sort(gameObjects, new Comparator<GameConstant>() {
					@Override
					public int compare(GameConstant o1, GameConstant o2) {
						return String.CASE_INSENSITIVE_ORDER.compare(
								o1.getName(), o2.getName());
					}
				});
			}

			// For each GameObject of the type.
			for (GameConstant gameObject : gameObjects) {
				DefaultMutableTreeNode objectNode = buildGameObjectNode(gameObject);
				if (gameObject instanceof GameConversation) {
					final List<GameConversationNode> roots = ((GameConversation) gameObject)
							.getConversationRoots();
					for (GameConversationNode dialog : roots) {
						this.resolveConversationNodeBranch(dialog, objectNode,
								new ArrayList<GameConversationNode>());
					}
				}
				// Add the GameObject node to the category.
				typeNode.add(objectNode);
			}

			// Add the category to the root if it has any elements
			if (typeNode.getChildCount() > 0)
				root.add(typeNode);
		}
		this.setRoot(root);

		// if the model has a tree, expand all rows.. HACK
		if (tree != null) {
			// expand the tree
			// TODO bug where this will cause an infinite loop when trying to
			// expand dialogues
			// for (int row = 0; row < tree.getRowCount(); row++) {
			// tree.expandRow(row);
			// }
		}
	}

	/**
	 * Recursively builds the given GameConversationNode and adds it the given
	 * DefaultMutableTreeNode. Checks the provided Collection to assure there is
	 * no infinite looping in the conversations
	 * 
	 * @param node
	 * @param parent
	 * @param previousNodes
	 */
	private void resolveConversationNodeBranch(GameConversationNode node,
			DefaultMutableTreeNode parent,
			Collection<GameConversationNode> previousNodes) {
		if (!previousNodes.contains(node)) {
			final DefaultMutableTreeNode conversationNode = buildGameObjectNode(node);
			parent.add(conversationNode);
			previousNodes.add(node);
			for (GameConversationNode child : node.getChildren()) {
				resolveConversationNodeBranch(child, conversationNode,
						previousNodes);
			}
		}
	}

	private DefaultMutableTreeNode buildGameObjectNode(GameConstant gameObject) {
		// Make a new BindingWidget for the GameObject.
		BindingWidget gameObjectBindingWidget = new BindingWidget(
				new KnowItBindingConstant(gameObject));
		String name = gameObject.getName();
		final JLabel nameLabel = ScriptWidgetFactory.buildLabel(name,
				Color.WHITE);
		gameObjectBindingWidget.add(nameLabel);
		// Set an empty border to prevent line crowding.
		gameObjectBindingWidget.setBorder(BorderFactory.createEmptyBorder(
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE));

		// Make a new node in the tree for the Game Object.
		return new DefaultMutableTreeNode(gameObjectBindingWidget);
	}
}
