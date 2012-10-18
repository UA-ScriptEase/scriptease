package scriptease.gui.SEGraph.editor;

import java.util.List;

import javax.swing.JPanel;

import scriptease.controller.StoryAdapter;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.gui.SEGraph.nodes.GraphNode;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.ScriptIt;

/**
 * JPanel used for assigning a ScriptIt to a selected DescribeIt path. Displays
 * the ScriptIt in the JPanel.
 * 
 * TODO This should be in some other class with all of the other describeit
 * stuff.
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class PathAssigner extends JPanel implements StoryComponentObserver {
	private DescribeIt describeIt;
	private List<GraphNode> path;
	private StoryComponentPanelTree tree;

	/**
	 * Creates a PathAssigner with nothing shown, and null settings. Call
	 * <code>setNode(DescribeIt describeIt)</code> to set up the PathAssigner.
	 */
	public PathAssigner() {
		this.describeIt = null;
		this.path = null;
		this.tree = new StoryComponentPanelTree();

		this.add(this.tree);
	}

	/**
	 * Sets up the PathAssigner for the DescribeIt node passed. Creates a
	 * ScriptIt for the DescribeIt, adds it to a StoryComponentPanelTree, and
	 * then displays it in the JPanel.
	 * 
	 * @param describeIt
	 */
	public void setNode(DescribeIt describeIt) {
		this.describeIt = describeIt;
		this.path = describeIt.buildPathFromSelectedNodes();

		final ScriptIt scriptIt = describeIt.getScriptItForPath(this.path);

		this.remove(this.tree);
		// TODO Need to reimplement this! tree = new StoryComponentPanelTree(scriptIt);
		this.add(this.tree);
		this.setOpaque(true);

		// listen for changes to the container
		scriptIt.addStoryComponentObserver(this);

		this.repaint();
		this.revalidate();
	}

	@Override
	public void componentChanged(StoryComponentEvent event) {
		final StoryComponent source = event.getSource();
		final StoryComponentChangeEnum type = event.getType();
		source.process(new StoryAdapter() {
			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				if (type == StoryComponentChangeEnum.CHANGE_CHILD_ADDED)
					PathAssigner.this.describeIt.assignScriptItToPath(PathAssigner.this.path, scriptIt);
				else if (type == StoryComponentChangeEnum.CHANGE_CHILD_REMOVED)
					PathAssigner.this.describeIt.assignScriptItToPath(PathAssigner.this.path, null);
			}
		});
	}
}