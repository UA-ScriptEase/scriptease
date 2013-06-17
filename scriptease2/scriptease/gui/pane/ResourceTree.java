package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.ResourceTreeObserver;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.component.ExpansionButton;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.io.model.Resource;
import scriptease.util.GUIOp;
import scriptease.util.StringOp;

/**
 * Draws a ResourceTree for the passed in StoryModel. This panel is a tree of
 * {@link Resource}s that can be searched by text and types.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
class ResourceTree extends JPanel {

	/*
	 * TODO
	 * 
	 * We're doing this wrong. The tree should only have resource containers.
	 * Then, the resource container should hold the objects.
	 * 
	 * Then we only need to update the resource containers. Makes things better!
	 * 
	 * TODO Each resource container AND resource panel needs to store its
	 * open/closed state.
	 * 
	 * TODO We don't need to redraw the resource panels. Ever.
	 */

	private Resource selectedResource = null;

	private String filterText;

	private final List<String> filterTypes;
	private final Map<Resource, JPanel> panelMap;
	private final ObserverManager<ResourceTreeObserver> observerManager;

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
	 * Creates a new {@link ResourceTree} with the passed in model.
	 * 
	 * @param model
	 *            Creates a new {@link ResourceTree} with the passed in model.
	 *            If the passed in model is null or not a {@link StoryModel},
	 *            then nothing is drawn. Use {@link #redrawTree(StoryModel)} to
	 *            draw the tree later with a StoryModel.
	 */
	protected ResourceTree() {
		super();
		this.panelMap = new HashMap<Resource, JPanel>();
		this.filterTypes = new ArrayList<String>();
		this.filterText = "";
		this.observerManager = new ObserverManager<ResourceTreeObserver>();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.fillTree();
	}

	/**
	 * Empty and fill the tree with game constants from the passed in model. If
	 * the model is not a story model, the tree will be empty. This method will
	 * not draw the tree. To draw it, call {@link #redrawTree()} or use one of
	 * the filter methods.
	 * 
	 * @param model
	 */
	protected void fillTree() {
		this.panelMap.clear();

		final SEModel model = SEModelManager.getInstance().getActiveModel();
		if (!(model instanceof StoryModel))
			return;

		final StoryModel story = (StoryModel) model;

		for (String type : model.getTypeKeywords()) {
			final Collection<Resource> gameObjects;

			gameObjects = story.getModule().getResourcesOfType(type);

			for (Resource constant : gameObjects) {
				if (constant.getOwner() == null)
					this.panelMap.put(constant, new ResourcePanel(constant, 0));
			}
		}

		for (DialogueLine line : story.getDialogueRoots()) {
			this.panelMap.put(line, new ResourcePanel(line, 0));
		}
	}

	/**
	 * Refills the tree and then redraws it.
	 */
	protected void repaintTree() {
		this.fillTree();
		this.redrawTree();
	}

	/**
	 * Filters the tree by text and redraws it.
	 * 
	 * @param filterText
	 */
	protected void filterByText(String filterText) {
		this.filterText = filterText;
		this.redrawTree();
	}

	/**
	 * Filters the tree by types and redraws it.
	 * 
	 * @param filterTypes
	 */
	protected void filterByTypes(Collection<String> filterTypes) {
		this.filterTypes.clear();
		this.filterTypes.addAll(filterTypes);
		this.redrawTree();
	}

	/**
	 * Adds a new {@link ResourceTreeObserver} to the {@link ResourceTree}.
	 * 
	 * @param object
	 * @param observer
	 */
	protected void addObserver(Object object, ResourceTreeObserver observer) {
		this.observerManager.addObserver(object, observer);
	}

	/**
	 * Notifies all {@link ResourceTreeObserver}s of a selection.
	 * 
	 * @param resource
	 */
	private void notifyResourceSelected(Resource resource) {
		for (ResourceTreeObserver observer : this.observerManager
				.getObservers()) {
			observer.resourceSelected(resource);
		}
	}

	/**
	 * Notifies all {@link ResourceTreeObserver}s of the edit button getting
	 * clicked.
	 * 
	 * @param resource
	 */
	private void notifyEditButtonClicked(Resource resource) {
		for (ResourceTreeObserver observer : this.observerManager
				.getObservers()) {
			observer.resourceEditButtonPressed(resource);
		}
	}

	private String getDialogueType() {
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		final String dialogueType;

		if (model == null || !(model instanceof StoryModel))
			dialogueType = null;
		else
			dialogueType = ((StoryModel) model).getModule().getDialogueType();

		return dialogueType;
	}

	/**
	 * Redraws the tree. If you have not set some filter types with
	 * {@link #filterByTypes(Collection)}, the tree may get redrawn as empty.
	 * Since that method calls this method, it is usually a better idea to just
	 * use it instead.
	 */
	private void redrawTree() {
		this.removeAll();

		final SEModel model = SEModelManager.getInstance().getActiveModel();

		if (model == null || !(model instanceof StoryModel)) {
			this.repaint();
			this.revalidate();
			return;
		}

		final String dialogueType;
		final Map<String, List<Resource>> typesToResources;

		typesToResources = new TreeMap<String, List<Resource>>();
		dialogueType = this.getDialogueType();

		// Find constants that match the filters.
		for (Resource constant : this.panelMap.keySet()) {
			if (constant.getOwner() == null
					&& (constant.getOwnerName() == null || constant
							.getOwnerName().isEmpty())
					&& this.matchesFilters(constant)) {
				for (String type : constant.getTypes()) {

					List<Resource> constantList = typesToResources.get(type);
					if (constantList == null) {
						constantList = new ArrayList<Resource>();
					}

					constantList.add(constant);
					typesToResources.put(type, constantList);
				}
			}
		}

		// Add the dialogue type even if there are no dialogues.
		if (StringOp.exists(dialogueType)
				&& typesToResources.get(dialogueType) == null
				// Check to make sure we aren't hiding dialogue types.
				&& this.filterTypes.contains(dialogueType)) {
			typesToResources.put(dialogueType, new ArrayList<Resource>());
		}

		for (String type : typesToResources.keySet()) {
			final List<Resource> constantList;
			final String typeName;

			constantList = typesToResources.get(type);
			typeName = model.getTypeDisplayText(type);

			Collections.sort(constantList, constantSorter);

			this.add(new ResourceContainer(typeName, constantList));
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
	 * JPanel for the passed in Resource. This panel contains a binding widget
	 * that the user can then drag and drop into the appropriate slots.
	 * 
	 */
	private class ResourcePanel extends JPanel {
		private final Resource resource;
		private final JPanel childPanel;

		public ResourcePanel(Resource resource, int indent) {
			this.resource = resource;
			this.childPanel = new JPanel();

			final int STRUT_SIZE = 10 * indent;

			final String resourceName;
			final String resourceOwnerName;

			final BindingWidget gameObjectBindingWidget;

			final JPanel resourcePanel;

			resourceName = resource.getName();
			resourceOwnerName = resource.getOwnerName();

			gameObjectBindingWidget = new BindingWidget(
					new KnowItBindingResource(resource));

			resourcePanel = new JPanel();

			this.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
			this.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);

			resourcePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			resourcePanel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
			resourcePanel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);
			resourcePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			this.childPanel.setLayout(new BoxLayout(this.childPanel,
					BoxLayout.PAGE_AXIS));

			if (resource.isLink()) {
				gameObjectBindingWidget.setBackground(GUIOp.scaleColour(
						gameObjectBindingWidget.getBackground(), 1.24));
			}

			gameObjectBindingWidget.add(ScriptWidgetFactory.buildLabel(
					resourceName, Color.WHITE));

			this.add(Box.createHorizontalStrut(STRUT_SIZE));

			if (resource.getChildren().size() > 0)
				resourcePanel.add(this.createExpandChildButton(indent));
			else
				resourcePanel.add(Box.createRigidArea(new Dimension(10, 0)));

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

				this.add(prefixLabel);
			}

			resourcePanel.add(gameObjectBindingWidget);

			if (resource.getTypes().contains(
					ResourceTree.this.getDialogueType())
					&& resource instanceof DialogueLine) {
				resourcePanel.add(this.resourceEditButton());
				resourcePanel.add(this.resourceRemoveButton());
			}

			this.add(resourcePanel);
			this.add(this.childPanel);

			for (Resource child : resource.getChildren()) {
				final ResourcePanel childResourcePanel;

				childResourcePanel = new ResourcePanel(child, indent + 1);
				this.childPanel.add(childResourcePanel);

			}

			this.childPanel.setVisible(false);
		}

		private ExpansionButton createExpandChildButton(final int indent) {
			final ExpansionButton button;

			button = ScriptWidgetFactory.buildExpansionButton(true);

			button.addActionListener(new ActionListener() {
				private boolean collapsed = true;

				@Override
				public void actionPerformed(ActionEvent e) {
					button.setCollapsed(this.collapsed ^= true);

					ResourcePanel.this.childPanel.setVisible(!this.collapsed);

					ResourceTree.this.redrawTree();
				}
			});

			return button;
		}

		private JButton resourceEditButton() {
			final JButton editButton = ComponentFactory.buildEditButton();

			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ResourceTree.this.notifyEditButtonClicked(resource);
				}
			});

			return editButton;
		}

		private JButton resourceRemoveButton() {
			final JButton removeButton = ComponentFactory.buildRemoveButton();

			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO This should fire listeners instead.
					if (!(resource instanceof DialogueLine))
						return;

					final StoryModel story;

					story = SEModelManager.getInstance().getActiveStoryModel();

					story.removeDialogueRoot((DialogueLine) resource);
					ResourceTree.this.repaintTree();
				}
			});

			return removeButton;
		}
	}

	/**
	 * Sets the panel's background to the passed in color.
	 * 
	 * @param constant
	 * @param color
	 */
	private void setResourcePanelBackground(Resource constant, Color color) {
		final JPanel panel;

		// TODO Need to do this differently. Based on listeners or something.
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
			final JPanel categoryPanel;

			categoryLabel = new JLabel(typeName);
			categoryPanel = new JPanel();

			categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

			categoryLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
			categoryLabel.setForeground(Color.DARK_GRAY);

			categoryLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					ResourceContainer.this.collapsed ^= true;
					redrawContainer(typeName, resources);
				}
			});

			categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			categoryPanel.setLayout(new BoxLayout(categoryPanel,
					BoxLayout.LINE_AXIS));
			categoryPanel.setOpaque(false);

			categoryPanel.add(categoryLabel);
			categoryPanel.add(ComponentFactory.buildSpacer(15, 15));

			final String dialogueType = ResourceTree.this.getDialogueType();

			if (StringOp.exists(dialogueType) && typeName.equals(dialogueType)) {
				categoryPanel.add(this.resourceAddButton());
			}

			categoryPanel.add(Box.createHorizontalGlue());

			if (this.collapsed) {
				categoryLabel.setIcon(ScriptEaseUI.EXPAND_ICON);
				this.add(categoryPanel);
			} else {
				categoryLabel.setIcon(ScriptEaseUI.COLLAPSE_ICON);
				this.add(categoryPanel);

				for (final Resource resource : resources) {
					this.addGameConstant(resource);
				}
			}

			this.add(Box.createHorizontalGlue());

			this.revalidate();
		}

		private JButton resourceAddButton() {
			final JButton addResource = ComponentFactory.buildAddButton();

			addResource.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Make a listener fire instead. "Resource added" or
					// something.
					final StoryModel story;

					story = SEModelManager.getInstance().getActiveStoryModel();

					story.addDialogueRoot();
					ResourceTree.this.repaintTree();
				}
			});

			return addResource;
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

					// TODO Need to do this differently. Maybe on notify
					// resource selected..

					setResourcePanelBackground(selectedResource,
							ScriptEaseUI.SELECTED_COLOUR);

					ResourceTree.this.notifyResourceSelected(selectedResource);
				}
			});

			this.add(constantPanel);

			final List<Resource> constantList;

			constantList = new ArrayList<Resource>(constant.getChildren());

			Collections.sort(constantList, constantSorter);

			for (Resource child : constantList) {
				if (matchesFilters(child)
						&& ResourceTree.this.panelMap.containsKey(child))
					this.addGameConstant(child);
			}
		}
	}
}