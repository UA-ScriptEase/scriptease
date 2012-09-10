package scriptease.gui.graph.renderers;

import java.awt.FlowLayout;

import javax.swing.JComponent;

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
	@Override
	protected void configureInternalComponents(JComponent component,
			StoryPoint node) {
		component.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		component.add(ScriptWidgetFactory.buildFanInPanel(node.getFanIn()));
		component.add(ScriptWidgetFactory.buildBindingWidget(node, false));
	}
}
