package scriptease.gui.SEGraph.renderers;

import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JSpinner;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;

/**
 * Special renderer for nodes representing StoryNodes {@link StoryNode}. Story
 * Points {@link StoryPoint} will be rendered with fan in spinners and binding
 * widgets. Story Groups {@link StoryGroup} will be rendered with expand and
 * collapse buttons.
 * 
 * @author kschenk
 * @author jyuen
 * 
 */
public class StoryNodeRenderer extends SEGraphNodeRenderer<StoryNode> {

	private final Map<StoryNode, StoryComponentObserver> weakStoryNodesToObservers;
	private SEGraph<StoryNode> graph;

	public StoryNodeRenderer(SEGraph<StoryNode> graph) {
		super(graph);
		this.graph = graph;
		this.weakStoryNodesToObservers = new WeakHashMap<StoryNode, StoryComponentObserver>();
	}

	@Override
	protected void configureInternalComponents(final JComponent component,
			final StoryNode node) {

		if (node instanceof StoryPoint) {
			final StoryComponentObserver fanInObserver;

			fanInObserver = new StoryComponentObserver() {
				@Override
				public void componentChanged(StoryComponentEvent event) {
					if (event.getType() == StoryComponentChangeEnum.CHANGE_FAN_IN) {
						StoryNodeRenderer.this.updateComponents(component,
								(StoryPoint) node);
					}
				}
			};

			if (node != null) {
				this.weakStoryNodesToObservers.put(node, fanInObserver);
				node.addStoryComponentObserver(fanInObserver);
			}

			component.setLayout(new BoxLayout(component, BoxLayout.LINE_AXIS));
			this.updateComponents(component, (StoryPoint) node);

		} else if (node instanceof StoryGroup) {
			component.setLayout(new BoxLayout(component, BoxLayout.LINE_AXIS));
			this.updateComponents(component, (StoryGroup) node);
		}
	}

	/**
	 * Updates the components in the passed in component to represent the passed
	 * in storyGroup
	 * 
	 * @param component
	 * @param storyGroup
	 */
	private void updateComponents(JComponent component, StoryGroup group) {
		// TODO STORYNODES : draw the appearance here for a story group!
		if (group != null) {
			final int VERTICAL_MARGIN = 100;
			final int HORIZONTAL_MARGIN = 10;

			component.add(Box.createVerticalStrut(VERTICAL_MARGIN));
			component.add(Box.createHorizontalStrut(HORIZONTAL_MARGIN));

			component.add(ScriptWidgetFactory.buildBindingWidget(group, true));

			component.add(Box.createVerticalStrut(VERTICAL_MARGIN));
			component.add(Box.createHorizontalStrut(HORIZONTAL_MARGIN));

			component.revalidate();
		}
	}

	/**
	 * Updates the components in the passed in component to represent the passed
	 * in storyPoint
	 * 
	 * @param component
	 * @param storyPoint
	 */
	private void updateComponents(JComponent component, StoryPoint storyPoint) {
		component.removeAll();

		if (storyPoint != null) {
			final int VERTICAL_MARGIN = 40;
			final int HORIZONTAL_MARGIN = 10;

			component.add(Box.createVerticalStrut(VERTICAL_MARGIN));
			component.add(Box.createHorizontalStrut(HORIZONTAL_MARGIN));

			final BindingWidget editableWidget;

			editableWidget = ScriptWidgetFactory.buildBindingWidget(storyPoint,
					true);

			if (this.graph.getStartNode() != storyPoint) {
				// If not start node, add a fan in spinner.
				final JSpinner fanInSpinner;
				final int SPACE_BETWEEN_COMPONENTS = 5;

				fanInSpinner = ScriptWidgetFactory.buildFanInSpinner(
						storyPoint, storyPoint.getParents().size());

				fanInSpinner.setMaximumSize(fanInSpinner.getPreferredSize());

				component.add(fanInSpinner);
				component.add(Box
						.createHorizontalStrut(SPACE_BETWEEN_COMPONENTS));
			}

			component.add(editableWidget);

			component.add(Box.createVerticalStrut(VERTICAL_MARGIN));
			component.add(Box.createHorizontalStrut(HORIZONTAL_MARGIN));

			component.revalidate();
		}
	}
}
