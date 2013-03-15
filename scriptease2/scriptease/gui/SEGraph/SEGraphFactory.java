package scriptease.gui.SEGraph;

import java.awt.Color;

import scriptease.gui.SEGraph.SEGraph.SelectionMode;
import scriptease.gui.SEGraph.models.DescribeItNodeGraphModel;
import scriptease.gui.SEGraph.models.StoryPointGraphModel;
import scriptease.gui.SEGraph.renderers.DescribeItNodeRenderer;
import scriptease.gui.SEGraph.renderers.EditableDescribeItNodeRenderer;
import scriptease.gui.SEGraph.renderers.StoryPointNodeRenderer;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.StoryPoint;
import scriptease.util.GUIOp;

/**
 * A factory for different graphs. This only creates the GUI for the graphs,
 * since functionality may vary. SEGraphObservers need to be added after
 * construction.
 * 
 * @author kschenk
 * 
 */
public class SEGraphFactory {

	private SEGraphFactory() {
	}

	/**
	 * Builds a graph used to select different nodes in a description.
	 * 
	 * @param start
	 * @return
	 */
	public static SEGraph<DescribeItNode> buildDescribeItGraph(
			DescribeItNode start) {
		final DescribeItNodeGraphModel describeItGraphModel;
		final SEGraph<DescribeItNode> graph;

		describeItGraphModel = new DescribeItNodeGraphModel(start);
		graph = new SEGraph<DescribeItNode>(describeItGraphModel,
				SelectionMode.SELECT_PATH_FROM_START, true);

		graph.setNodeRenderer(new DescribeItNodeRenderer(graph));
		graph.setBackground(GUIOp.scaleWhite(ScriptEaseUI.COLOUR_KNOWN_OBJECT,
				3.5));

		return graph;
	}

	/**
	 * Builds a graph for descriptions that allows the nodes to be edited.
	 * 
	 * @param start
	 * @return
	 */
	public static SEGraph<DescribeItNode> buildDescribeItEditorGraph(
			DescribeItNode start) {
		final DescribeItNodeGraphModel describeItGraphModel;
		final SEGraph<DescribeItNode> graph;

		describeItGraphModel = new DescribeItNodeGraphModel(start);
		graph = new SEGraph<DescribeItNode>(describeItGraphModel,
				SelectionMode.SELECT_PATH_FROM_START, false);

		graph.setNodeRenderer(new EditableDescribeItNodeRenderer(graph));

		GraphToolBarModeAction.useGraphCursorForJComponent(graph);

		return graph;
	}

	/**
	 * Builds a graph for story points that has draggable binding widgets and a
	 * fan in spinner. The binding widgets can have their names edited.
	 * 
	 * @param start
	 * @return
	 */
	public static SEGraph<StoryPoint> buildStoryGraph(StoryPoint start) {
		final SEGraph<StoryPoint> graph;
		final StoryPointGraphModel storyGraphModel;

		storyGraphModel = new StoryPointGraphModel(start);
		graph = new SEGraph<StoryPoint>(storyGraphModel);

		graph.setNodeRenderer(new StoryPointNodeRenderer(graph));
		graph.setBackground(Color.WHITE);

		GraphToolBarModeAction.useGraphCursorForJComponent(graph);

		return graph;
	}
}
