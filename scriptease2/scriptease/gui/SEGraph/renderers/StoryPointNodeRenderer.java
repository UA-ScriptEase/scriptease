package scriptease.gui.SEGraph.renderers;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
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

		component.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

		this.updateComponents(component, node);
	}

	/**
	 * Updates the components.
	 * 
	 * @param component
	 * @param editButton
	 */
	private void updateComponents(JComponent component, StoryPoint node) {
		component.removeAll();
		final BindingWidget editableWidget;
		final BindingWidget uneditableWidget;

		if (node != null) {

			editableWidget = ScriptWidgetFactory.buildBindingWidget(node, true);
			uneditableWidget = ScriptWidgetFactory.buildBindingWidget(node,
					false);

			if (this.graph.getStartNode() != node) {
				final JPanel fanInPanel;
				final JSpinner fanInSpinner;

				fanInPanel = new JPanel();
				fanInSpinner = ScriptWidgetFactory.buildFanInSpinner(node,
						getMaxFanIn(node));

				fanInPanel.setOpaque(false);
				fanInPanel.setBorder(BorderFactory.createLineBorder(
						Color.black, 1));

				fanInPanel.add(fanInSpinner);

				component.add(fanInPanel);
				component.add(editableWidget);
			} else {
				component.add(uneditableWidget);
			}

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
