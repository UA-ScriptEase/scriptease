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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.ResourceObserver;
import scriptease.controller.observer.ResourceTreeAdapter;
import scriptease.controller.observer.ResourceTreeObserver;
import scriptease.controller.observer.StoryModelAdapter;
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
import scriptease.translator.io.model.EditableResource;
import scriptease.translator.io.model.GameType;
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
	private Resource selectedResource = null;

	private String filterText;

	private final ObserverManager<ResourceTreeObserver> observerManager;

	private final Collection<ResourceContainer> containers;
	private final Collection<GameType> filterTypes;

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
		this.filterTypes = new ArrayList<GameType>();
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
		this.filterTypes.clear();
		this.removeAll();

		final StoryModel story;
		final List<GameType> types;

		story = SEModelManager.getInstance().getActiveStoryModel();

		if (story == null) {
			this.repaint();
			this.revalidate();
			return;
		}

		this.filterTypes.addAll(story.getTypes());

		types = new ArrayList<GameType>(this.filterTypes);

		GameType.sortByName(types);

		for (GameType type : types) {
			final ResourceContainer containerPanel;

			containerPanel = new ResourceContainer(type);

			this.add(containerPanel);
			this.containers.add(containerPanel);
		}

		this.repaint();

		final Container parent = this.getParent();

		// This is necessary to redraw the parent of the tree if the tree is
		// filled. We use this especially for the JSplitPane.
		if (parent != null)
			parent.validate();
		else
			this.revalidate();

		this.notifyResourceTreeFilled();
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
	protected void filterByTypes(Collection<GameType> filterTypes) {
		this.filterTypes.clear();
		this.filterTypes.addAll(filterTypes);

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
	 * Notifies all {@link ResourceTreeObserver}s that the tree was filled.
	 * 
	 * @param resource
	 */
	private void notifyResourceTreeFilled() {
		for (ResourceTreeObserver observer : this.observerManager
				.getObservers()) {
			observer.resourceTreeFilled();
		}
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
	 * Notifies all {@link ResourceTreeObserver}s of an add button getting
	 * clicked.
	 * 
	 * @param resource
	 */
	private void notifyAddButtonClicked(GameType type) {
		for (ResourceTreeObserver observer : this.observerManager
				.getObservers()) {
			observer.resourceAddButtonClicked(type);
		}
	}

	/**
	 * Notifies all {@link ResourceTreeObserver}s of a remove button getting
	 * clicked.
	 * 
	 * @param resource
	 */
	private void notifyRemoveButtonClicked(Resource resource) {
		for (ResourceTreeObserver observer : this.observerManager
				.getObservers()) {
			observer.resourceRemoveButtonClicked(resource);
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
			observer.resourceEditButtonClicked(resource);
		}
	}

	/**
	 * 
	 * @deprecated TODO This needs to be removed. Right now, we can't check from
	 *             a type to see if its resources should be editable. Once we
	 *             change types from strings to GameTypes (ticket #52135385),
	 *             the GameType itself almost needs to have an "editable"
	 *             property, meaning we can add new resources of that type. This
	 *             will mean changes to the way EditableResources are generated
	 *             as well, but will be more safe in the future.
	 * @return
	 */
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

	/**
	 * Container for all resource panels in the tree. Represents a type in the
	 * tree.
	 * 
	 * @author kschenk
	 * 
	 */
	private class ResourceContainer extends JPanel {
		// This map is, sadly, necessary to prevent memory leaks.
		private final Map<Resource, ResourceObserver> resourcesToObservers;
		private final Map<Resource, JPanel> resourcesToPanels;
		private final GameType type;
		private final JPanel container;

		/**
		 * Creates a new game object container with the passed in name and
		 * collection of GameObjects.
		 * 
		 * @param typeName
		 *            The name of the container
		 * @param displayText
		 *            The text to display as the name.
		 */
		public ResourceContainer(final GameType type) {
			this.type = type;
			this.container = new JPanel();
			this.resourcesToPanels = new HashMap<Resource, JPanel>();
			this.resourcesToObservers = new HashMap<Resource, ResourceObserver>();

			final JLabel categoryLabel;
			final ExpansionButton button;

			final JPanel categoryPanel;

			button = ScriptWidgetFactory.buildExpansionButton(false);

			categoryLabel = new JLabel(type.getName());
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

			if (StringOp.exists(dialogueType)
					&& type.getName().equals(dialogueType)) {
				final JButton addButton = ComponentFactory.buildAddButton();

				addButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ResourceTree.this.notifyAddButtonClicked(type);
						ResourceContainer.this.updateResourcePanels();
					}
				});

				categoryPanel.add(addButton);
			}

			categoryPanel.add(Box.createHorizontalGlue());

			this.add(categoryPanel);

			this.add(Box.createHorizontalGlue());

			this.add(this.container);

			if (StringOp.exists(dialogueType)
					&& type.getName().equals(dialogueType)) {
				final StoryModel story;

				story = SEModelManager.getInstance().getActiveStoryModel();

				if (story != null) {
					story.addStoryModelObserver(this, new StoryModelAdapter() {
						@Override
						public void dialogueRootAdded(DialogueLine root) {
							ResourceContainer.this.updateResourcePanels();
						}

						public void dialogueChildAdded(DialogueLine added,
								DialogueLine parent) {
							ResourceContainer.this.updateResourcePanels();
						};

						@Override
						public void dialogueRootRemoved(DialogueLine removed) {
							ResourceContainer.this.updateResourcePanels();
						}

						@Override
						public void dialogueChildRemoved(DialogueLine removed,
								DialogueLine parent) {
							ResourceContainer.this.updateResourcePanels();
						}
					});
				}
			}

			this.updateResourcePanels();
		}

		private void updateResourcePanels() {
			final StoryModel story;

			story = SEModelManager.getInstance().getActiveStoryModel();

			if (story == null || !ResourceTree.this.filterTypes.contains(type)) {
				this.setVisible(false);
				return;
			}

			this.setVisible(true);

			final String dialogueType = ResourceTree.this.getDialogueType();

			final List<Resource> resources;

			// TODO See above note on dialogue types. This entire section is
			// awkward since it relies on dialogues.
			if (StringOp.exists(dialogueType)
					&& type.getName().equals(dialogueType))
				resources = new ArrayList<Resource>(story.getDialogueRoots());
			else {
				resources = new ArrayList<Resource>(story.getModule()
						.getResourcesOfType(type.getName()));

				if (resources == null || resources.isEmpty()) {
					this.setVisible(false);
					return;
				}
			}

			Collections.sort(resources, constantSorter);

			// Go through the collection of resources and create panels for
			// those that do not already have any in the panel map.
			// Add them to the panel based on their position.
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

			// Needs to be a new set to avoid concurrent modifications.
			entrySet = new HashSet<Entry<Resource, JPanel>>(
					this.resourcesToPanels.entrySet());

			for (Entry<Resource, JPanel> entry : entrySet) {
				final Resource resource = entry.getKey();
				final JPanel panel = entry.getValue();

				// Check if the resource still exists
				if (resources.contains(resource)) {
					// Make visibility match search text
					panel.setVisible(ResourceTree.this
							.matchesSearchText(resource));
				} else {
					// Remove any that no longer exist in the model
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

			gameObjectBindingWidget = ScriptWidgetFactory
					.buildBindingWidget(new KnowItBindingResource(resource));

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

			final JLabel nameLabel;

			nameLabel = ScriptWidgetFactory.buildLabel(resourceName);

			gameObjectBindingWidget.add(nameLabel);

			resourcePanel.add(Box.createHorizontalStrut(STRUT_SIZE));

			if (resource.getChildren().size() > 0) {
				final ExpansionButton expansionButton;

				expansionButton = ScriptWidgetFactory
						.buildExpansionButton(true);

				expansionButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final boolean isVisible = childPanel.isVisible();
						expansionButton.setCollapsed(isVisible);
						childPanel.setVisible(!isVisible);
					}
				});

				resourcePanel.add(expansionButton);
			} else
				resourcePanel.add(Box.createRigidArea(new Dimension(10, 0)));

			// Add the owner label
			if (StringOp.exists(resourceOwnerName)) {
				final Color LINE_COLOR_1 = Color.red;
				final Color LINE_COLOR_2 = Color.blue;

				final JLabel prefixLabel = new JLabel();

				prefixLabel.setOpaque(true);
				prefixLabel.setBackground(Color.LIGHT_GRAY);
				prefixLabel.setText(" " + resourceOwnerName.charAt(0) + " ");

				if (indent % 2 == 0) {
					prefixLabel.setForeground(LINE_COLOR_2);
				} else {
					prefixLabel.setForeground(LINE_COLOR_1);
				}

				resourcePanel.add(prefixLabel);
			}

			resourcePanel.add(gameObjectBindingWidget);

			if (resource instanceof EditableResource) {
				// We add an observer if it's editable
				final EditableResource editable = (EditableResource) resource;

				// This prevents a memory leak where we would constantly add
				// observers to the resource.
				if (this.resourcesToObservers.containsKey(resource)) {
					editable.removeObserver(this.resourcesToObservers
							.get(resource));
				}

				final ResourceObserver observer;

				observer = this.resourceObserver(nameLabel);

				editable.addObserver(resource, observer);
				this.resourcesToObservers.put(resource, observer);

				if (editable.isRoot()) {
					// We add an edit and remove button to roots of editable
					// resources. Their behaviour is handled by listeners.
					final JButton editButton;
					final JButton removeButton;

					editButton = ComponentFactory.buildEditButton();
					removeButton = ComponentFactory.buildRemoveButton();

					editButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							ResourceTree.this.notifyEditButtonClicked(resource);
						}
					});

					removeButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							ResourceTree.this
									.notifyRemoveButtonClicked(resource);

							final Container parent;

							parent = panel.getParent();

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
			}

			panel.add(resourcePanel);
			panel.add(childPanel);

			final List<Resource> children;

			children = new ArrayList<Resource>(resource.getChildren());

			Collections.sort(children, constantSorter);

			for (Resource child : children) {
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

			ResourceTree.this.addObserver(this, new ResourceTreeAdapter() {

				@Override
				public void resourceSelected(Resource selected) {
					if (resource != selected) {
						panel.setBackground(ScriptEaseUI.UNSELECTED_COLOUR);
						panel.setBorder(BorderFactory.createLineBorder(
								Color.WHITE, 2));
					} else {
						panel.setBackground(ScriptEaseUI.SELECTED_COLOUR);
						panel.setBorder(BorderFactory.createLineBorder(
								Color.GREEN, 2));

					}
				}
			});

			childPanel.setVisible(false);

			return panel;
		}

		/**
		 * An observer that updates the category based on changes to an
		 * {@link EditableResource}.
		 * 
		 * @param nameLabel
		 * @return
		 */
		private ResourceObserver resourceObserver(final JLabel nameLabel) {
			return new ResourceObserver() {

				/**
				 * Finds the resource, removes it from the panel list, and
				 * updates the panels.
				 * 
				 * @param resource
				 */
				private void refreshCategoryPanel(EditableResource resource) {
					if (resourcesToPanels.containsKey(resource)) {
						this.remove(resource);
					} else
						// If it's not at the top level, it may be a child.
						// e.g. Dialogue line in a dialogue
						for (Resource key : resourcesToPanels.keySet()) {
							if (key.getDescendants().contains(resource)) {
								this.remove(key);
								break;
							}
						}
				}

				/**
				 * Removes a resource from the panels and updates the panels.
				 * 
				 * @param resource
				 */
				private void remove(Resource resource) {
					final JPanel panel = resourcesToPanels.get(resource);

					if (panel != null) {
						container.remove(panel);
						resourcesToPanels.remove(resource);

						updateResourcePanels();
					}
				}

				@Override
				public void childAdded(EditableResource resource, Resource child) {
					this.refreshCategoryPanel(resource);
				}

				@Override
				public void childRemoved(EditableResource resource,
						Resource child) {
					this.refreshCategoryPanel(resource);
				}

				@Override
				public void nameChanged(EditableResource resource, String name) {
					// If the root changed, we refresh the panel so it gets
					// rearranged alphabetically.
					if (resourcesToPanels.containsKey(resource)) {
						refreshCategoryPanel(resource);
					}

					nameLabel.setText(name);
				}
			};
		}

	}

	/**
	 * Returns the currently sleected resource
	 * 
	 * @return
	 */
	protected Resource getSelected() {
		return this.selectedResource;
	}
}