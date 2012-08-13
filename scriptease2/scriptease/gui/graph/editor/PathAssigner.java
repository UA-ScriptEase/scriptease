package scriptease.gui.graph.editor;

import java.util.List;

import javax.swing.JPanel;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.storycomponentpanel.setting.StoryComponentPanelStorySetting;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.complex.ScriptIt;

/**
 * JPanel used for assigning a ScriptIt to a selected DescribeIt path. Displays
 * the ScriptIt in the JPanel.
 * 
 * TODO This should be in some other class with all of the other describeit stuff.
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
		this.tree = new StoryComponentPanelTree(
				new StoryComponentPanelStorySetting());

		this.add(tree);
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

		this.remove(tree);
		tree = new StoryComponentPanelTree(scriptIt,
				new StoryComponentPanelStorySetting());
		this.add(tree);
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
		source.process(new AbstractNoOpStoryVisitor() {
			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				if (type == StoryComponentChangeEnum.CHANGE_CHILD_ADDED)
					describeIt.assignScriptItToPath(path, scriptIt);
				else if (type == StoryComponentChangeEnum.CHANGE_CHILD_REMOVED)
					describeIt.assignScriptItToPath(path, null);
			}
		});
	}
}