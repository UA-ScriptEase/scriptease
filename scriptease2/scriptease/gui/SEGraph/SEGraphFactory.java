package scriptease.gui.SEGraph;

import java.awt.Color;

import javax.swing.BorderFactory;

import scriptease.gui.SEGraph.SEGraph.SelectionMode;
import scriptease.gui.SEGraph.models.DescribeItNodeGraphModel;
import scriptease.gui.SEGraph.models.DialogueLineGraphModel;
import scriptease.gui.SEGraph.models.StoryPointGraphModel;
import scriptease.gui.SEGraph.renderers.DescribeItNodeRenderer;
import scriptease.gui.SEGraph.renderers.DialogueLineNodeRenderer;
import scriptease.gui.SEGraph.renderers.EditableDescribeItNodeRenderer;
import scriptease.gui.SEGraph.renderers.StoryPointNodeRenderer;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;

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
		graph.setBorder(BorderFactory.createLineBorder(Color.black));
		graph.setBackground(Color.WHITE);

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

		return graph;
	}

	/**
	 * Builds a DialogueLine graph based on the passed in DialogueLine.
	 * 
	 * @param dialogueLine
	 * @return
	 */
	public static SEGraph<DialogueLine> buildDialogueLineGraph(
			StoryModel story, DialogueLine dialogueLine) {
		final SEGraph<DialogueLine> graph;
		final DialogueLineGraphModel model;

		model = new DialogueLineGraphModel(story, dialogueLine);
		graph = new SEGraph<DialogueLine>(model);

		graph.setNodeRenderer(new DialogueLineNodeRenderer(graph));
		graph.setBackground(Color.WHITE);

		return graph;
	}
}
