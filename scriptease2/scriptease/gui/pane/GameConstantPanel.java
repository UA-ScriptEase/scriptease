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
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.translator.io.model.Resource;
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

	private Resource selectedConstant = null;

	private String filterText;
	private final List<String> filterTypes;

	private Map<Resource, JPanel> panelMap;

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
		this.panelMap = new HashMap<Resource, JPanel>();
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
			final Collection<Resource> gameObjects;

			gameObjects = ((StoryModel) model).getModule().getResourcesOfType(
					type);

			for (Resource constant : gameObjects) {
				this.panelMap.put(constant, createGameConstantPanel(constant));

				for (Resource child : constant.getChildren()) {
					final int indent;

					indent = 1;

					this.panelMap.put(
							child,
							createIndentedConversationPanel(child, indent,
									child.getOwnerName(), this.LINE_COLOR_1));

					this.addConversationRoots(child.getChildren(), indent + 1);
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

		final Map<String, List<Resource>> constantMap;

		constantMap = new TreeMap<String, List<Resource>>();

		// Find constants that match the filters.
		for (Resource constant : this.panelMap.keySet()) {
			if (constant.getOwnerName().isEmpty()
					&& this.matchesFilters(constant)) {
				for (String type : constant.getTypes()) {
					List<Resource> constantList = constantMap.get(type);
					if (constantList == null) {
						constantList = new ArrayList<Resource>();
					}
					constantList.add(constant);

					constantMap.put(type, constantList);
				}
			}
		}

		if (constantMap.size() <= 0)
			return;

		for (String type : constantMap.keySet()) {
			final List<Resource> constantList;

			constantList = constantMap.get(type);

			Collections.sort(constantList, new Comparator<Resource>() {
				@Override
				public int compare(Resource o1, Resource o2) {
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
	private boolean matchesFilters(Resource constant) {
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
	 * @param resource
	 * @return true if text is found
	 */
	private boolean matchesSearchText(Resource resource) {
		if (this.filterText.length() == 0)
			return true;

		final String searchText;

		searchText = this.filterText.toUpperCase();

		if (resource.getCodeText().toUpperCase().contains(searchText)
				|| resource.getName().toUpperCase().contains(searchText)
				|| resource.getTag().toUpperCase().contains(searchText)
				|| resource.getTemplateID().toUpperCase().contains(searchText)) {
			return true;
		}

		boolean nodeContainsText = false;

		for (Resource child : resource.getChildren()) {
			nodeContainsText = nodeContainsText || matchesSearchText(child);
		}

		return nodeContainsText;
	}

	/**
	 * Creates a JPanel for the passed in GameConstant. This panel contains a
	 * binding widget that the user can then drag and drop into the appropriate
	 * slots.
	 * 
	 * @param resource
	 *            The GameConstant to create a panel for
	 * @return
	 */
	private JPanel createGameConstantPanel(final Resource resource) {
		final JPanel objectPanel;
		final BindingWidget gameObjectBindingWidget;
		final String regularText;

		objectPanel = new JPanel();
		regularText = resource.getName();

		objectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		objectPanel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
		objectPanel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);

		objectPanel.setLayout(new BoxLayout(objectPanel, BoxLayout.X_AXIS));

		gameObjectBindingWidget = new BindingWidget(new KnowItBindingResource(
				resource));

		if (resource.isLink())
			gameObjectBindingWidget.setBackground(GUIOp.scaleColour(
					gameObjectBindingWidget.getBackground(), 1.24));

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
	private void addConversationRoots(List<Resource> roots, int indent) {
		if (roots.isEmpty()) {
			return;
		}

		for (Resource root : roots) {
			final String speaker;
			final JPanel nodePanel;
			final Color color;

			if (indent % 2 == 0) {
				color = this.LINE_COLOR_2;
			} else {
				color = this.LINE_COLOR_1;
			}

			speaker = root.getOwnerName();

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
	 * @param child
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
	private JPanel createIndentedConversationPanel(Resource child, int indent,
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

		nodePanel = createGameConstantPanel(child);

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
				final Collection<Resource> gameConstants) {
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
				final Collection<Resource> gameConstants) {
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

				for (final Resource gameConstant : gameConstants) {
					this.addGameConstant(gameConstant);
				}
			}

			this.add(Box.createHorizontalGlue());

			this.revalidate();
		}

		private void addGameConstant(final Resource constant) {
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

			for (Resource child : constant.getChildren()) {
				if (matchesFilters(child))
					this.addGameConstant(child);
			}
		}
	}

	private void setGameConstantBackground(Resource constant, Color color) {
		final JPanel panel;

		panel = this.panelMap.get(constant);

		if (panel == null)
			return;

		panel.setBackground(color);

		if (constant.isLink()) {
			// TODO Select all same nodes.
		}
	}
}