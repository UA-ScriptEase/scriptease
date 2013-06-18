package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	// TODO The panels + containers should listen to search changes.

	private Resource selectedResource = null;

	private String filterText;

	private final ObserverManager<ResourceTreeObserver> observerManager;

	private final Collection<ResourceContainer> containers;
	private final Collection<String> filterTypes;

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
		this.filterTypes = new ArrayList<String>();
		this.containers = new ArrayList<ResourceContainer>();
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
		this.containers.clear();
		this.removeAll();

		final StoryModel story;

		story = SEModelManager.getInstance().getActiveStoryModel();

		if (story == null) {
			this.repaint();
			this.revalidate();
			return;
		}

		final List<String> types;

		types = new ArrayList<String>(story.getTypeKeywords());

		Collections.sort(types);

		// Add the dialogue type even if there are no dialogues.
		for (String type : types) {

			final ResourceContainer containerPanel;

			containerPanel = new ResourceContainer(
					story.getTypeDisplayText(type));

			this.add(containerPanel);
			this.containers.add(containerPanel);
		}

		this.repaint();
		this.revalidate();
	}

	protected void updateCategory(String type) {
		for (ResourceContainer container : this.containers) {
			if (container.type.equals(type)) {
				container.updateResourcePanels();
				break;
			}
		}
	}

	/**
	 * Filters the tree by text and redraws it.
	 * 
	 * @param filterText
	 */
	protected void filterByText(String filterText) {
		this.filterText = filterText;

		for (ResourceContainer container : this.containers) {
			container.updateResourcePanels();
		}
	}

	/**
	 * Filters the tree by types and redraws it.
	 * 
	 * @param filterTypes
	 */
	protected void filterByTypes(Collection<String> filterTypes) {
		this.filterTypes.clear();
		this.filterTypes.addAll(filterTypes);

		// TODO Filter types arent working...

		// TODO Filtering is beyond slow.

		for (ResourceContainer container : this.containers) {
			container.updateResourcePanels();
		}
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

	private class ResourceContainer extends JPanel {
		private final Map<Resource, JPanel> resourcesToPanels;
		private final String type;
		private final JPanel container;

		/**
		 * Creates a new game object container with the passed in name and
		 * collection of GameObjects.
		 * 
		 * @param typeName
		 *            The name of the container
		 * @param gameConstants
		 *            The list of GameConstants in the container
		 */
		public ResourceContainer(String type) {
			this.type = type;
			this.container = new JPanel();
			this.resourcesToPanels = new HashMap<Resource, JPanel>();

			final JLabel categoryLabel;
			final ExpansionButton button;

			final JPanel categoryPanel;

			button = ScriptWidgetFactory.buildExpansionButton(false);

			categoryLabel = new JLabel(type);
			categoryPanel = new JPanel();

			categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			categoryLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
			categoryLabel.setForeground(Color.DARK_GRAY);

			button.setContentAreaFilled(false);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final boolean isVisible = container.isVisible();
					button.setCollapsed(isVisible);
					container.setVisible(!isVisible);
				}
			});

			categoryLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					button.doClick();
				}
			});

			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			this.setBackground(Color.WHITE);

			this.container.setLayout(new BoxLayout(this.container,
					BoxLayout.PAGE_AXIS));

			categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			categoryPanel.setLayout(new BoxLayout(categoryPanel,
					BoxLayout.LINE_AXIS));
			categoryPanel.setOpaque(false);

			categoryPanel.add(button);
			categoryPanel.add(categoryLabel);
			categoryPanel.add(ComponentFactory.buildSpacer(15, 15));

			final String dialogueType = ResourceTree.this.getDialogueType();

			if (StringOp.exists(dialogueType) && type.equals(dialogueType)) {
				final JButton addButton = ComponentFactory.buildAddButton();

				addButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Make a listener fire instead. "Resource added"
						// or something.
						final StoryModel story;

						story = SEModelManager.getInstance()
								.getActiveStoryModel();

						story.addDialogueRoot();

						ResourceContainer.this.updateResourcePanels();
					}
				});

				categoryPanel.add(addButton);
			}

			categoryPanel.add(Box.createHorizontalGlue());

			this.add(categoryPanel);

			this.add(Box.createHorizontalGlue());

			this.add(this.container);

			this.updateResourcePanels();
		}

		private void updateResourcePanels() {
			// TODO need to update when the resource is changed..?
			
			final StoryModel story;

			story = SEModelManager.getInstance().getActiveStoryModel();

			if (story == null || !ResourceTree.this.filterTypes.contains(type)) {
				this.setVisible(false);
				return;
			}

			this.setVisible(true);

			final String dialogueType = story.getModule().getDialogueType();

			final List<Resource> resources;

			if (StringOp.exists(dialogueType) && type.equals(dialogueType))
				resources = new ArrayList<Resource>(story.getDialogueRoots());
			else {
				resources = new ArrayList<Resource>(story.getModule()
						.getResourcesOfType(type));

				if (resources == null || resources.isEmpty()) {
					this.setVisible(false);
					return;
				}
			}

			Collections.sort(resources, constantSorter);

			// Go through the collection of resources and add those that do not
			// exist to the map.
			int previousIndex = -1;
			for (Resource resource : resources) {
				final JPanel panel = this.resourcesToPanels.get(resource);
				if (panel == null) {
					final JPanel newPanel;

					newPanel = this.createResourcePanel(resource, 0);

					this.resourcesToPanels.put(resource, newPanel);
					this.container.add(newPanel, ++previousIndex);
				} else
					previousIndex = GUIOp.getComponentIndex(panel);
			}

			final Collection<Entry<Resource, JPanel>> entrySet;

			entrySet = new HashSet<Entry<Resource, JPanel>>(
					this.resourcesToPanels.entrySet());

			// Go through the map and change visibility of resources. Remove any
			// that no longer exist in the model.
			for (Entry<Resource, JPanel> entry : entrySet) {
				final Resource resource = entry.getKey();
				final JPanel panel = entry.getValue();

				if (resources.contains(resource)) {
					panel.setVisible(ResourceTree.this
							.matchesSearchText(resource));
				} else {
					this.container.remove(panel);
					this.resourcesToPanels.remove(resource);
				}
			}

			this.revalidate();
		}

		/**
		 * JPanel for the passed in Resource. This panel contains a binding
		 * widget that the user can then drag and drop into the appropriate
		 * slots.
		 * 
		 */
		private JPanel createResourcePanel(final Resource resource, int indent) {
			final JPanel panel = new JPanel();

			final int STRUT_SIZE = 10 * indent;

			final String resourceName;
			final String resourceOwnerName;

			final BindingWidget gameObjectBindingWidget;

			final JPanel childPanel;
			final JPanel resourcePanel;

			resourceName = resource.getName();
			resourceOwnerName = resource.getOwnerName();

			gameObjectBindingWidget = new BindingWidget(
					new KnowItBindingResource(resource));

			childPanel = new JPanel();
			resourcePanel = new JPanel();

			panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
			panel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			resourcePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			resourcePanel.setBorder(ScriptEaseUI.UNSELECTED_BORDER);
			resourcePanel.setOpaque(false);
			resourcePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

			childPanel
					.setLayout(new BoxLayout(childPanel, BoxLayout.PAGE_AXIS));

			if (resource.isLink()) {
				gameObjectBindingWidget.setBackground(GUIOp.scaleColour(
						gameObjectBindingWidget.getBackground(), 1.24));
			}

			gameObjectBindingWidget.add(ScriptWidgetFactory.buildLabel(
					resourceName, Color.WHITE));

			panel.add(Box.createHorizontalStrut(STRUT_SIZE));

			if (resource.getChildren().size() > 0) {
				final ExpansionButton button;

				button = ScriptWidgetFactory.buildExpansionButton(true);

				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final boolean isVisible = childPanel.isVisible();
						button.setCollapsed(isVisible);
						childPanel.setVisible(!isVisible);
					}
				});

				resourcePanel.add(button);
			} else
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

				panel.add(prefixLabel);
			}

			resourcePanel.add(gameObjectBindingWidget);

			if (resource.getTypes().contains(
					ResourceTree.this.getDialogueType())
					&& resource instanceof DialogueLine) {

				final JButton editButton = ComponentFactory.buildEditButton();
				final JButton removeButton = ComponentFactory
						.buildRemoveButton();

				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ResourceTree.this.notifyEditButtonClicked(resource);
					}
				});

				removeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO This should fire listeners instead.
						if (!(resource instanceof DialogueLine))
							return;

						final StoryModel story;
						final Container parent;

						story = SEModelManager.getInstance()
								.getActiveStoryModel();
						parent = panel.getParent();

						story.removeDialogueRoot((DialogueLine) resource);

						if (parent != null) {
							parent.remove(panel);

							if (parent instanceof JComponent) {
								((JComponent) parent).revalidate();
							}
						}
					}
				});

				resourcePanel.add(editButton);
				resourcePanel.add(removeButton);
			}

			panel.add(resourcePanel);
			panel.add(childPanel);

			for (Resource child : resource.getChildren()) {
				childPanel.add(this.createResourcePanel(child, indent + 1));
			}

			if (selectedResource == resource)
				panel.setBackground(ScriptEaseUI.SELECTED_COLOUR);

			panel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					selectedResource = resource;
					ResourceTree.this.notifyResourceSelected(selectedResource);
				}
			});

			ResourceTree.this.addObserver(this, new ResourceTreeObserver() {

				@Override
				public void resourceSelected(Resource selected) {
					if (resource != selected)
						panel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);
					else
						panel.setBackground(ScriptEaseUI.SELECTED_COLOUR);

				}

				@Override
				public void resourceEditButtonPressed(Resource resource) {
				}
			});

			childPanel.setVisible(false);

			this.resourcesToPanels.put(resource, panel);

			return panel;
		}
	}
}