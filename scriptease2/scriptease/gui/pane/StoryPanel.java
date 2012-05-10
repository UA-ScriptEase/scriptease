package scriptease.gui.pane;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import scriptease.gui.graph.editor.GraphEditor;
import scriptease.gui.quests.QuestPanelEditor;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelSetting;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelStorySetting;
import scriptease.model.StoryModel;

/**
 * Main panel for editing Stories, under the toolbar. Provides a tree view of
 * the StoryComponent model.
 * 
 * @author remiller
 * @author mfchurch
 */
@SuppressWarnings("serial")
public class StoryPanel extends JPanel {
	private JScrollPane storyComponentTree;
	private final StoryModel model;
	
    /**
     * keep track so we can look up which QuestPoint the StoryPanel represents
     */
    private final QuestPoint questPoint;


	/**
	 * Used to do a reverse lookup on models to frames. This is useful for when
	 * we need to know about other frames that are also editing the same model.
	 * I know it's a bit hackish, but at the moment I'm more concerned with
	 * getting this working rather than beauty. - remiller
	 */
	private final static Map<StoryModel, List<StoryPanel>> modelsToPanes = new HashMap<StoryModel, List<StoryPanel>>();

	/**
	 * Gets the collection of panes that are currently displaying the given
	 * model. Cannot be null.
	 * 
	 * @param model
	 * @return
	 */
	public final static List<StoryPanel> getStoryPanelsForModel(StoryModel model) {
		final List<StoryPanel> panels = StoryPanel.modelsToPanes.get(model);

		if (panels == null)
			throw new IllegalStateException(
					"Should never have a null list of model display panels.");

		return panels;
	}

	/**
	 * Removes the given StoryPanel from list of StoryPanel's associated with
	 * the given model.
	 * 
	 * @param model
	 * @param panel
	 */
	public final static void removeStoryPanelForModel(StoryModel model,
			StoryPanel panel) {
		final List<StoryPanel> panels = StoryPanel.modelsToPanes.get(model);

		if (panels == null)
			throw new IllegalStateException(
					"Should never have a null list of model display panels.");

		panels.remove(panel);
		StoryPanel.modelsToPanes.put(model, panels);
	}

	/**
	 * Constructor used for QuestPoint model.
	 * 
	 * @param model
	 * @param questPoint
	 */
	public StoryPanel(StoryModel model, QuestPoint questPoint) {
		super(new GridLayout(0, 1));

		this.setOpaque(false);
		this.model = model;
		this.questPoint = questPoint;


		// update the models to panes map
		updateModelsToPanes(model);

		// Build the QuestPanelEditor.
		QuestPanelEditor questEditor = new QuestPanelEditor(model.getRoot()
				.getStartPoint());
		this.add(questEditor);

		// Story settings
		StoryComponentPanelSetting storySettings = new StoryComponentPanelStorySetting();

		// Build the StoryTree
		this.storyComponentTree = new StoryComponentPanelTree(questPoint, storySettings);

		// Add the StoryTree to the panel.
		//adds the tree to the pane
		this.add(this.storyComponentTree);
	}

	/**
	 * Update the map of models to panes to reflect this panel
	 * 
	 * @param model
	 */
	private void updateModelsToPanes(StoryModel model) {
		List<StoryPanel> panes = StoryPanel.modelsToPanes.get(model);

		if (panes == null) {
			panes = new ArrayList<StoryPanel>(); 
			StoryPanel.modelsToPanes.put(model, panes);
		}

		panes.add(this);
	}

	/**
	 * Gets the current Story tree;
	 * 
	 * @return the current tree .
	 */
	public JScrollPane getTree() {
		return this.storyComponentTree;
	}

	/**
	 * Updates the title bar to use the text from the model this frame
	 * represents.
	 * 
	 * @param model
	 */
	public String getTitle() {
		final StoryModel model = this.model;

		final String title;
		String modelTitle = /* "\"" + */model.getTitle() /* + "\"" */;
		if (modelTitle == null || modelTitle.equals(""))
			modelTitle = "<Untitled>";

		title = modelTitle + "(" + model.getModule().getLocation().getName()
				+ ")";

		return title;
	}

	public StoryModel getModel() {
		return this.model;
	}

	/**
	 * Checks if this StoryPanel contains the given GraphEditor
	 * 
	 * @param editor
	 * @return
	 */
	public boolean contains(GraphEditor editor) {
		final Component[] components = this.getComponents();
		for (int i = 0; i < components.length; i++) {
			final Component component = components[i];
			if (component == editor)
				return true;
		}
		return false;
	}

	/**
	 * Determines if this StoryFrame represents the given model.
	 * 
	 * @param model
	 *            The model to test against for representation.
	 * @return True if it represents the model, false otherwise.
	 */
	public boolean represents(StoryModel model) {
		return this.model == model;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [" + this.model + "]";
	}
	
	 public boolean represents(QuestPoint questPoint) {
         return this.questPoint != null && this.questPoint == questPoint;
 }

}
