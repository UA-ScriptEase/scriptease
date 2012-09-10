package scriptease.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import scriptease.controller.observer.graph.StoryPointGraphObserver;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.SEGraph;
import scriptease.gui.graph.builders.StoryPointBuilder;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.graph.renderers.StoryPointNodeRenderer;
import scriptease.gui.libraryeditor.LibraryEditorPanelFactory;
import scriptease.gui.pane.GameObjectPane;
import scriptease.gui.pane.LibraryPanel;
import scriptease.gui.quests.StoryPoint;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
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
		return PanelFactory.instance;
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

		final JToolBar graphToolBar = ToolBarFactory.getInstance()
				.buildGraphEditorToolBar();

		final JToolBar describeItToolBar = ToolBarFactory.getInstance()
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
		jPanel.setSize(new Dimension(0, 0));
		return jPanel;
	}

	private final static BiHashMap<PatternModel, List<JComponent>> modelsToComponents = new BiHashMap<PatternModel, List<JComponent>>();

	private final static Map<JComponent, StoryComponentPanelTree> componentsToTrees = new IdentityHashMap<JComponent, StoryComponentPanelTree>();

	private final static Map<JComponent, StoryPointGraphObserver> weakComponentToGraphObservers = new WeakHashMap<JComponent, StoryPointGraphObserver>();

	public JPanel buildStoryPanel(StoryModel model, StoryPoint start) {
		final JPanel storyPanel;
		final JPanel questPanel;
		final JToolBar graphToolBar;
		final SEGraph<StoryPoint> storyGraph;
		final StoryPointGraphObserver storyGraphObserver;
		final StoryComponentPanelTree storyComponentTree;

		storyPanel = new JPanel(new GridLayout(0, 1));
		questPanel = new JPanel(new BorderLayout(), true);
		graphToolBar = ToolBarFactory.getInstance().buildGraphEditorToolBar();
		storyGraph = new SEGraph<StoryPoint>(start,
				new StoryPointBuilder(start), new StoryPointNodeRenderer());
		storyGraphObserver = new StoryPointGraphObserver();
		storyComponentTree = new StoryComponentPanelTree(start);

		// Put the new pane to the map
		List<JComponent> panes;
		panes = PanelFactory.modelsToComponents.getValue(model);
		if (panes == null) {
			panes = new ArrayList<JComponent>();
		}
		panes.add(storyPanel);
		PanelFactory.modelsToComponents.put(model, panes);

		// Set up the Story Graph
		storyGraph.addSEGraphObserver(storyGraphObserver);

		PanelFactory.weakComponentToGraphObservers.put(storyPanel,
				storyGraphObserver);

		// Reset the ToolBar to select and add the Story Graph to it.
		ToolBarButtonAction.addJComponent(storyGraph);
		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

		questPanel.add(graphToolBar, BorderLayout.PAGE_START);
		questPanel.add(new JScrollPane(storyGraph), BorderLayout.CENTER);

		storyPanel.setOpaque(false);
		storyPanel.add(questPanel);
		storyPanel.add(storyComponentTree);

		PanelFactory.componentsToTrees.put(storyPanel, storyComponentTree);

		return storyPanel;
	}

	public JScrollPane buildLibraryEditorPanel(LibraryModel model) {
		final JPanel scbPanel;
		final JScrollPane scbScrollPane;

		List<JComponent> components;

		components = PanelFactory.modelsToComponents.getValue(model);
		scbPanel = LibraryEditorPanelFactory.getInstance()
				.buildLibraryEditorPanel(
						PanelFactory.getInstance().getMainLibraryPane());
		scbScrollPane = new JScrollPane(scbPanel);

		if (components == null) {
			components = new ArrayList<JComponent>();
			components.add(scbScrollPane);
			PanelFactory.modelsToComponents.put(model, components);
		}
		components.add(scbScrollPane);
		PatternModelManager.getInstance().add(model);

		return scbScrollPane;
	}

	public void setRootForTreeInComponent(JComponent component,
			StoryPoint questPoint) {
		PanelFactory.componentsToTrees.get(component).setRoot(questPoint);
	}

	/**
	 * NOTE: Methods that call this method should always either check if null is
	 * returned, or use {@link #getModelForPanel(JComponent)} to check if the
	 * panel passed in represents a StoryModel. Only Story Model panels are
	 * added to the map, so if you attempt to use a different kind of
	 * PatternModel, this method will just return null.
	 * 
	 * @param component
	 * @return
	 */
	public StoryComponentPanelTree getTreeForComponent(JComponent component) {
		return PanelFactory.componentsToTrees.get(component);
	}

	public PatternModel getModelForComponent(JComponent modelComponent) {
		for (List<JComponent> jComponentList : PanelFactory.modelsToComponents
				.getValues())
			if (jComponentList.contains(modelComponent))
				return PanelFactory.modelsToComponents.getKey(jComponentList);

		throw new IllegalStateException(
				"Encountered null model when attempting to get model for "
						+ modelComponent.getName());
	}

	/**
	 * Gets the collection of panes that are currently displaying the given
	 * model. Cannot be null.
	 * 
	 * @param model
	 * @return
	 */
	public List<JComponent> getComponentsForModel(PatternModel model) {
		final List<JComponent> panels = PanelFactory.modelsToComponents
				.getValue(model);

		if (panels == null) {
			System.out
					.println("WARNING: Encountered null list of model display "
							+ "panels when attempting to get panels for "
							+ model.getName());

			return new ArrayList<JComponent>();
		}

		return panels;
	}

	/**
	 * Removes the given component from the list of the component's associated
	 * with the given model.
	 * 
	 * @param model
	 * @param component
	 */
	public void removeComponentForModel(PatternModel model, JComponent component) {
		final List<JComponent> components = new ArrayList<JComponent>();

		if (PanelFactory.modelsToComponents.getValue(model) == null)
			throw new IllegalStateException(
					"Encountered null list of model display panels "
							+ "when attempting to remove panels for "
							+ model.getName());

		components.addAll(PanelFactory.modelsToComponents.getValue(model));

		components.remove(component);

		if (!components.isEmpty())
			PanelFactory.modelsToComponents.put(model, components);
		else
			PanelFactory.modelsToComponents.removeKey(model);
	}

	private static LibraryPanel mainLibraryPane = new LibraryPanel(true);

	public LibraryPanel getMainLibraryPane() {
		return PanelFactory.mainLibraryPane;
	}
}