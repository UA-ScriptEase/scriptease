package scriptease.gui.graph.renderers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;

import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.quests.StoryPoint;

/**
 * Special renderer for nodes representing StoryPoints. These components also
 * contain Fan In panels and Binding Widgets.
 * 
 * @author kschenk
 * 
 */
public class StoryPointNodeRenderer extends SEGraphNodeRenderer<StoryPoint> {
	private final StoryPoint start;
	private boolean inEditingMode = false;

	private final static Map<JComponent, StoryComponentObserver> weakComponentsToObservers = new WeakHashMap<JComponent, StoryComponentObserver>();

	public StoryPointNodeRenderer(StoryPoint start) {
		this.start = start;
	}

	@Override
	protected void configureInternalComponents(final JComponent component) {
		final StoryPoint node;
		final JButton editButton;
		final StoryComponentObserver fanInObserver;

		node = this.getNodeForComponent(component);
		editButton = new JButton();
		fanInObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_FAN_IN) {
					StoryPointNodeRenderer.this.updateComponents(component,
							editButton);
				}
			}
		};

		if (node != null) {
			weakComponentsToObservers.put(component, fanInObserver);
			node.addStoryComponentObserver(fanInObserver);
		}

		editButton.setFocusable(false);
		editButton.setContentAreaFilled(false);
		editButton.setOpaque(true);

		editButton.setBackground(new Color(222, 222, 222));
		editButton.setBorder(new LineBorder(Color.GRAY, 1, true));
		editButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		editButton.setPreferredSize(new Dimension(35, 20));

		editButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				StoryPointNodeRenderer.this.inEditingMode = !StoryPointNodeRenderer.this.inEditingMode;

				StoryPointNodeRenderer.this.updateComponents(component,
						editButton);
			}
		});

		component.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

		this.updateComponents(component, editButton);
	}

	/**
	 * Updates the components.
	 * 
	 * @param component
	 * @param editButton
	 */
	private void updateComponents(JComponent component, JButton editButton) {
		component.removeAll();
		final BindingWidget editableWidget;
		final BindingWidget uneditableWidget;
		final StoryPoint node;

		node = this.getNodeForComponent(component);
		if (node != null) {

			editableWidget = ScriptWidgetFactory.buildBindingWidget(node, true);
			uneditableWidget = ScriptWidgetFactory.buildBindingWidget(node,
					false);

			if (this.inEditingMode) {
				editButton.setText("Done");
				editButton.setBackground(new Color(222, 250, 222));
				component.add(ScriptWidgetFactory.buildFanInPanel(node,
						getMaxFanIn(node), true));
				component.add(editableWidget);
			} else {
				editButton.setText("Edit");
				editButton.setBackground(new Color(222, 222, 222));
				component.add(ScriptWidgetFactory.buildFanInPanel(node, 0,
						false));
				component.add(uneditableWidget);
			}

			if (node != this.start)
				component.add(editButton);

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

		for (StoryPoint descendant : this.start.getDescendants()) {
			for (StoryPoint successor : descendant.getSuccessors())
				if (successor == node) {
					parents.add(descendant);
				}
		}

		return parents.size();
	}
}
