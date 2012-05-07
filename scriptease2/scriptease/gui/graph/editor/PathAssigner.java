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
import scriptease.model.complex.StoryComponentContainer;

/**
 * JPanel used for assigning a ScriptIt to a selected DescribeIt path
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class PathAssigner extends JPanel implements StoryComponentObserver {
	private final DescribeIt describeIt;
	private final List<GraphNode> path;

	public PathAssigner(DescribeIt describeIt) {
		this.describeIt = describeIt;
		this.path = describeIt.buildPathFromSelectedNodes();

		// create a StoryComponentContainer as a drop target
		StoryComponentContainer container = new StoryComponentContainer(
				"Assigned DoIt");
		container.clearAllowableChildren();
		container.registerChildType(ScriptIt.class, 1);

		final ScriptIt scriptIt = describeIt.getScriptItForPath(this.path);
		// if a scriptIt is already assigned to the path, display it
		if (scriptIt != null)
			container.addStoryChild(scriptIt);

		StoryComponentPanelTree tree = new StoryComponentPanelTree(container,
				new StoryComponentPanelStorySetting());
		this.add(tree);
		this.setOpaque(true);

		// listen for changes to the container
		container.addStoryComponentObserver(this);
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
