package scriptease.gui.pane;

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

import scriptease.gui.cell.BindingWidget;
import scriptease.gui.cell.ScriptWidgetFactory;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.PatternModel;
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
 * Draws a Game Constant Panel for the passed in StoryModel.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class GameConstantPanel extends JPanel {

	/**
	 * Creates a new GameConstantPanel with the passed in model.
	 * 
	 * @param model
	 *            Creates a new GameConstantPanel with the passed in model. If
	 *            the passed in model is null or not a StoryModel, then nothing
	 *            is drawn. Use {@link #drawTree(StoryModel)} to draw the tree
	 *            later with a StoryModel.
	 */
	public GameConstantPanel(PatternModel model) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		if (model != null && model instanceof StoryModel) {
			final GameTypeManager typeManager;
			final List<String> types;

			typeManager = TranslatorManager.getInstance().getActiveTranslator()
					.getGameTypeManager();

			types = new ArrayList<String>(typeManager.getKeywords());

			this.drawTree((StoryModel) model, "", types);
		}
	}

	/**
	 * Returns all objects of the specified type.
	 * 
	 * @param type
	 * @return
	 */
	private Collection<GameConstant> getObjectsOfType(StoryModel model,
			final String type, final String searchText) {
		final List<GameConstant> allGameObjects;

		allGameObjects = new ArrayList<GameConstant>();

		final List<GameConstant> resourcesOfType = model.getModule()
				.getResourcesOfType(type);
		for (GameConstant gameConstant : resourcesOfType) {
			if (matchesSearchText(gameConstant, searchText))
				allGameObjects.add(gameConstant);
		}

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
	 * Determines whether any fields in the game constant match the passed in
	 * text. Recursively searches Conversations to see if any roots contain the
	 * text.
	 * 
	 * @param gameConstant
	 * @param searchText
	 * @return true if text is found
	 */
	private boolean matchesSearchText(GameConstant gameConstant,
			final String searchText) {
		if (searchText.length() == 0)
			return true;

		final String searchTextUpperCase;

		searchTextUpperCase = searchText.toUpperCase();

		if (gameConstant.getCodeText().toUpperCase()
				.contains(searchTextUpperCase)
				|| gameConstant.getName().toUpperCase()
						.contains(searchTextUpperCase)
				|| gameConstant.getTag().toUpperCase()
						.contains(searchTextUpperCase)
				|| gameConstant.getTemplateID().toUpperCase()
						.contains(searchTextUpperCase)) {

			return true;
		}

		if (gameConstant instanceof GameConversation) {
			boolean nodeContainsText = false;

			for (GameConversationNode child : ((GameConversation) gameConstant)
					.getConversationRoots()) {
				nodeContainsText = nodeContainsText
						|| matchesSearchText(child, searchText);
			}

			return nodeContainsText;
		} else if (gameConstant instanceof GameConversationNode) {
			boolean nodeContainsText = false;

			for (GameConstant child : ((GameConversationNode) gameConstant)
					.getChildren()) {
				nodeContainsText = nodeContainsText
						|| matchesSearchText(child, searchText);
			}

			return nodeContainsText;
		}

		return false;
	}

	/**
	 * Draws the tree.
	 */
	public void drawTree(PatternModel model, final String searchText,
			final Collection<String> validTypes) {
		this.removeAll();

		final List<String> types;

		types = new ArrayList<String>(validTypes);

		if (model == null || !(model instanceof StoryModel)) {
			return;
		}

		// Sort the types by alphabet
		Collections.sort(types, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				final GameTypeManager typeManager;

				typeManager = TranslatorManager.getInstance()
						.getActiveTranslator().getGameTypeManager();

				return String.CASE_INSENSITIVE_ORDER.compare(
						typeManager.getDisplayText(o1),
						typeManager.getDisplayText(o2));
			}
		});

		// Add the game objects to the tree model.
		for (String typeTag : types) {

			final Collection<GameConstant> gameObjects;

			gameObjects = this.getObjectsOfType((StoryModel) model, typeTag,
					searchText);

			// Ignore empty categories because they're confusing.
			if (gameObjects.size() <= 0)
				continue;
			else {
				final GameTypeManager typeManager;
				final String typeName;
				final GameObjectContainer container;

				typeManager = TranslatorManager.getInstance()
						.getActiveTranslator().getGameTypeManager();
				typeName = typeManager.getDisplayText(typeTag);
				container = new GameObjectContainer(typeName, gameObjects);

				this.add(container);
			}
		}

		this.repaint();
		this.revalidate();
	}

	/**
	 * Container of Game Objects. Displays the game objects inside it, and can
	 * be collapsed or expanded.
	 * 
	 * @author kschenk
	 * 
	 */
	private class GameObjectContainer extends JPanel {
		private boolean collapsed;

		/**
		 * Creates a new game object container with the passed in name and
		 * collection of GameObjects.
		 * 
		 * @param typeName
		 *            The name of the container
		 * @param gameConstants
		 *            The list of GameConstants in the container
		 */
		private GameObjectContainer(String typeName,
				final Collection<GameConstant> gameConstants) {
			this.collapsed = false;

			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			this.setBackground(Color.WHITE);

			this.redrawContainer(typeName, gameConstants);
		}

		/**
		 * Redraws the container. This also fires whenever the container is
		 * collapsed or expanded.
		 * 
		 * @param typeName
		 *            The name of the container
		 * @param gameConstants
		 *            The list of GameConstants in the container
		 */
		private void redrawContainer(final String typeName,
				final Collection<GameConstant> gameConstants) {
			this.removeAll();

			final JLabel categoryLabel;

			categoryLabel = new JLabel(typeName);

			categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

			categoryLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					GameObjectContainer.this.collapsed ^= true;
					redrawContainer(typeName, gameConstants);
				}
			});

			if (this.collapsed) {
				categoryLabel.setIcon(ScriptEaseUI.EXPAND_ICON);
				this.add(categoryLabel);
			} else {
				categoryLabel.setIcon(ScriptEaseUI.COLLAPSE_ICON);
				this.add(categoryLabel);

				for (GameConstant successor : gameConstants) {
					if (successor instanceof GameObject)
						this.add(createGameConstantPanel((GameObject) successor));
					else if (successor instanceof GameConversation)
						this.add(createGameConversationPanel((GameConversation) successor));
				}
			}

			this.add(Box.createHorizontalGlue());

			this.revalidate();
		}

		/**
		 * Creates a JPanel for the passed in GameConstant. This panel contains
		 * a binding widget that the user can then drag and drop into the
		 * appropriate slots.
		 * 
		 * @param gameConstant
		 *            The GameConstant to create a panel for
		 * @return
		 */
		private JPanel createGameConstantPanel(GameConstant gameConstant) {
			final JPanel objectPanel;
			final BindingWidget gameObjectBindingWidget;
			final String regularText;

			objectPanel = new JPanel();
			regularText = gameConstant.getName();

			objectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			objectPanel.setOpaque(true);

			objectPanel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
			objectPanel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);

			objectPanel.setLayout(new BoxLayout(objectPanel, BoxLayout.X_AXIS));

			gameObjectBindingWidget = new BindingWidget(
					new KnowItBindingConstant(gameConstant));

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
		 * Creates a game conversation panel using the passed in game
		 * conversation. This adds all of the game conversation's successors to
		 * the list as well.
		 * 
		 * @param gameConversation
		 *            The GameConversation to make a panel for
		 * @return
		 */
		private JPanel createGameConversationPanel(
				GameConversation gameConversation) {
			final JPanel convoPanel;

			convoPanel = new JPanel();
			convoPanel.setOpaque(false);

			convoPanel
					.setLayout(new BoxLayout(convoPanel, BoxLayout.PAGE_AXIS));

			convoPanel.add(createGameConstantPanel(gameConversation));

			for (GameConversationNode gameConversationNode : gameConversation
					.getConversationRoots()) {

				int indent;

				indent = 1;

				convoPanel
						.add(createIndentedPanel(gameConversationNode, indent));

				this.addConversationRoots(gameConversationNode.getChildren(),
						convoPanel, indent + 1);
			}

			convoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			return convoPanel;
		}

		/**
		 * Recursively adds all passed in roots to the passed in JPanel.
		 * 
		 * @param roots
		 *            The roots to add to the JPanel. Recursively adds these
		 *            roots' roots and so forth.
		 * @param convoPanel
		 *            The panel to add the roots to.
		 * @param indent
		 *            The amount to indent as defined in
		 *            {@link #createIndentedPanel(GameConstant, int)}.
		 *            Incremented for each level.
		 */
		private void addConversationRoots(
				List<? extends GameConversationNode> roots, JPanel convoPanel,
				int indent) {
			if (roots.isEmpty()) {
				return;
			}

			for (GameConversationNode root : roots) {
				// TODO We still need to implement linked conversations.
				if (root.isLink())
					continue;

				convoPanel.add(createIndentedPanel(root, indent));
				this.addConversationRoots(root.getChildren(), convoPanel,
						indent + 1);
			}
		}

		/**
		 * Creates an indented panel for the passed in GameConstant using the
		 * indent. Indents are made using a
		 * {@link Box#createRigidArea(Dimension)}, where the dimension is
		 * (5*indent, 0).
		 * 
		 * @param gameConstant
		 *            The constant to create a panel for
		 * @param indent
		 *            The indent
		 * @return
		 */
		private JPanel createIndentedPanel(GameConstant gameConstant, int indent) {
			final int STRUT_SIZE = 10 * indent;
			final JPanel indentedPanel;

			indentedPanel = new JPanel();

			indentedPanel.setLayout(new BoxLayout(indentedPanel,
					BoxLayout.LINE_AXIS));

			indentedPanel.setOpaque(false);

			indentedPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
			indentedPanel.add(createGameConstantPanel(gameConstant));

			indentedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			return indentedPanel;
		}
	}
}