package scriptease.gui.pane;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.observer.ResourceTreeAdapter;
import scriptease.controller.observer.ResourceTreeObserver;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Resource;
import scriptease.util.StringOp;

/**
 * Displays a tree of game objects and contains various methods of filtering
 * them, including a search field and a {@link TypeAction}.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class ResourcePanel extends JPanel {
	private static ResourcePanel instance = new ResourcePanel();

	private final ResourceTree resources;

	/**
	 * Gets the sole instance of the resource panel.
	 * 
	 * @return
	 */
	public static ResourcePanel getInstance() {
		return ResourcePanel.instance;
	}

	private ResourcePanel() {
		this.resources = new ResourceTree();

		final JPanel filterPane;
		final JPanel searchFilterPane;

		final JScrollPane treeScrollPane;

		final JTextField searchField;

		final TypeAction typeFilter;

		filterPane = new JPanel();
		searchFilterPane = new JPanel();

		searchField = ComponentFactory.buildJTextFieldWithTextBackground(20,
				"Search Resources", "");

		typeFilter = new TypeAction();

		treeScrollPane = new JScrollPane(this.resources,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.resources.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		this.resources.setBackground(Color.WHITE);

		treeScrollPane.setBackground(Color.WHITE);
		treeScrollPane.getVerticalScrollBar().setUnitIncrement(
				ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);
		treeScrollPane.setBorder(BorderFactory.createEmptyBorder());

		typeFilter.setAction(new Runnable() {
			@Override
			public void run() {
				ResourcePanel.this.resources.filterByTypes(typeFilter
						.getSelectedTypes());
			}
		});

		// Sets up the type filter.
		searchFilterPane.add(searchField);
		searchFilterPane.add(ComponentFactory.buildFlatButton(typeFilter));
		searchFilterPane.setLayout(new BoxLayout(searchFilterPane,
				BoxLayout.LINE_AXIS));
		searchFilterPane.setOpaque(false);

		// FilterPane Layout
		filterPane.setLayout(new BoxLayout(filterPane, BoxLayout.Y_AXIS));
		filterPane.add(searchFilterPane);
		filterPane.setMaximumSize(new Dimension(ScriptEaseUI.MAX_SCREEN_WIDTH,
				50));
		filterPane.setBackground(Color.WHITE);

		this.setPreferredSize(new Dimension(
				this.resources.getPreferredSize().width, 0));

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.add(filterPane);
		this.add(treeScrollPane);
		this.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1,
				ScriptEaseUI.SE_BLACK));

		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				ResourcePanel.this.resources.filterByText(searchField.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				insertUpdate(e);
			}
		});

		SEModelManager.getInstance().addSEModelObserver(this,
				new SEModelObserver() {

					public void modelChanged(SEModelEvent event) {
						final ResourcePanel panel = ResourcePanel.this;
						final SEModelEvent.Type eventType;

						eventType = event.getEventType();

						if (eventType == SEModelEvent.Type.ACTIVATED) {
							// If a model is activated
							panel.resources.fillTree();
							panel.setVisible(event.getPatternModel() instanceof StoryModel);

							// Another stupid hack we need because of
							// JSplitPanes. Awful awful awful.
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									final Container parent = panel.getParent();

									if (parent instanceof JSplitPane) {
										final JSplitPane libraryPane = (JSplitPane) parent;

										final double thisShouldntBeNecessary = 0.5d;
										libraryPane
												.setResizeWeight(thisShouldntBeNecessary);
										libraryPane
												.setDividerLocation(thisShouldntBeNecessary);
									}

								}
							});
						} else if (eventType == SEModelEvent.Type.REMOVED
								&& SEModelManager.getInstance()
										.getActiveModel() == null) {
							// If the last model is removed.
							panel.resources.fillTree();
							panel.setVisible(false);
						}
					}
				});

		this.setVisible(SEModelManager.getInstance().getActiveModel() instanceof StoryModel);

		this.addObserver(this, new ResourceTreeAdapter() {
			@Override
			public void resourceAddButtonClicked(GameType type) {
				final StoryModel story;
				final String dialogueType;

				story = SEModelManager.getInstance().getActiveStoryModel();

				if (story != null) {
					dialogueType = story.getModule().getDialogueType();
					if (StringOp.exists(dialogueType)
							&& type.getName().equals(dialogueType)) {

						UndoManager.getInstance().startUndoableAction(
								"Create Dialogue Root");
						story.createAndAddDialogueRoot();
						UndoManager.getInstance().endUndoableAction();
					}
				}
			}

			@Override
			public void resourceRemoveButtonClicked(Resource resource) {
				// This will obviously need changes if we ever add/remove
				// anything that isn't a dialogue line.
				if (!(resource instanceof DialogueLine))
					return;

				final StoryModel story;

				story = SEModelManager.getInstance().getActiveStoryModel();

				if (story != null) {
					UndoManager.getInstance().startUndoableAction(
							"Remove Dialogue Root");
					story.removeDialogueRoot((DialogueLine) resource);
					UndoManager.getInstance().endUndoableAction();
				}
			}
		});
	}

	/**
	 * Adds a {@link ResourceTreeObserver} directly to the {@link ResourcePanel}
	 * 's {@link ResourceTree}.
	 * 
	 * @param object
	 * @param observer
	 */
	public void addObserver(Object object, ResourceTreeObserver observer) {
		this.resources.addObserver(object, observer);
	}

	/**
	 * Returns the resource panel's currently selected resource
	 * 
	 */
	public Resource getSelected() {
		return this.resources.getSelected();
	}
}
