package scriptease.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.pane.GameObjectPane;
import scriptease.gui.pane.LibraryPane;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.PatternModel;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.GameObjectPicker;
import scriptease.util.BiHashMap;

/**
 * A factory class for different panels. All major panel construction should go
 * in here.
 * 
 * @author kschenk
 * 
 */
public class PanelFactory {
	private static PanelFactory instance = new PanelFactory();

	public static PanelFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a panel for editing Quests.
	 * 
	 * @param start
	 *            Start Point of the graph.
	 * @return
	 */
	public JPanel buildQuestPanel(final GraphNode start) {
		final JPanel questPanel = new JPanel(new BorderLayout(), true);
		final GraphPanel graphPanel = new GraphPanel(start);

		ToolBarButtonAction.addJComponent(graphPanel);

		final JToolBar graphToolBar = ToolBarFactory
				.buildGraphEditorToolBar(graphPanel);
		final JToolBar questToolBar = ToolBarFactory
				.buildQuestEditorToolBar(graphPanel);

		questPanel.add(graphToolBar.add(questToolBar), BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

		questPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

		return questPanel;
	}

	/**
	 * Creates a panel for editing DescribeIts.
	 * 
	 * @param start
	 *            Start Point of the graph
	 * @return
	 */
	public JPanel buildDescribeItPanel(final GraphNode start,
			final DescribeIt describeIt) {
		final JPanel describeItPanel = new JPanel(new BorderLayout(), true);
		final GraphPanel graphPanel = new GraphPanel(start);

		DescribeIt editedDescribeIt = describeIt.clone();
		editedDescribeIt.clearSelection();

		graphPanel.setHeadNode(editedDescribeIt.getHeadNode());

		ToolBarButtonAction.addJComponent(graphPanel);

		final JToolBar graphToolBar = ToolBarFactory
				.buildGraphEditorToolBar(graphPanel);

		final JToolBar describeItToolBar = ToolBarFactory
				.buildDescribeItToolBar(editedDescribeIt, graphPanel);

		describeItPanel.add(graphToolBar.add(describeItToolBar),
				BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

		describeItPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

		return describeItPanel;
	}

	/**
	 * Builds a pane containing all game objects in the active module, organized
	 * by category, allowing the user to drag them onto bindings in a Story.
	 * 
	 * @return A JPanel GameObject picker.
	 */
	public JPanel buildGameObjectPane(StoryModel storyModel) {
		GameObjectPicker picker;

		if (storyModel != null) {
			Translator translator = storyModel.getTranslator();
			if (translator != null) {
				// Get the picker
				if ((picker = translator.getCustomGameObjectPicker()) == null) {
					picker = new GameObjectPane(storyModel);
				}
				return picker.getPickerPanel();
			}
		}
		// otherwise return an empty hidden JPanel
		JPanel jPanel = new JPanel();
		jPanel.setVisible(false);
		return jPanel;
	}

	private final static BiHashMap<PatternModel, List<JPanel>> modelsToPanes = new BiHashMap<PatternModel, List<JPanel>>();

	private final static Map<JPanel, StoryComponentPanelTree> panesToTrees = new IdentityHashMap<JPanel, StoryComponentPanelTree>();

	public JPanel buildStoryPanel(StoryModel model, QuestPoint questPoint) {
		final JPanel storyPanel;
		final JPanel questPanel;
		final StoryComponentPanelTree storyComponentTree;

		List<JPanel> panes;

		storyPanel = new JPanel(new GridLayout(0, 1));
		questPanel = PanelFactory.getInstance().buildQuestPanel(
				model.getRoot().getStartPoint());
		storyComponentTree = new StoryComponentPanelTree(questPoint);

		panes = modelsToPanes.getValue(model);

		if(panes == null) {
			panes = new ArrayList<JPanel>();
			panes.add(storyPanel);
			modelsToPanes.put(model, panes);
		}

		storyPanel.setOpaque(false);

		panes.add(storyPanel);

		storyPanel.add(questPanel);

		storyPanel.add(storyComponentTree);

		panesToTrees.put(storyPanel, storyComponentTree);
		
		return storyPanel;
	}

	public void setRootForTreeInPanel(JPanel panel, QuestPoint questPoint) {
		panesToTrees.get(panel).setRoot(questPoint);
	}

	public StoryComponentPanelTree getTreeForPanel(JPanel panel) {
		return panesToTrees.get(panel);
	}

	public PatternModel getModelForPanel(JPanel modelPanel) {
		for (List<JPanel> jPanelList : modelsToPanes.getValues())
			if (jPanelList.contains(modelPanel))
				return modelsToPanes.getKey(jPanelList);

		throw new IllegalStateException(
				"Encountered null model when attempting to get model for "
						+ modelPanel.getName());
	}

	/**
	 * Gets the collection of panes that are currently displaying the given
	 * model. Cannot be null.
	 * 
	 * @param model
	 * @return
	 */
	public List<JPanel> getPanelsForModel(PatternModel model) {
		final List<JPanel> panels = modelsToPanes.getValue(model);

		if (panels == null)
			throw new IllegalStateException(
					"Encountered null list of model display panels when "
							+ "attempting to get panels for " + model.getName());

		return panels;
	}

	/**
	 * Removes the given StoryPanel from list of StoryPanel's associated with
	 * the given model.
	 * 
	 * @param model
	 * @param panel
	 */
	public void removeStoryPanelForModel(PatternModel model, JPanel panel) {
		final List<JPanel> panels = modelsToPanes.getValue(model);

		if (panels == null)
			throw new IllegalStateException(
					"Encountered null list of model display panels "
							+ "when attempting to remove panels for "
							+ model.getName());

		panels.remove(panel);
		modelsToPanes.put(model, panels);
	}

	private static LibraryPane mainLibraryPane = new LibraryPane(false);

	public LibraryPane getMainLibraryPane() {
		return mainLibraryPane;
	}
}