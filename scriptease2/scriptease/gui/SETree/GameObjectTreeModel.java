package scriptease.gui.SETree;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
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
import scriptease.model.StoryModelPool;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameConversation;
import scriptease.translator.io.model.GameConversationNode;

@SuppressWarnings("serial")
public class GameObjectTreeModel extends DefaultTreeModel implements Filterable {
	protected Filter filter;

	public GameObjectTreeModel() {
		this(null);
	}

	public GameObjectTreeModel(TreeNode root) {
		super(root);
		populateGameObjects();
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
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
		final StoryModel activeModel;
		final GameTypeManager typeManager;
		final List<String> types;
		final DefaultMutableTreeNode root;

		activeModel = StoryModelPool.getInstance().getActiveModel();
		typeManager = activeModel.getTranslator().getGameTypeManager();
		types = new ArrayList<String>(typeManager.getKeywords());

		root = new DefaultMutableTreeNode(new JLabel("Available Game Objects"));

		// sort types based on their display text, not keyword.
		Collections.sort(types, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(
						typeManager.getDisplayText(o1),
						typeManager.getDisplayText(o2));
			}
		});

		// add every game object available into the appropriate type category.
		if (activeModel != null) {
			List<GameConstant> gameObjects;
			DefaultMutableTreeNode typeNode;
			DefaultMutableTreeNode objectNode;

			for (final String type : types) {
				typeNode = new DefaultMutableTreeNode(new JLabel(
						typeManager.getDisplayText(type)));

				gameObjects = activeModel.getModule().getResourcesOfType(type);

				gameObjects = new ArrayList<GameConstant>(
						this.filterGameObjects(gameObjects));

				// sort game objects based on display name as well
				Collections.sort(gameObjects, new Comparator<GameConstant>() {
					@Override
					public int compare(GameConstant o1, GameConstant o2) {
						return String.CASE_INSENSITIVE_ORDER.compare(
								o1.getName(), o2.getName());
					}
				});

				for (GameConstant gameObject : gameObjects) {
					objectNode = buildGameObjectNode(gameObject);
					
					if (gameObject instanceof GameConversation) {
						final List<GameConversationNode> roots;

						roots = ((GameConversation) gameObject)
								.getConversationRoots();
						for (GameConversationNode dialog : roots) {
							this.resolveConversationNodeBranch(dialog,
									objectNode,
									new ArrayList<GameConversationNode>());
						}
					}

					// Add the GameObject node to the category.
					typeNode.add(objectNode);
				}

				// hide empty ones because they're obnoxious.
				if (typeNode.getChildCount() > 0)
					root.add(typeNode);
			}
		}

		this.setRoot(root);
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
