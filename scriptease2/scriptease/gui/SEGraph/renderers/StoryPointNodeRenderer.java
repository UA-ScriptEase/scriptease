package scriptease.gui.SEGraph.renderers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import scriptease.model.complex.StoryPoint;

/**
 * Special renderer for nodes representing StoryPoints. These components also
 * contain Fan In panels and Binding Widgets.
 * 
 * @author kschenk
 * 
 */
public class StoryPointNodeRenderer extends SEGraphNodeRenderer<StoryPoint> {
	private final Map<StoryPoint, StoryComponentObserver> weakStoryPointsToObservers = new WeakHashMap<StoryPoint, StoryComponentObserver>();

	private SEGraph<StoryPoint> graph;

	public StoryPointNodeRenderer(SEGraph<StoryPoint> graph) {
		super(graph);
		this.graph = graph;
	}

	@Override
	protected void configureInternalComponents(final JComponent component,
			final StoryPoint node) {
		final StoryComponentObserver fanInObserver;

		fanInObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_FAN_IN) {
					StoryPointNodeRenderer.this.updateComponents(component,
							node);
				}
			}
		};

		if (node != null) {
			this.weakStoryPointsToObservers.put(node, fanInObserver);
			node.addStoryComponentObserver(fanInObserver);
		}

		component.setLayout(new BoxLayout(component, BoxLayout.LINE_AXIS));

		this.updateComponents(component, node);
	}

	/**
	 * Updates the components in the passed in component to represent the passed
	 * in node.
	 * 
	 * @param component
	 * @param node
	 */
	private void updateComponents(JComponent component, StoryPoint node) {
		component.removeAll();

		if (node != null) {
			final int VERTICAL_MARGIN = 40;
			final int HORIZONTAL_MARGIN = 10;

			component.add(Box.createVerticalStrut(VERTICAL_MARGIN));
			component.add(Box.createHorizontalStrut(HORIZONTAL_MARGIN));

			final BindingWidget editableWidget;
			
			editableWidget = ScriptWidgetFactory.buildBindingWidget(node,
					true);
			
			if (this.graph.getStartNode() != node) {
				// If not start node, add a fan in spinner.
				final JSpinner fanInSpinner;
				final int SPACE_BETWEEN_COMPONENTS = 5;

				fanInSpinner = ScriptWidgetFactory.buildFanInSpinner(node,
						getMaxFanIn(node));

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

	/**
	 * Returns the max fan in for the Story Point.
	 * 
	 * @param node
	 * @return
	 */
	private Integer getMaxFanIn(StoryPoint node) {
		final Set<StoryPoint> parents;

		parents = new HashSet<StoryPoint>();

		for (StoryPoint descendant : this.getStartNode().getDescendants()) {
			for (StoryPoint successor : descendant.getSuccessors())
				if (successor == node) {
					parents.add(descendant);
				}
		}

		return parents.size();
	}
}
