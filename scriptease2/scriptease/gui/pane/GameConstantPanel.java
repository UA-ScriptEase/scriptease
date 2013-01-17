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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
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
import scriptease.util.GUIOp;

/**
 * Draws a Game Constant Panel for the passed in StoryModel. This panel is a
 * tree of Game Constants that can be searched by text and types.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class GameConstantPanel extends JPanel {
	private final Color LINE_COLOR_1 = Color.red;
	private final Color LINE_COLOR_2 = Color.blue;

	private GameConstant selectedConstant = null;

	private String filterText;
	private final List<String> filterTypes;

	private Map<GameConstant, JPanel> panelMap;

	/**
	 * Creates a new GameConstantPanel with the passed in model.
	 * 
	 * @param model
	 *            Creates a new GameConstantPanel with the passed in model. If
	 *            the passed in model is null or not a StoryModel, then nothing
	 *            is drawn. Use {@link #redrawTree(StoryModel)} to draw the tree
	 *            later with a StoryModel.
	 */
	public GameConstantPanel(PatternModel model) {
		super();
		this.panelMap = new HashMap<GameConstant, JPanel>();
		this.filterTypes = new ArrayList<String>();
		this.filterText = "";

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		final GameTypeManager typeManager;

		typeManager = TranslatorManager.getInstance()
				.getActiveGameTypeManager();

		if (typeManager != null) {
			this.filterTypes.addAll(typeManager.getKeywords());
		}

		this.fillTree(model);
		this.filterByTypes(this.filterTypes);
	}

	/**
	 * Empty and fill the tree with game constants from the passed in model. If
	 * the model is not a story model, the tree will be empty. This method will
	 * not draw the tree. To draw it, call {@link #redrawTree()} or use one of
	 * the filter methods.
	 * 
	 * @param model
	 */
	public void fillTree(PatternModel model) {
		this.panelMap.clear();

		if (!(model instanceof StoryModel))
			return;

		final GameTypeManager typeManager;
		final List<String> types;

		typeManager = TranslatorManager.getInstance()
				.getActiveGameTypeManager();
		types = new ArrayList<String>(typeManager.getKeywords());

		for (String type : types) {
			final Collection<GameConstant> gameObjects;

			gameObjects = ((StoryModel) model).getModule().getResourcesOfType(
					type);

			for (GameConstant constant : gameObjects) {
				if (constant instanceof GameObject)
					this.panelMap.put(constant,
							createGameConstantPanel(constant));
				else if (constant instanceof GameConversation) {
					this.panelMap.put(constant,
							createGameConstantPanel(constant));

					for (GameConversationNode gameConversationNode : ((GameConversation) constant)
							.getConversationRoots()) {
						final int indent;

						indent = 1;

						this.panelMap.put(
								gameConversationNode,
								createIndentedConversationPanel(
										gameConversationNode, indent,
										gameConversationNode.getSpeaker(),
										this.LINE_COLOR_1));

						this.addConversationRoots(
								gameConversationNode.getChildren(), indent + 1);
					}

				} else {
					final JPanel invalidPanel = new JPanel();
					invalidPanel.add(new JLabel("Invalid GameConstant"));
					this.panelMap.put(constant, invalidPanel);
				}
			}
		}
	}

	/**
	 * Filters the tree by text and redraws it.
	 * 
	 * @param filterText
	 */
	public void filterByText(String filterText) {
		this.filterText = filterText;
		this.redrawTree();
	}

	/**
	 * Filters the tree by types and redraws it.
	 * 
	 * @param filterTypes
	 */
	public void filterByTypes(Collection<String> filterTypes) {
		this.filterTypes.clear();
		this.filterTypes.addAll(filterTypes);
		this.redrawTree();
	}

	/**
	 * Redraws the tree. If you have not set some filter types with
	 * {@link #filterByTypes(Collection)}, the tree may get redrawn as empty.
	 * Since that method calls this method, it is usually a better idea to just
	 * use it instead.
	 */
	public void redrawTree() {
		this.removeAll();

		final Map<String, List<GameConstant>> constantMap;

		constantMap = new TreeMap<String, List<GameConstant>>();

		// Find constants that match the filters.
		for (GameConstant constant : this.panelMap.keySet()) {
			if (!(constant instanceof GameConversationNode)
					&& this.matchesFilters(constant)) {
				for (String type : constant.getTypes()) {
					List<GameConstant> constantList = constantMap.get(type);
					if (constantList == null) {
						constantList = new ArrayList<GameConstant>();
					}
					constantList.add(constant);

					constantMap.put(type, constantList);
				}
			}
		}

		if (constantMap.size() <= 0)
			return;

		for (String type : constantMap.keySet()) {
			final List<GameConstant> constantList;

			constantList = constantMap.get(type);

			Collections.sort(constantList, new Comparator<GameConstant>() {
				@Override
				public int compare(GameConstant o1, GameConstant o2) {
					return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(),
							o2.getName());
				}
			});

			final GameTypeManager typeManager;
			final String typeName;
			final GameObjectContainer container;

			typeManager = TranslatorManager.getInstance().getActiveTranslator()
					.getGameTypeManager();
			typeName = typeManager.getDisplayText(type);
			container = new GameObjectContainer(typeName, constantList);

			this.add(container);
		}

		this.repaint();
		this.revalidate();
	}

	/**
	 * Checks whether a constant matches the current filters.
	 * 
	 * @param constant
	 * @return
	 */
	private boolean matchesFilters(GameConstant constant) {
		boolean match = false;

		for (String type : constant.getTypes()) {
			if (this.filterTypes.contains(type)) {
				match = true;
				break;
			}
		}

		if (match == true) {
			match = matchesSearchText(constant);
		}

		return match;
	}

	/**
	 * Determines whether any fields in the game constant match the passed in
	 * text. Recursively searches Conversations to see if any roots contain the
	 * text.
	 * 
	 * @param gameConstant
	 * @return true if text is found
	 */
	private boolean matchesSearchText(GameConstant gameConstant) {
		if (this.filterText.length() == 0)
			return true;

		final String searchTextUpperCase;

		searchTextUpperCase = this.filterText.toUpperCase();

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
				nodeContainsText = nodeContainsText || matchesSearchText(child);
			}
			return nodeContainsText;
		} else if (gameConstant instanceof GameConversationNode) {
			boolean nodeContainsText = false;

			for (GameConstant child : ((GameConversationNode) gameConstant)
					.getChildren()) {
				nodeContainsText = nodeContainsText || matchesSearchText(child);
			}
			return nodeContainsText;
		}

		return false;
	}

	/**
	 * Creates a JPanel for the passed in GameConstant. This panel contains a
	 * binding widget that the user can then drag and drop into the appropriate
	 * slots.
	 * 
	 * @param gameConstant
	 *            The GameConstant to create a panel for
	 * @return
	 */
	private JPanel createGameConstantPanel(final GameConstant gameConstant) {
		final JPanel objectPanel;
		final BindingWidget gameObjectBindingWidget;
		final String regularText;

		objectPanel = new JPanel();
		regularText = gameConstant.getName();

		objectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		objectPanel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
		objectPanel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);

		objectPanel.setLayout(new BoxLayout(objectPanel, BoxLayout.X_AXIS));

		gameObjectBindingWidget = new BindingWidget(new KnowItBindingConstant(
				gameConstant));

		if (gameConstant instanceof GameConversationNode) {
			if (((GameConversationNode) gameConstant).isLink())
				gameObjectBindingWidget.setBackground(GUIOp.scaleColour(
						gameObjectBindingWidget.getBackground(), 1.24));
		}

		gameObjectBindingWidget.add(ScriptWidgetFactory.buildLabel(regularText,
				Color.WHITE));

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
	 * Recursively adds all passed in roots to the passed in JPanel.
	 * 
	 * @param roots
	 *            The roots to add to the JPanel. Recursively adds these roots'
	 *            roots and so forth.
	 * @param indent
	 *            The amount to indent as defined in
	 *            {@link #createIndentedConversationPanel(GameConstant, int)} .
	 *            Incremented for each level.
	 */
	private void addConversationRoots(
			List<? extends GameConversationNode> roots, int indent) {
		if (roots.isEmpty()) {
			return;
		}

		for (GameConversationNode root : roots) {
			final String speaker;
			final JPanel nodePanel;
			final Color color;

			if (indent % 2 == 0) {
				color = this.LINE_COLOR_2;
			} else {
				color = this.LINE_COLOR_1;
			}

			if (root.isLink())
				speaker = "Link";
			else
				speaker = root.getSpeaker();

			nodePanel = createIndentedConversationPanel(root, indent, speaker,
					color);

			this.panelMap.put(root, nodePanel);

			this.addConversationRoots(root.getChildren(), indent + 1);
		}
	}

	/**
	 * Creates an indented panel for the passed in GameConstant using the
	 * indent. Indents are made using a {@link Box#createHorizontalStrut(int)},
	 * where the dimension is (10*indent, 0).
	 * 
	 * @param gameConversationNode
	 *            The constant to create a panel for
	 * @param indent
	 *            The indent
	 * @param speaker
	 *            The speaker of the dialogue line
	 * @param color
	 *            The color of the speaker label
	 * 
	 * @return
	 */
	private JPanel createIndentedConversationPanel(
			GameConversationNode gameConversationNode, int indent,
			String speaker, Color color) {
		final int STRUT_SIZE = 10 * indent;
		final JLabel prefixLabel;
		final JPanel indentedPanel;

		prefixLabel = new JLabel();
		indentedPanel = new JPanel();

		indentedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		indentedPanel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
		indentedPanel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);

		indentedPanel.setLayout(new BoxLayout(indentedPanel, BoxLayout.X_AXIS));

		prefixLabel.setOpaque(true);

		prefixLabel.setBackground(Color.LIGHT_GRAY);

		if (speaker != null && !speaker.isEmpty() && color != null) {
			final char firstChar;

			firstChar = speaker.charAt(0);

			prefixLabel.setText(" " + firstChar + " ");

			prefixLabel.setForeground(color);
		}

		indentedPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
		indentedPanel.add(prefixLabel);

		final JPanel nodePanel;

		nodePanel = createGameConstantPanel(gameConversationNode);

		nodePanel.setOpaque(false);

		indentedPanel.add(nodePanel);

		return indentedPanel;
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

				for (final GameConstant gameConstant : gameConstants) {
					this.addGameConstant(gameConstant);
				}
			}

			this.add(Box.createHorizontalGlue());

			this.revalidate();
		}

		private void addGameConstant(final GameConstant constant) {
			final JComponent constantPanel;

			constantPanel = panelMap.get(constant);

			if (selectedConstant == constant)
				constantPanel.setBackground(ScriptEaseUI.SELECTED_COLOUR);

			if (constantPanel == null) {
				System.out.println("Constant has null component: " + constant);
				return;
			}

			constantPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					setGameConstantBackground(selectedConstant,
							ScriptEaseUI.UNSELECTED_COLOUR);

					selectedConstant = constant;

					setGameConstantBackground(selectedConstant,
							ScriptEaseUI.SELECTED_COLOUR);
				}
			});

			this.add(constantPanel);

			if (constant instanceof GameConversation) {
				for (GameConversationNode child : ((GameConversation) constant)
						.getConversationRoots()) {
					if (matchesFilters(child))
						this.addGameConstant(child);
				}
			} else if (constant instanceof GameConversationNode) {
				for (GameConstant child : ((GameConversationNode) constant)
						.getChildren()) {
					if (matchesFilters(child))
						this.addGameConstant(child);
				}
			}
		}
	}

	private void setGameConstantBackground(GameConstant constant, Color color) {
		final JPanel panel;

		panel = this.panelMap.get(constant);

		if (panel == null)
			return;

		panel.setBackground(color);

		if (constant instanceof GameConversationNode) {
			// TODO Find all links and colour them.
			// Go down children of parent GameConversation. Find all
			// GameConversationNodes that have the same name and children (?),
			// and are links.
		}
	}
}