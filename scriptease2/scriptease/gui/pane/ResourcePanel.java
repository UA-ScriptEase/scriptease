package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ExpansionButton;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.SEModel;
import scriptease.model.StoryModel;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.translator.io.model.Resource;
import scriptease.util.GUIOp;

/**
 * Draws a ResourcePanel for the passed in StoryModel. This panel is a tree of
 * {@link Resource}s that can be searched by text and types.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class ResourcePanel extends JPanel {
	private Resource selectedResource = null;

	private String filterText;
	private final List<String> filterTypes;

	private final Map<Resource, JPanel> panelMap;

	private static final Comparator<Resource> constantSorter = new Comparator<Resource>() {
		@Override
		public int compare(Resource o1, Resource o2) {
			String compareString1 = "";
			String compareString2 = "";

			for (String type : o1.getTypes()) {
				compareString1 += type;
			}

			for (String type : o2.getTypes()) {
				compareString2 += type;
			}

			compareString1 += o1.getName();
			compareString2 += o2.getName();

			return String.CASE_INSENSITIVE_ORDER.compare(compareString1,
					compareString2);
		}
	};

	/**
	 * Creates a new {@link ResourcePanel} with the passed in model.
	 * 
	 * @param model
	 *            Creates a new {@link ResourcePanel} with the passed in model.
	 *            If the passed in model is null or not a {@link StoryModel},
	 *            then nothing is drawn. Use {@link #redrawTree(StoryModel)} to
	 *            draw the tree later with a StoryModel.
	 */
	public ResourcePanel(SEModel model) {
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
	public void fillTree(SEModel model) {
		this.panelMap.clear();

		final GameTypeManager typeManager;

		typeManager = TranslatorManager.getInstance()
				.getActiveGameTypeManager();

		if (!(model instanceof StoryModel) || typeManager == null)
			return;

		for (String type : typeManager.getKeywords()) {
			final Collection<Resource> gameObjects;

			gameObjects = ((StoryModel) model).getModule().getResourcesOfType(
					type);

			for (Resource constant : gameObjects) {
				if (constant.getOwner() == null)
					this.panelMap.put(constant,
							createGameConstantPanel(constant, 0));
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
			if (constant.getOwner() == null
					&& (constant.getOwnerName() == null || constant
							.getOwnerName().isEmpty())
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

			Collections.sort(constantList, constantSorter);

			final GameTypeManager typeManager;
			final String typeName;
			final ResourceContainer container;

			typeManager = TranslatorManager.getInstance()
					.getActiveGameTypeManager();
			typeName = typeManager.getDisplayText(type);
			container = new ResourceContainer(typeName, constantList);

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
	private JPanel createGameConstantPanel(final Resource resource,
			final int indent) {
		final int STRUT_SIZE = 10 * indent;

		final String resourceName;
		final String resourceOwnerName;

		final JPanel objectPanel;
		final BindingWidget gameObjectBindingWidget;

		resourceName = resource.getName();
		resourceOwnerName = resource.getOwnerName();

		objectPanel = new JPanel();
		gameObjectBindingWidget = new BindingWidget(new KnowItBindingResource(
				resource));

		objectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		objectPanel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
		objectPanel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);
		objectPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		if (resource.isLink()) {
			gameObjectBindingWidget.setBackground(GUIOp.scaleColour(
					gameObjectBindingWidget.getBackground(), 1.24));
		}

		gameObjectBindingWidget.add(ScriptWidgetFactory.buildLabel(
				resourceName, Color.WHITE));

		objectPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

		if (resource.getChildren().size() > 0)
			objectPanel.add(this.createExpandChildButton(resource, indent));
		else
			objectPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		if (resourceOwnerName != null && !resourceOwnerName.isEmpty()) {
			final Color LINE_COLOR_1 = Color.red;
			final Color LINE_COLOR_2 = Color.blue;

			final JLabel prefixLabel;

			prefixLabel = new JLabel();

			prefixLabel.setOpaque(true);
			prefixLabel.setBackground(Color.LIGHT_GRAY);
			prefixLabel.setText(" " + resourceOwnerName.charAt(0) + " ");

			if (indent % 2 == 0) {
				prefixLabel.setForeground(LINE_COLOR_2);
			} else {
				prefixLabel.setForeground(LINE_COLOR_1);
			}

			objectPanel.add(prefixLabel);
		}

		objectPanel.add(gameObjectBindingWidget);

		// Need to do this because BoxLayout respects maximum size.
		objectPanel.setMaximumSize(objectPanel.getPreferredSize());

		return objectPanel;
	}

	private ExpansionButton createExpandChildButton(final Resource resource,
			final int indent) {
		final ExpansionButton button;

		button = ScriptWidgetFactory.buildExpansionButton(true);

		button.addActionListener(new ActionListener() {
			private boolean collapsed = true;

			private void setChildrenInvisible(List<Resource> children) {
				for (Resource child : children) {
					final JPanel panel = ResourcePanel.this.panelMap.get(child);
					if (panel != null) {
						ResourcePanel.this.panelMap.remove(child);
						this.setChildrenInvisible(child.getChildren());
					}
				}
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				button.setCollapsed(this.collapsed ^= true);

				if (this.collapsed) {
					this.setChildrenInvisible(resource.getChildren());
				} else {
					for (Resource child : resource.getChildren()) {
						final JPanel nodePanel;

						nodePanel = ResourcePanel.this.panelMap.get(child);

						if (nodePanel == null)
							ResourcePanel.this.panelMap.put(child,
									createGameConstantPanel(child, indent + 1));
						else
							nodePanel.setVisible(true);
					}
				}

				ResourcePanel.this.redrawTree();
			}
		});

		return button;
	}

	/**
	 * Sets the panel's background to the passed in color.
	 * 
	 * @param constant
	 * @param color
	 */
	private void setResourcePanelBackground(Resource constant, Color color) {
		final JPanel panel;

		panel = this.panelMap.get(constant);

		if (panel == null)
			return;

		panel.setBackground(color);

		if (constant.isLink()) {
			// TODO Select all same nodes.
		}
	}

	/**
	 * Container of Game Objects. Displays the game objects inside it, and can
	 * be collapsed or expanded.
	 * 
	 * @author kschenk
	 * 
	 */
	private class ResourceContainer extends JPanel {
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
		private ResourceContainer(String typeName,
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
		 * @param resources
		 *            The list of GameConstants in the container
		 */
		private void redrawContainer(final String typeName,
				final Collection<Resource> resources) {
			this.removeAll();

			final JLabel categoryLabel;

			categoryLabel = new JLabel(typeName);

			categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

			categoryLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					ResourceContainer.this.collapsed ^= true;
					redrawContainer(typeName, resources);
				}
			});

			if (this.collapsed) {
				categoryLabel.setIcon(ScriptEaseUI.EXPAND_ICON);
				this.add(categoryLabel);
			} else {
				categoryLabel.setIcon(ScriptEaseUI.COLLAPSE_ICON);
				this.add(categoryLabel);

				for (final Resource resource : resources) {
					this.addGameConstant(resource);
				}
			}

			this.add(Box.createHorizontalGlue());

			this.revalidate();
		}

		private void addGameConstant(final Resource constant) {
			final JComponent constantPanel;

			constantPanel = panelMap.get(constant);

			if (selectedResource == constant)
				constantPanel.setBackground(ScriptEaseUI.SELECTED_COLOUR);

			if (constantPanel == null) {
				System.err.println("Constant has null component: " + constant);
				return;
			}

			constantPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					setResourcePanelBackground(selectedResource,
							ScriptEaseUI.UNSELECTED_COLOUR);

					selectedResource = constant;

					setResourcePanelBackground(selectedResource,
							ScriptEaseUI.SELECTED_COLOUR);
				}
			});

			this.add(constantPanel);

			final List<Resource> constantList;

			constantList = new ArrayList<Resource>(constant.getChildren());

			Collections.sort(constantList, constantSorter);

			for (Resource child : constantList) {
				if (matchesFilters(child)
						&& ResourcePanel.this.panelMap.containsKey(child))
					this.addGameConstant(child);
			}
		}
	}
}