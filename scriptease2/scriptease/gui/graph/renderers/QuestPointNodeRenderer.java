package scriptease.gui.graph.renderers;

import java.awt.FlowLayout;

import javax.swing.JComponent;

import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.graph.SEGraph;
import scriptease.gui.quests.QuestPoint;

/**
 * Special renderer for nodes representing Quest Points. These components also
 * contain Fan In panels and Binding Widgets representing the Quest Point.
 * 
 * @author kschenk
 * 
 */
public class QuestPointNodeRenderer extends SEGraphNodeRenderer<QuestPoint> {

	public QuestPointNodeRenderer(SEGraph<QuestPoint> graph) {
		super(graph);
	}

	@Override
	protected void configureInternalComponents(JComponent component,
			QuestPoint node) {
		component.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		component.add(ScriptWidgetFactory.buildFanInPanel(node.getFanIn()));
		component.add(ScriptWidgetFactory.buildBindingWidget(node, false));
	}
}
