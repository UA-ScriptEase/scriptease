package scriptease.gui.SEGraph.renderers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ExpansionButton;
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
	private void updateComponents(final JComponent component,
			final StoryGroup group) {
		if (group == null)
			return;

		component.removeAll();

		final int VERTICAL_MARGIN = 60;
		final int HORIZONTAL_MARGIN = 20;

		final ExpansionButton expansionButton = new ExpansionButton(
				!group.isExpanded());

		expansionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				group.setExpanded(!group.isExpanded());
				StoryNodeRenderer.this.updateComponents(component, group);
			}
		});
		
		this.createBufferRectangle(VERTICAL_MARGIN, HORIZONTAL_MARGIN,
				component);

		component.add(expansionButton);

		this.createBufferRectangle(VERTICAL_MARGIN, HORIZONTAL_MARGIN,
				component);

		if (!group.isExpanded()) {
			// Draw the group as a single node if it isn't expanded.
			component.add(ScriptWidgetFactory.buildBindingWidget(group, true));
			
			this.createBufferRectangle(VERTICAL_MARGIN, HORIZONTAL_MARGIN,
					component);
		} else {
			// Draw the group as a subgraph.
			component.add(group.getSEGraph());
		}

		if (this.isStartNodeOfGroup(group)) {
			component.add(new JLabel("IN"));
			this.createBufferRectangle(VERTICAL_MARGIN, HORIZONTAL_MARGIN,
					component);
		}
		
		if (this.isExitNodeOfGroup(group)) {
			component.add(new JLabel("OUT"));
			this.createBufferRectangle(VERTICAL_MARGIN, HORIZONTAL_MARGIN,
					component);
		}

		this.hoveredComponent = component;
		this.reconfigureAppearance(component, group);
		component.revalidate();
	}

	/**
	 * Updates the components in the passed in component to represent the passed
	 * in storyPoint
	 * 
	 * @param component
	 * @param storyPoint
	 */
	private void updateComponents(final JComponent component,
			final StoryPoint storyPoint) {
		if (storyPoint == null)
			return;

		component.removeAll();

		final int VERTICAL_MARGIN = 40;
		final int HORIZONTAL_MARGIN = 10;

		this.createBufferRectangle(VERTICAL_MARGIN, HORIZONTAL_MARGIN,
				component);

		final BindingWidget editableWidget;

		editableWidget = ScriptWidgetFactory.buildBindingWidget(storyPoint,
				true);

		if (this.graph.getStartNode() != storyPoint) {
			// If not start node, add a fan in spinner.
			final JSpinner fanInSpinner;
			final int SPACE_BETWEEN_COMPONENTS = 5;

			fanInSpinner = ScriptWidgetFactory.buildFanInSpinner(storyPoint,
					storyPoint.getParents().size());

			fanInSpinner.setMaximumSize(fanInSpinner.getPreferredSize());

			component.add(fanInSpinner);
			component.add(Box.createHorizontalStrut(SPACE_BETWEEN_COMPONENTS));
		}

		component.add(editableWidget);

		this.createBufferRectangle(VERTICAL_MARGIN, HORIZONTAL_MARGIN,
				component);

		if (this.isStartNodeOfGroup(storyPoint)) {
			component.add(new JLabel("IN"));
			this.createBufferRectangle(VERTICAL_MARGIN, HORIZONTAL_MARGIN,
					component);
		}
		if (this.isExitNodeOfGroup(storyPoint)) {
			component.add(new JLabel("OUT"));
			this.createBufferRectangle(VERTICAL_MARGIN, HORIZONTAL_MARGIN,
					component);
		}

		component.revalidate();
	}

	/**
	 * Returns true if the passed in StoryNode {@link StoryNode} is the start
	 * node of a group.
	 * 
	 * @param storyNode
	 * @return
	 */
	private boolean isStartNodeOfGroup(StoryNode storyNode) {
		if (storyNode.getOwner() instanceof StoryGroup) {
			final StoryGroup owner = (StoryGroup) storyNode.getOwner();
			if (owner.getStartNode() == storyNode) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the passed in StoryNode {@link StoryNode} is the exit
	 * node of a group.
	 * 
	 * @param storyNode
	 * @return
	 */
	private boolean isExitNodeOfGroup(StoryNode storyNode) {
		if (storyNode.getOwner() instanceof StoryGroup) {
			final StoryGroup owner = (StoryGroup) storyNode.getOwner();
			if (owner.getExitNode() == storyNode) {
				return true;
			}
		}

		return false;
	}

	private void createBufferRectangle(int verticalMargin,
			int horizontalMargin, JComponent component) {
		component.add(Box.createVerticalStrut(verticalMargin));
		component.add(Box.createHorizontalStrut(horizontalMargin));
	}
}
