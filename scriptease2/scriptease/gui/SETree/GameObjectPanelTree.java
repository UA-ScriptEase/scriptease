package scriptease.gui.SETree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.controller.ModelAdapter;
import scriptease.gui.cell.BindingWidget;
import scriptease.gui.cell.ScriptWidgetFactory;
import scriptease.gui.filters.Filter;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameConversation;
import scriptease.translator.io.model.GameConversationNode;
import scriptease.translator.io.model.GameObject;
import scriptease.util.StringOp;

/**
 * Model side of the Game Object Panel Tree
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class GameObjectPanelTree extends JPanel {
	final Tree<Object> treeModel;

	Filter filter;

	private final int CONSTANT_OFFSET = 10;

	public GameObjectPanelTree() {
		super();
		this.treeModel = new Tree<Object>("Available Game Objects");

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setVisible(true);

		if (PatternModelManager.getInstance().getActiveModel() != null)
			this.drawTree();
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * Filter the StoryComponentPanelTree immediate children, does nothing if no
	 * filter is applied
	 * 
	 * @return
	 */
	private List<GameConstant> filterGameObjects(List<GameConstant> gameObjects) {
		List<GameConstant> filteredObjects = new ArrayList<GameConstant>();

		if (this.filter == null)
			if (gameObjects == null)
				return filteredObjects;
			else
				return gameObjects;

		for (GameConstant gameObject : gameObjects) {
			// If the child was accepted by the filter
			boolean accepted = this.filter.isAcceptable(gameObject);

			if (accepted)
				filteredObjects.add(gameObject);
		}

		return filteredObjects;
	}

	private Collection<GameConstant> getObjectsOfType(final String type) {
		final PatternModel activeModel;
		final List<GameConstant> allGameObjects = new ArrayList<GameConstant>();

		activeModel = PatternModelManager.getInstance().getActiveModel();

		activeModel.process(new ModelAdapter() {
			@Override
			public void processStoryModel(StoryModel storyModel) {
				allGameObjects.addAll(GameObjectPanelTree.this
						.filterGameObjects(storyModel.getModule()
								.getResourcesOfType(type)));

				Collections.sort(allGameObjects,
						new Comparator<GameConstant>() {
							@Override
							public int compare(GameConstant o1, GameConstant o2) {
								return String.CASE_INSENSITIVE_ORDER.compare(
										o1.getName(), o2.getName());
							}
						});
			}
		});

		return allGameObjects;
	}

	public void drawTree() {
		this.removeAll();
		this.treeModel.clear();

		final GameTypeManager typeManager;
		final List<String> types;

		typeManager = TranslatorManager.getInstance().getActiveTranslator()
				.getGameTypeManager();
		types = new ArrayList<String>(typeManager.getKeywords());

		// Sort the types by alphabet
		Collections.sort(types, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(
						typeManager.getDisplayText(o1),
						typeManager.getDisplayText(o2));
			}
		});

		// Add the game objects to the tree model.
		for (String typeName : types) {
			final Collection<GameConstant> gameObjects;

			gameObjects = this.getObjectsOfType(typeName);

			// Ignore empty categories because they're confusing.
			if (gameObjects.size() <= 0)
				continue;

			this.treeModel.addChild(this.treeModel.getHead(), typeName);

			for (GameConstant object : gameObjects) {
				this.treeModel.addChild(typeName, object);

				// Add dialogue lines
				if (object instanceof GameConversation) {
					final GameConversation parent;
					final List<GameConversationNode> roots;

					parent = (GameConversation) object;
					roots = parent.getConversationRoots();

					for (GameConversationNode root : roots) {
						this.treeModel.addChild(parent, root);
						this.addLines(root);
					}
				}
			}
		}

		final List<Object> firstRowExpansion;

		firstRowExpansion = (List<Object>) this.treeModel
				.getSuccessors(this.treeModel.getHead());

		// Add the game objects to the tree
		for (int i = 0; i < firstRowExpansion.size(); i++) {
			if (!(firstRowExpansion.get(i) instanceof String)) {
				continue;
			}

			final String typeName;

			typeName = (String) firstRowExpansion.get(i);

			this.add(new GameObjectContainer(typeName));
		}
	}

	/**
	 * Recursively adds all nodes from a conversation tree as types.
	 * 
	 * @param parent
	 */
	private void addLines(GameConversationNode parent) {

		if (parent.isTerminal()) {
			return;
		}

		final List<? extends GameConversationNode> children;

		children = parent.getChildren();

		if (children.size() == 0) {
			return;
		}

		for (GameConversationNode child : children) {
			if (!child.isLink()) {
				this.treeModel.addChild(parent, child);
				addLines(child);
			}
		}
	}

	/**
	 * Container of Game Objects.
	 * 
	 * @author kschenk
	 * 
	 */
	private class GameObjectContainer extends JPanel {
		private final String labelName;

		private boolean collapsed;

		public GameObjectContainer(String label) {
			this.collapsed = false;
			this.labelName = label;

			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			this.setBackground(Color.WHITE);

			redrawLabel();
		}

		private String convertToLabelViewFormat() {
			String labelView = this.labelName.substring(0, 1).toUpperCase();
			labelView += this.labelName.substring(1);
			return labelView;
		}

		private void redrawLabel() {
			this.removeAll();

			final JLabel categoryLabel;

			categoryLabel = new JLabel(convertToLabelViewFormat());

			categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

			categoryLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					GameObjectContainer.this.collapsed = !GameObjectContainer.this.collapsed;
					redrawLabel();
				}
			});

			if (this.collapsed) {
				categoryLabel.setIcon(ScriptEaseUI.EXPAND_ICON);
				this.add(categoryLabel);
			} else {
				categoryLabel.setIcon(ScriptEaseUI.COLLAPSE_ICON);
				this.add(categoryLabel);

				final Collection<Object> successors;

				successors = GameObjectPanelTree.this.treeModel
						.getSuccessors(labelName);

				for (Object successor : successors) {
					if (successor instanceof GameObject)
						this.add(createGameObjectComponent((GameObject) successor));
					else if (successor instanceof GameConversation)
						this.add(createGameConversationComponent((GameConversation) successor));
				}
			}

			this.add(Box.createHorizontalGlue());

			this.revalidate();
		}

		private JPanel createGameConversationComponent(
				GameConversation gameConversation) {
			final JPanel convoPanel;

			convoPanel = new JPanel();
			convoPanel.setOpaque(false);

			convoPanel
					.setLayout(new BoxLayout(convoPanel, BoxLayout.PAGE_AXIS));

			convoPanel.add(createGameObjectComponent(gameConversation));

			for (Object gameConversationNode : GameObjectPanelTree.this.treeModel
					.getSuccessors(gameConversation)) {
				convoPanel
						.add(createGameObjectComponent((GameConversationNode) gameConversationNode));
				this.addConversationRoots(
						(GameConversationNode) gameConversationNode, convoPanel);
			}

			convoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			return convoPanel;
		}

		private JPanel createGameObjectComponent(GameConstant gameObject) {
			final JPanel objectPanel;
			final BindingWidget gameObjectBindingWidget;
			final String regularText;

			objectPanel = new JPanel();
			regularText = gameObject.getName();

			objectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			objectPanel.setOpaque(true);

			objectPanel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
			objectPanel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);

			objectPanel.setLayout(new BoxLayout(objectPanel, BoxLayout.X_AXIS));

			gameObjectBindingWidget = new BindingWidget(
					new KnowItBindingConstant(gameObject));

			if (StringOp.wordCount(regularText) > 5)
				gameObjectBindingWidget.add(ScriptWidgetFactory.buildLabel(
						createShortHandViewofText(regularText), Color.WHITE));
			else
				gameObjectBindingWidget.add(ScriptWidgetFactory.buildLabel(
						regularText, Color.WHITE));

			gameObjectBindingWidget.setBorder(BorderFactory.createEmptyBorder(
					ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
					ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
					ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
					ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE));

			objectPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			objectPanel.add(gameObjectBindingWidget);
			objectPanel.add(Box.createRigidArea(new Dimension(5, 0)));

			return objectPanel;
		}

		/**
		 * Recursively add all children roots to tree.
		 * 
		 * @param parent
		 */
		private void addConversationRoots(GameConversationNode parent,
				JPanel convoPanel) {
			if (parent.isTerminal()) {
				return;
			}

			Collection<Object> successors = GameObjectPanelTree.this.treeModel
					.getSuccessors(parent);

			if (successors.size() == 0) {
				return;
			}

			for (Object successor : successors) {
				if (successor instanceof GameConversation)
					convoPanel
							.add(createGameObjectComponent((GameConversation) successor));
				else if (successor instanceof GameConversationNode)
					this.addConversationRoots((GameConversationNode) successor,
							convoPanel);
			}
		}

		private String createShortHandViewofText(final String text) {
			String truncatedVersion = "";
			int firstSpace = 0;
			int secondSpace = 0;

			firstSpace = text.indexOf(" ");
			secondSpace = text.indexOf(" ", firstSpace + 1);

			int lastSpace = text.lastIndexOf(" ");
			int penultimateSpace = text.substring(0, lastSpace - 1)
					.lastIndexOf(" ");

			truncatedVersion = text.substring(0, secondSpace + 1) + " ... "
					+ text.substring(penultimateSpace, text.length());

			return truncatedVersion;
		}
	}
}